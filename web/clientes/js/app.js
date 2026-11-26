import { getSession, logout, request } from './api.js';
import { openAuth } from './auth.js';
import { openBusiness } from './business.js';
import { createMap } from './map.js';
import { openProfile } from './profile.js';
import { openQr } from './qr.js';
import { openRedemptions } from './redemptions.js';
import { toast } from './ui.js';

// Todo lo que la pantalla enseña sale de este objeto. Cuando algo cambia, se cambia aquí y se llama a render(),
// que pone la página al día. Es lo mismo que hacía useState en React, pero a mano
const state = {
  businesses: [],
  loadError: null,
  query: '',
  prices: new Set(),
  selectedId: null,
  user: null,
  pendingCount: 0,
  me: null,        // tu ubicación, si la has pedido
  focus: null,     // hacia dónde vuela el mapa cuando no hay un local elegido
};

let businessView = null;   // la ficha del local abierta, si hay
let panel = null;          // la pantalla de canjes o de perfil abierta, si hay

const map = createMap({ onSelect: id => { state.selectedId = id; render(); } });

// Todos los locales, página a página (50 es el máximo por página): para un mapa de una ciudad son pocos
async function loadBusinesses() {
  state.loadError = null;
  try {
    let all = [];
    for (let page = 0; ; page++) {
      const data = await request(`/businesses?size=50&page=${page}`, { auth: false });
      all = all.concat(data.content);
      if (data.last ?? page + 1 >= data.totalPages) break;
    }
    state.businesses = all;
  } catch (e) {
    state.loadError = e.message;
  }
  render();
}

// El usuario con sus puntos, y cuántos canjes tiene por usar (el número rojo del botón de canjes)
async function loadUser() {
  if (!getSession()) {
    state.user = null;
    render();
    return;
  }
  try {
    const [profile, redemptions] = await Promise.all([request('/users/me'), request('/redemptions')]);
    state.user = profile;
    state.pendingCount = redemptions.filter(r => r.status === 'PENDING').length;
  } catch {
    state.user = null;
  }
  render();
}

// La búsqueda encuentra por nombre, dirección o etiqueta, sin distinguir mayúsculas ni tildes
function visibleBusinesses() {
  const normalize = text => text.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
  const words = normalize(state.query.trim());
  return state.businesses.filter(b =>
    (state.prices.size === 0 || state.prices.has(b.priceRange)) &&
    (!words || normalize([b.name, b.address, ...(b.tags ?? [])].join(' ')).includes(words)));
}

function businessNames() {
  return Object.fromEntries(state.businesses.map(b => [b.id, b.name]));
}

// Pone la página al día con el estado
function render() {
  const visible = visibleBusinesses();
  // Si el local abierto deja de pasar el filtro, se cierra su ficha
  if (state.selectedId && !visible.some(b => b.id === state.selectedId)) state.selectedId = null;
  const selected = state.businesses.find(b => b.id === state.selectedId);

  map.update({ businesses: visible, selectedId: state.selectedId, me: state.me, focus: state.focus });

  document.querySelector('#search-clear').hidden = !state.query;
  document.querySelectorAll('.chip').forEach(chip => chip.setAttribute('aria-pressed', String(state.prices.has(chip.dataset.price))));
  document.querySelector('#account').setAttribute('aria-label', state.user ? 'Tu cuenta' : 'Iniciar sesión');

  const points = document.querySelector('#points');
  points.hidden = !state.user;
  if (state.user) {
    points.innerHTML = `<span class="coin">★</span>${state.user.points}`;
    points.setAttribute('aria-label', `${state.user.points} puntos`);
  }

  document.querySelector('#side-buttons').classList.toggle('lifted', Boolean(selected));
  const badge = document.querySelector('#badge');
  badge.hidden = state.pendingCount === 0;
  badge.textContent = state.pendingCount;
  badge.setAttribute('aria-label', `${state.pendingCount} por usar`);

  const status = document.querySelector('#map-status');
  status.hidden = !(!selected && (state.loadError || (state.businesses.length > 0 && visible.length === 0)));
  if (!status.hidden) {
    status.innerHTML = state.loadError
      ? `No se pudieron cargar los locales <button>Reintentar</button>`
      : `Ningún local coincide <button>Quitar filtros</button>`;
    status.querySelector('button').onclick = state.loadError ? loadBusinesses : clearFilters;
  }

  // La ficha del local: se abre, se cambia de local o se cierra según selectedId
  if (selected && !businessView) {
    businessView = openBusiness({
      onClose: () => { businessView = null; state.selectedId = null; render(); },
      onNeedLogin: () => openAuth({ reason: 'Inicia sesión para ganar y gastar puntos.', onDone: afterLogin(() => {}) }),
      onChanged: () => { loadBusinesses(); setTimeout(loadUser, 1500); },
      onRedeem: redemption => { showQr(redemption); loadUser(); },
    });
  }
  if (selected) businessView.update({ business: selected, user: state.user, me: state.me });
  if (!selected && businessView) {
    businessView.remove();
    businessView = null;
  }
}

