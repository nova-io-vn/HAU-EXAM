import {useLocation} from 'react-router-dom'
import {getRouteMeta} from '../../app/router/routeConfig'
import {Button} from '../ui/Button'
import {NotificationBell} from '../../features/notifications'
import {UserMenu} from './UserMenu'
export function Header({collapsed,onToggle,onMobileMenu}){const{pathname}=useLocation();const page=getRouteMeta(pathname);return <header className="topbar"><Button variant="ghost" className="desktop-toggle" onClick={onToggle} aria-label={collapsed?'Mở rộng thanh bên':'Thu gọn thanh bên'}>☰</Button><Button variant="ghost" className="mobile-toggle" onClick={onMobileMenu} aria-label="Mở điều hướng">☰</Button><div className="topbar-context"><strong>{page?.title||'HAU-EXAM'}</strong><span>Đại học Kiến trúc Hà Nội</span></div><NotificationBell/><UserMenu/></header>}
