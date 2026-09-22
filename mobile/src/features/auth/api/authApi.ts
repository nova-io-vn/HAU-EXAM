import { api } from '@/src/services/api';
import type { Session } from '@/src/types/api';

export type Registration = { status: 'PENDING_APPROVAL' | string };
export type OtpVerification = { verified: boolean; resetToken: string | null };

export const authApi = {
  login: (input: { lecturerCode: string; password: string }) => api.post<Session>('/api/v1/auth/login', input),
  register: (input: { lecturerCode: string; password: string; fullName: string; email: string; facultyId?: string | null }) => api.post<Registration>('/api/v1/auth/register', input),
  refresh: (refreshToken: string) => api.post<Session>('/api/v1/auth/refresh', { refreshToken }),
  logout: (refreshToken: string) => api.post<null>('/api/v1/auth/logout', { refreshToken }),
  forgotPassword: (lecturerCode: string) => api.post<{ status: string }>('/api/v1/auth/forgot-password', { lecturerCode }),
  verifyOtp: (input: { lecturerCode: string; otp: string }) => api.post<OtpVerification>('/api/v1/auth/verify-otp', input),
  resetPassword: (input: { lecturerCode: string; otp: string; newPassword: string }) => api.post<null>('/api/v1/auth/reset-password', input),
};
