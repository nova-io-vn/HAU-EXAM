export const roleLabels: Record<string, string> = { SYSTEM_ADMIN: 'Quản trị viên hệ thống', SUBJECT_ADMIN: 'Quản trị viên chuyên môn', USER: 'Giảng viên' };
export const enumLabels: Record<string, string> = {
  EASY: 'Dễ', MEDIUM: 'Trung bình', HARD: 'Khó', MANUAL: 'Thủ công', AI: 'AI', IMPORT: 'Nhập từ dữ liệu',
  SINGLE_CHOICE: 'Một đáp án', MULTIPLE_CHOICE: 'Nhiều đáp án', TRUE_FALSE: 'Đúng / Sai', DRAFT: 'Bản nháp', PENDING_REVIEW: 'Chờ phê duyệt', APPROVED: 'Đã phê duyệt', NEED_REVISION: 'Cần chỉnh sửa', REJECTED: 'Đã từ chối', ARCHIVED: 'Đã lưu trữ',
  PENDING: 'Đang chờ', PROCESSING: 'Đang xử lý', COMPLETED: 'Hoàn tất', FAILED: 'Thất bại', RETRYING: 'Đang thử lại', CANCELLED: 'Đã hủy', ACTIVE: 'Đang hoạt động', PENDING_APPROVAL: 'Chờ phê duyệt', LOCKED: 'Đã khóa', INACTIVE: 'Ngừng hoạt động',
};
export function labelFor(value?: string | null) { return value ? enumLabels[value] || value : 'Chưa cập nhật'; }
export function roleLabelFor(value?: string | null) { return value ? roleLabels[value] || value : 'Chưa phân vai trò'; }
