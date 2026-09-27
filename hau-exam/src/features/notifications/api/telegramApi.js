import { api } from '../../../services/api/client'

export const telegramApi = {
  status: () => api.get('/api/v1/telegram/status'),
  link: () => api.post('/api/v1/telegram/link'),
  test: () => api.post('/api/v1/telegram/test'),
  unlink: () => api.post('/api/v1/telegram/unlink'),
  preferences: () => api.get('/api/v1/telegram/preferences'),
  savePreferences: (value) => api.put('/api/v1/telegram/preferences', value),
  admin: () => api.get('/api/v1/admin/telegram'),
  saveAdmin: (value) => api.put('/api/v1/admin/telegram', value),
  testAdmin: () => api.post('/api/v1/admin/telegram/test'),
}
