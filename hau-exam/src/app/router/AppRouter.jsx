import {Navigate,Route,Routes} from 'react-router-dom'
import {AppLayout} from '../layouts/AppLayout'
import {ForbiddenPage} from '../pages/ForbiddenPage'
import {NotFoundPage} from '../pages/NotFoundPage'
import {PlaceholderPage} from '../../components/shared/PlaceholderPage'
import {routes} from '../../constants/routes'
import {useAuth} from '../../features/auth/hooks/useAuth'
import {ForgotPasswordPage} from '../../features/auth/pages/ForgotPasswordPage'
import {LoginPage} from '../../features/auth/pages/LoginPage'
import {RegisterPage} from '../../features/auth/pages/RegisterPage'
import {RegistrationPendingPage} from '../../features/auth/pages/RegistrationPendingPage'
import {ResetPasswordPage} from '../../features/auth/pages/ResetPasswordPage'
import {VerifyOtpPage} from '../../features/auth/pages/VerifyOtpPage'
import {PendingRegistrationsPage,ProfilePage,UserDetailPage,UserListPage} from '../../features/users'
import {NotificationsPage} from '../../features/notifications'
import {CreateQuestionPage,EditQuestionPage,QuestionDetailPage,QuestionListPage} from '../../features/questions'
import {ProtectedRoute} from './ProtectedRoute'
import {RoleGuard} from './RoleGuard'
import {protectedRoutes} from './routeConfig'

function GuestRoute({children}){const{authenticated}=useAuth();return authenticated?<Navigate to={routes.dashboard} replace/>:children}
function RoutePage({route}){if(route.path===routes.users)return <UserListPage/>;if(route.path===routes.registrations)return <PendingRegistrationsPage/>;if(route.path===routes.userDetail)return <UserDetailPage/>;if(route.path===routes.profile)return <ProfilePage/>;if(route.path===routes.notifications)return <NotificationsPage/>;if(route.path===routes.questions)return <QuestionListPage/>;if(route.path===routes.newQuestion)return <CreateQuestionPage/>;if(route.path==='/questions/:id')return <QuestionDetailPage/>;if(route.path==='/questions/:id/edit')return <EditQuestionPage/>;return <PlaceholderPage title={route.title} description={route.description}/>}
export function AppRouter(){return <Routes><Route path={routes.login} element={<GuestRoute><LoginPage/></GuestRoute>}/><Route path={routes.register} element={<GuestRoute><RegisterPage/></GuestRoute>}/><Route path={routes.registrationPending} element={<GuestRoute><RegistrationPendingPage/></GuestRoute>}/><Route path={routes.forgotPassword} element={<GuestRoute><ForgotPasswordPage/></GuestRoute>}/><Route path={routes.verifyOtp} element={<GuestRoute><VerifyOtpPage/></GuestRoute>}/><Route path={routes.resetPassword} element={<GuestRoute><ResetPasswordPage/></GuestRoute>}/><Route element={<ProtectedRoute/>}><Route element={<AppLayout/>}>{protectedRoutes.map(route=><Route key={route.path} path={route.path} element={<RoleGuard allowedRoles={route.roles}><RoutePage route={route}/></RoleGuard>}/>) }<Route path={routes.forbidden} element={<ForbiddenPage/>}/><Route path="*" element={<NotFoundPage/>}/></Route></Route><Route path="/" element={<Navigate to={routes.dashboard} replace/>}/></Routes>}
