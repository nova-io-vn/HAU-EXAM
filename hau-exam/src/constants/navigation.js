import {routes} from './routes'
import {roles} from './roles'

const dashboard={label:'Dashboard',to:routes.dashboard,icon:'D'}
const notifications={label:'Thông báo',to:routes.notifications,icon:'N'}
export const navigationByRole={
  [roles.SYSTEM_ADMIN]:[dashboard,{label:'Quản lý người dùng',to:routes.users,icon:'U'},{label:'Duyệt đăng ký',to:routes.registrations,icon:'R'},{label:'Quản lý khoa',to:routes.faculties,icon:'F'},notifications],
  [roles.SUBJECT_ADMIN]:[dashboard,{label:'Ngân hàng câu hỏi',to:routes.questions,icon:'Q'},{label:'Hàng đợi xét duyệt',to:routes.review,icon:'V'},{label:'Ma trận đề',to:routes.matrices,icon:'M'},{label:'Bộ đề',to:routes.exams,icon:'E'},{label:'Không gian AI',to:routes.generate,icon:'A'},notifications],
  [roles.USER]:[dashboard,{label:'Câu hỏi của tôi',to:routes.myQuestions,icon:'Q',end:true},{label:'Tạo câu hỏi',to:routes.newQuestion,icon:'+'},{label:'Tạo sinh AI',to:routes.generate,icon:'A'},{label:'Tài liệu',to:routes.documents,icon:'D'},{label:'Chatbot',to:routes.chat,icon:'C'},notifications],
}
