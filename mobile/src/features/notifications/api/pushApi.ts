import { api, request } from '@/src/services/api';

export type PushPlatform = 'IOS' | 'ANDROID';
export const pushApi = {
  register: (input: { token: string; platform: PushPlatform; deviceIdentifier?: string }) => api.post<null>('/api/v1/notifications/devices', input),
  unregister: (input: { token: string; platform: PushPlatform; deviceIdentifier?: string }) => request<null>('/api/v1/notifications/devices', { method: 'DELETE', body: input }),
};
