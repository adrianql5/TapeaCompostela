import { login, register } from './api.js';
import { esc } from './ui.js';

// La primera pantalla: entrar o crear una cuenta de local (owner: true en el registro). Los requisitos de cada
// campo se ven antes de enviar, los mismos que valida el backend (RegisterRequestDTO)
export function renderAuth(root, onDone) {
  let mode = 'login';
  const form = { username: '', name: '', email: '', password: '' };
  let touched = {};

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
          email: !/^\S+@\S+\.\S+$/.test(form.email) ? 'Un email como bar@correo.com' : null,
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

  // Pinta la pantalla entera al empezar y al cambiar entre "Entrar" y "Crear cuenta". Mientras se escribe solo
  // cambian los textos de ayuda (showHints), para que el campo no pierda el foco
  function render() {
    root.innerHTML = `
      <main class="page" style="padding-top: calc(var(--safe-top) + 48px)">
        <div class="biz-mark" style="margin-bottom: 20px" aria-hidden="true">TC</div>
        <h1 style="font-size: 34px">Tu bar en TapeaCompostela</h1>
        <p class="muted" style="margin-top: 10px">Aparece en el mapa, ofrece recompensas y valida los canjes con el móvil.</p>

        <div class="segmented" role="group" aria-label="Tengo cuenta o no">
          <button type="button" data-mode="login" aria-pressed="${mode === 'login'}">Entrar</button>
          <button type="button" data-mode="register" aria-pressed="${mode === 'register'}">Crear cuenta</button>
        </div>

        <form novalidate>
          ${field('username', 'Usuario', 'autocomplete="username" autocapitalize="none" maxlength="30"')}
          ${mode === 'register' ? field('name', 'Tu nombre', 'autocomplete="name" maxlength="100"') : ''}
          ${mode === 'register' ? field('email', 'Email', 'type="email" autocomplete="email" inputmode="email"') : ''}
          ${field('password', 'Contraseña', `type="password" autocomplete="${mode === 'login' ? 'current-password' : 'new-password'}"`)}
          <div class="alert error" hidden></div>
          <button class="btn btn-primary btn-block">${mode === 'login' ? 'Entrar' : 'Crear cuenta de local'}</button>
        </form>
      </main>`;

    root.querySelectorAll('[data-mode]').forEach(button => button.addEventListener('click', () => {
      mode = button.dataset.mode;
      render();
    }));
    root.querySelectorAll('input').forEach(input => {
      input.addEventListener('input', () => { form[input.name] = input.value; showHints(); });
      input.addEventListener('blur', () => { touched[input.name] = true; showHints(); });
    });
    root.querySelector('form').addEventListener('submit', submit);
    showHints();
  }

  // Debajo de cada campo: el problema en rojo si ya lo has tocado y está mal, o (al crear cuenta) ✓ si está bien
  function showHints() {
    const found = problems();
    for (const name of Object.keys(found)) {
      const bad = touched[name] && found[name];
      const hint = root.querySelector(`#${name}-hint`);
      root.querySelector(`#${name}`).setAttribute('aria-invalid', String(Boolean(bad)));
      hint.hidden = !(mode === 'register' || bad);
      hint.className = `hint ${bad ? 'bad' : ''}`;
      hint.textContent = found[name] ?? '✓';
    }
  }

  async function submit(event) {
    event.preventDefault();
    const button = root.querySelector('form .btn');
    const alert = root.querySelector('.alert');
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
      onDone();
    } catch (e) {
      alert.textContent = e.message;
      alert.hidden = false;
      button.disabled = false;
      button.textContent = mode === 'login' ? 'Entrar' : 'Crear cuenta de local';
    }
  }

  render();
}
