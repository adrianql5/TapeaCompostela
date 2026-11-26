// Piezas de la interfaz que usan todas las pantallas: crear HTML de forma segura, los iconos, los avisos, la hoja
// que sube desde abajo y las estrellas

// Cualquier texto que venga de la API (un nombre, un comentario) se escapa antes de meterlo en el HTML. Si no, una
// reseña con "<script>" se ejecutaría en el móvil de quien la lee
export function esc(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;').replaceAll("'", '&#39;');
}

// Convierte un trozo de HTML en un elemento del DOM, para añadirlo a la página con append()
export function el(html) {
  const template = document.createElement('template');
  template.innerHTML = html.trim();
  return template.content.firstElementChild;
}

// Iconos en SVG, dibujados con trazos de 2px. aria-hidden: son decoración, el texto o el aria-label del botón
// es lo que lee un lector de pantalla
const PATHS = {
  search: '<circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" />',
  close: '<path d="M18 6 6 18M6 6l12 12" />',
  back: '<path d="M15 18l-6-6 6-6" />',
  user: '<circle cx="12" cy="8" r="4" /><path d="M4 21c0-4 4-6 8-6s8 2 8 6" />',
  ticket: '<path d="M3 8a2 2 0 0 0 2-2h14a2 2 0 0 0 2 2v2a2 2 0 0 0 0 4v2a2 2 0 0 0-2 2H5a2 2 0 0 0-2-2v-2a2 2 0 0 0 0-4z" /><path d="M13 6v12" stroke-dasharray="2 2" />',
  locate: '<circle cx="12" cy="12" r="4" /><path d="M12 2v3M12 19v3M2 12h3M19 12h3" />',
  phone: '<path d="M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2" />',
  route: '<path d="M3 11l18-8-8 18-2-8z" />',
  pin: '<path d="M12 22s7-6.5 7-12a7 7 0 0 0-14 0c0 5.5 7 12 7 12z" /><circle cx="12" cy="10" r="2.5" />',
  check: '<path d="M5 12.5l4.5 4.5L19 7" />',
  pen: '<path d="M4 20h4L19 9l-4-4L4 16z" />',
};

export function icon(name, size = 22) {
  return `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${PATHS[name]}</svg>`;
}

// Un aviso arriba de la pantalla que desaparece solo: toast('Reseña publicada', 'ok').
// role="status": un lector de pantalla lee el aviso en cuanto aparece
const toastRegion = el('<div role="status" aria-live="polite"></div>');
document.body.append(toastRegion);
let toastTimer;

export function toast(text, kind = 'info') {
  clearTimeout(toastTimer);
  const box = el(`<div class="toast ${kind}">${kind === 'ok' ? icon('check') : ''}${esc(text)}</div>`);
  box.addEventListener('click', () => box.remove());
  toastRegion.replaceChildren(box);
  toastTimer = setTimeout(() => box.remove(), kind === 'error' ? 5000 : 3000);
}

// Las hojas abiertas, de abajo arriba. Escape solo cierra la de arriba, y cada hoja nueva se pinta por encima
// de las anteriores (también su fondo oscuro)
const openSheets = [];

window.addEventListener('keydown', event => {
  if (event.key === 'Escape' && openSheets.length > 0) openSheets.at(-1).close();
});

// Una hoja que sube desde abajo, como en las apps de patinetes. size: "peek" (media pantalla), "full" o "auto"
// (lo que mida el contenido). Se cierra con close(), tocando el fondo oscuro (si hay), con Escape o arrastrando el
// tirador hacia abajo. Si es resizable, arrastrarlo hacia arriba la abre entera y un toque alterna los dos tamaños.
// parent: dónde se añade. Por defecto al final de la página; una hoja que se abre desde una pantalla completa va
// dentro de esa pantalla, para quedar por encima de ella.
// Devuelve un objeto con body (donde se mete el contenido), setSize(), close() y remove()
export function openSheet({ label, size = 'auto', scrim = false, resizable = false, parent = document.body, onClose = () => {}, onSizeChange = () => {} }) {
  const z = 1000 + openSheets.length * 10;
  const scrimEl = scrim ? el(`<div class="scrim" style="z-index: ${z - 1}"></div>`) : null;
  const section = el(`
    <section class="sheet ${size}" style="z-index: ${z}" role="dialog" aria-modal="${scrim}" aria-label="${esc(label)}">
      <button class="sheet-handle"></button>
      <div class="sheet-body"></div>
    </section>`);
  const handle = section.querySelector('.sheet-handle');

  const sheet = {
    body: section.querySelector('.sheet-body'),
    size,
    setSize(newSize) {
      section.classList.replace(sheet.size, newSize);
      sheet.size = newSize;
      handle.setAttribute('aria-label', newSize === 'full' ? 'Reducir' : 'Ampliar');
      onSizeChange(newSize);
    },
    // Quita la hoja sin avisar a onClose: para cuando quien la abrió ya sabe que se va
    remove() {
      if (!openSheets.includes(sheet)) return false;
      openSheets.splice(openSheets.indexOf(sheet), 1);
      scrimEl?.remove();
      section.remove();
      return true;
    },
    // La cierra el usuario (la X, el fondo, Escape, arrastrar): se quita y se avisa a onClose
    close() {
      if (sheet.remove()) onClose();
    },
  };
  handle.setAttribute('aria-label', size === 'full' ? 'Reducir' : 'Ampliar');

  let start = null;
  handle.addEventListener('pointerdown', event => { start = event.clientY; });
  handle.addEventListener('pointerup', event => {
    if (start === null) return;
    const moved = event.clientY - start;
    start = null;
    if (moved < -40 && resizable) sheet.setSize('full');
    else if (moved > 40) {
      if (sheet.size === 'full' && resizable) sheet.setSize('peek');
      else sheet.close();
    } else if (Math.abs(moved) < 5 && resizable) {
      sheet.setSize(sheet.size === 'full' ? 'peek' : 'full');
    }
  });
  scrimEl?.addEventListener('click', () => sheet.close());

  openSheets.push(sheet);
  if (scrimEl) parent.append(scrimEl);
  parent.append(section);
  return sheet;
}

// Las estrellas de una reseña, solo para leer
export function stars(value) {
  return `<span aria-label="${value} de 5 estrellas" style="color: var(--gold); letter-spacing: 1px">${'★'.repeat(value)}<span style="color: var(--line)">${'★'.repeat(5 - value)}</span></span>`;
}

const WORDS = ['', 'Malo', 'Regular', 'Bien', 'Muy bien', 'Excelente'];

// El selector de 1 a 5 estrellas al escribir una reseña. Devuelve el elemento; onChange recibe el número elegido
export function starsInput(onChange) {
  const box = el(`
    <div>
      <div class="stars-input" role="radiogroup" aria-label="Puntuación">
        ${[1, 2, 3, 4, 5].map(n => `<button type="button" role="radio" aria-checked="false" aria-label="${n}: ${WORDS[n]}" data-n="${n}">★</button>`).join('')}
      </div>
      <p class="muted" style="text-align: center; margin-top: -8px; margin-bottom: 16px; min-height: 22px">Toca una estrella</p>
    </div>`);
  box.querySelectorAll('button').forEach(button => button.addEventListener('click', () => {
    const value = Number(button.dataset.n);
    box.querySelectorAll('button').forEach(b => {
      b.classList.toggle('on', Number(b.dataset.n) <= value);
      b.setAttribute('aria-checked', String(Number(b.dataset.n) === value));
    });
    box.querySelector('p').textContent = WORDS[value];
    onChange(value);
  }));
  return box;
}
