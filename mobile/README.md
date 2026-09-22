> **Vị trí đặt file:** `mobile/README.md`

# HAU Exam Bank Mobile Docs

Mobile uses Expo Router with a TypeScript foundation. All HTTP requests target the API Gateway through `EXPO_PUBLIC_API_BASE_URL`; internal service ports must not be configured here.

Setup:

```bash
npm install
npx expo start
npm run typecheck
npm run lint
npm run build
```

Copy `.env.example` to `.env.local` for a local Gateway URL. Tokens are handled through the secure-storage abstraction (`expo-secure-store` on native; session storage on web).

Thứ tự đọc:
1. `/AGENTS.md`
2. `/mobile/AGENTS.md`
3. `/mobile/docs/ARCHITECTURE.md`
4. `/mobile/docs/DESIGN_SYSTEM.md`
5. `/mobile/docs/NAVIGATION.md`
6. `/mobile/docs/API-INTEGRATION.md`
7. `/mobile/docs/DEVICE-INTEGRATION.md`
8. `/mobile/docs/FEATURES.md`
9. `/mobile/docs/SECURITY.md`
10. Backend docs của feature đang làm
