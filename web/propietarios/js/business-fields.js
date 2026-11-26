import { request } from './api.js';
import { PRICE_LABEL } from './format.js';
import { locationPicker, SANTIAGO } from './location-picker.js';
import { el, esc } from './ui.js';

// Los campos de un local, repartidos en grupos. Los usan el asistente de alta (un grupo por paso) y la pantalla
// de editar (todos a la vez). Todos son obligatorios: así la ficha que ven los clientes siempre está completa.
// Cada grupo recibe el objeto business y lo cambia directamente mientras se escribe; ctx trae:
//   ctx.showProblems(): si ya hay que enseñar en rojo lo que falta
//   ctx.onChange(): avisa al asistente o al editor de que algo cambió (para activar sus botones)
// Cada grupo devuelve su elemento con un método refresh() que vuelve a escribir los textos de ayuda

export function emptyBusiness() {
  return { name: '', description: '', phone: '', address: '', latitude: null, longitude: null, priceRange: null, tags: [] };
}

// Qué le falta a cada grupo. Un texto por problema, o null si está bien. Las longitudes máximas son las del
// backend (BusinessDTO)
export function problemsOf(business) {
  return {
    name: business.name.trim().length < 2 ? 'Escribe el nombre del local' : null,
    description: business.description.trim().length < 20 ? `Cuéntalo en al menos 20 letras (llevas ${business.description.trim().length})` : null,
    phone: !/^\+?[0-9 ]{9,15}$/.test(business.phone.trim()) ? 'Un teléfono de 9 cifras, como 981 123 456' : null,
    address: business.address.trim().length < 5 ? 'Escribe la calle y el número' : null,
    location: business.latitude === null ? 'Mueve el mapa hasta que el pin quede sobre tu local' : null,
    priceRange: !business.priceRange ? 'Elige un precio' : null,
    tags: business.tags.length === 0 ? 'Elige al menos una etiqueta' : null,
  };
}

// Un campo con su etiqueta y su texto de ayuda (o el problema, en rojo)
function field(id, label, control, hint = '') {
  return `
    <div class="field">
      <label for="${id}">${label}</label>
      ${control}
      <span class="hint" data-hint="${id}" data-normal="${esc(hint)}"></span>
    </div>`;
}

// Pone el texto de ayuda de cada campo del grupo: el problema si hay que enseñarlo, o la ayuda normal si tiene
function showHints(box, business, ctx) {
  const problems = problemsOf(business);
  box.querySelectorAll('[data-hint]').forEach(hint => {
    const problem = problems[hint.dataset.hint];
    const bad = ctx.showProblems() && problem;
    hint.textContent = bad ? problem : hint.dataset.normal;
    hint.className = `hint ${bad ? 'bad' : ''}`;
    hint.hidden = !hint.textContent;
  });
}

// Conecta cada input con su campo de business
function bindInputs(box, business, ctx) {
  box.querySelectorAll('[data-field]').forEach(input => input.addEventListener('input', () => {
    business[input.dataset.field] = input.value;
    box.refresh();
    ctx.onChange();
  }));
}

export function basicsFields(business, ctx) {
  const box = el(`
    <div>
      ${field('name', 'Nombre del local',
        `<input id="name" data-field="name" value="${esc(business.name)}" maxlength="150" autocomplete="organization" placeholder="O Gato Negro" />`)}
      ${field('description', 'Descripción',
        `<textarea id="description" data-field="description" maxlength="2000" placeholder="Taberna de siempre en la Raíña: vino en cunca, empanada y pulpo los jueves.">${esc(business.description)}</textarea>`,
        'Lo que verán los clientes al abrir tu local: qué cocináis, qué os hace especiales.')}
      ${field('phone', 'Teléfono',
        `<input id="phone" data-field="phone" type="tel" inputmode="tel" value="${esc(business.phone)}" maxlength="20" autocomplete="tel" placeholder="981 123 456" />`)}
    </div>`);
  box.refresh = () => showHints(box, business, ctx);
  bindInputs(box, business, ctx);
  box.refresh();
  return box;
}

