const API_BASE_URL = (import.meta.env.VITE_LYONTAXIS_API_URL || 'http://localhost:8001/api/v1').replace(/\/$/, '')

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}/${path.replace(/^\//, '')}`, {
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
      ...(options.headers || {}),
    },
    ...options,
    body: options.body && typeof options.body !== 'string' ? JSON.stringify(options.body) : options.body,
  })

  const payload = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error(payload.message || `API request failed (${response.status})`)
  }

  return payload
}

export const lyonTaxisApi = {
  baseUrl: API_BASE_URL,
  health: () => request('/shared/health'),
  admin: {
    dashboard: (token) => request('/admin/dashboard/stats', { token }),
    drivers: (token, query = '') => request(`/admin/drivers${query ? `?${query}` : ''}`, { token }),
    trips: (token, query = '') => request(`/admin/trips${query ? `?${query}` : ''}`, { token }),
    users: (token, query = '') => request(`/admin/users${query ? `?${query}` : ''}`, { token }),
    updateTripStatus: (token, tripId, status) => request(`/admin/trips/${tripId}/status`, {
      method: 'PATCH', token, body: { status },
    }),
  },
  client: {
    trips: (token, query = '') => request(`/client/trips${query ? `?${query}` : ''}`, { token }),
    createTrip: (token, body) => request('/client/trips', { method: 'POST', token, body }),
    profile: (token) => request('/client/user/profile', { token }),
  },
  driver: {
    trips: (token, query = '') => request(`/driver/trips${query ? `?${query}` : ''}`, { token }),
    profile: (token) => request('/driver/user/profile', { token }),
  },
}
