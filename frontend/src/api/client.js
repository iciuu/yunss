const API_BASE = '/api/v1'

export function getSession() {
  return JSON.parse(localStorage.getItem('yss-session') || 'null')
}

export function setSession(session) {
  localStorage.setItem('yss-session', JSON.stringify(session))
}

export function clearSession() {
  localStorage.removeItem('yss-session')
}

export async function api(path, options = {}) {
  const session = getSession()
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(session?.token ? { Authorization: `Bearer ${session.token}` } : {}),
      ...(options.headers || {})
    }
  })
  const body = await response.json()
  if (body.code !== 0) throw new Error(body.message || '请求失败')
  return body.data
}

export function post(path, payload) {
  return api(path, { method: 'POST', body: JSON.stringify(payload || {}) })
}

export function put(path, payload) {
  return api(path, { method: 'PUT', body: JSON.stringify(payload || {}) })
}
