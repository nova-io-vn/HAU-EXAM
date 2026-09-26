import { useCallback, useEffect, useRef, useState } from "react";
import { Button, ConfirmDialog, Input } from "../../../components/ui";
import { getErrorMessage } from "../../../services/api/errorMessages";
import { aiKnowledgeApi } from "../api/aiKnowledgeApi";

const acceptedExtensions = ".txt,.md,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx";

function fileSize(bytes) {
  if (!Number.isFinite(bytes)) return "—";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function updatedAt(value) {
  return value ? new Intl.DateTimeFormat("vi-VN", { dateStyle: "short", timeStyle: "short" }).format(new Date(value)) : "—";
}

export function AiKnowledgeSettingsCard() {
  const fileInput = useRef(null);
  const [documents, setDocuments] = useState([]);
  const [form, setForm] = useState({ title: "", description: "", file: null });
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [pendingDelete, setPendingDelete] = useState(null);

  const load = useCallback(async () => {
    try {
      setDocuments(await aiKnowledgeApi.list());
      setError("");
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Không thể tải kho tri thức AI."));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(load, 0);
    return () => clearTimeout(timer);
  }, [load]);

  async function upload(event) {
    event.preventDefault();
    if (!form.title.trim() || !form.file) return;
    setBusy("upload");
    setError("");
    setMessage("");
    try {
      const result = await aiKnowledgeApi.upload({ ...form, title: form.title.trim() });
      setDocuments((current) => [result, ...current.filter((item) => item.id !== result.id)]);
      setForm({ title: "", description: "", file: null });
      if (fileInput.current) fileInput.current.value = "";
      setMessage(`Đã trích xuất và chia ${result.chunkCount ?? 0} đoạn tri thức từ tài liệu.`);
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Không thể xử lý tài liệu."));
    } finally {
      setBusy("");
    }
  }

  async function toggle(document) {
    setBusy(document.id);
    setError("");
    try {
      const result = await aiKnowledgeApi.setEnabled(document.id, !document.enabled);
      setDocuments((current) => current.map((item) => item.id === result.id ? result : item));
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Không thể đổi trạng thái tài liệu."));
    } finally {
      setBusy("");
    }
  }

  async function reprocess(document) {
    setBusy(document.id);
    setError("");
    setMessage("");
    try {
      await aiKnowledgeApi.reprocess(document.id);
      await load();
      setMessage(`Đã chunk lại “${document.title}”.`);
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Không thể chunk lại tài liệu."));
    } finally {
      setBusy("");
    }
  }

  async function remove() {
    if (!pendingDelete) return;
    setBusy(pendingDelete.id);
    setError("");
    try {
      await aiKnowledgeApi.remove(pendingDelete.id);
      setDocuments((current) => current.filter((item) => item.id !== pendingDelete.id));
      setPendingDelete(null);
      setMessage("Đã xóa tài liệu và toàn bộ chunk liên quan.");
    } catch (requestError) {
      setError(getErrorMessage(requestError, "Không thể xóa tài liệu."));
    } finally {
      setBusy("");
    }
  }

  return (
    <article className="surface settings-card knowledge-settings-card">
      <span className="eyebrow">KHO TRI THỨC AI</span>
      <h2>Thêm tài liệu để AI chunking</h2>
      <p>Tài liệu được trích xuất, chia thành các đoạn có ngữ nghĩa và lập chỉ mục để trợ lý AI tra cứu.</p>
      <form className="knowledge-upload-form" onSubmit={upload}>
        <Input label="Tên tài liệu" name="knowledgeTitle" value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} placeholder="Ví dụ: Quy chế đào tạo 2026" required />
        <Input label="Mô tả" name="knowledgeDescription" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} placeholder="Phạm vi nội dung tài liệu" />
        <label className="field knowledge-file-field" htmlFor="knowledgeFile">
          <span>Tệp tài liệu</span>
          <input ref={fileInput} id="knowledgeFile" type="file" accept={acceptedExtensions} onChange={(event) => setForm((current) => ({ ...current, file: event.target.files?.[0] || null }))} required />
          <small>TXT, Markdown, PDF, Word, Excel hoặc PowerPoint · tối đa 10 MB</small>
        </label>
        <div className="settings-actions"><Button type="submit" loading={busy === "upload"} disabled={!form.title.trim() || !form.file}>Tải lên và chunking</Button></div>
      </form>
      {error && <p className="field-error" role="alert">{error}</p>}
      {message && <p className="settings-success" role="status">{message}</p>}
      <div className="knowledge-document-list" aria-busy={loading}>
        {loading && <p>Đang tải danh sách tài liệu…</p>}
        {!loading && documents.length === 0 && <div className="knowledge-empty"><strong>Chưa có tài liệu</strong><span>Thêm tài liệu đầu tiên để xây dựng ngữ cảnh cho trợ lý AI.</span></div>}
        {documents.map((document) => (
          <section className="knowledge-document" key={document.id}>
            <div className="knowledge-document-main">
              <div><strong>{document.title}</strong><span>{document.originalFileName}</span></div>
              <span className={`knowledge-state ${document.enabled ? "is-enabled" : ""}`}>{document.enabled ? "Đang sử dụng" : "Đã tắt"}</span>
            </div>
            <div className="knowledge-document-meta">
              <span>{document.chunkCount ?? 0} chunk</span><span>{fileSize(document.fileSize)}</span><span>Cập nhật {updatedAt(document.updatedAt || document.createdAt)}</span>
            </div>
            {document.processingError && <p className="field-error">{document.processingError}</p>}
            <div className="knowledge-document-actions">
              <Button type="button" variant="secondary" onClick={() => toggle(document)} loading={busy === document.id}>{document.enabled ? "Tạm tắt" : "Bật sử dụng"}</Button>
              <Button type="button" variant="secondary" onClick={() => reprocess(document)} disabled={Boolean(busy)}>Chunk lại</Button>
              <Button type="button" variant="danger" onClick={() => setPendingDelete(document)} disabled={Boolean(busy)}>Xóa</Button>
            </div>
          </section>
        ))}
      </div>
      <ConfirmDialog open={Boolean(pendingDelete)} title="Xóa tài liệu khỏi kho AI?" description={pendingDelete ? `Tài liệu “${pendingDelete.title}” và toàn bộ chunk sẽ bị xóa.` : ""} confirmLabel="Xóa tài liệu" danger loading={Boolean(pendingDelete && busy === pendingDelete.id)} onConfirm={remove} onClose={() => !busy && setPendingDelete(null)} />
    </article>
  );
}
