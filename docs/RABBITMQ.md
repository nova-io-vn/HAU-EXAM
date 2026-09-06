> **Vị trí đặt file:** `docs/RABBITMQ.md`

# RabbitMQ Convention

> Bảng event và trạng thái theo source hiện tại được chuẩn hóa tại [05-RABBITMQ.md](05-RABBITMQ.md).

## Mục đích

RabbitMQ dùng cho event và tác vụ bất đồng bộ. Không dùng thay REST cho
mọi request.

## Exchange đề xuất

-   `auth.exchange`
-   `user.exchange`
-   `question.exchange`
-   `exam.exchange`
-   `ai.exchange`
-   `notification.exchange`

Ưu tiên Topic Exchange.

## Event envelope

``` json
{
  "eventId": "uuid",
  "eventType": "QUESTION_APPROVED",
  "correlationId": "uuid",
  "occurredAt": "ISO-8601",
  "version": 1,
  "payload": {}
}
```

## Reliability

-   Durable queue cho business event quan trọng.
-   Persistent message khi phù hợp.
-   Manual ACK sau khi xử lý thành công.
-   Retry có delay/backoff.
-   Không retry vô hạn.
-   Sau giới hạn chuyển DLQ.
-   Consumer phải idempotent.
-   Không gửi JPA Entity.
-   Không gửi file PDF/ảnh lớn qua RabbitMQ; gửi ID/storage reference.

## Consistency

Với event gắn chặt DB transaction, ưu tiên Transactional Outbox khi
triển khai reliability hoàn chỉnh. Consumer quan trọng nên có
inbox/idempotency record hoặc cơ chế durable tương đương.
