import {routes} from './routes'
import {roles} from './roles'

const allRoles=Object.values(roles)
const feature=(label,keywords,route,allowedRoles=allRoles,description='')=>({label,keywords,route,roles:allowedRoles,description})

export const searchableFeatures=[
  feature('Tổng quan',['trang chủ','dashboard'],routes.dashboard),
  feature('Hồ sơ cá nhân',['hồ sơ','tài khoản','profile'],routes.profile),
  feature('Trợ lý HAU QM',['hỏi đáp','hướng dẫn','trợ lý','hau ai'],routes.systemHelp),
  feature('Thông báo',['notification','tin mới'],routes.notifications),
  feature('Tạo câu hỏi',['câu hỏi','question','tạo mới'],routes.newQuestion,[roles.USER]),
  feature('Câu hỏi của tôi',['câu hỏi','question','bản nháp'],routes.myQuestions,[roles.USER]),
  feature('Tạo câu hỏi bằng AI',['ai','tạo sinh','generation'],routes.generate,[roles.USER,roles.SUBJECT_ADMIN]),
  feature('Tài liệu AI',['tài liệu','document','học liệu'],routes.documents,[roles.USER]),
  feature('Môn học',['subject','học phần'],routes.subjects,[roles.SUBJECT_ADMIN]),
  feature('Duyệt câu hỏi',['review','xét duyệt'],routes.review,[roles.SUBJECT_ADMIN]),
  feature('Ma trận đề',['exam','matrix','ma trận'],routes.matrices,[roles.SUBJECT_ADMIN]),
  feature('Quản lý người dùng',['giảng viên','tài khoản','user'],routes.users,[roles.SYSTEM_ADMIN]),
  feature('Quản lý khoa',['faculty','khoa'],routes.faculties,[roles.SYSTEM_ADMIN]),
  feature('Cấu hình Email',['smtp','email','thư'],routes.settings,[roles.SYSTEM_ADMIN]),
  feature('Yêu cầu liên hệ',['hỗ trợ','contact','liên hệ'],routes.contactAdmin,[roles.SYSTEM_ADMIN]),
]

export function searchFeatures(query,role){
  const value=query.trim().toLocaleLowerCase('vi-VN')
  if(!value)return []
  return searchableFeatures.filter(item=>item.roles.includes(role)&&[item.label,...item.keywords].some(text=>text.toLocaleLowerCase('vi-VN').includes(value)))
}
