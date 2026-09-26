import { useEffect, useState } from "react";
import { Button, Input } from "../../../components/ui";
import { PasswordInput } from "../../auth/components/PasswordInput";
import { getErrorMessage } from "../../../services/api/errorMessages";
import { platformSettingsApi } from "../api/platformSettingsApi";

export function VercelAnalyticsSettingsCard() {
  const [status,setStatus]=useState(null);
  const [form,setForm]=useState({projectId:"",teamId:"",token:""});
  const [loading,setLoading]=useState(true),[saving,setSaving]=useState(false),[error,setError]=useState(""),[message,setMessage]=useState("");
  async function load(){setLoading(true);try{const next=await platformSettingsApi.vercelAnalyticsStatus();setStatus(next);setForm(current=>({...current,projectId:next.projectId||"",teamId:next.teamId||""}));setError("")}catch(e){setError(getErrorMessage(e,"Không thể kiểm tra cấu hình Vercel Analytics."))}finally{setLoading(false)}}
  useEffect(()=>{const timer=setTimeout(load,0);return()=>clearTimeout(timer)},[]);
  async function save(event){event.preventDefault();setSaving(true);setMessage("");setError("");try{const next=await platformSettingsApi.saveVercelAnalytics(form);setStatus(next);setForm(current=>({...current,token:""}));setMessage("Đã lưu cấu hình Vercel Analytics.")}catch(e){setError(getErrorMessage(e,"Không thể lưu cấu hình Vercel Analytics."))}finally{setSaving(false)}}
  return <article className="surface settings-card">
    <span className="eyebrow">VERCEL WEB ANALYTICS</span><h2>Thống kê người truy cập</h2>
    <p>Nhập thông tin API để Dashboard quản trị đọc số liệu từ project Vercel. Token được mã hóa ở backend và không hiển thị lại.</p>
    {!loading&&status&&<dl><div><dt>Project ID</dt><dd>{status.projectId||"Chưa cấu hình"}</dd></div><div><dt>Team ID</dt><dd>{status.teamId||"Chưa cấu hình"}</dd></div><div><dt>Token</dt><dd>{status.tokenConfigured?"Đã cấu hình":"Còn thiếu"}</dd></div></dl>}
    <form className="settings-form" onSubmit={save}>
      <Input label="VERCEL_PROJECT_ID" value={form.projectId} onChange={e=>setForm({...form,projectId:e.target.value})} placeholder="prj_xxxxxxxxx" />
      <Input label="VERCEL_TEAM_ID" value={form.teamId} onChange={e=>setForm({...form,teamId:e.target.value})} placeholder="team_xxxxxxxxx" />
      <PasswordInput label="VERCEL_TOKEN" value={form.token} onChange={e=>setForm({...form,token:e.target.value})} placeholder={status?.tokenConfigured?"Nhập token mới để thay đổi":"x-vc-xxxxxxxxx"} />
      <small className="settings-note">Nhập đủ 3 trường rồi bấm lưu. Sau đó mở lại Dashboard hoặc bấm thử lại.</small>
      <div className="settings-actions"><Button type="submit" loading={saving} disabled={!form.projectId.trim()||!form.teamId.trim()||!form.token.trim()}>Lưu Vercel Analytics</Button><Button type="button" variant="secondary" onClick={load} loading={loading}>Kiểm tra lại</Button></div>
    </form>
    {error&&<p className="field-error" role="alert">{error}</p>}{message&&<p className="settings-success" role="status">{message}</p>}
  </article>;
}
