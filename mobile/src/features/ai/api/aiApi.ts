import { api } from '@/src/services/api';

export type DocumentItem = { id: string; originalName: string; contentType: string; size: number; storageKey: string; checksum: string; createdAt: string };
export type JobStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
export type JobType = 'QUESTION_GENERATION' | 'ANALYSIS' | 'CHAT';
export type AiJob = { jobId: string; type: JobType; status: JobStatus; resultReference: string | null; errorCode: string | null; errorMessage: string | null; createdAt: string; startedAt: string | null; completedAt: string | null };
export type WorkspacePage<T> = { items: T[]; page: number; size: number; totalElements: number; totalPages: number };
export type GeneratedQuestion = { question: string; options: { label: string; content: string }[]; correctAnswer: string; explanation: string; difficulty?: string; topicId?: string | null };
export type ChatResult = { answer?: string; references?: string[] | { title?: string; source?: string; page?: number }[]; [key: string]: unknown };
export type Conversation = { id: string; title: string; createdAt: string; updatedAt: string };
export type ChatMessage = { id: string; role: 'USER'|'ASSISTANT'|'SYSTEM'; content: string; status: string; createdAt: string };
export type SystemHelpResult={answer:string;actions:{type:'NAVIGATE';label:string;routeKey:string}[]};

function page(path: string, pageNumber = 0) { return api.get<WorkspacePage<DocumentItem | AiJob>>(`${path}?page=${pageNumber}&size=20`); }

export const aiApi = {
  uploadDocument: (file: { uri: string; name: string; mimeType: string }) => { const form = new FormData(); form.append('file', { uri: file.uri, name: file.name, type: file.mimeType } as unknown as Blob); return api.post<DocumentItem>('/api/v1/documents', form); },
  documents: (pageNumber = 0) => page('/api/v1/documents', pageNumber) as Promise<WorkspacePage<DocumentItem>>,
  jobs: (pageNumber = 0) => page('/api/v1/ai/jobs', pageNumber) as Promise<WorkspacePage<AiJob>>,
  createGeneration: (input: { documentId: string; count: number; difficulty?: string; topicId?: string; subjectId?: string; chapterId?: string; language?: 'VI'|'EN'; includeImages?: boolean }) => api.post<AiJob>('/api/v1/ai/generate/questions', input),
  createChat: (input: { documentId?: string; message: string }) => api.post<AiJob>('/api/v1/chat', input),
  getJob: (id: string) => api.get<AiJob>(`/api/v1/ai/jobs/${id}`),
  getResult: <T>(id: string) => api.get<T>(`/api/v1/ai/jobs/${id}/result`),
  conversations: () => api.get<Conversation[]>('/api/v1/ai/chat/conversations'),
  createConversation: () => api.post<Conversation>('/api/v1/ai/chat/conversations', {}),
  messages: (id: string) => api.get<ChatMessage[]>(`/api/v1/ai/chat/conversations/${id}/messages`),
  sendMessage: (id: string, content: string) => api.post<ChatMessage>(`/api/v1/ai/chat/conversations/${id}/messages`, { content }),
  deleteConversation: (id: string) => api.delete(`/api/v1/ai/chat/conversations/${id}`),
  attachDocument: (id: string, documentId: string) => api.post<string[]>(`/api/v1/ai/chat/conversations/${id}/documents`, { documentId }),
  systemHelp:(message:string)=>api.post<SystemHelpResult>('/api/v1/ai/system-help',{message}),
};
