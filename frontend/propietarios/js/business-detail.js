import { request } from './api.js';
import { basicsFields, locationFields, problemsOf, styleFields } from './business-fields.js';
import { PRICE_LABEL, priceSymbols, rating, shortDate } from './format.js';
import { miniMap } from './location-picker.js';
import { el, esc, icon, openSheet, stars, toast } from './ui.js';

// Un local del propietario: cómo va (nota, reseñas, recompensas), sus recompensas, lo que dicen los clientes y
// sus datos. GET /businesses/{id}/summary trae el local, las 3 últimas reseñas y las recompensas de una vez
export function openBusinessDetail({ id, onChanged, onDeleted }) {
  let summary = null;
  let reviews = [];
  let nextCursor = null;
  let error = null;

  const screen = el(`
    <div class="screen" role="dialog" aria-label="Local">
      <div class="screen-head">
        <button class="icon-btn plain" aria-label="Volver a tus locales">${icon('back')}</button>
        <h1 style="flex: 1; min-width: 0"></h1>
      </div>
      <div class="content"><div class="skeleton" style="height: 160px; border-radius: 22px"></div></div>
    </div>`);
  const content = screen.querySelector('.content');
  document.body.append(screen);
  const close = () => screen.remove();
  screen.querySelector('.screen-head button').addEventListener('click', close);

  async function load() {
    try {
      const data = await request(`/businesses/${id}/summary`);
      summary = data;
      reviews = data.latestReviews;
      nextCursor = data.latestReviews.length === 3 ? data.latestReviews.at(-1).id : null;
    } catch (e) {
      error = e.message;
    }
    render();
  }

  async function loadMoreReviews() {
    const page = await request(`/reviews?business=${id}&limit=10&cursor=${nextCursor}`);
    reviews = [...reviews, ...page.data];
    nextCursor = page.nextCursor;
    render();
  }

  function render() {
    if (error) {
      content.innerHTML = `<div class="alert error">${esc(error)}</div>`;
      return;
    }
    const business = summary.business;
    screen.setAttribute('aria-label', business.name);
    screen.querySelector('h1').textContent = business.name;
    const plural = (n, one, many) => (n === 1 ? one : many);

    content.innerHTML = `
      <div data-slot="map"></div>
      <p class="muted">${esc(business.address)}</p>

      <div class="stats">
        <div class="stat"><strong>${business.ratingCount > 0 ? rating(business.averageRating) : '–'}</strong><span>nota media</span></div>
        <div class="stat"><strong>${business.ratingCount}</strong><span>${plural(business.ratingCount, 'reseña', 'reseñas')}</span></div>
        <div class="stat"><strong>${summary.rewards.length}</strong><span>${plural(summary.rewards.length, 'recompensa', 'recompensas')}</span></div>
      </div>

      <section class="section">
        <div class="section-head">
          <h3>Recompensas</h3>
          <button class="btn btn-ghost btn-small" data-action="add-reward">Añadir</button>
        </div>
        ${summary.rewards.length === 0 ? '<div class="empty">Ofrece algo a cambio de puntos (una caña, un postre) para que los clientes vuelvan.</div>' : ''}
        ${summary.rewards.map(reward => `
          <div class="reward-row">
            <div class="cost">${reward.pointsCost}<small>pts</small></div>
            <div>
              <strong>${esc(reward.title)}</strong>
              ${reward.description ? `<p class="muted small">${esc(reward.description)}</p>` : ''}
            </div>
          </div>`).join('')}
      </section>

      <section class="section">
        <h3 style="margin-bottom: 12px">Lo que dicen los clientes</h3>
        ${reviews.length === 0 ? '<div class="empty">Aún no hay reseñas. Aparecerán aquí cuando los clientes opinen.</div>' : ''}
        ${reviews.map(review => `
          <article class="review">
            <div class="review-head">
              <span><strong>${esc(review.user)}</strong> ${stars(review.rating)}</span>
              <span class="muted small">${shortDate(review.createdAt)}</span>
            </div>
            ${review.comment ? `<p>${esc(review.comment)}</p>` : ''}
          </article>`).join('')}
        ${nextCursor ? '<button class="btn btn-ghost btn-block" style="margin-top: 8px" data-action="more">Ver más reseñas</button>' : ''}
      </section>

      <section class="section">
        <div class="section-head">
          <h3>Datos del local</h3>
          <button class="btn btn-ghost btn-small" data-action="edit">${icon('pen', 18)} Editar</button>
        </div>
        <div class="review-list">
          <div><span>Teléfono</span><span>${esc(business.phone ?? '–')}</span></div>
          <div><span>Precio</span><span>${'€'.repeat(priceSymbols(business.priceRange))} ${PRICE_LABEL[business.priceRange] ?? ''}</span></div>
          <div><span>Etiquetas</span><span>${esc([...(business.tags ?? [])].sort().join(', ') || '–')}</span></div>
        </div>
        ${business.description ? `<p style="margin-top: 14px">${esc(business.description)}</p>` : ''}
      </section>

      <div class="danger">
        <button class="btn btn-ghost btn-block" style="color: var(--error)" data-action="delete">Borrar el local</button>
      </div>`;

    content.querySelector('[data-slot="map"]').replaceWith(miniMap(business.latitude, business.longitude));
    content.querySelector('[data-action="add-reward"]').addEventListener('click', () => openRewardForm(screen, id, load));
    content.querySelector('[data-action="more"]')?.addEventListener('click', loadMoreReviews);
    content.querySelector('[data-action="edit"]').addEventListener('click', () => openEditBusiness(business, () => { load(); onChanged(); }));
    content.querySelector('[data-action="delete"]').addEventListener('click', () => openDeleteBusiness(screen, business, () => { close(); onDeleted(); }));
  }

  load();
}

