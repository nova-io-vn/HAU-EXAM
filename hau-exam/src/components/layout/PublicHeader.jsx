import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { routes } from '../../constants/routes'
import { useAuth } from '../../features/auth/hooks/useAuth'
import { Icon } from '../ui'
import { UserMenu } from './UserMenu'
import logo from '../../assets/logo.jpg';
const publicNavigation = [
  { label: 'Trang chủ', to: '/', end: true },
  { label: 'Trung tâm hỗ trợ', to: routes.support },
  { label: 'Tải ứng dụng', to: routes.download },
  { label: 'Liên hệ', to: routes.contact },
]

export function PublicHeader() {
  const [menuOpen, setMenuOpen] = useState(false)
  const auth = useAuth()
  const closeMenu = () => setMenuOpen(false)

  return (
    <header className="public-header">
      <div className="public-container public-header-inner">
        <Link className="public-logo" to="/" aria-label="HAU-EXAM - Trang chủ" onClick={closeMenu}>
          <span aria-hidden="true"><img src={logo} alt="Hauexam" /></span>
          <strong>HAU-EXAM</strong>
        </Link>
        <div className={`public-navigation-panel ${menuOpen ? 'is-open' : ''}`} id="public-navigation">
          <nav className="public-navigation" aria-label="Điều hướng công khai">
            {publicNavigation.map((item) => (
              <NavLink key={item.to} to={item.to} end={item.end} className={({ isActive }) => (isActive ? 'is-active' : undefined)} onClick={closeMenu}>{item.label}</NavLink>
            ))}
          </nav>
        </div>
        <div className="public-header-actions">
          {auth.authenticated ? <UserMenu compact /> : <Link className="public-login-button" to={routes.login} onClick={closeMenu} aria-label="Đăng nhập" title="Đăng nhập"><Icon name="user" size={19} /></Link>}
        </div>
        <button className="public-menu-toggle" type="button" aria-label={menuOpen ? 'Đóng menu điều hướng' : 'Mở menu điều hướng'} aria-expanded={menuOpen} aria-controls="public-navigation" onClick={() => setMenuOpen((open) => !open)}><span /><span /><span /></button>
      </div>
    </header>
  )
}
