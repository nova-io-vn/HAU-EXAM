import {Link} from 'react-router-dom'
import {Button} from '../../../components/ui'
import {routes} from '../../../constants/routes'
import {AuthLayout} from '../components/AuthLayout'
export function RegistrationPendingPage(){return <AuthLayout title="Đang chờ xác nhận" description="Đăng ký thành công. Tài khoản đang chờ quản trị viên xác nhận."><div className="auth-success" role="status"><span aria-hidden="true">✓</span><p>Bạn chỉ có thể đăng nhập sau khi tài khoản được duyệt.</p></div><Link to={routes.login}><Button className="auth-wide">Về trang đăng nhập</Button></Link></AuthLayout>}
