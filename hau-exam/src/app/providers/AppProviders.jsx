import {useEffect} from 'react'
import {BrowserRouter,useLocation,useNavigate} from 'react-router-dom'
import {routes} from '../../constants/routes'
import {useAuthEvents} from '../../hooks/useAuthEvents'
import {authApi} from '../../features/auth/api/authApi'
import {useAuth} from '../../features/auth/hooks/useAuth'
import {authStore} from '../../stores/authStore'
import {Loading} from '../../components/ui'
const authPaths=new Set([routes.login,routes.register,routes.registrationPending,routes.forgotPassword,routes.verifyOtp,routes.resetPassword])
function AuthEventHandler(){const navigate=useNavigate();const location=useLocation();useAuthEvents({onUnauthorized:()=>{if(!authPaths.has(location.pathname))navigate(routes.login,{replace:true,state:{message:'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'}})},onForbidden:()=>{if(location.pathname!==routes.forbidden)navigate(routes.forbidden,{replace:true})}});return null}
function AuthBootstrap({children}){const{bootstrapping}=useAuth();useEffect(()=>{authStore.bootstrapAuth(authApi.refresh)},[]);return bootstrapping?<Loading label="Đang khôi phục phiên đăng nhập"/>:children}
export function AppProviders({children}){return <BrowserRouter><AuthEventHandler/><AuthBootstrap>{children}</AuthBootstrap></BrowserRouter>}
