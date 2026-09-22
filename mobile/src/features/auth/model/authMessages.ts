import { normalizeError } from '@/src/services/api';

const messages: Record<string, string> = {
  INVALID_CREDENTIALS: 'Mã giảng viên hoặc mật khẩu không đúng.',
  ACCOUNT_PENDING_APPROVAL: 'Tài khoản đang chờ Quản trị viên hệ thống phê duyệt.',
  ACCOUNT_REJECTED: 'Tài khoản đã bị từ chối.',
  ACCOUNT_LOCKED: 'Tài khoản đang bị khóa. Vui lòng liên hệ quản trị viên.',
  LECTURER_CODE_ALREADY_EXISTS: 'Mã giảng viên đã được đăng ký.',
  OTP_EXPIRED: 'Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.',
  INVALID_OTP: 'Mã OTP không đúng hoặc đã được sử dụng.',
  INVALID_RESET_AUTHORIZATION: 'Thông tin xác thực đặt lại mật khẩu không hợp lệ.',
  SESSION_EXPIRED: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
  COMMON_NETWORK: 'Không thể kết nối đến máy chủ.',
  COMMON_TIMEOUT: 'Kết nối máy chủ quá thời gian.',
  INTERNAL_ERROR: 'Máy chủ đang gặp sự cố. Vui lòng thử lại sau.',
};

export function authErrorMessage(error: unknown, fallback = 'Không thể hoàn tất yêu cầu. Vui lòng thử lại.') {
  const normalized = normalizeError(error);
  if (messages[normalized.code]) return messages[normalized.code];
  if (normalized.kind === 'NETWORK') return messages.COMMON_NETWORK;
  if (normalized.kind === 'TIMEOUT') return messages.COMMON_TIMEOUT;
  if (normalized.kind === 'FORBIDDEN') return normalized.message || 'Tài khoản chưa được kích hoạt hoặc không có quyền truy cập.';
  if (normalized.kind === 'SERVER') return messages.INTERNAL_ERROR;
  return fallback;
}
