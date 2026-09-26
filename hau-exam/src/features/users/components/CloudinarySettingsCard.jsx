import { useEffect, useState } from "react";
import { Button } from "../../../components/ui";
import { getErrorMessage } from "../../../services/api/errorMessages";
import { platformSettingsApi } from "../api/platformSettingsApi";

export function CloudinarySettingsCard() {
  const [status, setStatus] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      setStatus(await platformSettingsApi.cloudinaryStatus());
      setError("");
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Không thể kiểm tra cấu hình Cloudinary."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = setTimeout(load, 0);
    return () => clearTimeout(timer);
  }, []);

  return (
    <article className="surface settings-card cloudinary-settings-card">
      <span className="eyebrow">LƯU TRỮ CLOUDINARY</span>
      <h2>Khóa tải ảnh backend</h2>
      <p>Cloudinary dùng cho ảnh hồ sơ, ảnh câu hỏi và ảnh trong hỗ trợ chat. Secret chỉ được đọc từ môi trường backend.</p>
      {loading && <p>Đang kiểm tra cấu hình…</p>}
      {!loading && status && (
        <dl>
          <div><dt>Cloud name</dt><dd>{status.cloudName || "Chưa cấu hình"}</dd></div>
          <div><dt>API key</dt><dd>{status.apiKeyConfigured ? "Đã cấu hình" : "Còn thiếu"}</dd></div>
          <div><dt>API secret</dt><dd>{status.apiSecretConfigured ? "Đã cấu hình" : "Còn thiếu"}</dd></div>
        </dl>
      )}
      <div className="cloudinary-env-list">
        <code>CLOUDINARY_CLOUD_NAME</code>
        <code>CLOUDINARY_API_KEY</code>
        <code>CLOUDINARY_API_SECRET</code>
      </div>
      <small className={`settings-note ${status?.configured ? "" : "settings-note-warning"}`}>{status?.configured ? "Cloudinary đã sẵn sàng" : "Thêm đủ 3 biến vào môi trường backend rồi khởi động lại service"}</small>
      {error && <p className="field-error" role="alert">{error}</p>}
      <div className="settings-actions"><Button type="button" variant="secondary" onClick={load} loading={loading}>Kiểm tra lại</Button></div>
    </article>
  );
}
