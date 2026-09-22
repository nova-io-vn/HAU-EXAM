> **Vị trí đặt file:** `mobile/AGENTS.md`

# Mobile AGENTS.md — HAU Exam Bank

## Vai trò

Mobile là client React Native của cùng Backend Microservices. Mobile không có backend riêng và không gọi trực tiếp từng service nội bộ.

```text
React Native -> API Gateway -> Backend Services
```

## Công nghệ

- React Native
- TypeScript
- Giữ toolchain hiện có nếu project đã khởi tạo
- Không tự đổi Expo/React Native CLI
- HTTP qua API client chung
- Navigation thống nhất
- Không hard-code internal service ports

## Nghiệp vụ theo role

### USER
- Login/register/forgot password/OTP
- Profile
- My Questions
- Create/Edit/Submit Question
- Upload ảnh/tài liệu khi backend hỗ trợ
- AI Generation
- AI Job status
- Chatbot
- Notifications

### SUBJECT_ADMIN
- Review Queue
- Question Detail
- Approve/Reject/Request Revision trong đúng faculty
- Notifications
- Xem Exam/Matrix khi phù hợp mobile

### SYSTEM_ADMIN
Chỉ triển khai subset thực tế trên mobile như:
- Pending registrations
- Quick approve/reject
- User lookup/detail

Không ép toàn bộ admin desktop lên mobile.

## Role

Chỉ:
- SYSTEM_ADMIN
- SUBJECT_ADMIN
- USER

Không Permission system.

## Faculty Scope

SUBJECT_ADMIN chỉ thao tác dữ liệu đúng faculty. Client-side filtering không phải security.

## API

Mọi request:
`React Native -> API Gateway`

Không gọi trực tiếp auth-service/question-service/... bằng port nội bộ.

Base URL lấy từ environment/config.

Không tự invent field. Trước khi làm feature phải đọc `API.md` backend tương ứng.

## Authentication

- lecturerCode + password
- JWT access/refresh theo backend contract
- Không lưu password
- Không log token/OTP
- Token đi qua secure storage abstraction
- Logout phải clear local auth state và gọi backend logout/revoke nếu contract có

## Navigation

```text
AuthStack
├── Login
├── Register
├── PendingApproval
├── ForgotPassword
├── VerifyOtp
└── ResetPassword

MainTabs
├── Home
├── Questions
├── AI
├── Notifications
└── Profile
```

SUBJECT_ADMIN có thể có Review tab.

## Structure

```text
mobile/src/
├── app/
│   ├── navigation/
│   ├── providers/
│   └── bootstrap/
├── components/
│   ├── ui/
│   └── shared/
├── features/
│   ├── auth/
│   ├── profile/
│   ├── questions/
│   ├── review/
│   ├── ai/
│   ├── exams/
│   └── notifications/
├── services/
│   ├── api/
│   ├── websocket/
│   └── storage/
├── hooks/
├── stores/
├── types/
├── utils/
├── constants/
└── theme/
```

## Design

Kế thừa design language Web:
- Apple-inspired
- background #F5F5F7
- surface #FFFFFF
- text #1D1D1F
- accent #0071E3
- không gradient trang trí
- không heavy shadow
- không glassmorphism đại trà

Không bê nguyên table/layout desktop xuống mobile.

## Device Integration

Chỉ xin permission khi user thực sự dùng:
- Camera
- Photo Library
- Notification permission

Không tự xin location/microphone/contacts/bluetooth.

## Notifications

Phân biệt:
- In-app notification
- WebSocket realtime
- OS Push Notification

Không coi WebSocket là OS push.

Push chỉ triển khai khi backend có token-registration/provider contract rõ.

## Security

Không:
- hard-code secret
- log credential
- bypass TLS
- disable SSL verification
- tin role/facultyId editable local
- dùng UI hiding thay authorization

## Agent workflow

1. Đọc `/AGENTS.md`
2. Đọc `/mobile/AGENTS.md`
3. Đọc mobile docs liên quan
4. Đọc backend service AGENTS
5. Đọc backend API docs
6. Implement đúng contract
7. Test/typecheck/build
8. Báo file thay đổi
