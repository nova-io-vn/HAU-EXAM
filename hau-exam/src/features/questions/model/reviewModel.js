export function canReview(question, auth) {
  return auth.role === 'SUBJECT_ADMIN' && Boolean(auth.facultyId) &&
    question?.facultyId === auth.facultyId && question?.status === 'PENDING_REVIEW'
}

export const reviewActions = {
  approve: {label: 'Phê duyệt', title: 'Phê duyệt câu hỏi?', description: 'Câu hỏi sẽ được đưa vào ngân hàng đã duyệt để sử dụng tạo bộ đề.'},
  reject: {label: 'Từ chối', title: 'Từ chối câu hỏi?', description: 'Nêu rõ lý do để tác giả hiểu quyết định xét duyệt.', required: true},
  requestRevision: {label: 'Yêu cầu chỉnh sửa', title: 'Yêu cầu chỉnh sửa', description: 'Chỉ rõ nội dung tác giả cần sửa trước khi gửi duyệt lại.', required: true},
}
