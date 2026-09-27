import { routes } from '../../constants/routes.js'
import { roles } from '../../constants/roles.js'

const allRoles = Object.values(roles)
const crumb = (label, to) => ({ label, to })
const route = (path, title, section, allowedRoles, sectionPath) => ({
  path,
  title,
  documentTitle: title,
  breadcrumb: section === title ? [crumb(title)] : [crumb(section, sectionPath), crumb(title)],
  roles: allowedRoles,
})

export const protectedRoutes = [
  route(routes.dashboard, 'Tổng quan', 'Tổng quan', allRoles),
  route(routes.profile, 'Hồ sơ cá nhân', 'Tài khoản', allRoles),
  route(routes.questions, 'Ngân hàng câu hỏi', 'Ngân hàng câu hỏi', [roles.SUBJECT_ADMIN, roles.USER]),
  route(routes.myQuestions, 'Câu hỏi của tôi', 'Ngân hàng câu hỏi', [roles.USER], routes.questions),
  route(routes.newQuestion, 'Tạo câu hỏi', 'Ngân hàng câu hỏi', [roles.USER], routes.myQuestions),
  route('/questions/:id', 'Chi tiết câu hỏi', 'Ngân hàng câu hỏi', [roles.SUBJECT_ADMIN, roles.USER], routes.questions),
  route('/questions/:id/edit', 'Chỉnh sửa câu hỏi', 'Ngân hàng câu hỏi', [roles.USER], routes.myQuestions),
  route(routes.review, 'Câu hỏi chờ duyệt', 'Ngân hàng câu hỏi', [roles.SUBJECT_ADMIN], routes.questions),
  route('/review/:id', 'Duyệt câu hỏi', 'Ngân hàng câu hỏi', [roles.SUBJECT_ADMIN], routes.review),
  route(routes.matrices, 'Ma trận đề', 'Đề thi', [roles.SUBJECT_ADMIN], routes.exams),
  route('/exam-matrices/new', 'Tạo ma trận', 'Đề thi', [roles.SUBJECT_ADMIN], routes.exams),
  route('/exam-matrices/:id/edit', 'Chỉnh sửa ma trận', 'Đề thi', [roles.SUBJECT_ADMIN], routes.exams),
  route('/exam-matrices/:id', 'Chi tiết ma trận', 'Đề thi', [roles.SUBJECT_ADMIN], routes.exams),
  route(routes.exams, 'Quản lý đề thi', 'Đề thi', [roles.SUBJECT_ADMIN]),
  route('/exams/generate', 'Tạo đề thi', 'Đề thi', [roles.SUBJECT_ADMIN], routes.exams),
  route('/exams/:id', 'Thông tin đề thi', 'Đề thi', [roles.SUBJECT_ADMIN], routes.exams),
  route(routes.documents, 'Tài liệu của tôi', 'Trợ lý AI', [roles.USER], routes.generate),
  route(routes.generate, 'Trợ lý AI', 'Trợ lý AI', [roles.SUBJECT_ADMIN, roles.USER]),
  route('/ai/analysis', 'Phân tích AI', 'Trợ lý AI', [roles.SUBJECT_ADMIN, roles.USER], routes.generate),
  route('/ai/jobs/:id', 'Chi tiết tác vụ AI', 'Trợ lý AI', [roles.SUBJECT_ADMIN, roles.USER], routes.aiJobs),
  route(routes.aiJobs, 'Tác vụ AI', 'Trợ lý AI', [roles.SUBJECT_ADMIN, roles.USER], routes.generate),
  route(routes.chat, 'Chatbot học liệu', 'Trợ lý AI', [roles.USER], routes.generate),
  route(routes.notifications, 'Thông báo', 'Thông báo', allRoles),
  route(routes.telegram, 'Thông báo Telegram', 'Thông báo', [roles.SYSTEM_ADMIN, roles.SUBJECT_ADMIN], routes.notifications),
  route(routes.adminAiJobs, 'Theo dõi tác vụ AI', 'Quản trị hệ thống', [roles.SYSTEM_ADMIN]),
  route(routes.systemHelp, 'Trợ lý HAU QM', 'Trợ giúp', allRoles),
  route(routes.contactAdmin, 'Yêu cầu liên hệ', 'Trợ giúp', [roles.SYSTEM_ADMIN]),
  route(routes.users, 'Quản lý giảng viên', 'Quản trị hệ thống', [roles.SYSTEM_ADMIN]),
  route(routes.userDetail, 'Chi tiết giảng viên', 'Quản trị hệ thống', [roles.SYSTEM_ADMIN], routes.users),
  route(routes.registrations, 'Tài khoản chờ duyệt', 'Quản trị hệ thống', [roles.SYSTEM_ADMIN]),
  route(routes.faculties, 'Quản lý khoa', 'Quản trị hệ thống', [roles.SYSTEM_ADMIN]),
  route(routes.facultyDetail, 'Chi tiết khoa', 'Quản trị hệ thống', [roles.SYSTEM_ADMIN], routes.faculties),
  route(routes.settings, 'Cấu hình hệ thống', 'Quản trị hệ thống', [roles.SYSTEM_ADMIN]),
  route(routes.subjects, 'Môn học', 'Nội dung học thuật', [roles.SUBJECT_ADMIN]),
  route(routes.knowledge, 'Cấu trúc kiến thức', 'Nội dung học thuật', [roles.SUBJECT_ADMIN]),
  route(routes.coverage, 'Độ bao phủ kiến thức', 'Nội dung học thuật', [roles.SUBJECT_ADMIN]),
  route(routes.help, 'Trung tâm trợ giúp', 'Trợ giúp', allRoles),
]

function matches(pattern, pathname) {
  const patternParts = pattern.split('/').filter(Boolean)
  const pathParts = pathname.split('/').filter(Boolean)
  return patternParts.length === pathParts.length && patternParts.every((part, index) => part.startsWith(':') || part === pathParts[index])
}

export function getRouteMeta(pathname) {
  return protectedRoutes.find(item => matches(item.path, pathname))
}
