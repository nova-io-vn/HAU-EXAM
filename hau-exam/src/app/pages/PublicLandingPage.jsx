import { Link } from 'react-router-dom'
import { LandingWorkflow } from '../../components/landing/LandingWorkflow'
import { PublicLayout } from '../../components/layout/PublicLayout'
import { MediaImage } from '../../components/shared/MediaImage'
import { Icon } from '../../components/ui'
import { routes } from '../../constants/routes'
import { useBranding } from '../../features/branding/brandingContext'
import banner from '../../assets/banner.jpg'
import campus from '../../assets/toam.jpg'
import trangchu from '../../assets/trangchu.png'

const features = [
  ['book', 'Ngân hàng câu hỏi', 'Tổ chức Subject, Chapter, Topic, mức độ và hình ảnh câu hỏi.'],
  ['sparkles', 'AI hỗ trợ', 'Phân tích học liệu và tạo gợi ý có cấu trúc để người dùng kiểm tra.'],
  ['check', 'Phê duyệt chuyên môn', 'Theo dõi quy trình xét duyệt và phạm vi Khoa rõ ràng.'],
  ['grid', 'Ma trận đề', 'Phân bổ câu hỏi theo chủ đề, chương và mức độ khó.'],
  ['file', 'Quản lý tài liệu', 'Tập trung học liệu phục vụ quy trình xây dựng câu hỏi.'],
  ['bell', 'Thông báo & trao đổi', 'Theo dõi cập nhật và trao đổi qua các kênh được cấu hình.'],
]

const seo = {
  title: 'HAU Exam | Ngân hàng câu hỏi và đề thi trắc nghiệm HAU',
  description: 'HAU Exam hỗ trợ tạo, quản lý và phê duyệt ngân hàng câu hỏi, đề thi trắc nghiệm tại Trường Đại học Kiến trúc Hà Nội, kết hợp AI và quy trình quản lý chuyên môn.',
  path: '/',
  image: banner,
  jsonLd: { '@context': 'https://schema.org', '@type': 'WebSite', name: 'HAU Exam', alternateName: 'HAU QM', url: 'https://exam.nova.io.vn/' },
}

export function PublicLandingPage() {
  const branding = useBranding()
  return (
    <PublicLayout seo={seo}>
      <div className="public-landing">
        <section className="public-hero public-hero-enhanced">
          <div className="public-hero-copy">
            <span className="eyebrow">HAU EXAM · HỆ THỐNG HỖ TRỢ CHUYÊN MÔN</span>
            <h1>HAU Exam – Hệ thống quản lý ngân hàng câu hỏi và đề thi trắc nghiệm</h1>
            <p>Hỗ trợ xây dựng, quản lý, phê duyệt câu hỏi và tạo đề thi trong môi trường số dành cho hoạt động chuyên môn tại Trường Đại học Kiến trúc Hà Nội.</p>
            <div className="public-actions">
              <Link className="button button-primary" to={routes.login}>Đăng nhập →</Link>
              <a className="button button-secondary" href="#about">Tìm hiểu hệ thống</a>
              <Link className="button button-ghost" to={routes.register}>Đăng ký tài khoản</Link>
            </div>
          </div>
          <div className="public-hero-visual">
            <MediaImage className="public-hero-photo" src={banner} alt="Tòa nhà Trường Đại học Kiến trúc Hà Nội" width={904} height={339} loading="eager" />
            <div className="public-hero-visual-label"><strong>{branding.shortName || 'HAU Exam'}</strong><span>Quản lý nội dung chuyên môn theo quy trình rõ ràng</span></div>
          </div>
        </section>

        <section id="about" className="public-section public-intro">
          <div>
            <span className="eyebrow">HAU EXAM TRONG THỰC TẾ</span>
            <h2>Xây dựng ngân hàng câu hỏi trong một quy trình thống nhất.</h2>
            <p>HAU Exam hỗ trợ từ xây dựng câu hỏi, phân loại học liệu và phê duyệt chuyên môn đến thiết kế ma trận, tạo bộ đề và quản lý phiên bản.</p>
            <p>Đây là hệ thống hỗ trợ hoạt động chuyên môn, không phải nền tảng tổ chức thi trực tuyến.</p>
          </div>
          <figure className="public-trangchu-visual">
            <img src={trangchu} alt="Giao diện hệ thống HAU Exam quản lý ngân hàng câu hỏi và đề thi" width="800" height="533" />
            <figcaption>Giao diện tổng quan hỗ trợ người dùng theo dõi công việc trong HAU Exam.</figcaption>
          </figure>
        </section>

        <section id="features" className="public-section public-section-muted landing-feature-section">
          <span className="eyebrow">NĂNG LỰC CỐT LÕI</span>
          <h2>Công cụ rõ ràng cho từng bước làm việc.</h2>
          <div className="public-feature-grid">
            {features.map(([icon, title, text]) => <article key={title}><span className="public-feature-icon"><Icon name={icon} size={20} /></span><h3>{title}</h3><p>{text}</p></article>)}
          </div>
        </section>

        <LandingWorkflow />

        <section className="public-section public-roles">
          <span className="eyebrow">DÀNH CHO AI?</span>
          <h2>Phân vai rõ ràng, dữ liệu đúng phạm vi.</h2>
          <div className="public-role-grid">
            <article><h3>Giảng viên</h3><p>Tạo câu hỏi, tải học liệu và theo dõi nội dung của mình.</p></article>
            <article><h3>Quản trị viên chuyên môn</h3><p>Đánh giá nội dung trong phạm vi Khoa được phân công.</p></article>
            <article><h3>Quản trị viên hệ thống</h3><p>Quản lý tài khoản, vai trò, Khoa và thiết lập cấp hệ thống.</p></article>
          </div>
        </section>

        <section className="public-section public-context">
          <div className="public-context-image"><img src={campus} alt="Tòa nhà Trường Đại học Kiến trúc Hà Nội" width="365" height="547" loading="lazy" /></div>
          <div>
            <span className="eyebrow">VỀ TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI</span>
            <h2>Không gian chuyên môn trong bối cảnh một trường đại học kiến trúc.</h2>
            <p>HAU Exam được định hướng là hệ thống hỗ trợ quản lý ngân hàng câu hỏi và đề thi trắc nghiệm tại Trường Đại học Kiến trúc Hà Nội.</p>
            <div className="public-actions"><a className="button button-secondary" href="https://hau.edu.vn" target="_blank" rel="noopener noreferrer">Website HAU ↗</a><a className="button button-ghost" href="https://www.facebook.com/DHKIENTRUCHN" target="_blank" rel="noopener noreferrer">Facebook HAU ↗</a></div>
          </div>
        </section>

        <section className="public-section public-cta" style={{ '--cta-image': `url(${banner})` }}>
          <div><span className="eyebrow">{branding.shortName || 'HAU Exam'}</span><h2>Bắt đầu với HAU Exam</h2><p>Tạo, quản lý và phê duyệt ngân hàng câu hỏi trong một quy trình thống nhất.</p></div>
          <div className="public-actions"><Link className="button button-primary" to={routes.login}>Đăng nhập</Link><Link className="button button-light" to={routes.register}>Đăng ký tài khoản</Link></div>
        </section>
      </div>
    </PublicLayout>
  )
}
