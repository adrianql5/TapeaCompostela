import { login, register } from './api.js';
import { esc, openSheet } from './ui.js';

// Entrar o crear una cuenta, en la misma hoja. Se puede ver el mapa sin cuenta: esta hoja solo sale cuando hace
// falta (canjear, escribir una reseña o ver tus puntos). Los requisitos de cada campo se ven antes de enviar,
// los mismos que valida el backend (RegisterRequestDTO)
export function openAuth({ reason, onDone }) {
  let mode = 'login';
  const form = { username: '', name: '', email: '', password: '' };
  let touched = {};

  const sheet = openSheet({ label: 'Iniciar sesión', scrim: true });

  // Para entrar basta con que no estén vacíos; las longitudes mínimas solo se piden al crear la cuenta
  function problems() {
    return mode === 'login'
      ? {
          username: !form.username.trim() ? 'Escribe tu usuario' : null,
          password: !form.password ? 'Escribe tu contraseña' : null,
        }
      : {
          username: form.username.length < 3 ? 'Mínimo 3 caracteres' : null,
          name: !form.name.trim() ? 'Escribe tu nombre' : null,
          email: !/^\S+@\S+\.\S+$/.test(form.email) ? 'Un email como ana@correo.com' : null,
          password: form.password.length < 6 ? 'Mínimo 6 caracteres' : null,
        };
  }

  function field(name, label, attributes) {
    return `
      <div class="field">
        <label for="${name}">${label}</label>
        <input id="${name}" name="${name}" value="${esc(form[name])}" aria-describedby="${name}-hint" ${attributes} />
        <span id="${name}-hint" class="hint"></span>
      </div>`;
  }

  // Pinta la hoja entera. Solo hace falta al abrirla y al cambiar entre "Entrar" y "Crear cuenta": mientras se
  // escribe, showHints() cambia solo los textos de ayuda (si se volviera a pintar todo, el campo perdería el foco)
  function render() {
    sheet.body.innerHTML = `
      <h2>${mode === 'login' ? 'Hola de nuevo' : 'Crea tu cuenta'}</h2>
      ${reason ? `<p class="muted" style="margin-top: 6px">${esc(reason)}</p>` : ''}

      <div class="segmented" role="group" aria-label="Tengo cuenta o no">
        <button type="button" data-mode="login" aria-pressed="${mode === 'login'}">Entrar</button>
        <button type="button" data-mode="register" aria-pressed="${mode === 'register'}">Crear cuenta</button>
      </div>

      <form novalidate>
        ${field('username', 'Usuario', 'autocomplete="username" autocapitalize="none" maxlength="30"')}
        ${mode === 'register' ? field('name', 'Nombre', 'autocomplete="name" maxlength="100"') : ''}
        ${mode === 'register' ? field('email', 'Email', 'type="email" autocomplete="email" inputmode="email"') : ''}
        ${field('password', 'Contraseña', `type="password" autocomplete="${mode === 'login' ? 'current-password' : 'new-password'}"`)}
        <div class="alert error" hidden></div>
        <button class="btn btn-primary btn-block">${mode === 'login' ? 'Entrar' : 'Crear cuenta y entrar'}</button>
      </form>`;

    sheet.body.querySelectorAll('[data-mode]').forEach(button => button.addEventListener('click', () => {
      mode = button.dataset.mode;
      render();
    }));
    sheet.body.querySelectorAll('input').forEach(input => {
      input.addEventListener('input', () => { form[input.name] = input.value; showHints(); });
      input.addEventListener('blur', () => { touched[input.name] = true; showHints(); });
    });
    sheet.body.querySelector('form').addEventListener('submit', submit);
    showHints();
  }

  // Debajo de cada campo: el problema en rojo si ya lo has tocado y está mal, o (al crear cuenta) ✓ si está bien
  function showHints() {
    const found = problems();
    for (const name of Object.keys(found)) {
      const bad = touched[name] && found[name];
      const hint = sheet.body.querySelector(`#${name}-hint`);
      sheet.body.querySelector(`#${name}`).setAttribute('aria-invalid', String(Boolean(bad)));
      hint.hidden = !(mode === 'register' || bad);
      hint.className = `hint ${bad ? 'bad' : ''}`;
      hint.textContent = found[name] ?? '✓';
    }
  }

  async function submit(event) {
    event.preventDefault();
    const button = sheet.body.querySelector('form .btn');
    const alert = sheet.body.querySelector('.alert');
    if (!Object.values(problems()).every(p => !p)) {
      touched = { username: true, name: true, email: true, password: true };
      showHints();
      return;
    }
    button.disabled = true;
    button.textContent = 'Un momento…';
    alert.hidden = true;
    try {
      if (mode === 'login') await login(form.username, form.password);
      else await register(form);
      sheet.close();
      onDone();
    } catch (e) {
      alert.textContent = e.message;
      alert.hidden = false;
      button.disabled = false;
      button.textContent = mode === 'login' ? 'Entrar' : 'Crear cuenta y entrar';
    }
  }

  render();
}
