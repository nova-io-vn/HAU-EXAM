import {Navigate,Outlet,useLocation} from 'react-router-dom'
import {routes} from '../../constants/routes'
import {useAuth} from '../../features/auth/hooks/useAuth'
export function ProtectedRoute(){const{authenticated}=useAuth();const location=useLocation();return authenticated?<Outlet/>:<Navigate to={routes.login} replace state={{from:location.pathname,message:'Vui lòng đăng nhập để tiếp tục.'}}/>}
