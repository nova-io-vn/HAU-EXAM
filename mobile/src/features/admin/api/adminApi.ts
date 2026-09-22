import { api } from '@/src/services/api';

export type AdminRole = 'SYSTEM_ADMIN' | 'SUBJECT_ADMIN' | 'USER';
export type UserStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'REJECTED' | 'LOCKED';
export type AdminUser = { id: string; lecturerCode: string; fullName: string; dateOfBirth?: string | null; age?: number; phone?: string | null; email: string; address?: string | null; facultyId?: string | null; role: AdminRole; status: UserStatus; createdAt: string; updatedAt: string };
export type PageResult<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number };

export type UserListFilters = { keyword?: string; facultyId?: string; role?: AdminRole; status?: UserStatus };

export const adminApi = {
  listUsers: (page = 0, size = 20, filters: UserListFilters = {}) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    Object.entries(filters).forEach(([key, value]) => { if (value) params.set(key, value); });
    return api.get<PageResult<AdminUser>>(`/api/v1/users?${params.toString()}`);
  },
  getUser: (id: string) => api.get<AdminUser>(`/api/v1/users/${id}`),
  approve: (id: string) => api.post<AdminUser>(`/api/v1/users/${id}/approve`),
  reject: (id: string) => api.post<AdminUser>(`/api/v1/users/${id}/reject`),
  lock: (id: string) => api.post<AdminUser>(`/api/v1/users/${id}/lock`),
  unlock: (id: string) => api.post<AdminUser>(`/api/v1/users/${id}/unlock`),
};
