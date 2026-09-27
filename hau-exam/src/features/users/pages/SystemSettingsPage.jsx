import { useEffect, useMemo, useState } from "react";
import { PageHeader } from "../../../components/shared/PageHeader";
import { Button, Input, Select } from "../../../components/ui";
import { PasswordInput } from "../../auth/components/PasswordInput";
import { getErrorMessage } from "../../../services/api/errorMessages";
import { emailSecurityLabels } from "../../../utils/enumLabels";
import { adminSupportApi } from "../../support/api/adminSupportApi";
import { AiKnowledgeSettingsCard } from "../../ai/components/AiKnowledgeSettingsCard";
import { CloudinarySettingsCard } from "../components/CloudinarySettingsCard";
import { VercelAnalyticsSettingsCard } from "../components/VercelAnalyticsSettingsCard";
import { BrandingSettingsCard } from "../components/BrandingSettingsCard";
import { toast } from "../../notifications/store/notificationStore";
import { api } from "../../../services/api/client";
import { TelegramBotSettingsCard } from "../../notifications/components/TelegramBotSettingsCard";
import {
  comparableEmailSettings,
  emailSettingsPayload,
  emptyEmailSettings,
  normalizeEmailSettings,
  updateEmailSetting,
  validateEmailSettings,
} from "../model/smtpSettings";

function AiSettingsCard() {
  const [form,setForm]=useState({provider:"GEMINI",model:"gemini-2.5-flash",apiKey:""}),[configured,setConfigured]=useState(false),[message,setMessage]=useState(""),[busy,setBusy]=useState(false);
  useEffect(()=>{api.get("/api/v1/admin/ai-settings").then(v=>{if(!v)throw new Error("Không nhận được cấu hình AI.");setForm(x=>({...x,provider:v.provider||x.provider,model:v.model||x.model}));setConfigured(Boolean(v.apiKeyConfigured))}).catch(e=>setMessage(e.message))},[]);
  async function save(){setBusy(true);setMessage("");try{const v=await api.put("/api/v1/admin/ai-settings",{provider:form.provider,model:form.model,apiKey:form.apiKey||undefined});setConfigured(Boolean(v.apiKeyConfigured));setForm(x=>({...x,apiKey:""}));setMessage("Đã lưu cấu hình AI.");toast.success("Đã lưu cấu hình AI.")}catch(e){setMessage(e.message);toast.error(e.message,{title:"Không thể lưu cấu hình AI"})}finally{setBusy(false)}}
  async function test(){setBusy(true);try{const v=await api.post("/api/v1/admin/ai-settings/test",{});const ok=v.status==="CONFIGURATION_VALID";setMessage(ok?"Cấu hình hợp lệ.":"Kiểm tra thất bại.");ok?toast.success("Kết nối AI hoạt động bình thường."):toast.warning("Cấu hình AI chưa hợp lệ.")}catch(e){setMessage(e.message);toast.error(e.message,{title:"Không thể kiểm tra AI"})}finally{setBusy(false)}}
  return <article className="surface settings-card"><span className="eyebrow">CẤU HÌNH AI</span><h2>Nhà cung cấp và model</h2><div className="settings-form"><Select label="Nhà cung cấp" value={form.provider} options={[{value:"GEMINI",label:"Google Gemini"},{value:"OPENAI",label:"OpenAI"},{value:"MISTRAL",label:"Mistral"}]} onChange={e=>setForm({...form,provider:e.target.value})}/><Input label="Model" value={form.model} onChange={e=>setForm({...form,model:e.target.value})} placeholder="gemini-2.5-flash"/><PasswordInput label="API Key" value={form.apiKey} onChange={e=>setForm({...form,apiKey:e.target.value})} placeholder={configured?"Đã cấu hình · nhập mới để thay đổi":"Nhập API key"}/><small className="settings-note">API key không được trả về giao diện; để trống sẽ giữ key hiện tại.</small><div className="settings-actions"><Button variant="secondary" onClick={test} loading={busy}>Kiểm tra kết nối</Button><Button onClick={save} loading={busy}>Lưu cấu hình</Button></div>{message&&<p className="settings-note" role="status">{message}</p>}</div></article>;
}

