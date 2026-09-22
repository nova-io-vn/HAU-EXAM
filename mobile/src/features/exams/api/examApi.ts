import { api } from '@/src/services/api';

export type ExamMatrixRule = { id: string; chapterId: string; topicId: string | null; difficulty: 'EASY' | 'MEDIUM' | 'HARD'; questionCount: number };
export type ExamMatrix = { id: string; name: string; facultyId: string; subjectId: string; totalQuestions: number; rules: ExamMatrixRule[]; createdAt: string; updatedAt: string };
export type ExamQuestionReference = { id: string; questionId: string; position: number; matrixRuleId: string };
export type ExamVersion = { id: string; version: number; questions: ExamQuestionReference[]; createdAt: string };
export type GeneratedExam = { id: string; name: string; facultyId: string; subjectId: string; matrixId: string; templateId: string | null; versions: ExamVersion[]; createdAt: string; updatedAt: string };

export const examApi = {
  listMatrices: () => api.get<ExamMatrix[]>('/api/v1/exam-matrices'),
  getMatrix: (id: string) => api.get<ExamMatrix>(`/api/v1/exam-matrices/${id}`),
  listExams: () => api.get<GeneratedExam[]>('/api/v1/exams'),
  getExam: (id: string) => api.get<GeneratedExam>(`/api/v1/exams/${id}`),
};
