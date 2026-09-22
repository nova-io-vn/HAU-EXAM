import { API_BASE_URL, assertApiConfigured } from '@/src/config/env';
import { ApiError } from './errors';
import { secureStorage } from '@/src/services/storage/secureStorage';
import type { ApiResponse, Session } from '@/src/types/api';

const refreshKey = 'hau-exam.refresh-token';
const REQUEST_TIMEOUT_MS = 15000;
let refreshPromise: Promise<string> | null = null;
let accessToken: string | null = null;
let sessionExpiredHandler: (() => void) | null = null;

export function setAccessToken(token: string | null) { accessToken = token; }
export function getAccessToken() { return accessToken; }
export function registerSessionExpiredHandler(handler: (() => void) | null) { sessionExpiredHandler = handler; return () => { if (sessionExpiredHandler === handler) sessionExpiredHandler = null; }; }
export async function getRefreshToken() { return secureStorage.getItem(refreshKey); }
export async function saveRefreshToken(token: string) { await secureStorage.setItem(refreshKey, token); }
export async function clearTokens() { accessToken = null; await secureStorage.deleteItem(refreshKey); }

function errorKind(status?: number) {
  if (!status) return 'NETWORK' as const;
  if (status === 401) return 'UNAUTHORIZED' as const;
  if (status === 403) return 'FORBIDDEN' as const;
  if (status === 404) return 'NOT_FOUND' as const;
  if (status === 409) return 'CONFLICT' as const;
  if (status >= 400 && status < 500) return 'VALIDATION' as const;
  if (status >= 500) return 'SERVER' as const;
  return 'UNKNOWN' as const;
}

async function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      const token = await getRefreshToken();
      if (!token) throw new ApiError({ kind: 'UNAUTHORIZED', code: 'SESSION_EXPIRED', message: 'Phiên đăng nhập đã hết hạn.' });
      const response = await request<Session>('/api/v1/auth/refresh', { method: 'POST', body: { refreshToken: token }, skipRefresh: true });
      setAccessToken(response.accessToken);
      await saveRefreshToken(response.refreshToken);
      return response.accessToken;
    })().finally(() => { refreshPromise = null; });
  }
  return refreshPromise;
}

export type ApiRequestOptions = Omit<RequestInit, 'body'> & { body?: unknown; skipRefresh?: boolean };
export async function request<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  try { assertApiConfigured(); } catch (error) { throw new ApiError({ kind: 'CONFIGURATION', code: 'COMMON_CONFIGURATION', message: (error as Error).message }); }
  const { body, skipRefresh, headers, signal, ...fetchOptions } = options;
  let response: Response;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  const abortFromCaller = () => controller.abort();
  signal?.addEventListener('abort', abortFromCaller, { once: true });
  try {
    response = await fetch(`${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`, {
      ...fetchOptions,
      signal: controller.signal,
      credentials: 'include',
      headers: { Accept: 'application/json', ...(body !== undefined && !(typeof FormData !== 'undefined' && body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}), ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}), ...headers },
      body: body === undefined ? undefined : (typeof FormData !== 'undefined' && body instanceof FormData ? body as FormData : JSON.stringify(body)),
    });
  } catch {
    if (controller.signal.aborted && !signal?.aborted) throw new ApiError({ kind: 'TIMEOUT', code: 'COMMON_TIMEOUT', message: 'Kết nối máy chủ quá thời gian.' });
    throw new ApiError({ kind: 'NETWORK', code: 'COMMON_NETWORK', message: 'Không thể kết nối máy chủ.' });
  } finally { clearTimeout(timeout); signal?.removeEventListener('abort', abortFromCaller); }
  if (response.status === 401 && !skipRefresh) {
    try { await refreshAccessToken(); return request<T>(path, { ...options, skipRefresh: true }); }
    catch { await clearTokens(); sessionExpiredHandler?.(); throw new ApiError({ kind: 'UNAUTHORIZED', code: 'SESSION_EXPIRED', message: 'Phiên đăng nhập đã hết hạn.' }); }
  }
  let payload: ApiResponse<T> | null = null;
  try { payload = await response.json(); } catch { /* empty response */ }
  if (!response.ok || payload?.success === false) throw new ApiError({ kind: errorKind(response.status), code: payload?.code || `HTTP_${response.status}`, message: payload?.message || 'Yêu cầu không thành công.', status: response.status, correlationId: payload?.correlationId });
  return (payload?.data ?? payload) as T;
}

export const api = {
  get: <T>(path: string, options?: ApiRequestOptions) => request<T>(path, { ...options, method: 'GET' }),
  post: <T>(path: string, body?: unknown, options?: ApiRequestOptions) => request<T>(path, { ...options, method: 'POST', body }),
  put: <T>(path: string, body?: unknown, options?: ApiRequestOptions) => request<T>(path, { ...options, method: 'PUT', body }),
  patch: <T>(path: string, body?: unknown, options?: ApiRequestOptions) => request<T>(path, { ...options, method: 'PATCH', body }),
  delete: <T>(path: string, options?: ApiRequestOptions) => request<T>(path, { ...options, method: 'DELETE' }),
};
