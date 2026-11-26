import { el, esc, icon } from './ui.js';

// Tu cuenta: los puntos que tienes, cómo ganar más y cerrar sesión
export function openProfile({ user, onClose, onLogout, onRedemptions }) {
  const screen = el(`
    <div class="screen" role="dialog" aria-label="Tu cuenta">
      <div class="screen-head">
        <button class="icon-btn plain" data-action="back" aria-label="Volver al mapa">${icon('back')}</button>
        <h1>Hola, ${esc(user.name.split(' ')[0])}</h1>
      </div>

      <div class="big-points">
        <div class="coin">★</div>
        <div>
          <strong>${user.points}</strong>
          <div style="opacity: 0.75">puntos para gastar</div>
        </div>
      </div>

      <h3 style="margin-bottom: 12px">Cómo funciona</h3>
      <ol class="how">
        <li>Escribe una reseña de un local: ganas 10 puntos.</li>
        <li>Gástalos en las recompensas de los locales.</li>
        <li>Enseña el código QR en la barra.</li>
      </ol>

      <button class="btn btn-ghost btn-block" data-action="redemptions">Ver tus canjes</button>

      <div style="margin-top: 32px; padding-top: 20px; border-top: 1px solid var(--line)">
        <p class="muted small">Has entrado como <strong>${esc(user.username)}</strong> (${esc(user.email)})</p>
        <button class="btn btn-ghost btn-block" style="margin-top: 12px; color: var(--error)" data-action="logout">Cerrar sesión</button>
      </div>
    </div>`);
  document.body.append(screen);

  function close() {
    screen.remove();
    onClose();
  }
  screen.querySelector('[data-action="back"]').addEventListener('click', close);
  screen.querySelector('[data-action="redemptions"]').addEventListener('click', onRedemptions);
  screen.querySelector('[data-action="logout"]').addEventListener('click', onLogout);

  return { close };
}
