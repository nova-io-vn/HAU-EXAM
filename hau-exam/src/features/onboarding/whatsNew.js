import whatsNewPlaceholder from '../../assets/404.gif'

export const CURRENT_WHATS_NEW = {
  version: '2026.09.27',
  enabled: true,
  guideId: 'main-onboarding',
  title: 'HAU Exam vừa được cập nhật',
  subtitle: 'Khám phá những thay đổi giúp công việc rõ ràng và liền mạch hơn.',
  // TODO: replace with src/assets/whats-new.gif.
  image: whatsNewPlaceholder,
  items: [
    ['Trợ lý Kute', 'Hướng dẫn sử dụng hệ thống theo từng bước.'],
    ['Tin nhắn', 'Trao đổi trực tiếp với giảng viên và quản trị viên.'],
    ['Ngân hàng câu hỏi', 'Quản lý và phê duyệt câu hỏi theo quy trình chuyên môn.'],
    ['Quản lý đề thi', 'Xây dựng ma trận và tạo đề từ câu hỏi đã được phê duyệt.'],
  ],
}

const key = (userId) => `hau-qm:whats-new:${userId || 'anonymous'}`

export function hasSeenWhatsNew(userId, version = CURRENT_WHATS_NEW.version) {
  try { return window.localStorage.getItem(key(userId)) === version } catch { return false }
}

export function markWhatsNewSeen(userId, version = CURRENT_WHATS_NEW.version) {
  try { window.localStorage.setItem(key(userId), version) } catch { /* storage may be unavailable */ }
}
