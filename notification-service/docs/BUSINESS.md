> **Vị trí đặt file:** `backend/notification-service/docs/BUSINESS.md`

# Notification Service --- Business

## Delivery channels

-   IN_APP.
-   WEBSOCKET.
-   EMAIL.

## Use cases

-   Consume domain event.
-   Tạo notification persistent khi cần.
-   Push realtime tới đúng user.
-   Gửi email quan trọng.
-   List/unread/mark read.
-   Scheduled/system notification.
-   Scheduler tìm notification đến hạn và phát.

## Rules

-   WebSocket là delivery realtime, không phải durable store.
-   Sau reconnect, client lấy lại notification từ REST/DB.
-   User chỉ nhận destination của mình.
