import {useEffect} from 'react'
import {BrowserRouter,useLocation,useNavigate} from 'react-router-dom'
import {routes} from '../../constants/routes'
import {useAuthEvents} from '../../hooks/useAuthEvents'
import {authApi} from '../../features/auth/api/authApi'
import {usersApi} from '../../features/users/api/usersApi'
import {useAuth} from '../../features/auth/hooks/useAuth'
import {authStore} from '../../stores/authStore'
import {PageLoader} from '../../components/ui'
import {ThemeProvider} from './ThemeProvider'
const authPaths=new Set([routes.login,routes.register,routes.registrationPending,routes.forgotPassword,routes.verifyOtp,routes.resetPassword])
function AuthEventHandler(){const navigate=useNavigate();const location=useLocation();useAuthEvents({onUnauthorized:()=>{if(!authPaths.has(location.pathname))navigate(routes.login,{replace:true,state:{message:'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'}})},onForbidden:()=>{if(location.pathname!==routes.forbidden)navigate(routes.forbidden,{replace:true})}});return null}
function AuthBootstrap({children}){const{bootstrapping,authenticated,currentUser}=useAuth();useEffect(()=>{authStore.bootstrapAuth(authApi.refresh)},[]);useEffect(()=>{if(!authenticated||!currentUser?.id)return;let active=true;usersApi.getMe().then(profile=>{if(active)authStore.updateCurrentUser(profile)}).catch(()=>undefined);return()=>{active=false}},[authenticated,currentUser?.id]);return bootstrapping?<PageLoader label="Đang khôi phục phiên đăng nhập"/>:children}
export function AppProviders({children}){return <ThemeProvider><BrowserRouter><AuthEventHandler/><AuthBootstrap>{children}</AuthBootstrap></BrowserRouter></ThemeProvider>}
