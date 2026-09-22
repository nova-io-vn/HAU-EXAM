> **Vị trí đặt file:** `mobile/docs/DEVICE-INTEGRATION.md`

# Device Integration

## Camera/Photo Library
Use case:
- avatar
- Question image
- QuestionOption image
- AI document/image nếu API hỗ trợ

Handle:
- denied
- permanently denied
- cancel
- unsupported type
- too large
- upload failure

## File Picker
Chỉ nhận loại file đúng backend contract.

## Notifications
Phân biệt in-app, WebSocket và OS push.

## Lifecycle
Khi app foreground lại, refresh notification unread count và stale data phù hợp. Không tự chạy background task nặng.
