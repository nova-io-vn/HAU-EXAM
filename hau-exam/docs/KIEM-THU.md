# Kiểm thử Web

`npm run lint` và `npm run build` kiểm tra static/build. Playwright `npm run test:e2e` kiểm tra UI thật với fixture tại Gateway boundary.

Critical flows hiện có: login, register pending, admin approval, forgot password screens, question workflow, notification read, deterministic AI job và exam generation. Suite đã PASS 8/8 trong hai lượt liên tiếp.

Đây là E2E mock/fixture, không phải live backend E2E. Live flow qua Gateway, RabbitMQ, database và AI provider là NOT TESTED khi Docker daemon không chạy.