export function locationFields(business, ctx) {
  // Hasta que el mapa se mueve por primera vez no hay coordenadas: empieza en el centro de Compostela
  const start = business.latitude === null ? SANTIAGO : business;
  const box = el(`
    <div>
      <div data-slot="picker"></div>
      <p class="hint bad" data-hint="location" data-normal="" style="margin-top: -6px; margin-bottom: 14px"></p>
      ${field('address', 'Dirección',
        `<input id="address" data-field="address" value="${esc(business.address)}" autocomplete="street-address" placeholder="Rúa da Raíña, 12" />`,
        'Calle y número, como la verán los clientes.')}
    </div>`);
  box.querySelector('[data-slot="picker"]').replaceWith(locationPicker(start, position => {
    business.latitude = position.latitude;
    business.longitude = position.longitude;
    box.refresh();
    ctx.onChange();
  }));
  box.refresh = () => {
    showHints(box, business, ctx);
    // El aviso del mapa no es un campo: solo sale cuando hay que enseñar lo que falta
    box.querySelector('[data-hint="location"]').className = 'hint bad';
  };
  bindInputs(box, business, ctx);
  box.refresh();
  return box;
}

const PRICE_HINTS = ['menos de 15 €', '15-30 €', 'más de 30 €'];

export function styleFields(business, ctx) {
  const box = el(`
    <div>
      <h3 style="margin-bottom: 10px">¿Cuánto cuesta comer?</h3>
      <div class="options" role="radiogroup" aria-label="Precio">
        ${Object.entries(PRICE_LABEL).map(([value, label], index) => `
          <button type="button" class="option" role="radio" data-price="${value}">
            <span class="price">${'€'.repeat(index + 1)}</span>
            <span>${label} <span class="muted small">(${PRICE_HINTS[index]} por persona)</span></span>
          </button>`).join('')}
      </div>
      <p class="hint bad" data-hint="priceRange" data-normal="" style="margin-top: 8px"></p>

      <h3 style="margin: 24px 0 10px">Etiquetas</h3>
      <p class="muted small" style="margin-bottom: 12px">Los clientes buscan por ellas. Elige las que te describan.</p>
      <div class="skeleton" style="height: 44px"></div>
      <div class="tag-picker"></div>
      <p class="hint bad" data-hint="tags" data-normal="" style="margin-top: 8px"></p>
    </div>`);

  box.refresh = () => {
    box.querySelectorAll('[data-price]').forEach(option => {
      const selected = business.priceRange === option.dataset.price;
      option.setAttribute('aria-checked', String(selected));
      option.setAttribute('aria-pressed', String(selected));
    });
    box.querySelectorAll('[data-tag]').forEach(tag => tag.setAttribute('aria-pressed', String(business.tags.includes(tag.dataset.tag))));
    showHints(box, business, ctx);
  };

  box.querySelectorAll('[data-price]').forEach(option => option.addEventListener('click', () => {
    business.priceRange = option.dataset.price;
    box.refresh();
    ctx.onChange();
  }));

  // Las etiquetas que existen (GET /tags): se elige entre ellas
  request('/tags', { auth: false }).catch(() => []).then(tags => {
    box.querySelector('.skeleton').remove();
    const picker = box.querySelector('.tag-picker');
    picker.innerHTML = tags.map(tag => `<button type="button" data-tag="${esc(tag.name)}">${esc(tag.name)}</button>`).join('');
    picker.querySelectorAll('[data-tag]').forEach(button => button.addEventListener('click', () => {
      const name = button.dataset.tag;
      business.tags = business.tags.includes(name) ? business.tags.filter(t => t !== name) : [...business.tags, name];
      box.refresh();
      ctx.onChange();
    }));
    box.refresh();
  });

  box.refresh();
  return box;
}