function clearFilters() {
  state.query = '';
  state.prices = new Set();
  document.querySelector('#search').value = '';
  render();
}

// Lo que necesita una cuenta pasa por aquí: si no hay sesión, primero se pide, y después se continúa
function requireLogin(reason, then) {
  if (state.user) then();
  else openAuth({ reason, onDone: afterLogin(then) });
}

function afterLogin(then) {
  return async () => {
    await loadUser();
    toast('Sesión iniciada', 'ok');
    then();
  };
}

function showPanel(open) {
  panel?.close();
  panel = open();
}

function showRedemptions() {
  showPanel(() => openRedemptions({ businessNames: businessNames(), onClose: () => { panel = null; }, onOpen: showQr }));
}

function showProfile() {
  showPanel(() => openProfile({
    user: state.user,
    onClose: () => { panel = null; },
    onLogout: handleLogout,
    onRedemptions: showRedemptions,
  }));
}

function showQr(redemption) {
  openQr({
    redemption,
    businessName: businessNames()[redemption.businessId],
    onClose: loadUser,
    onUsed: loadUser,
  });
}

async function handleLogout() {
  await logout();
  state.user = null;
  state.pendingCount = 0;
  panel?.close();
  render();
  toast('Has cerrado sesión');
}

function locate() {
  if (!navigator.geolocation) {
    toast('Tu navegador no puede darnos tu ubicación', 'error');
    return;
  }
  navigator.geolocation.getCurrentPosition(
    position => {
      const here = { latitude: position.coords.latitude, longitude: position.coords.longitude };
      state.me = here;
      state.selectedId = null;
      state.focus = { ...here };
      render();
    },
    () => toast('Sin permiso para usar tu ubicación. Actívalo en los ajustes del navegador', 'error'),
    { enableHighAccuracy: true, timeout: 10000 });
}

// Los botones fijos de index.html
const search = document.querySelector('#search');
search.addEventListener('input', () => { state.query = search.value; render(); });
document.querySelector('#search-clear').addEventListener('click', () => { state.query = ''; search.value = ''; render(); });
document.querySelectorAll('.chip').forEach(chip => chip.addEventListener('click', () => {
  const price = chip.dataset.price;
  state.prices.has(price) ? state.prices.delete(price) : state.prices.add(price);
  render();
}));
document.querySelector('#account').addEventListener('click', () => requireLogin(null, showProfile));
document.querySelector('#points').addEventListener('click', showProfile);
document.querySelector('#open-redemptions').addEventListener('click', () => requireLogin('Inicia sesión para ver tus canjes.', showRedemptions));
document.querySelector('#locate').addEventListener('click', locate);

loadBusinesses();
loadUser();
