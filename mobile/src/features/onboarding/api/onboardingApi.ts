import { api } from '@/src/services/api';

export type OnboardingState = { role: 'SYSTEM_ADMIN' | 'SUBJECT_ADMIN' | 'USER'; versionCompleted: number; completedAt?: string | null };
export const CURRENT_ONBOARDING_VERSION = 1;
export const onboardingApi = {
  current: () => api.get<OnboardingState>('/api/v1/users/me/onboarding'),
  complete: (version: number) => api.post<OnboardingState>('/api/v1/users/me/onboarding/complete', { version }),
};
