import { request } from './api.js';
import { el, esc, icon, openSheet } from './ui.js';

// Validar un canje escaneando el QR del móvil del cliente. El QR es "<id>.<ventana>.<firma>": la primera parte
// dice qué canje es, y el QR entero va en la cabecera Redemption-QR como prueba de que el cliente está delante.
// El resultado sale en grande, verde o rojo, para verlo de un vistazo con la barra llena de gente.
// La cámara se abre con getUserMedia (solo funciona con HTTPS o en localhost) y jsQR busca el QR en sus imágenes
export function openScanner() {
  let busy = false;
  let result = null;       // { ok: true, redemption } o { ok: false, message }
  let stream = null;
  let timer = null;
  let sheet = null;

  const scanner = el(`
    <div class="scanner" role="dialog" aria-label="Escanear un canje">
      <div class="scanner-top">
        <strong>Escanear canje</strong>
      </div>
      <div class="scanner-view">
        <video autoplay playsinline muted style="position: absolute; inset: 0; width: 100%; height: 100%"></video>
        <div class="scanner-frame" aria-hidden="true"></div>
        <div class="scanner-below">
          <p class="scanner-hint">Apunta al código QR del cliente</p>
          <button class="btn scanner-close">${icon('close')} Cerrar</button>
        </div>
      </div>
    </div>`);
  const video = scanner.querySelector('video');
  const hint = scanner.querySelector('.scanner-hint');
  document.body.append(scanner);
  // Cerrar va debajo del recuadro, donde ya está el pulgar al apuntar con el móvil
  scanner.querySelector('.scanner-close').addEventListener('click', close);
  // Safari del iPhone solo pinta el vídeo de la cámara dentro de la página si es inline y sin sonido. En el HTML ya
  // están los atributos, pero hay que poner también las propiedades: si no, se queda en negro
  video.muted = true;
  video.playsInline = true;

  function close() {
    clearInterval(timer);
    stream?.getTracks().forEach(track => track.stop());   // apaga la cámara
    sheet?.remove();
    scanner.remove();
  }

  // La cámara trasera (facingMode: environment). La resolución es solo una preferencia (ideal), no un mínimo:
  // con algunas cámaras, pedir un mínimo haría que no arrancase
  async function startCamera() {
    try {
      stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment', width: { ideal: 720 }, height: { ideal: 720 } } });
      video.srcObject = stream;
      // Se espera a que lleguen los datos de la cámara antes de reproducir
      await new Promise(resolve => video.readyState >= 1 ? resolve() : video.addEventListener('loadedmetadata', resolve, { once: true }));
      await video.play();
      timer = setInterval(scanFrame, 200);
    } catch {
      hint.textContent = 'No se puede usar la cámara';
      showCameraError();
    }
  }

  // Cinco veces por segundo: copia la imagen de la cámara en un canvas y le pide a jsQR que busque un QR
  const canvas = document.createElement('canvas');
  function scanFrame() {
    if (busy || result || !video.videoWidth) return;
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const context = canvas.getContext('2d', { willReadFrequently: true });
    context.drawImage(video, 0, 0);
    const code = jsQR(context.getImageData(0, 0, canvas.width, canvas.height).data, canvas.width, canvas.height);
    if (code?.data) validate(code.data);
  }

  async function validate(qr) {
    if (busy || result) return;
    busy = true;
    hint.textContent = 'Comprobando…';
    navigator.vibrate?.(60);
    try {
      const id = qr.trim().split('.')[0];
      if (!/^\d+$/.test(id)) throw new Error('Este QR no es de TapeaCompostela');
      const redemption = await request(`/redemptions/${id}`, {
        method: 'PATCH',
        contentType: 'application/json-patch+json',
        headers: { 'Redemption-QR': qr.trim() },
        body: [{ op: 'replace', path: '/status', value: 'USED' }],
      });
      result = { ok: true, redemption };
    } catch (e) {
      result = { ok: false, message: e.status === 403 ? 'Este canje es de otro local' : e.message };
    } finally {
      busy = false;
      hint.textContent = stream ? 'Apunta al código QR del cliente' : 'No se puede usar la cámara';
    }
    showResult();
  }

  function showCameraError() {
    sheet = openSheet({ label: 'Sin cámara', parent: scanner, onClose: close });
    sheet.body.innerHTML = `
      <h2>Necesitamos la cámara</h2>
      <p style="margin: 10px 0 16px">
        Dale permiso a esta web para usar la cámara (en los ajustes del navegador) y vuelve a abrir el escáner.
      </p>
      <details>
        <summary class="muted small" style="cursor: pointer; margin-bottom: 12px">Pegar el texto del QR a mano</summary>
        <div class="field">
          <label for="qr">Texto del QR</label>
          <input id="qr" placeholder="12.59808429.-Lxl4w…" autocapitalize="none" />
        </div>
        <button class="btn btn-dark btn-block" disabled>Validar</button>
      </details>`;
    const input = sheet.body.querySelector('#qr');
    const button = sheet.body.querySelector('.btn');
    input.addEventListener('input', () => { button.disabled = !input.value.trim(); });
    button.addEventListener('click', () => {
      sheet.remove();   // sin cerrar el escáner: en su lugar saldrá el resultado
      validate(input.value);
    });
  }

  function showResult() {
    sheet = openSheet({ label: result.ok ? 'Canje válido' : 'Canje no válido', parent: scanner, onClose: close });
    sheet.body.innerHTML = `
      <div class="result">
        <div class="result-mark ${result.ok ? 'ok' : 'bad'}">${result.ok ? icon('check', 52) : icon('close', 52)}</div>
        ${result.ok ? `
          <h2>Canje válido</h2>
          <p style="font-size: 20px; font-weight: 700; margin-top: 10px">${esc(result.redemption.reward)}</p>
          <p class="muted" style="margin-top: 4px">para ${esc(result.redemption.user)}, código ${esc(result.redemption.code)}</p>` : `
          <h2>No se puede canjear</h2>
          <p style="margin-top: 10px">${esc(result.message)}</p>`}
      </div>
      <button class="btn btn-dark btn-block" style="margin-top: 24px" data-action="again">Escanear otro</button>
      <button class="btn btn-ghost btn-block" style="margin-top: 8px" data-action="done">Terminar</button>`;
    sheet.body.querySelector('[data-action="done"]').addEventListener('click', close);
    sheet.body.querySelector('[data-action="again"]').addEventListener('click', () => {
      // Se quita la hoja del resultado sin cerrar el escáner, y vuelve a buscar QR
      sheet.remove();
      result = null;
      if (!stream) showCameraError();
    });
  }

  startCamera();
}
