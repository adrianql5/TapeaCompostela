// Todas las llamadas a la API pasan por aquí. Las rutas empiezan por / (/businesses, /reviews...): la web la sirve el
// mismo Spring Boot que la API, así que van al mismo servidor y no hace falta CORS

const SESSION_KEY = 'tapea-session';

// La sesión son los dos tokens del login, guardados en localStorage para que sigan ahí al recargar la página
export function getSession() {
  try {
    const saved = localStorage.getItem(SESSION_KEY);
    return saved ? JSON.parse(saved) : null;
  } catch {
    return null;
  }
}

function saveSession(tokens) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(tokens));
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY);
}

export async function login(username, password) {
  saveSession(await request('/auth/login', { method: 'POST', body: { username, password }, auth: false }));
}

export async function register(data) {
  await request('/auth/register', { method: 'POST', body: { ...data, owner: true }, auth: false });
  await login(data.username, data.password);
}

export async function logout() {
  const session = getSession();
  clearSession();
  if (session) {
    // Invalida el refresh token en el servidor. Si falla da igual: en el navegador ya no hay sesión
    await request('/auth/logout', { method: 'POST', body: { refreshToken: session.refreshToken }, auth: false }).catch(() => {});
  }
}

// Pide tokens nuevos con el refresh token. Si llegan varias peticiones con 401 a la vez, todas esperan al mismo
// refresh: el servidor rota el refresh token y un segundo intento con el viejo fallaría
let refreshing = null;

function refreshTokens() {
  if (!refreshing) {
    refreshing = (async () => {
      const response = await fetch('/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: getSession().refreshToken }),
      });
      if (!response.ok) {
        clearSession();
        return false;
      }
      saveSession(await response.json());
      return true;
    })().finally(() => { refreshing = null; });
  }
  return refreshing;
}

// Los textos de la API están en inglés: los más habituales se traducen, el resto se enseñan tal cual
const TRANSLATIONS = [
  // Spring Security traduce este mensaje según el idioma del navegador
  [/^(Bad credentials|Credenciales erróneas)/, 'Usuario o contraseña incorrectos'],
  [/^You already reviewed business/, 'Ya has escrito una reseña de este local'],
  [/^The username .* is already in use/, 'Ese nombre de usuario ya existe. Prueba con otro'],
  [/^The email .* is already in use/, 'Ya hay una cuenta con ese email. Inicia sesión'],
  [/^The reward costs (\d+) points and you have (\d+)/, 'No tienes puntos suficientes'],
  [/^The refresh token has expired/, 'Tu sesión ha caducado. Vuelve a iniciar sesión'],
  [/^The QR is not valid or has expired/, 'El QR no es válido o ha caducado. Pide al cliente que abra otra vez su código'],
  [/^Redemption .* is USED/, 'Este canje ya se usó'],
  [/^Redemption .* is EXPIRED/, 'Este canje ha caducado: pasaron 30 días'],
  [/^Tag .* not found/, 'Una de las etiquetas ya no existe. Recarga la página'],
  [/^You cannot give points to yourself/, 'No puedes darte puntos a ti mismo: escanea el QR del cliente'],
];

const FIELDS = { username: 'Usuario', password: 'Contraseña', email: 'Email', name: 'Nombre', address: 'Dirección', phone: 'Teléfono', description: 'Descripción', title: 'Título', pointsCost: 'Puntos', visitPoints: 'Puntos por visita' };

function translate(text) {
  const found = TRANSLATIONS.find(([pattern]) => pattern.test(text));
  return found ? found[1] : text;
}

// Hace una petición y devuelve el JSON de la respuesta. Si la respuesta es un error, lanza un Error con el texto del
// ProblemDetail del backend ("detail" y, si hay, los "errors" de la validación)
export async function request(path, { method = 'GET', body, headers = {}, auth = true, contentType = 'application/json' } = {}) {
  const send = () => {
    const allHeaders = { ...headers };
    if (body !== undefined) allHeaders['Content-Type'] = contentType;
    const session = getSession();
    if (auth && session) allHeaders['Authorization'] = `Bearer ${session.accessToken}`;
    return fetch(path, { method, headers: allHeaders, body: body === undefined ? undefined : JSON.stringify(body) });
  };

  let response;
  try {
    response = await send();
    // El access token caduca a la hora: se renueva una vez y se repite la petición
    if (response.status === 401 && auth && getSession() && await refreshTokens()) {
      response = await send();
    }
  } catch {
    throw new Error('Sin conexión con el servidor. Comprueba tu red e inténtalo otra vez');
  }

  if (!response.ok) {
    const problem = await response.json().catch(() => null);
    if (problem?.errors) {
      throw new Error(Object.entries(problem.errors).map(([field, message]) => `${FIELDS[field] ?? field}: ${message}`).join('. '));
    }
    const messages = { 401: 'Inicia sesión para continuar', 403: 'No tienes permiso para hacer esto', 502: 'El servidor no responde', 504: 'El servidor no responde' };
    const error = new Error(problem?.detail ? translate(problem.detail) : messages[response.status] ?? `Error ${response.status}`);
    error.status = response.status;
    throw error;
  }
  return response.status === 204 ? null : response.json();
}