// Editar el local: los mismos campos que al darlo de alta, todos juntos. Se manda un JSON Patch solo con lo que
// ha cambiado (PATCH /businesses/{id}), así no se pisa nada más
function openEditBusiness(business, onSaved) {
  const original = {
    name: business.name, description: business.description ?? '', phone: business.phone ?? '', address: business.address,
    latitude: business.latitude, longitude: business.longitude, priceRange: business.priceRange, tags: [...(business.tags ?? [])].sort(),
  };
  const form = structuredClone(original);
  let showProblems = false;

  const wizard = el(`
    <div class="wizard" role="dialog" aria-label="Editar el local">
      <div class="wizard-top">
        <div class="screen-head" style="margin-bottom: 0">
          <button class="icon-btn plain" aria-label="Cancelar">${icon('back')}</button>
          <h1>Editar</h1>
        </div>
      </div>
      <div class="wizard-body"></div>
      <div class="wizard-foot">
        <button class="btn btn-ghost" data-action="cancel">Cancelar</button>
        <button class="btn btn-primary" data-action="save"></button>
      </div>
    </div>`);
  const body = wizard.querySelector('.wizard-body');
  const save = wizard.querySelector('[data-action="save"]');
  document.body.append(wizard);
  const close = () => wizard.remove();
  wizard.querySelector('.wizard-top .icon-btn').addEventListener('click', close);
  wizard.querySelector('[data-action="cancel"]').addEventListener('click', close);

  const changed = () => Object.keys(original).filter(key =>
    JSON.stringify(original[key]) !== JSON.stringify(key === 'tags' ? [...form.tags].sort() : form[key]));
  const updateSave = () => {
    save.disabled = changed().length === 0;
    save.textContent = changed().length === 0 ? 'Sin cambios' : 'Guardar cambios';
  };

  const ctx = { showProblems: () => showProblems, onChange: updateSave };
  const groups = [basicsFields(form, ctx), locationFields(form, ctx), styleFields(form, ctx)];
  body.append(groups[0], el('<h3 style="margin: 8px 0 10px">Ubicación</h3>'), groups[1], groups[2]);
  const alert = el('<div class="alert error" style="margin-top: 16px" hidden></div>');
  body.append(alert);
  updateSave();

  save.addEventListener('click', async () => {
    if (!Object.values(problemsOf(form)).every(p => !p)) {
      showProblems = true;
      groups.forEach(group => group.refresh());
      return;
    }
    save.disabled = true;
    save.textContent = 'Guardando…';
    alert.hidden = true;
    try {
      await request(`/businesses/${business.id}`, {
        method: 'PATCH',
        contentType: 'application/json-patch+json',
        body: changed().map(key => ({ op: 'replace', path: `/${key}`, value: typeof form[key] === 'string' ? form[key].trim() : form[key] })),
      });
      toast('Cambios guardados', 'ok');
      close();
      onSaved();
    } catch (e) {
      alert.textContent = e.message;
      alert.hidden = false;
      updateSave();
    }
  });
}

const PRESETS = [30, 60, 120];

