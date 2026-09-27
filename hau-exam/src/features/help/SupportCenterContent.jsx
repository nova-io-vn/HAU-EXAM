import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Icon, Input } from "../../components/ui";
import campus from "../../assets/toam.jpg";
import { useBranding } from "../branding/brandingContext";

const categories = [
  ["sparkles", "Bắt đầu sử dụng", "Đăng nhập, nhận biết workspace theo vai trò và thiết lập hồ sơ."],
  ["database", "Quản lý câu hỏi", "Tạo, chỉnh sửa, phân loại và theo dõi trạng thái câu hỏi."],
  ["file", "Tạo đề thi", "Xây dựng ma trận, sinh đề, xem phiên bản và xuất PDF."],
  ["messages", "AI Assistant", "Hỏi đáp an toàn và khai thác học liệu theo ngữ cảnh."],
  ["folder", "Tài liệu", "Tải học liệu lên và theo dõi quá trình xử lý."],
  ["user", "Tài khoản & bảo mật", "Cập nhật hồ sơ, giao diện và đổi mật khẩu an toàn."],
];
const guides = [
  ["Bắt đầu với HAU QM", "Đăng nhập bằng mã giảng viên, kiểm tra vai trò và khoa ở khu vực tài khoản. Menu chỉ hiển thị chức năng phù hợp với vai trò đã được duyệt."],
  ["Tạo và quản lý câu hỏi", "Mở Câu hỏi của tôi, chọn Tạo câu hỏi, chọn môn học – chương – chủ đề, nhập nội dung và đáp án. Bạn có thể lưu nháp để hoàn thiện sau."],
  ["Quy trình gửi duyệt", "Từ câu hỏi nháp, kiểm tra nội dung rồi chọn Gửi phê duyệt. Câu hỏi chuyển sang Chờ duyệt; khi được yêu cầu chỉnh sửa, cập nhật theo phản hồi và gửi lại."],
  ["Quản trị chuyên môn", "SUBJECT_ADMIN duyệt, từ chối hoặc yêu cầu chỉnh sửa câu hỏi trong đúng khoa được phân công. Mỗi quyết định cần dựa trên nội dung chuyên môn và lịch sử phản hồi."],
  ["Tạo và quản lý đề thi", "Tạo ma trận phân bố theo chương và độ khó, kiểm tra tổng số câu rồi chọn Tạo đề. Đề được lưu tại Exam Service, có thể xem chi tiết, tạo phiên bản mới và xuất PDF."],
  ["Upload tài liệu", "Mở Tài liệu của tôi, chọn tệp đúng định dạng và giới hạn dung lượng. Chờ trạng thái xử lý hoàn tất trước khi dùng tài liệu cho tác vụ AI."],
  ["Sử dụng AI Assistant", "Bạn có thể hỏi cách dùng HAU QM hoặc kiến thức phổ thông an toàn. Khi dùng học liệu, hãy chọn đúng tài liệu và luôn kiểm tra kết quả AI trước khi đưa vào quy trình duyệt."],
  ["Tin nhắn", "Dùng biểu tượng tin nhắn trên Header. Giảng viên liên hệ SUBJECT_ADMIN cùng khoa; SUBJECT_ADMIN có thể trao đổi với giảng viên được phân công và SYSTEM_ADMIN."],
  ["Thông báo", "Biểu tượng chuông hiển thị số thông báo chưa đọc. Mở thông báo để xem thay đổi trạng thái câu hỏi, tác vụ AI và các thông tin hệ thống."],
  ["Hồ sơ cá nhân", "Mở menu tài khoản → Hồ sơ cá nhân để cập nhật tên, thông tin liên hệ và ảnh đại diện. Các thay đổi chỉ có hiệu lực sau khi máy chủ xác nhận."],
  ["Đổi mật khẩu", "Mở menu tài khoản → Đổi mật khẩu. Nhập đúng mật khẩu hiện tại và mật khẩu mới tối thiểu 8 ký tự. Sau khi đổi, hệ thống thu hồi refresh token và yêu cầu đăng nhập lại."],
  ["Liên hệ quản trị viên", "Dùng Tin nhắn để trao đổi trực tiếp theo phạm vi quyền. Với lỗi không đăng nhập được, dùng biểu mẫu Liên hệ công khai và không gửi mật khẩu, OTP hoặc token."],
];
const faqs = [
  ["Làm sao tạo câu hỏi?", "Mở Câu hỏi của tôi → Tạo câu hỏi, chọn cấu trúc kiến thức, nhập nội dung và đáp án rồi lưu nháp."],
  ["Làm sao gửi câu hỏi để phê duyệt?", "Mở chi tiết câu hỏi nháp, kiểm tra lại dữ liệu và chọn Gửi phê duyệt."],
  ["Tại sao câu hỏi bị yêu cầu chỉnh sửa?", "Quản trị viên chuyên môn đã phát hiện nội dung cần bổ sung. Mở lịch sử phản hồi, chỉnh sửa và gửi lại."],
  ["Làm sao tạo đề thi?", "SUBJECT_ADMIN tạo ma trận hợp lệ, sau đó mở Đề thi → Tạo đề và chọn ma trận tương ứng."],
  ["Làm sao sử dụng tài liệu với AI?", "Tải tài liệu lên, chờ xử lý hoàn tất, sau đó chọn tài liệu trong workspace AI trước khi tạo tác vụ."],
  ["Làm sao liên hệ quản trị viên?", "Chọn biểu tượng Tin nhắn trên Header và chọn người liên hệ được phép theo khoa/vai trò."],
  ["Làm sao đổi mật khẩu?", "Mở menu Avatar → Đổi mật khẩu, xác nhận mật khẩu hiện tại và nhập mật khẩu mới."],
];

