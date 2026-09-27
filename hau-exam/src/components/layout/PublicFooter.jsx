import { Link } from 'react-router-dom'
import { routes } from '../../constants/routes'
import { BrandLogo } from '../../features/branding/BrandLogo'
import { useBranding } from '../../features/branding/brandingContext'
import { Icon } from '../ui'

export function PublicFooter() {
  const branding = useBranding(); const year = new Date().getFullYear()
  return <footer className="public-footer"><div className="public-container public-footer-grid">
    <div className="public-footer-brand"><Link className="public-logo" to="/" aria-label="HAU Exam - Trang chủ"><span aria-hidden="true"><BrandLogo /></span><strong>{branding.shortName || 'HAU Exam'}</strong></Link><p>Hệ thống hỗ trợ tạo, quản lý, phê duyệt ngân hàng câu hỏi và đề thi trắc nghiệm tại Trường Đại học Kiến trúc Hà Nội.</p></div>
    <nav aria-label="Điều hướng hệ thống"><strong>Hệ thống</strong><Link to="/">Trang chủ</Link><Link to="/gioi-thieu">Giới thiệu</Link><Link to="/tinh-nang">Tính năng</Link><Link to="/ngan-hang-cau-hoi">Ngân hàng câu hỏi</Link><Link to="/quan-ly-de-thi">Quản lý đề thi</Link><Link to="/huong-dan">Hướng dẫn sử dụng</Link></nav>
    <nav aria-label="Hỗ trợ và pháp lý"><strong>Hỗ trợ</strong><Link to={routes.support}>Trung tâm trợ giúp</Link><Link to={routes.contact}>Liên hệ</Link><Link to={routes.terms}>Điều khoản sử dụng</Link><Link to={routes.privacy}>Chính sách bảo mật</Link></nav>
    <div className="public-footer-university"><strong>TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI</strong><p>Số 129, đường Trần Phú, phường Hà Đông, thành phố Hà Nội.</p><a href="https://hau.edu.vn" target="_blank" rel="noopener noreferrer">Website Trường Đại học Kiến trúc Hà Nội <span aria-hidden="true">↗</span></a><a href="https://www.facebook.com/DHKIENTRUCHN" target="_blank" rel="noopener noreferrer"><Icon name="messages" size={15} /> Facebook Đại học Kiến trúc Hà Nội <span aria-hidden="true">↗</span></a></div>
    <div className="public-footer-bottom"><span>© {year} HAU Exam. Hệ thống hỗ trợ quản lý ngân hàng câu hỏi và đề thi trắc nghiệm.</span><span><Link to={routes.terms}>Điều khoản sử dụng</Link><Link to={routes.privacy}>Chính sách bảo mật</Link></span></div>
  </div></footer>
}
