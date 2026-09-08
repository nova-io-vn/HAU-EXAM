import {useEffect,useMemo,useState} from 'react'
import {PageHeader} from '../../../components/shared/PageHeader'
import {Button} from '../../../components/ui/Button'
import {Input} from '../../../components/ui/Input'
import {adminSupportApi} from '../../support/api/adminSupportApi'

const empty={smtpHost:'',smtpPort:587,smtpUsername:'',smtpPassword:'',fromEmail:'',fromName:'HAU QM',security:'STARTTLS',enabled:false}
const securityLabels={NONE:'Không mã hóa',STARTTLS:'STARTTLS',SSL_TLS:'SSL/TLS'}

export function SystemSettingsPage(){
 const[form,setForm]=useState(empty),[recipient,setRecipient]=useState(''),[state,setState]=useState({loading:true,saving:false,sending:false,error:'',success:''})
 const update=(name,value)=>setForm(current=>({...current,[name]:value}))
 const valid=useMemo(()=>!form.enabled||Boolean(form.smtpHost&&form.smtpPort&&form.fromEmail&&form.security&& (form.security==='NONE'||form.smtpUsername) && (form.passwordConfigured||form.smtpPassword)),[form])
 const load=()=>adminSupportApi.emailSettings().then(data=>setForm({...empty,...data,smtpPassword:'',passwordConfigured:Boolean(data.passwordConfigured)}))
 useEffect(()=>{load().catch(error=>setState(current=>({...current,error:error.message||'Không thể tải cấu hình email'}))).finally(()=>setState(current=>({...current,loading:false})))},[])
 const save=async()=>{setState(current=>({...current,saving:true,error:'',success:''}));try{await adminSupportApi.saveEmailSettings(form);await load();setState(current=>({...current,saving:false,success:'Đã cập nhật cấu hình email.'}))}catch(error){setState(current=>({...current,saving:false,error:error.message||'Không thể lưu cấu hình email.'}))}}
 const sendTest=async()=>{setState(current=>({...current,sending:true,error:'',success:''}));try{await adminSupportApi.testEmail(recipient);setState(current=>({...current,sending:false,success:'Email kiểm tra đã được gửi thành công.'}))}catch(error){setState(current=>({...current,sending:false,error:error.message||'Không thể gửi email kiểm tra.'}))}}
 return <section className="admin-settings"><PageHeader title="Cài đặt hệ thống" description="Theo dõi các cấu hình nền tảng và quản lý kênh email."/><div className="settings-grid">
  <article className="surface settings-card"><span className="eyebrow">THÔNG TIN CHUNG</span><h2>HAU QM</h2><dl><div><dt>Đơn vị</dt><dd>Trường Đại học Kiến trúc Hà Nội</dd></div><div><dt>Kiến trúc</dt><dd>Microservices + API Gateway</dd></div><div><dt>Vai trò</dt><dd>Quản trị viên hệ thống · Quản trị viên chuyên môn · Giảng viên</dd></div></dl></article>
  <article className="surface settings-card"><span className="eyebrow">ĐĂNG KÝ & PHÊ DUYỆT</span><h2>Kiểm soát tài khoản</h2><p>Tài khoản giảng viên mới luôn ở trạng thái chờ phê duyệt. SYSTEM_ADMIN phân công Khoa và vai trò trước khi kích hoạt.</p><span className="settings-note">Đang áp dụng theo chính sách nghiệp vụ</span></article>
  <article className="surface settings-card"><span className="eyebrow">BẢO MẬT & PHIÊN</span><h2>Xác thực</h2><p>JWT, refresh token, BCrypt và đồng bộ security snapshot được quản lý bởi Auth Service.</p><span className="settings-note">Thông tin bí mật không hiển thị tại đây</span></article>
  <article className="surface settings-card email-settings-card"><span className="eyebrow">CẤU HÌNH EMAIL</span><h2>SMTP</h2>
   {state.loading?<p>Đang tải cấu hình email...</p>:<div className="settings-form">
    <Input label="Máy chủ SMTP" name="smtpHost" value={form.smtpHost} onChange={e=>update('smtpHost',e.target.value)} placeholder="smtp.gmail.com"/>
    <Input label="Cổng" name="smtpPort" type="number" min="1" max="65535" value={form.smtpPort} onChange={e=>update('smtpPort',Number(e.target.value))}/>
    <Input label="Email / Tài khoản SMTP" name="smtpUsername" type="email" value={form.smtpUsername} onChange={e=>update('smtpUsername',e.target.value)} placeholder="email@example.com"/>
    <Input label="Mật khẩu ứng dụng / SMTP" name="smtpPassword" type="password" value={form.smtpPassword} onChange={e=>update('smtpPassword',e.target.value)} placeholder={form.passwordConfigured?'••••••••••••••••':'Nhập mật khẩu ứng dụng'} helper={form.passwordConfigured?'Đã cấu hình mật khẩu. Để trống nếu bạn không muốn thay đổi mật khẩu hiện tại.':'Với Gmail, sử dụng Mật khẩu ứng dụng thay cho mật khẩu tài khoản thông thường.'}/>
    <Input label="Email người gửi" name="fromEmail" type="email" value={form.fromEmail} onChange={e=>update('fromEmail',e.target.value)} placeholder="email@example.com"/>
    <Input label="Tên người gửi" name="fromName" value={form.fromName} onChange={e=>update('fromName',e.target.value)} placeholder="HAU QM"/>
    <label className="field" htmlFor="smtp-security"><span>Bảo mật</span><select id="smtp-security" value={form.security} onChange={e=>update('security',e.target.value)}>{Object.entries(securityLabels).map(([value,label])=><option key={value} value={value}>{label}</option>)}</select></label>
    <label className="settings-toggle"><input type="checkbox" checked={Boolean(form.enabled)} onChange={e=>update('enabled',e.target.checked)}/><span>Bật gửi email</span></label>
    <div className="settings-actions"><Button onClick={save} loading={state.saving} disabled={!valid}>Lưu cấu hình</Button></div>
   </div>}
   <div className="settings-divider"><h3>Kiểm tra gửi email</h3><Input label="Email nhận kiểm tra" name="testRecipient" type="email" value={recipient} onChange={e=>setRecipient(e.target.value)} placeholder="admin@example.com"/><Button onClick={sendTest} loading={state.sending} disabled={!recipient||!valid}>Gửi email kiểm tra</Button></div>
   {state.error&&<p className="field-error">{state.error}</p>}{state.success&&<p className="settings-success">{state.success}</p>}
  </article>
  <article className="surface settings-card"><span className="eyebrow">THÔNG BÁO</span><h2>Notification delivery</h2><p>Thông báo trong ứng dụng, WebSocket và email được xử lý tập trung bởi Notification Service.</p><span className="settings-note">Mật khẩu SMTP không được trả về giao diện</span></article>
 </div></section>
}
