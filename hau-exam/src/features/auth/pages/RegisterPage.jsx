import {useState} from 'react'
import {Link,useNavigate} from 'react-router-dom'
import {Button,Input} from '../../../components/ui'
import {routes} from '../../../constants/routes'
import {authApi} from '../api/authApi'
import {AuthAlert} from '../components/AuthAlert'
import {AuthLayout} from '../components/AuthLayout'
import {PasswordInput} from '../components/PasswordInput'
import {getAuthErrorMessage} from '../model/authErrors'

export function RegisterPage(){const[form,setForm]=useState({lecturerCode:'',password:''});const[error,setError]=useState('');const[loading,setLoading]=useState(false);const navigate=useNavigate();async function submit(event){event.preventDefault();setError('');setLoading(true);try{await authApi.register({lecturerCode:form.lecturerCode.trim(),password:form.password});navigate(routes.registrationPending,{replace:true})}catch(reason){setError(getAuthErrorMessage(reason,'Không thể đăng ký tài khoản.'))}finally{setLoading(false)}}return <AuthLayout title="Đăng ký tài khoản" description="Tài khoản mới cần được quản trị viên hệ thống xác nhận." footer={<span>Đã có tài khoản? <Link to={routes.login}>Đăng nhập</Link></span>}><AuthAlert>{error}</AuthAlert><form className="auth-form" onSubmit={submit}><Input label="Mã giảng viên" name="lecturerCode" autoComplete="username" required value={form.lecturerCode} onChange={event=>setForm({...form,lecturerCode:event.target.value})}/><PasswordInput label="Mật khẩu" name="password" autoComplete="new-password" required value={form.password} onChange={event=>setForm({...form,password:event.target.value})}/><Button type="submit" loading={loading}>Đăng ký</Button></form></AuthLayout>}
