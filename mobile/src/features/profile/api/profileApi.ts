import { api } from '@/src/services/api';

export type UserRole = 'SYSTEM_ADMIN' | 'SUBJECT_ADMIN' | 'USER';
export type UserStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'REJECTED' | 'LOCKED';
export type UserProfile = { id: string; lecturerCode: string; fullName: string; dateOfBirth: string | null; phone: string | null; email: string; address: string | null; avatar: string | null; avatarUrl?: string | null; avatarPublicId?: string | null; academicRank?: 'NONE'|'PGS'|'GS'; academicDegree?: 'NONE'|'CN'|'KS'|'THS'|'TS'; facultyId: string | null; role: UserRole; status: UserStatus; createdAt?: string; updatedAt?: string };
export type UpdateProfileInput = Pick<UserProfile, 'fullName' | 'dateOfBirth' | 'phone' | 'email' | 'address' | 'avatar'>;

export const profileApi = {
  getMe: () => api.get<UserProfile>('/api/v1/users/me'),
  updateMe: (input: UpdateProfileInput) => api.put<UserProfile>('/api/v1/users/me', input),
  uploadAvatar: (file: { uri: string; name: string; mimeType: string }) => {
    const body = new FormData();
    body.append('file', { uri: file.uri, name: file.name, type: file.mimeType } as unknown as Blob);
    return api.put<UserProfile>('/api/v1/users/me/avatar', body);
  },
};
