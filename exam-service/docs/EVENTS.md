> **Vị trí đặt file:** `backend/exam-service/docs/EVENTS.md`

# Exam Service --- Events

Event chỉ tạo khi có consumer thực: - `exam.generated` - `exam.updated`
nếu Notification/audit cần.

Không dùng RabbitMQ cho query danh sách câu hỏi đồng bộ. Khi cần chọn
câu theo điều kiện, dùng contract với Question Service hoặc cơ chế dữ
liệu được thiết kế rõ ràng.
