import { useEffect, useState } from "react";
import { Button } from "../../../components/ui";
import { Input } from "../../../components/ui";
import { PasswordInput } from "../../auth/components/PasswordInput";
import { getErrorMessage } from "../../../services/api/errorMessages";
import { platformSettingsApi } from "../api/platformSettingsApi";

export function CloudinarySettingsCard() {
  const [status, setStatus] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ cloudName: "", apiKey: "", apiSecret: "" });
  const [message, setMessage] = useState("");

  async function load() {
    setLoading(true);
    try {
      const next = await platformSettingsApi.cloudinaryStatus();
      setStatus(next);
      setForm(current => ({ ...current, cloudName: next.cloudName || "" }));
      setError("");
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Không thể kiểm tra cấu hình Cloudinary."));
    } finally {
      setLoading(false);
    }
  }

  async function save(event) {
    event.preventDefault();
    setSaving(true); setMessage(""); setError("");
    try {
      const next = await platformSettingsApi.saveCloudinary(form);
      setStatus(next); setForm(current => ({ ...current, apiKey: "", apiSecret: "" }));
      setMessage("Đã lưu cấu hình Cloudinary. Có thể upload ảnh đại diện ngay.");
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Không thể lưu cấu hình Cloudinary."));
    } finally { setSaving(false); }
  }

  useEffect(() => {
    const timer = setTimeout(load, 0);
    return () => clearTimeout(timer);
  }, []);

  return (
    <article className="surface settings-card cloudinary-settings-card">
      <span className="eyebrow">LƯU TRỮ CLOUDINARY</span>
      <h2>Khóa tải ảnh backend</h2>
      <p>Nhập thông tin Cloudinary tại đây. API secret được mã hóa và chỉ lưu ở User Service, không trả về trình duyệt.</p>
      {loading && <p>Đang kiểm tra cấu hình…</p>}
      {!loading && status && (
        <dl>
          <div><dt>Cloud name</dt><dd>{status.cloudName || "Chưa cấu hình"}</dd></div>
          <div><dt>API key</dt><dd>{status.apiKeyConfigured ? "Đã cấu hình" : "Còn thiếu"}</dd></div>
          <div><dt>API secret</dt><dd>{status.apiSecretConfigured ? "Đã cấu hình" : "Còn thiếu"}</dd></div>
          <div><dt>Nguồn cấu hình</dt><dd>{status.source === "database" ? "Cài đặt hệ thống" : "Biến môi trường"}</dd></div>
        </dl>
      )}
      <form className="settings-form cloudinary-form" onSubmit={save}>
        <Input label="Cloud name" value={form.cloudName} onChange={event => setForm({ ...form, cloudName: event.target.value })} placeholder="djkzentjq" />
        <Input label="API key" value={form.apiKey} onChange={event => setForm({ ...form, apiKey: event.target.value })} placeholder={status?.apiKeyConfigured ? "Nhập mới để thay đổi" : "Nhập API key"} />
        <PasswordInput label="API secret" value={form.apiSecret} onChange={event => setForm({ ...form, apiSecret: event.target.value })} placeholder={status?.apiSecretConfigured ? "Nhập mới để thay đổi" : "Nhập API secret"} />
        <small className="settings-note">Lưu ý: khi lưu, nhập đủ cả 3 giá trị. Secret hiện tại không bao giờ được hiển thị lại.</small>
        <div className="settings-actions"><Button type="submit" loading={saving} disabled={!form.cloudName.trim() || !form.apiKey.trim() || !form.apiSecret.trim()}>Lưu Cloudinary</Button></div>
      </form>
      <small className={`settings-note ${status?.configured ? "" : "settings-note-warning"}`}>{status?.configured ? "Cloudinary đã sẵn sàng" : "Nhập đủ 3 thông tin ở trên để bật upload ảnh"}</small>
      {error && <p className="field-error" role="alert">{error}</p>}
      {message && <p className="settings-success" role="status">{message}</p>}
      <div className="settings-actions"><Button type="button" variant="secondary" onClick={load} loading={loading}>Kiểm tra lại</Button></div>
    </article>
  );
}