// Nueva recompensa (POST /rewards). Los puntos se eligen de unos valores típicos o se escriben a mano;
// una reseña da 10 puntos, así que se explica cuántas reseñas cuesta conseguirla
function openRewardForm(screen, businessId, onCreated) {
  const sheet = openSheet({ label: 'Nueva recompensa', scrim: true, parent: screen });
  sheet.body.innerHTML = `
    <form>
      <h2 style="margin-bottom: 18px">Nueva recompensa</h2>
      <div class="field">
        <label for="title">Qué das</label>
        <input id="title" maxlength="150" placeholder="Una caña gratis" />
      </div>
      <div class="field">
        <label for="rdesc">Detalles <span class="muted">(opcional)</span></label>
        <input id="rdesc" maxlength="1000" placeholder="Estrella Galicia de barril" />
      </div>
      <div class="field">
        <label for="points">Puntos que cuesta</label>
        <div class="presets" role="group" aria-label="Valores típicos">
          ${PRESETS.map(value => `<button type="button" data-points="${value}">${value}</button>`).join('')}
        </div>
        <input id="points" type="number" inputmode="numeric" min="1" value="30" />
        <span class="hint"></span>
      </div>
      <div class="alert error" hidden></div>
      <button class="btn btn-primary btn-block" data-action="submit">Añadir recompensa</button>
    </form>`;
  const $ = selector => sheet.body.querySelector(selector);
  const submit = $('[data-action="submit"]');

  function refresh() {
    const points = Number($('#points').value);
    sheet.body.querySelectorAll('[data-points]').forEach(b => b.setAttribute('aria-pressed', String(Number(b.dataset.points) === points)));
    const reviewsNeeded = Math.ceil(points / 10);
    $('.hint').textContent = `Un cliente la consigue con ${Math.max(1, reviewsNeeded)} ${reviewsNeeded === 1 ? 'reseña' : 'reseñas'}.`;
    submit.disabled = !($('#title').value.trim().length >= 2 && points > 0);
  }
  sheet.body.querySelectorAll('[data-points]').forEach(b => b.addEventListener('click', () => { $('#points').value = b.dataset.points; refresh(); }));
  $('#points').addEventListener('input', refresh);
  $('#title').addEventListener('input', refresh);
  refresh();

  $('form').addEventListener('submit', async event => {
    event.preventDefault();
    submit.disabled = true;
    submit.textContent = 'Añadiendo…';
    $('.alert').hidden = true;
    try {
      await request('/rewards', { method: 'POST', body: {
        businessId, title: $('#title').value.trim(), description: $('#rdesc').value.trim() || null, pointsCost: Number($('#points').value), active: true,
      } });
      toast('Recompensa añadida', 'ok');
      sheet.close();
      onCreated();
    } catch (e) {
      $('.alert').textContent = e.message;
      $('.alert').hidden = false;
      submit.textContent = 'Añadir recompensa';
      refresh();
    }
  });
}

// Borrar no se puede deshacer y se lleva las reseñas y las recompensas: se pide escribir el nombre del local
function openDeleteBusiness(screen, business, onDeleted) {
  const sheet = openSheet({ label: 'Borrar el local', scrim: true, parent: screen });
  sheet.body.innerHTML = `
    <h2>¿Borrar ${esc(business.name)}?</h2>
    <p style="margin: 10px 0 18px">Desaparecerá del mapa con sus ${business.ratingCount} reseñas y sus recompensas. No se puede deshacer.</p>
    <div class="field">
      <label for="confirm">Escribe <strong>${esc(business.name)}</strong> para confirmar</label>
      <input id="confirm" autocomplete="off" />
    </div>
    <div class="alert error" hidden></div>
    <button class="btn btn-block" style="background: var(--error); color: #fff" disabled data-action="delete">Borrar para siempre</button>
    <button class="btn btn-ghost btn-block" style="margin-top: 8px" data-action="cancel">Cancelar</button>`;
  const input = sheet.body.querySelector('#confirm');
  const remove = sheet.body.querySelector('[data-action="delete"]');
  const alert = sheet.body.querySelector('.alert');
  input.addEventListener('input', () => { remove.disabled = input.value.trim() !== business.name; });
  sheet.body.querySelector('[data-action="cancel"]').addEventListener('click', () => sheet.close());

  remove.addEventListener('click', async () => {
    remove.disabled = true;
    remove.textContent = 'Borrando…';
    try {
      await request(`/businesses/${business.id}`, { method: 'DELETE' });
      toast(`${business.name} borrado`);
      sheet.close();
      onDeleted();
    } catch (e) {
      alert.textContent = e.message;
      alert.hidden = false;
      remove.disabled = false;
      remove.textContent = 'Borrar para siempre';
    }
  });
}
