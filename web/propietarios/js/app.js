import { getSession, logout, request } from './api.js';
import { renderAuth } from './auth.js';
import { openBusinessDetail } from './business-detail.js';
import { rating } from './format.js';
import { openNewBusiness } from './new-business.js';
import { openScanner } from './scanner.js';
import { esc, toast } from './ui.js';

// Arriba tus locales; abajo, siempre a mano, el botón de escanear (lo que más se hace con el bar abierto).
// Lo que se abre encima (un local, el alta, el escáner) lo añaden sus propios archivos al final de la página
const root = document.querySelector('#root');
let user = null;
let businesses = null;
let error = null;

async function loadUser() {
  if (!getSession()) {
    user = null;
    render();
    return;
  }
  try {
    user = await request('/users/me');
  } catch {
    user = null;
  }
  render();
  loadBusinesses();
}

// Tus locales: GET /businesses?owner=<tu usuario>
async function loadBusinesses() {
  if (!user) return;
  error = null;
  try {
    const page = await request(`/businesses?owner=${encodeURIComponent(user.username)}&size=50`);
    businesses = page.content;
  } catch (e) {
    error = e.message;
  }
  render();
}

async function handleLogout() {
  await logout();
  user = null;
  businesses = null;
  render();
}

function render() {
  document.querySelector('.scan-dock')?.remove();
  if (!user) {
    renderAuth(root, loadUser);
    return;
  }

  // Una cuenta de cliente no puede tener locales: se explica en vez de enseñar botones que darían 403
  if (!user.roles.includes('OWNER')) {
    root.innerHTML = `
      <main class="page">
        <h1>Esta cuenta es de cliente</h1>
        <p style="margin: 12px 0 24px">
          <strong>${esc(user.username)}</strong> no puede tener locales. Cierra sesión y crea una cuenta de local con otro usuario.
        </p>
        <button class="btn btn-dark btn-block">Cerrar sesión</button>
      </main>`;
    root.querySelector('button').addEventListener('click', handleLogout);
    return;
  }

  root.innerHTML = `
    <main class="page">
      <div class="page-head">
        <div>
          <p class="muted">Hola, ${esc(user.name.split(' ')[0])}</p>
          <h1>Tus locales</h1>
        </div>
        <button class="btn btn-ghost btn-small" data-action="logout">Salir</button>
      </div>

      ${error ? `<div class="alert error">${esc(error)} <button class="btn btn-ghost btn-small" data-action="retry">Reintentar</button></div>` : ''}
      ${!businesses && !error ? '<div class="skeleton" style="height: 84px; border-radius: 22px"></div>' : ''}

      ${businesses?.length === 0 ? `
        <div class="empty" style="text-align: left; padding: 22px">
          <h2 style="color: var(--ink)">Da de alta tu primer local</h2>
          <p style="margin: 8px 0 18px">En 4 pasos estará en el mapa de los clientes: nombre, ubicación, precio y etiquetas.</p>
          <button class="btn btn-dark btn-block" data-action="create">Empezar</button>
        </div>` : ''}

      ${(businesses ?? []).map(business => `
        <button class="biz-card" data-id="${business.id}">
          <span class="biz-mark ${business.ratingCount === 0 ? 'is-new' : ''}">
            ${business.ratingCount > 0 ? `★${rating(business.averageRating)}` : 'Nuevo'}
          </span>
          <div>
            <strong>${esc(business.name)}</strong>
            <span class="muted small">${esc(business.address)}</span>
            <span class="muted small">${business.ratingCount} ${business.ratingCount === 1 ? 'reseña' : 'reseñas'}</span>
          </div>
        </button>`).join('')}

      ${businesses?.length > 0 ? '<button class="add-card" data-action="create">+ Añadir otro local</button>' : ''}
    </main>`;

  root.querySelector('[data-action="logout"]').addEventListener('click', handleLogout);
  root.querySelector('[data-action="retry"]')?.addEventListener('click', loadBusinesses);
  root.querySelectorAll('[data-action="create"]').forEach(b => b.addEventListener('click', createBusiness));
  root.querySelectorAll('.biz-card').forEach(card => card.addEventListener('click', () => openBusiness(Number(card.dataset.id))));

  if (businesses?.length > 0) {
    const dock = document.createElement('div');
    dock.className = 'scan-dock';
    dock.innerHTML = '<button class="btn btn-primary">Escanear canje</button>';
    dock.querySelector('button').addEventListener('click', () => openScanner());
    root.after(dock);
  }
}

function openBusiness(id) {
  openBusinessDetail({ id, onChanged: loadBusinesses, onDeleted: loadBusinesses });
}

function createBusiness() {
  openNewBusiness({
    onCreated: created => {
      toast(`${created.name} ya está en el mapa`, 'ok');
      loadBusinesses();
      openBusiness(created.id);
    },
  });
}

// Mientras se comprueba la sesión guardada no se pinta nada (como antes: sin parpadeo de la pantalla de entrar)
if (getSession()) loadUser();
else render();
