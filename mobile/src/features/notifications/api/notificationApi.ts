import { api } from '@/src/services/api';

export type NotificationItem = { id: string; type: string; title: string; content: string; referenceId: string | null; referenceType: string | null; read: boolean; readAt: string | null; createdAt: string };
export type UnreadCount = { count: number };

export const notificationApi = {
  list: (page = 0) => api.get<NotificationItem[]>(`/api/v1/notifications?page=${page}&size=20`),
  unreadCount: () => api.get<UnreadCount>('/api/v1/notifications/unread-count'),
  markRead: (id: string) => api.post<NotificationItem>(`/api/v1/notifications/${id}/read`),
  markAllRead: () => api.post<number>('/api/v1/notifications/read-all'),
};
