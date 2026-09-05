import {Navigate} from 'react-router-dom'
import {routes} from '../../constants/routes'
import {useAuth} from '../../features/auth/hooks/useAuth'
export function RoleGuard({allowedRoles,children}){const{role}=useAuth();return allowedRoles.includes(role)?children:<Navigate to={routes.forbidden} replace/>}
