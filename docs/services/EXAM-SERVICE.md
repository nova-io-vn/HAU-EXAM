# Exam Service

## 1. Mục đích

Quản lý exam matrix, template, generated exam, version và allocation question. Không phải hệ thống thi online.

## 2. Domain/database

Package `com.examservice`, database `exam_db`, migration V1/V2. Matrix chứa name, faculty, subject, total questions và distribution rules; exam liên kết matrix/template và version.

## 3. API chính

`POST/PUT/GET /api/v1/exam-matrices`, `POST /api/v1/exam-matrices/{id}/validate`, `POST/GET /api/v1/exam-templates`, `POST /api/v1/exams/generate`, `GET /api/v1/exams`, `GET /api/v1/exams/{id}`, `POST /api/v1/exams/{id}/versions`.

## 4. Integration/security

Resource server dùng JWKS. Service lấy question qua API contract/config `QUESTION_SERVICE_URL`; không truy cập Question DB. Event producer/consumer không có workflow AI riêng được ghi nhận ngoài controller/config hiện tại.

## 5. Chạy/test

Port `8084`; Question URL localhost khi dev, Docker service name. Root `mvn clean verify` PASS. Export/download và live generation cần kiểm thử infrastructure riêng nếu deployment bật.
