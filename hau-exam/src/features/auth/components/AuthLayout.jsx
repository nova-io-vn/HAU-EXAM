import { Link } from "react-router-dom";
import { routes } from "../../../constants/routes";
import campus from '../../../assets/toam.jpg';
import { BrandLogo } from '../../branding/BrandLogo';
import { useBranding } from '../../branding/brandingContext';

export function AuthLayout({
  title,
  description,
  children,
  footer,
  activeTab = "login",
}) {
  const branding = useBranding();
  return (
    <main className="auth-page">
      <section className="auth-brand-panel" style={{ '--auth-campus': `url(${campus})` }}>
        <div className="auth-brand-content">
        <div className="auth-brand-mark"><BrandLogo /></div>
        <span className="auth-overline">{branding.shortName} SYSTEM</span>
        <h1>
          Không gian khảo thí
          <br />
          dành cho giảng viên HAU.
        </h1>
        <p>
          Quản lý học liệu, xây dựng ngân hàng câu hỏi và phối hợp phê duyệt
          trong một không gian thống nhất.
        </p>
        </div>
      </section>
      <section className="auth-card" aria-labelledby="auth-title">
        <Link className="auth-brand" to="/">
          <span><BrandLogo /></span>
          <strong>{branding.shortName}</strong>
        </Link>
        <div className="auth-tabs">
          <Link
            className={activeTab === "login" ? "active" : ""}
            to={routes.login}
          >
            Đăng nhập
          </Link>
          <Link
            className={activeTab === "register" ? "active" : ""}
            to={routes.register}
          >
            Đăng ký tài khoản
          </Link>
        </div>
        <header>
          <p className="eyebrow">HỆ THỐNG QUẢN LÝ KHẢO THÍ</p>
          <h2 id="auth-title">{title}</h2>
          <p>{description}</p>
        </header>
        {children}
        {footer && <footer>{footer}</footer>}
        <Link className="auth-home-link" to="/">
          ← Quay về Trang chủ công khai
        </Link>
      </section>
    </main>
  );
}
