import {Link,useNavigate} from 'react-router-dom'
import {routes} from '../../constants/routes'
import {roles} from '../../constants/roles'
import {authApi} from '../../features/auth/api/authApi'
import {useAuth} from '../../features/auth/hooks/useAuth'
import {authStore} from '../../stores/authStore'
import {Icon} from '../ui'
import {Avatar} from '../shared/Avatar'
import {formatAcademicName,formatRoleFaculty} from '../../features/users/model/academic'

export function UserMenu(){const auth=useAuth();const navigate=useNavigate();const name=formatAcademicName(auth.currentUser||{});async function logout(){const token=authStore.getRefreshToken();try{if(token)await authApi.logout(token)}finally{authStore.clear();navigate(routes.login,{replace:true})}}return <details className="user-menu"><summary data-tour="profile-menu" aria-label="Mở menu tài khoản"><Avatar user={auth.currentUser||{}} size="sm"/><span className="profile-copy"><strong>{name}</strong><small>{formatRoleFaculty(auth.currentUser||{},auth.facultyId)}</small></span><Icon name="chevron" size={14}/></summary><div className="user-menu-popover"><div className="user-menu-heading"><strong>Hồ sơ tài khoản</strong><span>{formatRoleFaculty(auth.currentUser||{},auth.facultyId)}</span></div><Link to={routes.profile}>Hồ sơ cá nhân</Link><Link to={routes.help}>Trung tâm trợ giúp</Link>{auth.role===roles.SYSTEM_ADMIN&&<Link to={routes.settings}>Cài đặt hệ thống</Link>}<button type="button" onClick={logout}><Icon name="logout" size={15}/>Đăng xuất</button></div></details>}