export function SystemSettingsPage() {
  const [form, setForm] = useState({ ...emptyEmailSettings });
  const [persistedForm, setPersistedForm] = useState(null);
  const [recipient, setRecipient] = useState("");
  const [state, setState] = useState({
    loading: true,
    saving: false,
    sending: false,
    loadError: "",
    loadCorrelationId: "",
    saveError: "",
    saveCorrelationId: "",
    testError: "",
    testCorrelationId: "",
    saveSuccess: "",
    testSuccess: "",
  });
  const validationError = useMemo(() => validateEmailSettings(form), [form]);
  const isDirty =
    !persistedForm ||
    comparableEmailSettings(form) !== comparableEmailSettings(persistedForm) ||
    Boolean(form.smtpPassword.trim());

  function update(name, value) {
    setForm((current) => updateEmailSetting(current, name, value));
    setState((current) => ({ ...current, saveError: "", testError: "", saveSuccess: "", testSuccess: "" }));
  }

  async function load() {
    const next = normalizeEmailSettings(await adminSupportApi.emailSettings());
    setForm(next);
    setPersistedForm(next);
  }

  useEffect(() => {
    load()
      .catch((error) =>
        setState((current) => ({
          ...current,
          loadError: getErrorMessage(error, "Không thể tải cấu hình email."),
          loadCorrelationId: error.correlationId || "",
        })),
      )
      .finally(() =>
        setState((current) => ({ ...current, loading: false })),
      );
  }, []);

  async function save() {
    if (validationError) {
      setState((current) => ({ ...current, saveError: validationError, saveCorrelationId: "", saveSuccess: "" }));
      return;
    }
    setState((current) => ({ ...current, saving: true, saveError: "", saveCorrelationId: "", saveSuccess: "" }));
    try {
      await adminSupportApi.saveEmailSettings(emailSettingsPayload(form));
      await load();
      setState((current) => ({
        ...current,
        saving: false,
        saveSuccess: "Đã lưu cấu hình email.",
      }));
      toast.success("Đã lưu cấu hình email.");
    } catch (error) {
      toast.error(getErrorMessage(error, "Không thể lưu cấu hình email."), { title: "Không thể lưu cấu hình" });
      setState((current) => ({
        ...current,
        saving: false,
        saveError: getErrorMessage(error, "Không thể lưu cấu hình email."),
        saveCorrelationId: error.correlationId || "",
      }));
    }
  }

  async function sendTest() {
    if (validationError || isDirty) return;
    setState((current) => ({ ...current, sending: true, testError: "", testSuccess: "" }));
    try {
      await adminSupportApi.testEmail(recipient.trim());
      setState((current) => ({
        ...current,
        sending: false,
        testSuccess: "Email kiểm tra đã được gửi thành công.",
      }));
      toast.success("Email kiểm tra đã được gửi thành công.");
    } catch (error) {
      toast.error(getErrorMessage(error, "Không thể gửi email kiểm tra."), { title: "Gửi email thất bại" });
      setState((current) => ({
        ...current,
        sending: false,
        testError: getErrorMessage(error, "Không thể gửi email kiểm tra."),
        testCorrelationId: error.correlationId || "",
      }));
    }
  }

  return (
    <section className="admin-settings">
      <PageHeader
        title="Cài đặt hệ thống"
        description="Quản lý AI, kho tri thức, lưu trữ ảnh và các kênh tích hợp của hệ thống."
      />
      {state.loadError && <p className="editor-error" role="alert">{state.loadError}{state.loadCorrelationId && <small> Mã đối chiếu: {state.loadCorrelationId}</small>}</p>}
      {state.saveError && <p className="editor-error" role="alert">{state.saveError}{state.saveCorrelationId && <small> Mã đối chiếu: {state.saveCorrelationId}</small>}</p>}
      {state.saveSuccess && <p className="settings-success" role="status">{state.saveSuccess}</p>}
      <div className="settings-grid">
        <BrandingSettingsCard />
        <TelegramBotSettingsCard />
        <AiSettingsCard />
        <CloudinarySettingsCard />
        <VercelAnalyticsSettingsCard />
        <AiKnowledgeSettingsCard />
        <article className="surface settings-card email-settings-card">
          <span className="eyebrow">CẤU HÌNH EMAIL</span>
          <h2>Máy chủ gửi thư</h2>
          {state.loading ? (
            <p>Đang tải cấu hình email…</p>
          ) : (
            <div className="settings-form">
              <Input label="SMTP Host" name="smtpHost" value={form.smtpHost} onChange={(event) => update("smtpHost", event.target.value)} placeholder="smtp.gmail.com" />
              <Input label="SMTP Port" name="smtpPort" type="number" min="1" max="65535" value={form.smtpPort} onChange={(event) => update("smtpPort", Number(event.target.value))} />
              <Select label="Bảo mật" value={form.security} onChange={(event) => update("security", event.target.value)} options={Object.entries(emailSecurityLabels).map(([value, label]) => ({ value, label }))} />
              <label className="settings-toggle"><input type="checkbox" checked={Boolean(form.enabled)} onChange={(event) => update("enabled", event.target.checked)} /><span>Bật gửi email</span></label>
              <Input label="SMTP Username" name="smtpUsername" type="email" value={form.smtpUsername} onChange={(event) => update("smtpUsername", event.target.value)} placeholder="email@example.com" />
              <PasswordInput
                label="App Password / SMTP Password"
                name="smtpPassword"
                autoComplete="new-password"
                value={form.smtpPassword}
                onChange={(event) => update("smtpPassword", event.target.value)}
                placeholder={form.passwordConfigured ? "••••••••••••" : "Nhập mật khẩu ứng dụng"}
                helper={form.passwordConfigured ? "Mật khẩu SMTP đã được cấu hình. Để trống nếu không thay đổi." : "Với Gmail, sử dụng Mật khẩu ứng dụng."}
              />
              <Input label="From Email" name="fromEmail" type="email" value={form.fromEmail} onChange={(event) => update("fromEmail", event.target.value)} placeholder="email@example.com" />
              <Input label="From Name" name="fromName" value={form.fromName} onChange={(event) => update("fromName", event.target.value)} placeholder="HAU QM" />
              {validationError && <p className="field-error" role="alert">{validationError}</p>}
              <div className="settings-actions"><Button onClick={save} loading={state.saving} disabled={Boolean(validationError) || state.sending}>Lưu cấu hình</Button></div>
            </div>
          )}
        </article>
        <article className="surface settings-card email-test-card">
          <span className="eyebrow">KIỂM TRA GỬI EMAIL</span>
          <h2>Gửi email kiểm tra</h2>
          <p>Kiểm tra cấu hình đã lưu; hãy lưu thay đổi trước khi gửi test.</p>
          <Input label="Email nhận kiểm tra" name="testRecipient" type="email" value={recipient} onChange={(event) => setRecipient(event.target.value)} placeholder="admin@example.com" />
          <Button onClick={sendTest} loading={state.sending} disabled={!recipient.trim() || Boolean(validationError) || isDirty || state.saving}>Gửi email kiểm tra</Button>
          {isDirty && <p className="settings-note">Lưu cấu hình trước khi gửi email kiểm tra.</p>}
          {state.testError && <p className="field-error" role="alert">{state.testError}{state.testCorrelationId && <small> Mã đối chiếu: {state.testCorrelationId}</small>}</p>}
          {state.testSuccess && <p className="settings-success" role="status">{state.testSuccess}</p>}
        </article>
        <article className="surface settings-card">
          <span className="eyebrow">THÔNG BÁO</span>
          <h2>Thông báo tập trung</h2>
          <p>Thông báo trong ứng dụng, WebSocket và email được xử lý tập trung bởi Notification Service.</p>
          <span className="settings-note">Mật khẩu SMTP không được trả về giao diện</span>
        </article>
      </div>
    </section>
  );
}
