import {Link,useNavigate} from 'react-router-dom'
import {routes} from '../../constants/routes'
import {authApi} from '../../features/auth/api/authApi'
import {useAuth} from '../../features/auth/hooks/useAuth'
import {authStore} from '../../stores/authStore'
export function UserMenu(){const auth=useAuth();const navigate=useNavigate();const label=auth.currentUser?.lecturerCode||'Người dùng';const initials=label.slice(0,2).toUpperCase();async function logout(){const refreshToken=authStore.getRefreshToken();try{if(refreshToken)await authApi.logout(refreshToken)}finally{authStore.clear();navigate(routes.login,{replace:true})}}return <details className="user-menu"><summary aria-label="Mở menu tài khoản"><span className="user-avatar">{initials}</span><span className="profile-copy"><strong>{label}</strong><small>{auth.role||'USER'}</small></span><span aria-hidden="true">⌄</span></summary><div className="user-menu-popover"><div><strong>{label}</strong><span>{auth.facultyId?`Khoa: ${auth.facultyId}`:'Chưa có thông tin khoa'}</span></div><Link to={routes.profile}>Hồ sơ cá nhân</Link><button type="button" onClick={logout}>Đăng xuất</button></div></details>}
