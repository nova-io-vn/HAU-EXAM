import { Link } from 'react-router-dom'
import { routes } from '../../constants/routes'
import { PublicLayout } from '../../components/layout/PublicLayout'
import { MediaImage } from '../../components/shared/MediaImage'
import { Icon } from '../../components/ui'
import { LANDING_MEDIA } from '../../config/landingMedia'

const features = [
  ['Ngân hàng câu hỏi', 'Soạn thảo, phân loại và theo dõi workflow trong phạm vi Khoa.'],
  ['AI hỗ trợ tạo sinh', 'Đưa học liệu vào quy trình tạo câu hỏi có cấu trúc và kiểm duyệt.'],
  ['Phê duyệt chuyên môn', 'Quản trị viên chuyên môn xem xét và đưa câu hỏi vào ngân hàng chính thức.'],
  ['Độ bao phủ kiến thức', 'Theo dõi nội dung theo Môn học, Chương và Chủ đề.'],
  ['Ma trận đề', 'Xây dựng phân bố câu hỏi và phiên bản đề từ ngân hàng đã duyệt.'],
  ['Thông báo tập trung', 'Nhận cập nhật workflow qua in-app, realtime và email.'],
]

const journeys = [
  ['questionBank', 'Soạn câu hỏi', 'Tổ chức nội dung theo môn học, chương và chủ đề.'],
  ['review', 'Phê duyệt chuyên môn', 'Giữ quy trình review rõ ràng, đúng phạm vi quản trị.'],
  ['exam', 'Tạo đề thi', 'Thiết kế ma trận và sinh bộ đề từ nội dung đã duyệt.'],
]

export function PublicLandingPage() {
  return <PublicLayout>
    <div className="public-landing">
      <section className="public-hero public-hero-enhanced">
        <div className="public-hero-copy">
          <span className="eyebrow">HỆ THỐNG QUẢN LÝ KHẢO THÍ</span>
          <h1>Xây dựng ngân hàng câu hỏi thông minh và có hệ thống.</h1>
          <p>Nền tảng hỗ trợ giảng viên HAU quản lý học liệu, tạo câu hỏi, AI, phê duyệt và xây dựng ma trận theo phạm vi chuyên môn.</p>
          <div className="public-actions">
            <Link className="button button-primary public-hero-primary" to={routes.login}>Bắt đầu trải nghiệm <span aria-hidden="true">→</span></Link>
            <a className="button button-secondary" href="#features">Khám phá hệ thống</a>
          </div>
          <div className="public-hero-note"><span aria-hidden="true">●</span> Một không gian làm việc thống nhất cho giáo dục hiện đại</div>
        </div>
        <div className="public-hero-visual" aria-label="Xem trước giao diện HAU-EXAM">
          <MediaImage className="public-hero-photo" src={LANDING_MEDIA.hero} alt="Hình ảnh minh họa môi trường học thuật hiện đại" width={1200} height={800} loading="eager" />
          <div className="public-product-preview">
            <div className="preview-top"><b>HAU-EXAM</b><span>Dashboard</span><i /></div>
            <div className="preview-body"><aside><em /><em /><em /><em /></aside><div><div className="preview-title" /><div className="preview-kpis"><i /><i /><i /></div><div className="preview-table"><i /><i /><i /><i /></div></div></div>
            <div className="preview-badge"><Icon name="check" size={14} /> Workflow rõ ràng</div>
          </div>
          <div className="public-mobile-card"><span>Mobile workspace</span><strong>Luôn sẵn sàng</strong><MediaImage src={LANDING_MEDIA.mobile} alt="Hình ảnh minh họa giao diện trên thiết bị di động" width={700} height={900} /></div>
        </div>
      </section>

      <section className="public-section public-media-intro" aria-labelledby="teaching-title">
        <div className="public-media-intro-image"><MediaImage src={LANDING_MEDIA.teaching} alt="Hình ảnh minh họa lớp học đại học và công nghệ giáo dục" width={1200} height={800} /><span>Hình ảnh minh họa</span></div>
        <div><span className="eyebrow">MÔI TRƯỜNG GIẢNG DẠY HIỆN ĐẠI</span><h2 id="teaching-title">Được xây dựng cho cách giảng dạy ngày hôm nay.</h2><p>HAU-EXAM kết nối học liệu, ngân hàng câu hỏi và quy trình chuyên môn trong một workspace gọn gàng, dễ theo dõi.</p><div className="public-check-list"><span>Ngân hàng câu hỏi có cấu trúc</span><span>AI hỗ trợ, con người kiểm duyệt</span><span>Ma trận và tạo đề theo phạm vi</span></div></div>
      </section>

      <section id="features" className="public-section public-section-muted"><span className="eyebrow">MỘT KHÔNG GIAN THỐNG NHẤT</span><h2>Tập trung vào nội dung chuyên môn.</h2><div className="public-feature-grid">{features.map(([title, text]) => <article key={title}><strong>{title}</strong><p>{text}</p></article>)}</div></section>

      <section className="public-section public-journeys" aria-labelledby="journeys-title"><span className="eyebrow">TRẢI NGHIỆM DÀNH CHO GIẢNG VIÊN</span><h2 id="journeys-title">Mỗi bước làm việc đều rõ ràng hơn.</h2><div className="public-journey-grid">{journeys.map(([image, title, text]) => <article className="public-journey-card" key={title}><div className="public-journey-image"><MediaImage src={LANDING_MEDIA[image]} alt={`${title} - hình ảnh minh họa`} width={900} height={600} /><span>Hình ảnh minh họa</span></div><div><strong>{title}</strong><p>{text}</p></div></article>)}</div></section>

      <section id="workflow" className="public-section public-workflow"><span className="eyebrow">QUY TRÌNH RÕ RÀNG</span><h2>Từ học liệu đến câu hỏi được phê duyệt.</h2><ol>{['Tải tài liệu và tạo câu hỏi thủ công hoặc bằng AI.', 'Chỉnh sửa, phân loại và gửi câu hỏi cho quản trị viên chuyên môn.', 'Review theo phạm vi Khoa, sau đó đưa câu hỏi đã duyệt vào ngân hàng.'].map((text, i) => <li key={text}><b>0{i + 1}</b><span>{text}</span></li>)}</ol></section>

      <section id="contact" className="public-section public-cta" style={{ '--cta-image': `url(${LANDING_MEDIA.ctaBackground})` }}><div><span className="eyebrow">HAU-EXAM</span><h2>Sẵn sàng trải nghiệm HAU-EXAM?</h2><p>Bắt đầu từ một workspace rõ ràng hơn cho nội dung và quy trình khảo thí.</p></div><div className="public-actions"><Link className="button button-primary" to={routes.login}>Bắt đầu trải nghiệm <span aria-hidden="true">→</span></Link><Link className="button button-light" to={routes.download}>Tải ứng dụng</Link></div></section>
    </div>
  </PublicLayout>
}
