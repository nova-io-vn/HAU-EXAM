import {useState} from 'react'
import {Link,useNavigate} from 'react-router-dom'
import {Button,Input} from '../../../components/ui'
import {routes} from '../../../constants/routes'
import {authApi} from '../api/authApi'
import {AuthAlert} from '../components/AuthAlert'
import {AuthLayout} from '../components/AuthLayout'
import {getAuthErrorMessage} from '../model/authErrors'
export function ForgotPasswordPage(){const[lecturerCode,setLecturerCode]=useState('');const[error,setError]=useState('');const[loading,setLoading]=useState(false);const navigate=useNavigate();async function submit(event){event.preventDefault();setLoading(true);setError('');try{const identity=lecturerCode.trim();await authApi.forgotPassword({lecturerCode:identity});navigate(routes.verifyOtp,{state:{lecturerCode:identity}})}catch(reason){setError(getAuthErrorMessage(reason,'Không thể gửi yêu cầu OTP.'))}finally{setLoading(false)}}return <AuthLayout title="Quên mật khẩu" description="Nhập mã giảng viên để yêu cầu mã OTP." footer={<Link to={routes.login}>Quay lại đăng nhập</Link>}><AuthAlert>{error}</AuthAlert><form className="auth-form" onSubmit={submit}><Input label="Mã giảng viên" name="lecturerCode" autoComplete="username" required value={lecturerCode} onChange={event=>setLecturerCode(event.target.value)}/><Button type="submit" loading={loading}>Gửi mã OTP</Button></form></AuthLayout>}
