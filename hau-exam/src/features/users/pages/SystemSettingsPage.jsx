import {useEffect,useState} from 'react'
import {PageHeader} from '../../../components/shared/PageHeader'
import {Button} from '../../../components/ui/Button'
import {Input} from '../../../components/ui/Input'
import {adminSupportApi} from '../../support/api/adminSupportApi'

export function SystemSettingsPage(){
 const[email,setEmail]=useState(null),[recipient,setRecipient]=useState(''),[state,setState]=useState({loading:true,sending:false,error:'',success:''})
 useEffect(()=>{adminSupportApi.emailSettings().then(response=>setEmail(response.data?.data||response.data)).catch(error=>setState(current=>({...current,error:error.message||'Không thể tải cấu hình email'}))).finally(()=>setState(current=>({...current,loading:false})))},[])
 const sendTest=async()=>{if(!recipient)return;setState(current=>({...current,sending:true,error:'',success:''}));try{await adminSupportApi.testEmail(recipient);setState(current=>({...current,sending:false,success:'Email kiểm tra đã được gửi.'}))}catch(error){setState(current=>({...current,sending:false,error:error.message||'Không thể gửi email kiểm tra.'}))}}
 return <section className="admin-settings"><PageHeader title="Cài đặt hệ thống" description="Theo dõi các cấu hình nền tảng và kiểm tra kênh email."/><div className="settings-grid">
  <article className="surface settings-card"><span className="eyebrow">THÔNG TIN CHUNG</span><h2>HAU QM</h2><dl><div><dt>Đơn vị</dt><dd>Trường Đại học Kiến trúc Hà Nội</dd></div><div><dt>Kiến trúc</dt><dd>Microservices + API Gateway</dd></div><div><dt>Vai trò</dt><dd>Quản trị viên hệ thống · Quản trị viên chuyên môn · Giảng viên</dd></div></dl></article>
  <article className="surface settings-card"><span className="eyebrow">ĐĂNG KÝ & PHÊ DUYỆT</span><h2>Kiểm soát tài khoản</h2><p>Tài khoản giảng viên mới luôn ở trạng thái chờ phê duyệt. SYSTEM_ADMIN phân công Khoa và vai trò trước khi kích hoạt.</p><span className="settings-note">Đang áp dụng theo chính sách nghiệp vụ</span></article>
  <article className="surface settings-card"><span className="eyebrow">BẢO MẬT & PHIÊN</span><h2>Xác thực</h2><p>JWT, refresh token, BCrypt và đồng bộ security snapshot qua RabbitMQ được quản lý bởi Auth Service.</p><span className="settings-note">Thông tin bí mật không hiển thị tại đây</span></article>
  <article className="surface settings-card"><span className="eyebrow">CẤU HÌNH EMAIL</span><h2>SMTP</h2>{state.loading?<p>Đang tải cấu hình email...</p>:state.error?<p className="field-error">{state.error}</p>:<dl><div><dt>Máy chủ</dt><dd>{email?.host||'Chưa cấu hình'}</dd></div><div><dt>Người gửi</dt><dd>{email?.fromEmail||'Chưa cấu hình'}</dd></div><div><dt>Trạng thái</dt><dd>{email?.configured?'Đã cấu hình':'Chưa cấu hình'}</dd></div></dl>}<Input label="Email nhận kiểm tra" name="testRecipient" type="email" value={recipient} onChange={event=>setRecipient(event.target.value)} placeholder="admin@example.com"/><Button onClick={sendTest} loading={state.sending} disabled={!recipient}>Gửi email kiểm tra</Button>{state.success&&<p className="settings-success">{state.success}</p>}</article>
  <article className="surface settings-card"><span className="eyebrow">THÔNG BÁO</span><h2>Notification delivery</h2><p>Thông báo trong ứng dụng, WebSocket và email được xử lý tập trung bởi Notification Service.</p><span className="settings-note">Mật khẩu SMTP không được trả về giao diện</span></article>
 </div></section>
}
