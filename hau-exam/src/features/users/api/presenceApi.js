import { api } from '../../../services/api/client'

export const presenceApi = {
  heartbeat: () => api.post('/api/v1/presence/heartbeat', {}),
  onlineCount: () => api.get('/api/v1/presence/online-count'),
  onlineUsers: () => api.get('/api/v1/presence/online-users'),
}
