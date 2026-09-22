> **Vị trí đặt file:** `mobile/docs/NAVIGATION.md`

# Mobile Navigation

## Auth Stack
Login, Register, Pending Approval, Forgot Password, Verify OTP, Reset Password.

## USER Tabs
Home, Questions, AI, Notifications, Profile.

## SUBJECT_ADMIN Tabs
Home, Review, Questions, Notifications, Profile.

## SYSTEM_ADMIN
Chỉ expose các thao tác mobile có giá trị thực tế; quản trị bảng lớn vẫn Web-first.

Notification deep-link tới Question/AI Job/Exam phải kiểm tra auth/authorization và dữ liệu tồn tại trước khi mở.
