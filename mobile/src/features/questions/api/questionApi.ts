import { api } from '@/src/services/api';

export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TRUE_FALSE';
export type QuestionStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'NEED_REVISION' | 'REJECTED' | 'ARCHIVED';
export type QuestionSource = 'MANUAL' | 'AI';
export type CatalogItem = { id: string; name: string; code?: string; subjectId?: string; chapterId?: string; facultyId?: string };
export type QuestionOption = { id?: string; label: string; content: string; imageUrl: string | null; storageKey: string | null; correct: boolean; sortOrder: number };
export type QuestionReview = { id: string; reviewerId: string; action: string; comment: string | null; timestamp: string };
export type Question = { id: string; facultyId: string; subjectId: string; chapterId: string; topicId: string | null; content: string; imageUrl: string | null; storageKey: string | null; type: QuestionType; difficulty: Difficulty; status: QuestionStatus; source: QuestionSource; createdBy: string; createdAt: string; updatedAt: string; options: QuestionOption[]; reviewHistory: QuestionReview[] };
export type QuestionInput = { facultyId: string; subjectId: string; chapterId: string; topicId: string | null; content: string; imageUrl: string | null; storageKey: string | null; type: QuestionType; difficulty: Difficulty; options: QuestionOption[] };
export type PageResult<T> = { items: T[]; page: number; size: number; totalElements: number; totalPages: number };
export type QuestionFilters = { subjectId?: string; chapterId?: string; topicId?: string; difficulty?: Difficulty; status?: QuestionStatus; source?: QuestionSource; keyword?: string; page?: number; size?: number };
export type QuestionStatistics = { total: number; draft: number; pendingReview: number; approved: number; needRevision: number; rejected: number };
export type StoredImage = { url: string; secureUrl?: string | null; publicId: string; format?: string | null; width?: number | null; height?: number | null; bytes?: number | null };

function query(filters: QuestionFilters) {
  const params = new URLSearchParams();
  Object.entries({ ...filters, page: filters.page ?? 0, size: filters.size ?? 20 }).forEach(([key, value]) => { if (value) params.set(key, String(value)); });
  return params.toString();
}

export const questionApi = {
  search: (filters: QuestionFilters = {}) => api.get<PageResult<Question>>(`/api/v1/questions?${query(filters)}`),
  statistics: () => api.get<QuestionStatistics>('/api/v1/questions/statistics'),
  get: (id: string) => api.get<Question>(`/api/v1/questions/${id}`),
  create: (input: QuestionInput) => api.post<Question>('/api/v1/questions', input),
  update: (id: string, input: QuestionInput) => api.put<Question>(`/api/v1/questions/${id}`, input),
  uploadImage: (file: { uri: string; name: string; mimeType: string }, kind: 'question' | 'option' = 'question') => {
    const body = new FormData();
    body.append('file', { uri: file.uri, name: file.name, type: file.mimeType } as unknown as Blob);
    return api.post<StoredImage>(`/api/v1/questions/images?kind=${kind}`, body);
  },
  submit: (id: string) => api.post<Question>(`/api/v1/questions/${id}/submit`),
  approve: (id: string, comment?: string) => api.post<Question>(`/api/v1/questions/${id}/approve`, comment ? { reason: comment } : undefined),
  reject: (id: string, reason: string) => api.post<Question>(`/api/v1/questions/${id}/reject`, { reason }),
  requestRevision: (id: string, reason: string) => api.post<Question>(`/api/v1/questions/${id}/request-revision`, { reason }),
  subjects: (facultyId?: string) => api.get<CatalogItem[]>(`/api/v1/subjects${facultyId ? `?facultyId=${encodeURIComponent(facultyId)}` : ''}`),
  chapters: (subjectId: string) => api.get<CatalogItem[]>(`/api/v1/chapters?subjectId=${encodeURIComponent(subjectId)}`),
  topics: (chapterId: string) => api.get<CatalogItem[]>(`/api/v1/topics?chapterId=${encodeURIComponent(chapterId)}`),
};
