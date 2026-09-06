# 11. Kiểm thử

| Loại | Ý nghĩa | Trạng thái |
|---|---|---|
| Unit/Application | Domain, use case, adapter bằng JUnit/Mockito | Có ở nhiều service |
| Integration | Spring context, JPA/Flyway H2, security/JWKS, event contract | `mvn clean verify` PASS |
| E2E fixture | Playwright đi qua UI nhưng mock Gateway boundary | 8/8 PASS hai lần |
| Live E2E | Gateway/RabbitMQ/DB/AI thật | NOT TESTED trong lượt cuối vì Docker daemon unavailable |
| Build test | Maven, Web lint/build, Mobile typecheck | PASS |

Lệnh backend: `mvn clean verify`. Web: `npm run lint`, `npm run build`, `npm run test:e2e`. Mobile hiện có `npm run typecheck`; native Android/iOS phụ thuộc môi trường Expo/SDK.
