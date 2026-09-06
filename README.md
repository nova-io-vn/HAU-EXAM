> **Vị trí đặt file:** `README.md`

# HAU Exam Agent Documentation Pack

Bộ tài liệu này được đặt vào repository theo đúng cấu trúc thư mục hiện
tại.

Thứ tự Agent nên đọc:

1.  `/AGENTS.md`
2.  `/backend/AGENTS.md` hoặc `/frontend/AGENTS.md`
3.  `AGENTS.md` gần service/feature đang sửa nhất
4.  `docs/BUSINESS.md`
5.  `docs/API.md`
6.  `docs/DATABASE.md`
7.  `docs/EVENTS.md`

`AGENTS.md` là luật làm việc. `docs/*.md` là specification/nghiệp vụ.

Không tự ý thay đổi kiến trúc 8 service đã khóa.

## Chạy Backend bằng Docker Compose

Compose dựng PostgreSQL, Redis, RabbitMQ, Eureka, API Gateway và sáu business service. Web và Mobile phải gọi Backend qua API Gateway tại `http://localhost:8080`; các cổng service nội bộ không được dùng làm endpoint client.

1. Sao chép `.env.example` thành `.env` và thay toàn bộ giá trị `replace-with-...` bằng secret dành riêng cho môi trường development. Không commit `.env`.
2. Build và khởi động hệ thống:

   ```powershell
   docker compose up -d --build
   ```

3. Kiểm tra trạng thái và log:

   ```powershell
   docker compose ps
   docker compose logs -f
   ```

4. Dừng hệ thống nhưng giữ dữ liệu:

   ```powershell
   docker compose down
   ```

Để reset hoàn toàn volume development, chạy lệnh sau. **Cảnh báo: thao tác này xóa toàn bộ database, RabbitMQ data và tài liệu AI đang lưu trong volume, không thể khôi phục từ Docker volume sau khi xóa.**

```powershell
docker compose down --volumes
```
