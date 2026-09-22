# Kiến trúc Mobile

```text
Expo Router app/
  -> app/_layout.tsx + RootNavigator
  -> bootstrap/providers
  -> RoleBasedNavigator
  -> feature screens
  -> services/api + secure storage + WebSocket
```

Code nền nằm ở `src/app`, `src/components`, `src/features`, `src/services`, `src/stores`, `src/theme`. Business screen vẫn nằm trong Expo Router `app/` và dùng các feature screen tương ứng.

Environment tập trung tại `src/config/env.ts`; HTTP client tại `src/services/api/client.ts`; auth state tại `src/stores/authStore.ts`; secure token adapter tại `src/services/storage/secureStorage.ts`.
