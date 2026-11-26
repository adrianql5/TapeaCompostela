import { request } from './api.js';
import { shortDate } from './format.js';
import { el, esc, icon } from './ui.js';

const STATUS = { PENDING: 'Para usar', USED: 'Usado', EXPIRED: 'Caducado' };

// Tus canjes (GET /redemptions): primero los que están por usar, que abren su QR al tocarlos
export function openRedemptions({ businessNames, onClose, onOpen }) {
  const screen = el(`
    <div class="screen" role="dialog" aria-label="Tus canjes">
      <div class="screen-head">
        <button class="icon-btn plain" aria-label="Volver al mapa">${icon('back')}</button>
        <h1>Tus canjes</h1>
      </div>
      <div class="list"><div class="skeleton" style="height: 76px"></div></div>
    </div>`);
  const list = screen.querySelector('.list');
  document.body.append(screen);

  function close() {
    screen.remove();
    onClose();
  }
  screen.querySelector('.screen-head button').addEventListener('click', close);

  function ticket(redemption) {
    const pending = redemption.status === 'PENDING';
    return `
      <button class="ticket ${pending ? 'pending' : ''}" data-id="${redemption.id}" ${pending ? '' : 'disabled'}>
        <span class="ticket-icon">${icon('ticket')}</span>
        <div>
          <strong>${esc(redemption.reward)}</strong>
          <span class="muted small">${esc(businessNames[redemption.businessId] ?? 'Local')}, ${shortDate(redemption.redeemedAt)}</span>
        </div>
        <span class="status ${redemption.status}">${STATUS[redemption.status]}</span>
      </button>`;
  }

  request('/redemptions')
    .then(redemptions => {
      const pending = redemptions.filter(r => r.status === 'PENDING');
      const past = redemptions.filter(r => r.status !== 'PENDING');
      list.innerHTML = `
        ${redemptions.length === 0 ? `
          <div class="empty" style="text-align: left">
            <strong style="color: var(--ink)">Todavía no has canjeado nada</strong>
            <ol class="how" style="margin-top: 14px; margin-bottom: 0">
              <li>Escribe reseñas de los locales: cada una te da 10 puntos.</li>
              <li>Abre un local en el mapa y elige una recompensa.</li>
              <li>Aquí tendrás el código para enseñarlo en la barra.</li>
            </ol>
          </div>` : ''}
        ${pending.map(ticket).join('')}
        ${past.length > 0 ? `<h3 style="margin: 24px 0 12px">Anteriores</h3>${past.map(ticket).join('')}` : ''}`;
      list.querySelectorAll('.ticket.pending').forEach(button => button.addEventListener('click', () => {
        onOpen(redemptions.find(r => r.id === Number(button.dataset.id)));
      }));
    })
    .catch(e => { list.innerHTML = `<div class="alert error">${esc(e.message)}</div>`; });

  return { close };
}
