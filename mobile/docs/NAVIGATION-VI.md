# Navigation Mobile

Root flow:

```text
Bootstrap -> Auth stack (chưa đăng nhập)
          -> Main/RoleBasedNavigator (đã xác thực)
```

Role chính:

| Role | Primary destinations |
|---|---|
| USER | Home, Questions, AI, Notifications, Profile |
| SUBJECT_ADMIN | Home, Review, Questions, Notifications, Profile |
| SYSTEM_ADMIN | Home, Approvals, Users, Notifications, Profile |

Secondary routes gồm question detail/editor, review detail, AI job/chat, exam matrix/detail/version và admin detail. Role guard chỉ là UX; backend vẫn quyết định 401/403 và faculty scope.
