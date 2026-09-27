import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button, Dialog } from "../../../components/ui";
import { routes } from "../../../constants/routes";
import { authStore } from "../../../stores/authStore";
import { toast } from "../../notifications/store/notificationStore";
import { authApi } from "../api/authApi";
import { PasswordInput } from "./PasswordInput";

const empty = { currentPassword: "", newPassword: "", confirmPassword: "" };
export function ChangePasswordDialog({ open, onClose }) {
  const [form, setForm] = useState(empty);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  function close() { if (!busy) { setForm(empty); setError(""); onClose(); } }
  async function submit(event) {
    event.preventDefault(); setError("");
    if (form.newPassword.length < 8) { setError("Mật khẩu mới phải có ít nhất 8 ký tự."); return; }
    if (form.newPassword !== form.confirmPassword) { setError("Xác nhận mật khẩu mới không khớp."); return; }
    setBusy(true);
    try {
      await authApi.changePassword({ currentPassword: form.currentPassword, newPassword: form.newPassword });
      toast.success("Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
      onClose();
      setTimeout(() => { authStore.clear(); navigate(routes.login, { replace: true, state: { message: "Mật khẩu đã được đổi. Vui lòng đăng nhập lại." } }); }, 900);
    } catch (reason) {
      const message = reason.code === "INVALID_CURRENT_PASSWORD" ? "Mật khẩu hiện tại không đúng." : (reason.message || "Vui lòng kiểm tra dữ liệu và thử lại.");
      setError(message); toast.error(message, { title: "Không thể đổi mật khẩu" });
    } finally { setBusy(false); }
  }
  return <Dialog open={open} title="Đổi mật khẩu" subtitle="Tất cả refresh token sẽ bị thu hồi sau khi đổi mật khẩu." onClose={close} footer={<><Button variant="secondary" onClick={close} disabled={busy}>Hủy</Button><Button type="submit" form="change-password-form" loading={busy}>Đổi mật khẩu</Button></>}>
    <form id="change-password-form" className="settings-form" onSubmit={submit}>
      <PasswordInput label="Mật khẩu hiện tại" required autoComplete="current-password" value={form.currentPassword} onChange={event => setForm({ ...form, currentPassword: event.target.value })} placeholder="Nhập mật khẩu hiện tại" />
      <PasswordInput label="Mật khẩu mới" required minLength="8" maxLength="100" autoComplete="new-password" value={form.newPassword} onChange={event => setForm({ ...form, newPassword: event.target.value })} placeholder="Ít nhất 8 ký tự" helper="Dùng mật khẩu riêng, khó đoán và không trùng mật khẩu hiện tại." />
      <PasswordInput label="Xác nhận mật khẩu mới" required minLength="8" maxLength="100" autoComplete="new-password" value={form.confirmPassword} onChange={event => setForm({ ...form, confirmPassword: event.target.value })} placeholder="Nhập lại mật khẩu mới" />
      {error && <p className="field-error" role="alert">{error}</p>}
    </form>
  </Dialog>;
}
