> **Vị trí đặt file:** `frontend/docs/ROUTES.md`

# Frontend Routes

Route thực tế có thể điều chỉnh khi UX được chốt, nhưng phải phản ánh
role.

Ví dụ:

``` text
/login

/dashboard

/questions
/questions/new
/questions/:id
/questions/:id/edit
/review

/exam-matrices
/exams

/ai/documents
/ai/generate
/ai/jobs
/chat

/notifications

/admin/users
/admin/registrations
/admin/faculties
```

Route guard chỉ hỗ trợ UX. Backend vẫn enforce role/faculty.
