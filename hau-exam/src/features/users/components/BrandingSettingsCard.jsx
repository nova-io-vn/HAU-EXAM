import { useEffect, useState } from "react";
import { Button, Input } from "../../../components/ui";
import { brandingApi } from "../../branding/api/brandingApi";
import { useBranding } from "../../branding/brandingContext";
import { toast } from "../../notifications/store/notificationStore";

const allowed = new Set(["image/jpeg", "image/png", "image/webp", "image/gif"]);
export function BrandingSettingsCard() {
  const global = useBranding();
  const [form, setForm] = useState({ systemName: global.systemName, shortName: global.shortName, logoUrl: global.logoUrl, faviconUrl: global.faviconUrl });
  const [busy, setBusy] = useState("");
  useEffect(() => { brandingApi.admin().then(value => value && setForm(value)).catch(error => toast.error(error.message, { title: "Không thể tải thương hiệu" })); }, []);
  async function save(event) {
    event.preventDefault(); setBusy("save");
    try { const value = await brandingApi.save({ systemName: form.systemName.trim(), shortName: form.shortName.trim() }); setForm(value); global.apply(value); toast.success("Đã lưu cấu hình thương hiệu."); }
    catch (error) { toast.error(error.message || "Vui lòng thử lại.", { title: "Không thể lưu thương hiệu" }); }
    finally { setBusy(""); }
  }
  async function upload(kind, file) {
    if (!file) return;
    if (!allowed.has(file.type) || file.size > 5 * 1024 * 1024) { toast.warning("Chọn ảnh JPG, PNG, WEBP hoặc GIF nhỏ hơn 5 MB."); return; }
    setBusy(kind);
    try { const value = await (kind === "logo" ? brandingApi.uploadLogo(file) : brandingApi.uploadFavicon(file)); setForm(value); global.apply(value); toast.success(kind === "logo" ? "Đã cập nhật logo hệ thống." : "Đã cập nhật favicon."); }
    catch (error) { toast.error(error.message || "Vui lòng thử lại.", { title: "Không thể tải ảnh" }); }
    finally { setBusy(""); }
  }
  return <article className="surface settings-card branding-settings-card">
    <span className="eyebrow">THƯƠNG HIỆU HỆ THỐNG</span><h2>Nhận diện hiển thị</h2>
    <form className="settings-form" onSubmit={save}>
      <Input label="Tên hệ thống" required maxLength="120" value={form.systemName || ""} onChange={event => setForm({ ...form, systemName: event.target.value })} placeholder="VD: HAU QM" />
      <Input label="Tên ngắn" required maxLength="40" value={form.shortName || ""} onChange={event => setForm({ ...form, shortName: event.target.value })} placeholder="VD: HAU QM" />
      <div className="branding-upload-row"><img src={form.logoUrl || global.logo} alt="Xem trước logo hệ thống" /><div><strong>Logo hệ thống</strong><span>JPG, PNG, WEBP hoặc GIF · tối đa 5 MB</span><label className="button button-secondary">{busy === "logo" ? "Đang tải..." : "Tải logo mới"}<input hidden type="file" accept="image/png,image/jpeg,image/webp,image/gif" disabled={Boolean(busy)} onChange={event => { void upload("logo", event.target.files?.[0]); event.target.value = ""; }} /></label></div></div>
      <div className="branding-upload-row branding-favicon"><img src={form.faviconUrl || global.logo} alt="Xem trước favicon" /><div><strong>Favicon</strong><span>Ảnh vuông hiển thị trên tab trình duyệt</span><label className="button button-secondary">{busy === "favicon" ? "Đang tải..." : "Tải favicon mới"}<input hidden type="file" accept="image/png,image/jpeg,image/webp,image/gif" disabled={Boolean(busy)} onChange={event => { void upload("favicon", event.target.files?.[0]); event.target.value = ""; }} /></label></div></div>
      <Button type="submit" loading={busy === "save"} disabled={Boolean(busy) || !form.systemName?.trim() || !form.shortName?.trim()}>Lưu thương hiệu</Button>
    </form>
  </article>;
}
