import { useState } from "react";
import { PageHeader } from "../../components/shared/PageHeader";
import { Button, Input } from "../../components/ui";
import { useAuth } from "../auth/hooks/useAuth";
const guides = {
  SYSTEM_ADMIN: [
    [
      "Bắt đầu sử dụng",
      "Đăng nhập, kiểm tra menu theo vai trò và theo dõi trạng thái hệ thống.",
    ],
    [
      "Quản lý Khoa",
      "Tạo, cập nhật, tìm kiếm và kích hoạt hoặc vô hiệu hóa Khoa bằng dữ liệu thật từ hệ thống.",
    ],
    [
      "Quản lý giảng viên",
      "Tìm kiếm giảng viên, gán Khoa và cập nhật vai trò theo phân quyền.",
    ],
    [
      "Phê duyệt tài khoản",
      "Chọn Khoa và vai trò trước khi phê duyệt tài khoản đăng ký.",
    ],
    [
      "Phân quyền",
      "Quản trị viên chuyên môn chỉ thao tác nội dung thuộc Khoa được phân công.",
    ],
    ["Thông báo", "Xem, đánh dấu đã đọc và mở toàn bộ thông báo."],
    ["Cài đặt", "Cấu hình hệ thống ở phạm vi được backend hỗ trợ."],
  ],
  SUBJECT_ADMIN: [
    [
      "Bắt đầu sử dụng",
      "Kiểm tra Khoa được phân công và các mục nội dung trong thanh điều hướng.",
    ],
    ["Quản lý môn học", "Tạo và cập nhật môn học thuộc Khoa của bạn."],
    [
      "Cấu trúc kiến thức",
      "Quản lý Chương và Chủ đề theo đúng quan hệ môn học.",
    ],
    [
      "Ngân hàng câu hỏi",
      "Tìm kiếm, lọc và xem câu hỏi trong phạm vi được phép.",
    ],
    [
      "Phê duyệt câu hỏi",
      "Mở hàng đợi, xem nội dung và phê duyệt, từ chối hoặc yêu cầu chỉnh sửa.",
    ],
    [
      "Tạo câu hỏi bằng AI",
      "Theo dõi tác vụ AI; câu hỏi sinh ra vẫn phải qua quy trình phê duyệt.",
    ],
    ["Ma trận đề", "Tạo ma trận từ các câu hỏi đã được phê duyệt."],
    ["Thông báo", "Theo dõi phản hồi và trạng thái xử lý mới nhất."],
  ],
  USER: [
    [
      "Bắt đầu sử dụng",
      "Kiểm tra Khoa và vai trò Giảng viên sau khi đăng nhập.",
    ],
    [
      "Câu hỏi của tôi",
      "Tìm kiếm, lọc và theo dõi trạng thái các câu hỏi do bạn tạo.",
    ],
    [
      "Tạo câu hỏi",
      "Chọn đúng Môn, Chương, Chủ đề rồi lưu nháp hoặc gửi phê duyệt.",
    ],
    [
      "Tài liệu của tôi",
      "Tải và theo dõi trạng thái tài liệu theo giới hạn backend.",
    ],
    [
      "Tạo câu hỏi bằng AI",
      "Chọn tài liệu, tạo tác vụ và chỉnh sửa kết quả trước khi gửi duyệt.",
    ],
    [
      "Gửi phê duyệt",
      "Câu hỏi ở trạng thái nháp hoặc cần chỉnh sửa có thể được gửi lại.",
    ],
    [
      "Xử lý yêu cầu chỉnh sửa",
      "Đọc phản hồi, cập nhật nội dung và gửi lại phê duyệt.",
    ],
    ["Ma trận đề", "Xem các chức năng ma trận được cấp quyền."],
    ["Thông báo", "Xem thông báo duyệt câu hỏi, AI và hệ thống."],
  ],
};
const subtitles = {
  SYSTEM_ADMIN: "Hướng dẫn quản trị hệ thống, Khoa, giảng viên và tài khoản.",
  SUBJECT_ADMIN:
    "Hướng dẫn quản lý nội dung, phê duyệt câu hỏi và phân tích chuyên môn.",
  USER: "Hướng dẫn tạo câu hỏi, AI, tài liệu và quy trình phê duyệt.",
};
export function HelpPage() {
  const { role } = useAuth();
  const [term, setTerm] = useState("");
  const [open, setOpen] = useState(0);
  const items = (guides[role] || guides.USER).filter(([title, text]) =>
    (title + " " + text).toLowerCase().includes(term.toLowerCase()),
  );
  return (
    <section className="help-page">
      <PageHeader
        title="Trung tâm trợ giúp"
        description={subtitles[role] || subtitles.USER}
      />
      <div className="surface">
        <Input
          label="Tìm kiếm hướng dẫn..."
          value={term}
          onChange={(event) => setTerm(event.target.value)}
          placeholder="Tìm kiếm hướng dẫn..."
        />
        {items.map(([title, text], index) => (
          <article key={title} className="help-item">
            <button
              type="button"
              onClick={() => setOpen(open === index ? -1 : index)}
              aria-expanded={open === index}
            >
              <strong>{title}</strong>
              <span>{open === index ? "−" : "+"}</span>
            </button>
            {open === index && <p>{text}</p>}
          </article>
        ))}
        {!items.length && (
          <p className="help-empty">Không tìm thấy hướng dẫn phù hợp.</p>
        )}
        <Button
          variant="secondary"
          onClick={() =>
            window.dispatchEvent(new CustomEvent("hau:restart-onboarding"))
          }
        >
          Xem lại hướng dẫn hệ thống
        </Button>
      </div>
    </section>
  );
}
