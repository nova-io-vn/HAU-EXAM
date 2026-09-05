import {useState} from 'react'
import {Link,useLocation,useNavigate} from 'react-router-dom'
import {Button,Input} from '../../../components/ui'
import {routes} from '../../../constants/routes'
import {authStore} from '../../../stores/authStore'
import {authApi} from '../api/authApi'
import {AuthAlert} from '../components/AuthAlert'
import {AuthLayout} from '../components/AuthLayout'
import {PasswordInput} from '../components/PasswordInput'
import {getAuthErrorMessage} from '../model/authErrors'

export function LoginPage(){
  const[form,setForm]=useState({lecturerCode:'',password:''});const[error,setError]=useState('');const[loading,setLoading]=useState(false);const navigate=useNavigate();const location=useLocation()
  async function submit(event){event.preventDefault();setError('');setLoading(true);try{const session=await authApi.login({lecturerCode:form.lecturerCode.trim(),password:form.password});authStore.setSession(session);navigate(location.state?.from||routes.dashboard,{replace:true})}catch(reason){setError(getAuthErrorMessage(reason,'Không thể đăng nhập.'))}finally{setLoading(false)}}
  return <AuthLayout title="Đăng nhập" description="Truy cập không gian quản lý ngân hàng đề thi." footer={<span>Chưa có tài khoản? <Link to={routes.register}>Đăng ký</Link></span>}><AuthAlert>{location.state?.message||error}</AuthAlert><form className="auth-form" onSubmit={submit}><Input label="Mã giảng viên" name="lecturerCode" autoComplete="username" required value={form.lecturerCode} onChange={event=>setForm({...form,lecturerCode:event.target.value})}/><PasswordInput label="Mật khẩu" name="password" autoComplete="current-password" required value={form.password} onChange={event=>setForm({...form,password:event.target.value})}/><Link className="auth-link auth-forgot" to={routes.forgotPassword}>Quên mật khẩu?</Link><Button type="submit" loading={loading}>Đăng nhập</Button></form></AuthLayout>
}
