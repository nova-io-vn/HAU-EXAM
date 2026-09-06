# Chức năng Web

| Nhóm | Có trong source | Trạng thái |
|---|---|---|
| Auth | Login, register pending, forgot/OTP/reset | ĐÃ CÓ |
| User | Profile, admin pending/user management | ĐÃ CÓ ở route/component |
| Question | List, detail, create/edit, submit, filter | ĐÃ CÓ |
| Review | Queue, detail, approve/reject/revision | ĐÃ CÓ |
| AI | Document, job, result, chatbot | PARTIAL; provider live chưa test |
| Exam | Matrix, template, generate, detail | ĐÃ CÓ ở UI/API |
| Notification | List, unread, read/read-all, realtime | ĐÃ CÓ |
| Online testing | Attempt/countdown/answer/grading/anti-cheat | NOT IMPLEMENTED |

AI E2E hiện dùng deterministic fixture; không đồng nghĩa provider thật đã ổn định.
