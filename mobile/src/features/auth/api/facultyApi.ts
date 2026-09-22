import { api } from '@/src/services/api';
export type PublicFaculty = { id: string; code: string; name: string; active?: boolean };
export const facultyApi = { list: () => api.get<{ content: PublicFaculty[] }>('/api/v1/public/faculties?active=true&page=0&size=100') };
