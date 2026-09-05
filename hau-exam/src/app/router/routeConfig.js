import {routes} from '../../constants/routes'
import {roles} from '../../constants/roles'

const allRoles=Object.values(roles)
export const protectedRoutes=[
  {path:routes.dashboard,title:'Dashboard',description:'Không gian làm việc học thuật của HAU-EXAM.',roles:allRoles},
  {path:routes.profile,title:'Hồ sơ cá nhân',description:'Xem và cập nhật thông tin hồ sơ của bạn.',roles:allRoles},
  {path:routes.questions,title:'Ngân hàng câu hỏi',description:'Tìm kiếm và quản lý câu hỏi theo phạm vi được cấp.',roles:[roles.SUBJECT_ADMIN,roles.USER]},
  {path:routes.myQuestions,title:'Câu hỏi của tôi',roles:[roles.USER]},
  {path:routes.newQuestion,title:'Tạo câu hỏi',description:'Soạn câu hỏi thủ công để gửi xét duyệt.',roles:[roles.USER]},
  {path:'/questions/:id',title:'Chi tiết câu hỏi',description:'Theo dõi nội dung và trạng thái xét duyệt.',roles:[roles.SUBJECT_ADMIN,roles.USER]},
  {path:'/questions/:id/edit',title:'Chỉnh sửa câu hỏi',description:'Chỉnh sửa nội dung khi trạng thái cho phép.',roles:[roles.USER]},
  {path:routes.review,title:'Hàng đợi xét duyệt',description:'Không gian xét duyệt theo phạm vi khoa.',roles:[roles.SUBJECT_ADMIN]},
  {path:'/review/:id',title:'Xét duyệt câu hỏi',roles:[roles.SUBJECT_ADMIN]},
  {path:routes.matrices,title:'Ma trận đề',description:'Thiết kế phân bố câu hỏi cho bộ đề.',roles:[roles.SUBJECT_ADMIN]},
  {path:'/exam-matrices/new',title:'Tạo ma trận',roles:[roles.SUBJECT_ADMIN]},
  {path:'/exam-matrices/:id/edit',title:'Chỉnh sửa ma trận',roles:[roles.SUBJECT_ADMIN]},
  {path:'/exam-matrices/:id',title:'Chi tiết ma trận',roles:[roles.SUBJECT_ADMIN]},
  {path:'/exams/generate',title:'Sinh bộ đề',roles:[roles.SUBJECT_ADMIN]},
  {path:'/exams/:id',title:'Phiên bản bộ đề',roles:[roles.SUBJECT_ADMIN]},
  {path:routes.exams,title:'Bộ đề',description:'Quản lý bộ đề và các phiên bản đã tạo.',roles:[roles.SUBJECT_ADMIN]},
  {path:routes.documents,title:'Tài liệu AI',description:'Upload và theo dõi tài liệu học liệu.',roles:[roles.USER]},
  {path:routes.generate,title:'Không gian AI',description:'Khởi tạo tác vụ AI bất đồng bộ.',roles:[roles.SUBJECT_ADMIN,roles.USER]},
  {path:'/ai/analysis',title:'AI Analysis',roles:[roles.SUBJECT_ADMIN,roles.USER]},
  {path:'/ai/jobs/:id',title:'AI Job',roles:[roles.SUBJECT_ADMIN,roles.USER]},
  {path:routes.aiJobs,title:'AI Jobs',description:'Theo dõi trạng thái xử lý AI.',roles:[roles.SUBJECT_ADMIN,roles.USER]},
  {path:routes.chat,title:'Trợ lý học liệu',description:'Khai thác nội dung trong ngữ cảnh tài liệu.',roles:[roles.USER]},
  {path:routes.notifications,title:'Thông báo',description:'Theo dõi thông báo và trạng thái đã đọc.',roles:allRoles},
  {path:routes.users,title:'Quản lý người dùng',description:'Quản lý hồ sơ và vai trò hệ thống.',roles:[roles.SYSTEM_ADMIN]},
  {path:routes.userDetail,title:'Chi tiết người dùng',description:'Xem hồ sơ và thực hiện tác vụ quản trị tài khoản.',roles:[roles.SYSTEM_ADMIN]},
  {path:routes.registrations,title:'Duyệt đăng ký',description:'Xử lý tài khoản đang chờ phê duyệt.',roles:[roles.SYSTEM_ADMIN]},
  {path:routes.faculties,title:'Quản lý khoa',description:'Quản lý phạm vi khoa của người dùng.',roles:[roles.SYSTEM_ADMIN]},
]

function matches(pattern,pathname){const patternParts=pattern.split('/').filter(Boolean);const pathParts=pathname.split('/').filter(Boolean);return patternParts.length===pathParts.length&&patternParts.every((part,index)=>part.startsWith(':')||part===pathParts[index])}
export function getRouteMeta(pathname){return protectedRoutes.find(route=>matches(route.path,pathname))}
