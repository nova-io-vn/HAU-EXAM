export type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T | null;
  correlationId?: string;
};

export type Session = {
  accessToken: string;
  refreshToken: string;
  userId?: string;
  lecturerCode?: string;
  fullName?: string;
  facultyName?: string;
  role?: 'SYSTEM_ADMIN' | 'SUBJECT_ADMIN' | 'USER';
  facultyId?: string;
  avatar?: string | null;
  avatarUrl?: string | null;
  academicRank?: 'NONE'|'PGS'|'GS';
  academicDegree?: 'NONE'|'CN'|'KS'|'THS'|'TS';
};
