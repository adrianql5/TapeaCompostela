import { request } from './api.js';
import { el, esc, icon } from './ui.js';

// Tu QR para sumar puntos, a pantalla completa. Lo enseñas al pagar y el dueño del local lo escanea: ganas los
// puntos por visita de ese local. Como el QR de un canje, el backend firma uno nuevo cada 30 segundos
// (GET /users/me/qr), así que una captura no sirve. Mientras está abierto se miran tus puntos cada 3 segundos
// (GET /users/me) para enseñar al momento cuántos acabas de ganar
export function openMyQr({ user, onClose }) {
  let qr = null;
  let points = user.points;
  let gained = 0;
  let error = null;
  let qrTimer, tick, poll;

  const screen = el(`
    <div class="screen" role="dialog" aria-label="Tu QR para sumar puntos">
      <div class="screen-head">
        <button class="icon-btn plain" aria-label="Volver">${icon('back')}</button>
      </div>
      <div class="qr-screen"></div>
    </div>`);
  const content = screen.querySelector('.qr-screen');
  document.body.append(screen);

  function close() {
    clearTimeout(qrTimer);
    clearInterval(tick);
    clearInterval(poll);
    screen.remove();
    onClose();
  }
  screen.querySelector('.screen-head button').addEventListener('click', close);

  // Un QR nuevo cada vez que caduca el anterior
  async function fetchQr() {
    try {
      qr = await request('/users/me/qr');
      error = null;
      qrTimer = setTimeout(fetchQr, Math.max(1000, new Date(qr.expiresAt).getTime() - Date.now()));
    } catch (e) {
      error = e.message;
      qrTimer = setTimeout(fetchQr, 5000);
    }
    render();
  }

  // Si los puntos han subido, el local acaba de escanear el QR. Se puede volver a escanear: no hay límite
  async function checkPoints() {
    const me = await request('/users/me').catch(() => null);
    if (me && me.points > points) {
      gained = me.points - points;
      points = me.points;
      render();
    }
  }

  function qrSvg(text) {
    const code = qrcode(0, 'M');
    code.addData(text);
    code.make();
    return code.createSvgTag({ cellSize: 4, margin: 0, scalable: true });
  }

  function secondsLeft() {
    return qr ? Math.max(0, Math.round((new Date(qr.expiresAt).getTime() - Date.now()) / 1000)) : 30;
  }

  function render() {
    const left = secondsLeft();
    content.innerHTML = `
      <h1>Tu QR para sumar puntos</h1>
      <p class="muted" style="margin-top: 6px">Tienes <strong>${points}</strong> puntos</p>
      ${gained ? `<div class="gained" role="status">${icon('check', 24)} +${gained} puntos</div>` : ''}
      <div class="qr-card" aria-label="Código QR">
        ${qr ? qrSvg(qr.qr) : '<div class="skeleton" style="width: min(68vw, 300px); aspect-ratio: 1"></div>'}
        <div class="countdown" aria-hidden="true"><div style="width: ${(left / 30) * 100}%"></div></div>
      </div>
      <p class="small muted" aria-live="off">El código cambia en <span data-left>${left}</span> s</p>
      ${error ? `<div class="alert error" style="margin-top: 16px">${esc(error)}</div>` : ''}
      <ol class="how" style="margin-top: 28px; text-align: left; width: 100%">
        <li>Enséñalo al pagar en un local de TapeaCompostela.</li>
        <li>El local lo escanea con su móvil.</li>
        <li>Ganas los puntos por visita de ese local: siempre más que con una reseña.</li>
      </ol>`;
  }

  // Cada segundo solo se mueven la barra y el número (si se volviera a dibujar el QR, parpadearía)
  function updateCountdown() {
    if (!qr) return;
    const left = secondsLeft();
    content.querySelector('.countdown div').style.width = `${(left / 30) * 100}%`;
    content.querySelector('[data-left]').textContent = left;
  }

  render();
  fetchQr();
  tick = setInterval(updateCountdown, 1000);
  poll = setInterval(checkPoints, 3000);
}
