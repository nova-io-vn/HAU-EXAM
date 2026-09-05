import {BrowserRouter,useNavigate} from 'react-router-dom'
import {routes} from '../../constants/routes'
import {useAuthEvents} from '../../hooks/useAuthEvents'
function AuthEventHandler(){const navigate=useNavigate();useAuthEvents({onUnauthorized:()=>navigate(routes.login,{replace:true,state:{message:'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'}})});return null}
export function AppProviders({children}){return <BrowserRouter><AuthEventHandler/>{children}</BrowserRouter>}
