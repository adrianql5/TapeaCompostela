import { request } from './api.js';
import { distance, PRICE_LABEL, priceSymbols, rating, shortDate, walking } from './format.js';
import { esc, icon, openSheet, stars, starsInput, toast } from './ui.js';

// La ficha de un local. Primero sube a media pantalla con lo básico (nombre, nota, precio, distancia); al tirar
// hacia arriba se ve entera: recompensas, reseñas y datos de contacto.
// GET /businesses/{id}/summary trae el local, sus 3 últimas reseñas y sus recompensas en una sola petición.
// app.js llama a update() cada vez que cambia el local elegido, el usuario o tu ubicación
export function openBusiness({ onClose, onNeedLogin, onRedeem, onChanged }) {
  let props = null;          // { business, user, me }: lo que manda app.js
  let summary = null;
  let reviews = [];
  let nextCursor = null;
  let error = null;

  const sheet = openSheet({ label: '', size: 'peek', resizable: true, onClose, onSizeChange: render });

  async function load() {
    const id = props.business.id;
    try {
      const data = await request(`/businesses/${id}/summary`);
      if (id !== props.business.id) return;   // mientras tanto se eligió otro local
      summary = data;
      reviews = data.latestReviews;
      // Las 3 de la summary son la primera página: si vinieron 3, puede haber más a partir de la última
      nextCursor = data.latestReviews.length === 3 ? data.latestReviews.at(-1).id : null;
    } catch (e) {
      error = e.message;
    }
    render();
  }

  async function loadMoreReviews() {
    try {
      const page = await request(`/reviews?business=${props.business.id}&limit=10&cursor=${nextCursor}`);
      reviews = [...reviews, ...page.data];
      nextCursor = page.nextCursor;
    } catch (e) {
      error = e.message;
    }
    render();
  }

  function update(newProps) {
    const changedBusiness = props?.business.id !== newProps.business.id;
    props = newProps;
    if (changedBusiness) {
      summary = null;
      error = null;
      if (sheet.size !== 'peek') sheet.setSize('peek');
      load();
    }
    render();
  }

  // Vuelve a pintar todo el contenido de la hoja con el estado actual
  function render() {
    const { business, user, me } = props;
    const detail = summary?.business ?? business;
    const alreadyReviewed = user && reviews.some(review => review.user === user.username);
    const meters = me ? distance(me, business) : null;
    const symbols = priceSymbols(detail.priceRange);
    sheet.body.closest('.sheet').setAttribute('aria-label', business.name);

    sheet.body.innerHTML = `
      <div class="sheet-head">
        <div>
          <h2>${esc(detail.name)}</h2>
          <div class="meta-row">
            ${detail.ratingCount > 0
              ? `<span class="rating"><span class="star">★</span>${rating(detail.averageRating)} <span class="muted">(${detail.ratingCount})</span></span>`
              : '<span class="muted">Sin reseñas todavía</span>'}
            <span class="price" aria-label="Precio: ${PRICE_LABEL[detail.priceRange]}">${'€'.repeat(symbols)}<span class="off">${'€'.repeat(3 - symbols)}</span></span>
            ${meters !== null ? `<span class="muted">${walking(meters)}</span>` : ''}
          </div>
        </div>
        <button class="icon-btn plain" data-action="close" aria-label="Cerrar">${icon('close', 20)}</button>
      </div>

      ${detail.tags?.length > 0
        ? `<div class="tags">${[...detail.tags].sort().map(tag => `<span class="tag">${esc(tag)}</span>`).join('')}</div>`
        : ''}

      <div class="quick-actions">
        <a class="btn btn-primary" target="_blank" rel="noreferrer"
           href="https://www.google.com/maps/dir/?api=1&destination=${business.latitude},${business.longitude}&travelmode=walking">
          ${icon('route', 20)} Cómo llegar
        </a>
        <button class="btn btn-ghost" data-action="size">${sheet.size === 'full' ? 'Ver menos' : 'Ver más'}</button>
      </div>

      ${error ? `<div class="alert error" style="margin-top: 16px">${esc(error)}</div>` : ''}

      ${!summary && !error ? `
        <div style="display: grid; gap: 10px; margin-top: 24px">
          <div class="skeleton" style="height: 22px; width: 50%"></div>
          <div class="skeleton" style="height: 120px"></div>
        </div>` : ''}

      ${summary ? `
        ${detail.description ? `<p style="margin-top: 20px">${esc(detail.description)}</p>` : ''}

        <section class="section">
          <div class="section-head">
            <h3>Recompensas</h3>
            ${user ? `<span class="muted small">Tienes ${user.points} pts</span>` : ''}
          </div>
          ${summary.rewards.length === 0
            ? '<div class="empty">Este local aún no ofrece recompensas</div>'
            : `<div class="rewards">${summary.rewards.map(reward => rewardCard(reward, user)).join('')}</div>`}
        </section>

        <section class="section">
          <div class="section-head">
            <h3>Reseñas</h3>
            ${!alreadyReviewed ? `<button class="btn btn-ghost btn-small" data-action="write">${icon('pen', 18)} Escribir</button>` : ''}
          </div>
          ${!alreadyReviewed ? '<p class="muted small" style="margin-top: -6px; margin-bottom: 12px">Cada reseña te da 10 puntos.</p>' : ''}
          ${reviews.length === 0 ? '<div class="empty">Nadie ha escrito una reseña. Sé el primero</div>' : ''}
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
          <h3 style="margin-bottom: 4px">Información</h3>
          <div class="info-row">${icon('pin')} ${esc(detail.address)}</div>
          ${detail.phone ? `<a class="info-row" href="tel:${esc(detail.phone)}">${icon('phone')} ${esc(detail.phone)}</a>` : ''}
        </section>` : ''}`;

    // Los botones de la ficha. data-action dice qué hace cada uno
    const on = (action, handler) => sheet.body.querySelectorAll(`[data-action="${action}"]`).forEach(b => b.addEventListener('click', handler));
    on('close', () => sheet.close());
    on('size', () => sheet.setSize(sheet.size === 'full' ? 'peek' : 'full'));
    on('more', loadMoreReviews);
    on('login', onNeedLogin);
    on('write', () => (user ? openReviewForm() : onNeedLogin()));
    on('redeem', event => {
      const reward = summary.rewards.find(r => r.id === Number(event.currentTarget.dataset.reward));
      openRedeemConfirm(reward, detail.name);
    });
  }

  function rewardCard(reward, user) {
    const missing = user ? reward.pointsCost - user.points : 0;
    return `
      <article class="reward">
        <div class="cost">${reward.pointsCost} <small>pts</small></div>
        <strong>${esc(reward.title)}</strong>
        <p>${esc(reward.description)}</p>
        ${!user ? '<button class="btn btn-primary btn-small" data-action="login">Inicia sesión para canjear</button>' : ''}
        ${user && missing > 0 ? `<span class="missing">Te faltan ${missing} pts. Escribe reseñas para ganarlos</span>` : ''}
        ${user && missing <= 0 ? `<button class="btn btn-primary btn-small" data-action="redeem" data-reward="${reward.id}">Canjear</button>` : ''}
      </article>`;
  }

  // Escribir una reseña: POST /reviews. Los puntos llegan unos segundos después (los da un evento en el backend)
  function openReviewForm() {
    const business = props.business;
    let rating = 0;
    const form = openSheet({ label: 'Escribir una reseña', scrim: true });
    form.body.innerHTML = `
      <form>
        <h2>¿Qué tal en ${esc(business.name)}?</h2>
        <div data-slot="stars"></div>
        <div class="field">
          <label for="comment">Comentario <span class="muted">(opcional)</span></label>
          <textarea id="comment" maxlength="2000" placeholder="La tapa, el trato, el ambiente..."></textarea>
        </div>
        <div class="alert error" hidden></div>
        <button class="btn btn-primary btn-block" disabled>Publicar reseña</button>
        <button type="button" class="btn btn-ghost btn-block" style="margin-top: 8px">Cancelar</button>
      </form>`;
    const [submit, cancel] = form.body.querySelectorAll('button.btn');
    const alert = form.body.querySelector('.alert');
    form.body.querySelector('[data-slot="stars"]').replaceWith(starsInput(value => { rating = value; submit.disabled = false; }));
    cancel.addEventListener('click', () => form.close());

    form.body.querySelector('form').addEventListener('submit', async event => {
      event.preventDefault();
      submit.disabled = true;
      submit.textContent = 'Publicando…';
      alert.hidden = true;
      try {
        const comment = form.body.querySelector('#comment').value.trim();
        await request('/reviews', { method: 'POST', body: { businessId: business.id, rating, comment: comment || null } });
        toast('Reseña publicada. +10 puntos', 'ok');
        form.close();
        load();
        onChanged();
      } catch (e) {
        alert.textContent = e.message;
        alert.hidden = false;
        submit.disabled = false;
        submit.textContent = 'Publicar reseña';
      }
    });
  }

  // Confirmar antes de gastar puntos: no se puede deshacer. La Idempotency-Key se crea al abrir esta hoja, así que
  // si la red falla y se pulsa otra vez, el backend devuelve el mismo canje en vez de cobrar dos veces.
  // crypto.randomUUID() solo existe con HTTPS o en localhost: si no está, vale cualquier texto que no se repita
  function openRedeemConfirm(reward, businessName) {
    const user = props.user;
    const idempotencyKey = crypto.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
    const confirm = openSheet({ label: 'Confirmar canje', scrim: true });
    confirm.body.innerHTML = `
      <h2>${esc(reward.title)}</h2>
      <p class="muted" style="margin-top: 6px">${esc(businessName)}</p>
      <div class="big-points" style="margin-top: 20px">
        <div class="coin">${reward.pointsCost}</div>
        <div>
          <div>Gastas ${reward.pointsCost} puntos</div>
          <div style="opacity: 0.7">Te quedarán ${user.points - reward.pointsCost}</div>
        </div>
      </div>
      <p class="muted small" style="margin-bottom: 16px">
        Recibirás un código QR. Enséñalo en el local para que lo escaneen; tienes 30 días para usarlo.
      </p>
      <div class="alert error" hidden></div>
      <button class="btn btn-primary btn-block">Canjear por ${reward.pointsCost} puntos</button>
      <button class="btn btn-ghost btn-block" style="margin-top: 8px">Cancelar</button>`;
    const [redeem, cancel] = confirm.body.querySelectorAll('button');
    const alert = confirm.body.querySelector('.alert');
    cancel.addEventListener('click', () => confirm.close());

    redeem.addEventListener('click', async () => {
      redeem.disabled = true;
      redeem.textContent = 'Canjeando…';
      alert.hidden = true;
      try {
        const redemption = await request('/redemptions', {
          method: 'POST',
          headers: { 'Idempotency-Key': idempotencyKey },
          body: { rewardId: reward.id },
        });
        confirm.close();
        onRedeem(redemption);
      } catch (e) {
        alert.textContent = e.message;
        alert.hidden = false;
        redeem.disabled = false;
        redeem.textContent = `Canjear por ${reward.pointsCost} puntos`;
      }
    });
  }

  return { update, remove: () => sheet.remove() };
}