export function SupportCenterContent({ authenticated = false }) {
  const branding = useBranding(); const [term, setTerm] = useState("");
  const results = useMemo(() => guides.filter(([title, text]) => `${title} ${text}`.toLowerCase().includes(term.trim().toLowerCase())), [term]);
  return <div className="support-center">
    <section className="support-hero" style={{ "--support-campus": `url(${campus})` }}><div><span className="eyebrow">TRUNG TÂM HỖ TRỢ {branding.shortName}</span><h1>Bạn cần hỗ trợ vấn đề gì?</h1><p>Tìm hướng dẫn theo quy trình thực tế dành cho giảng viên và quản trị viên HAU.</p><Input label="Tìm kiếm hướng dẫn" value={term} onChange={event => setTerm(event.target.value)} placeholder="Tìm kiếm hướng dẫn, câu hỏi thường gặp..." /></div></section>
    <section className="support-category-grid" aria-label="Chủ đề hỗ trợ">{categories.map(([icon, title, text]) => <article className="surface" key={title}><span><Icon name={icon} size={22} /></span><h2>{title}</h2><p>{text}</p></article>)}</section>
    <section className="support-guide-section"><div className="support-section-heading"><span className="eyebrow">HƯỚNG DẪN THEO QUY TRÌNH</span><h2>Sử dụng {branding.shortName} hiệu quả</h2></div><div className="support-guide-grid">{results.map(([title, text], index) => <article className="surface" key={title}><b>{String(index + 1).padStart(2, "0")}</b><div><h3>{title}</h3><p>{text}</p></div></article>)}</div>{!results.length && <p className="support-no-results">Không tìm thấy hướng dẫn phù hợp. Hãy thử từ khóa ngắn hơn hoặc liên hệ quản trị viên.</p>}</section>
    <section className="support-feature-block"><div><span className="eyebrow">HỖ TRỢ ĐÚNG NGƯỜI, ĐÚNG PHẠM VI</span><h2>Kết nối nhanh khi cần thêm trợ giúp</h2><p>Tin nhắn sử dụng tên thật, vai trò và khoa để bạn biết chính xác mình đang trao đổi với ai. Backend vẫn kiểm tra RBAC và phạm vi khoa cho mọi cuộc trò chuyện.</p>{authenticated ? <span className="support-chat-hint">Chọn biểu tượng tin nhắn ở góc phải Header để bắt đầu.</span> : <Link className="button button-primary" to="/contact">Gửi yêu cầu hỗ trợ</Link>}</div><img src={campus} alt="Tòa nhà Trường Đại học Kiến trúc Hà Nội" /></section>
    <section className="support-faq"><div className="support-section-heading"><span className="eyebrow">CÂU HỎI THƯỜNG GẶP</span><h2>Giải đáp nhanh</h2></div>{faqs.map(([question, answer]) => <details key={question}><summary>{question}<span>+</span></summary><p>{answer}</p></details>)}</section>
  </div>;
}
