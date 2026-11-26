import { request } from './api.js';
import { el, esc, icon } from './ui.js';

// El QR de un canje, a pantalla completa para que el local lo escanee. El backend firma un QR nuevo cada 30
// segundos (GET /redemptions/{id}/qr): aquí se pide otro cuando llega expiresAt, y una barra enseña el tiempo
// que le queda. Mientras está abierto se pregunta cada pocos segundos si el local ya lo validó, para enseñar
// el check en cuanto pasa
export function openQr({ redemption, businessName, onClose, onUsed }) {
  let qr = null;
  let status = redemption.status;
  let error = null;
  let qrTimer, tick, poll;

  const screen = el(`
    <div class="screen" role="dialog" aria-label="Código del canje">
      <div class="screen-head">
        <button class="icon-btn plain" aria-label="Volver">${icon('back')}</button>
      </div>
      <div class="qr-screen"></div>
    </div>`);
  const content = screen.querySelector('.qr-screen');
  document.body.append(screen);

  function close() {
    stop();
    screen.remove();
    onClose();
  }
  screen.querySelector('.screen-head button').addEventListener('click', close);

  function stop() {
    clearTimeout(qrTimer);
    clearInterval(tick);
    clearInterval(poll);
  }

  // Un QR nuevo cada vez que caduca el anterior
  async function fetchQr() {
    try {
      qr = await request(`/redemptions/${redemption.id}/qr`);
      error = null;
      render();
      qrTimer = setTimeout(fetchQr, Math.max(1000, new Date(qr.expiresAt).getTime() - Date.now()));
    } catch (e) {
      // 409: el canje ya no está pendiente (lo acaban de usar o ha caducado)
      if (e.status === 409) checkStatus();
      else {
        error = e.message;
        render();
        qrTimer = setTimeout(fetchQr, 5000);
      }
    }
  }

  async function checkStatus() {
    const fresh = await request(`/redemptions/${redemption.id}`).catch(() => null);
    if (fresh && fresh.status !== 'PENDING' && status === 'PENDING') {
      status = fresh.status;
      stop();
      render();
      if (status === 'USED') onUsed();
    }
  }

  // El QR, dibujado como SVG por qrcode-generator (nivel de corrección M, como antes)
  function qrSvg(text) {
    const code = qrcode(0, 'M');
    code.addData(text);
    code.make();
    return code.createSvgTag({ cellSize: 4, margin: 0, scalable: true });
  }

  function render() {
    const left = qr ? Math.max(0, Math.round((new Date(qr.expiresAt).getTime() - Date.now()) / 1000)) : 30;
    content.innerHTML = `
      <h1>${esc(redemption.reward)}</h1>
      <p class="muted" style="margin-top: 6px">${esc(businessName)}</p>

      ${status === 'USED' ? `
        <div class="done-mark">${icon('check', 60)}</div>
        <h2>¡Canjeado!</h2>
        <p class="muted" style="margin-top: 8px">Que aproveche.</p>
        <button class="btn btn-dark btn-block" style="margin-top: 32px">Volver al mapa</button>` : ''}

      ${status === 'EXPIRED' ? '<p class="alert error" style="margin-top: 32px">Este canje ha caducado: pasaron 30 días sin usarlo.</p>' : ''}

      ${status === 'PENDING' ? `
        <div class="qr-card" aria-label="Código QR">
          ${qr ? qrSvg(qr.qr) : '<div class="skeleton" style="width: min(68vw, 300px); aspect-ratio: 1"></div>'}
          <div class="countdown" aria-hidden="true"><div style="width: ${(left / 30) * 100}%"></div></div>
        </div>
        <p class="small muted" aria-live="off">El código cambia en <span data-left>${left}</span> s</p>
        <p class="code">${esc(redemption.code)}</p>
        ${error ? `<div class="alert error" style="margin-top: 16px">${esc(error)}</div>` : ''}
        <ol class="how" style="margin-top: 28px; text-align: left; width: 100%">
          <li>Enseña esta pantalla en la barra.</li>
          <li>El local escanea el código con su móvil.</li>
          <li>Verás un check verde cuando esté hecho.</li>
        </ol>
        <p class="small muted">Una captura de pantalla no sirve: el código cambia cada 30 segundos.</p>` : ''}`;
    content.querySelector('.btn-dark')?.addEventListener('click', close);
  }

  // Cada segundo solo se mueven la barra y el número (si se volviera a dibujar el QR, parpadearía)
  function updateCountdown() {
    if (!qr) return;
    const left = Math.max(0, Math.round((new Date(qr.expiresAt).getTime() - Date.now()) / 1000));
    content.querySelector('.countdown div').style.width = `${(left / 30) * 100}%`;
    content.querySelector('[data-left]').textContent = left;
  }

  render();
  if (status === 'PENDING') {
    fetchQr();
    // El reloj de la barra, y la pregunta "¿ya está usado?" cada 3 segundos
    tick = setInterval(updateCountdown, 1000);
    poll = setInterval(checkStatus, 3000);
  }
}
