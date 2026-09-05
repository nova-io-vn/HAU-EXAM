import {useState} from 'react'
import {Link,useLocation,useNavigate} from 'react-router-dom'
import {Button,Input} from '../../../components/ui'
import {routes} from '../../../constants/routes'
import {authApi} from '../api/authApi'
import {AuthAlert} from '../components/AuthAlert'
import {AuthLayout} from '../components/AuthLayout'
import {getAuthErrorMessage} from '../model/authErrors'
export function VerifyOtpPage(){const location=useLocation();const navigate=useNavigate();const lecturerCode=location.state?.lecturerCode;const[otp,setOtp]=useState('');const[error,setError]=useState('');const[loading,setLoading]=useState(false);if(!lecturerCode)return <NavigateToForgot/>;async function submit(event){event.preventDefault();setLoading(true);setError('');try{await authApi.verifyOtp({lecturerCode,otp:otp.trim()});navigate(routes.resetPassword,{replace:true,state:{lecturerCode,otp:otp.trim()}})}catch(reason){setError(getAuthErrorMessage(reason,'Không thể xác minh OTP.'))}finally{setLoading(false)}}return <AuthLayout title="Xác minh OTP" description={`Nhập mã OTP đã được gửi cho tài khoản ${lecturerCode}.`} footer={<Link to={routes.forgotPassword}>Yêu cầu mã mới</Link>}><AuthAlert>{error}</AuthAlert><form className="auth-form" onSubmit={submit}><Input label="Mã OTP" name="otp" autoComplete="one-time-code" required value={otp} onChange={event=>setOtp(event.target.value)}/><Button type="submit" loading={loading}>Xác minh</Button></form></AuthLayout>}
function NavigateToForgot(){const navigate=useNavigate();return <AuthLayout title="Thiếu thông tin xác minh" description="Hãy bắt đầu lại yêu cầu đặt lại mật khẩu."><Button className="auth-wide" onClick={()=>navigate(routes.forgotPassword,{replace:true})}>Yêu cầu OTP</Button></AuthLayout>}
