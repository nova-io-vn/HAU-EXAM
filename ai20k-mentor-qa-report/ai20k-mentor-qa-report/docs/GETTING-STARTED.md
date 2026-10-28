# Getting Started · ai20k-mentor-qa-report

## 30 giây đầu tiên

1. **Cài** skill (xem `INSTALL.md`)
2. **Gọi**: `/ai20k-mentor-qa-report`
3. **Cung cấp**: URL production của sản phẩm phần mềm cần QA
4. Skill hỏi chế độ (headless / screenshot) → chọn `2` (screenshot) để có bằng chứng ảnh
5. Skill vào link, click toàn luồng, xuất **QA Report 2 trục**

## Skill này làm gì

Vào **link production thật** của một sản phẩm phần mềm (web app / chatbot / SaaS / platform đa role), mô phỏng người dùng thật, rồi xuất **báo cáo QA chuyên nghiệp 2 trục**:

| Trục | Nội dung | Điểm |
|------|----------|------|
| **A · QA hệ thống** | Functional / Integration / UAT qua mọi role, kèm bug cards có ảnh | /100 |
| **B · Độ chính xác AI** | Kịch bản hội thoại đa lượt, đối chiếu ground truth | /10 |

Báo cáo **không sửa code** (report-only) — thiết kế để **mentor** dùng coaching đội build sản phẩm.

## 3 use case phổ biến

### 1. QA sản phẩm web đa role

```
/ai20k-mentor-qa-report
→ Cho URL + credential các role (STUDENT / ADMIN / STAFF)
→ Skill login đủ role, click toàn luồng, soi Network/Console
→ Xuất report Trục A với bug cards + ảnh
```

### 2. Đánh giá chatbot / RAG

```
/ai20k-mentor-qa-report
→ Cho URL chatbot + nguồn đối chiếu (trang chính thức, policy, KB)
→ Skill chạy kịch bản đa lượt (test coreference, chống bịa)
→ Xuất report Trục B với verdict ĐÚNG/MỘT PHẦN/SAI + chuẩn đối chiếu
```

### 3. QA toàn diện (cả 2 trục)

Sản phẩm có cả UI + chatbot → skill tự chạy cả A và B, xuất report tổng hợp.

## Đọc report (xem chi tiết trong ebook kèm theo)

- **Bảng điểm tổng** → sức khỏe sản phẩm trong 2 giây (A /100, B /10)
- **A2 bug summary** → bảng 1 dòng/lỗi, chọn lỗi nghiêm trọng nhất để bàn
- **A3 bug cards** → 5 trường: hiện tượng / tái hiện / nguyên nhân gốc / ghi chú dev / ảnh
- **B3 kịch bản** → hội thoại 👤↔🤖 + chuẩn đối chiếu + kết luận

## Cần tùy chỉnh?

- `[Tên sản phẩm]`, `[URL]`, `[role]`, `[nguồn đối chiếu]` → điền theo sản phẩm thực tế mỗi lần QA
- Nếu sản phẩm trên Linux/Windows → thay `screencapture` bằng công cụ chụp window tương đương
- Mức độ severity có thể điều chỉnh theo tiêu chuẩn riêng của chương trình mentor
