import { request } from './api.js';
import { basicsFields, emptyBusiness, locationFields, problemsOf, styleFields } from './business-fields.js';
import { PRICE_LABEL } from './format.js';
import { el, esc, icon } from './ui.js';

// Dar de alta un local en 4 pasos cortos, en vez de un formulario largo: en el móvil se rellena mejor así.
// No se pasa de un paso hasta que está completo, y el último deja revisarlo todo antes de publicar
const STEPS = [
  { title: '¿Cómo se llama tu local?', fields: ['name', 'description', 'phone'] },
  { title: '¿Dónde está?', fields: ['location', 'address'] },
  { title: '¿Cómo es?', fields: ['priceRange', 'tags'] },
  { title: 'Revisa y publica', fields: [] },
];

export function openNewBusiness({ onCreated }) {
  let step = 0;
  const business = emptyBusiness();
  let showProblems = false;
  let group = null;   // el grupo de campos del paso actual

  const wizard = el(`
    <div class="wizard" role="dialog" aria-label="Dar de alta un local">
      <div class="wizard-top">
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span class="muted small" data-step></span>
          <button class="icon-btn plain" aria-label="Cancelar el alta">${icon('close', 20)}</button>
        </div>
        <div class="progress" aria-hidden="true"><div></div></div>
        <h1 style="margin-top: 12px"></h1>
      </div>
      <div class="wizard-body"></div>
      <div class="wizard-foot"></div>
    </div>`);
  const body = wizard.querySelector('.wizard-body');
  const foot = wizard.querySelector('.wizard-foot');
  document.body.append(wizard);
  wizard.querySelector('.wizard-top .icon-btn').addEventListener('click', () => wizard.remove());

  const stepOk = () => STEPS[step].fields.every(field => !problemsOf(business)[field]);
  const ctx = { showProblems: () => showProblems, onChange: () => updateNext() };

  function render() {
    wizard.querySelector('[data-step]').textContent = `Paso ${step + 1} de ${STEPS.length}`;
    wizard.querySelector('.progress div').style.width = `${((step + 1) / STEPS.length) * 100}%`;
    wizard.querySelector('h1').textContent = STEPS[step].title;

    body.replaceChildren();
    group = null;
    if (step === 0) group = basicsFields(business, ctx);
    if (step === 1) group = locationFields(business, ctx);
    if (step === 2) group = styleFields(business, ctx);
    if (group) body.append(group);
    if (step === 3) {
      body.innerHTML = `
        <p class="muted" style="margin-bottom: 16px">Así lo verán los clientes. Puedes cambiarlo cuando quieras.</p>
        <div class="review-list">
          <div><span>Nombre</span><span>${esc(business.name)}</span></div>
          <div><span>Teléfono</span><span>${esc(business.phone)}</span></div>
          <div><span>Dirección</span><span>${esc(business.address)}</span></div>
          <div><span>Precio</span><span>${PRICE_LABEL[business.priceRange]}</span></div>
          <div><span>Etiquetas</span><span>${esc(business.tags.join(', '))}</span></div>
        </div>
        <p style="margin-top: 16px">${esc(business.description)}</p>
        <div class="alert error" style="margin-top: 16px" hidden></div>`;
    }

    foot.innerHTML = `
      ${step > 0 ? '<button class="btn btn-ghost" data-action="back">Atrás</button>' : ''}
      ${step < STEPS.length - 1
        ? '<button class="btn btn-dark" data-action="next">Siguiente</button>'
        : '<button class="btn btn-primary" data-action="publish">Publicar local</button>'}`;
    foot.querySelector('[data-action="back"]')?.addEventListener('click', () => { showProblems = false; step--; render(); });
    foot.querySelector('[data-action="next"]')?.addEventListener('click', next);
    foot.querySelector('[data-action="publish"]')?.addEventListener('click', publish);
    updateNext();
  }

  // "Siguiente" se ve apagado mientras falta algo, pero se puede tocar: así enseña qué falta
  function updateNext() {
    foot.querySelector('[data-action="next"]')?.setAttribute('aria-disabled', String(!stepOk()));
  }

  function next() {
    if (!stepOk()) {
      showProblems = true;
      group?.refresh();
      return;
    }
    showProblems = false;
    step++;
    render();
  }

  async function publish() {
    const button = foot.querySelector('[data-action="publish"]');
    const alert = body.querySelector('.alert');
    button.disabled = true;
    button.textContent = 'Publicando…';
    alert.hidden = true;
    try {
      const created = await request('/businesses', {
        method: 'POST',
        body: { ...business, name: business.name.trim(), description: business.description.trim(), phone: business.phone.trim(), address: business.address.trim() },
      });
      wizard.remove();
      onCreated(created);
    } catch (e) {
      alert.textContent = e.message;
      alert.hidden = false;
      button.disabled = false;
      button.textContent = 'Publicar local';
    }
  }

  render();
}
