import { Link, useLocation } from 'react-router-dom'
import { routes } from '../../constants/routes'
import { PublicHeader } from './PublicHeader'
import logo from '../../assets/logo.jpg';
export function PublicLayout({ children }) {
  const location = useLocation()

  return (
    <div className="public-layout">
      <a className="public-skip-link" href="#public-main">Bỏ qua điều hướng</a>
      <PublicHeader />
      <main className="public-page-transition" id="public-main" key={location.pathname}>
        {children}
      </main>
      <footer className="public-footer">
        <div className="public-container public-footer-grid">
          <div className="public-footer-brand">
            <Link className="public-logo" to="/">
              <span aria-hidden="true"><img src={logo} alt="Hauexam" /></span>
              <strong>HAU-EXAM</strong>
            </Link>
            <p>Nền tảng quản lý học liệu và ngân hàng câu hỏi theo phạm vi chuyên môn.</p>
          </div>
          <div>
            <strong>Hệ thống</strong>
            <Link to="/">Trang chủ</Link>
            <Link to={routes.download}>Tải ứng dụng</Link>
          </div>
          <div>
            <strong>Hỗ trợ</strong>
            <Link to={routes.support}>Trung tâm hỗ trợ</Link>
            <Link to={routes.contact}>Liên hệ</Link>
          </div>
          <div>
            <strong>Pháp lý</strong>
            <Link to={routes.terms}>Điều khoản sử dụng</Link>
            <Link to={routes.privacy}>Chính sách bảo mật</Link>
          </div>
          <small>© 2026 HAU-EXAM · Đại học Kiến trúc Hà Nội</small>
        </div>
      </footer>
    </div>
  )
}
