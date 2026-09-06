# 4. Cấu trúc dự án

```text
/
├── pom.xml
├── eureka/                 # Eureka Server
├── gateway/                # API Gateway
├── auth-service/ user-service/ question-service/
├── exam-service/ ai-service/ notification-service/
├── hau-exam/               # React Web
├── mobile/                 # React Native/Expo
├── docker-compose.yml
└── docs/
```

| Module | Java package root |
|---|---|
| Auth | `com.authservice` |
| User | `com.userservice` |
| Question | `com.questionservice` |
| Exam | `com.examservice` |
| AI | `com.aiservice` |
| Notification | `com.notificationservice` |
| Gateway | `com.gateway` |
| Eureka | `com.eureka` |

Root Maven là aggregator `packaging=pom` và có 8 module theo filesystem.
