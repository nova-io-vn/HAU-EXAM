export type ApiErrorKind = 'NETWORK' | 'TIMEOUT' | 'CONFIGURATION' | 'UNAUTHORIZED' | 'FORBIDDEN' | 'NOT_FOUND' | 'VALIDATION' | 'CONFLICT' | 'SERVER' | 'UNKNOWN';

export class ApiError extends Error {
  readonly kind: ApiErrorKind;
  readonly code: string;
  readonly status?: number;
  readonly correlationId?: string;

  constructor(input: { kind: ApiErrorKind; code: string; message: string; status?: number; correlationId?: string }) {
    super(input.message);
    this.name = 'ApiError';
    this.kind = input.kind;
    this.code = input.code;
    this.status = input.status;
    this.correlationId = input.correlationId;
  }
}

export function normalizeError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;
  return new ApiError({ kind: 'UNKNOWN', code: 'COMMON_UNKNOWN', message: 'Đã có lỗi xảy ra. Vui lòng thử lại.' });
}
