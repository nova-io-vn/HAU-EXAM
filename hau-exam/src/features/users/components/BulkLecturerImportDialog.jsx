import { useRef, useState } from 'react'
import { Button, Dialog } from '../../../components/ui'
import { usersApi } from '../api/usersApi'
import { toast } from '../../notifications/store/notificationStore'

export function BulkLecturerImportDialog({ open, onClose, onImported }) {
  const input = useRef(null)
  const [file, setFile] = useState(null)
  const [preview, setPreview] = useState(null)
  const [busy, setBusy] = useState('')
  const [error, setError] = useState('')
  function choose(value) {
    setPreview(null); setError('')
    if (value && !value.name.toLowerCase().endsWith('.xlsx')) { setFile(null); setError('Chỉ chấp nhận tệp .xlsx.'); if (input.current) input.current.value = ''; return }
    setFile(value)
  }
  async function previewFile() {
    if (!file) return
    setBusy('preview'); setError('')
    try { setPreview(await usersApi.previewImport(file)) }
    catch (reason) { setError(reason.message || 'Không thể đọc tệp Excel.') }
    finally { setBusy('') }
  }
  async function confirm() {
    if (!file || !preview || preview.errorCount || preview.duplicateCount) return
    setBusy('confirm'); setError('')
    try { const result = await usersApi.confirmImport(file); toast.success(`Đã nhập ${result.importedCount} giảng viên ở trạng thái chờ duyệt.`); onImported?.(); close() }
    catch (reason) { setError(reason.message || 'Không thể nhập giảng viên.') }
    finally { setBusy('') }
  }
  async function downloadTemplate() {
    setBusy('template')
    try { const blob = await usersApi.importTemplate(); const url = URL.createObjectURL(blob); const anchor = document.createElement('a'); anchor.href = url; anchor.download = 'hau-lecturer-import-template.xlsx'; anchor.click(); URL.revokeObjectURL(url) }
    catch (reason) { setError(reason.message || 'Không thể tải file mẫu.') }
    finally { setBusy('') }
  }
  function close() { if (busy) return; setFile(null); setPreview(null); setError(''); if (input.current) input.current.value = ''; onClose() }
  return <Dialog open={open} size="large" title="Nhập giảng viên từ Excel" subtitle="Xem trước và sửa toàn bộ lỗi trước khi xác nhận; hệ thống không nhập một phần." onClose={close} footer={<><Button variant="secondary" disabled={Boolean(busy)} onClick={close}>Hủy</Button><Button loading={busy === 'confirm'} disabled={!preview || preview.errorCount > 0 || preview.duplicateCount > 0 || Boolean(busy)} onClick={confirm}>Xác nhận import</Button></>}>
    <div className="bulk-import-toolbar"><Button variant="secondary" loading={busy === 'template'} disabled={Boolean(busy)} onClick={downloadTemplate}>Tải file mẫu</Button><label className="field"><span>File .xlsx</span><input ref={input} type="file" accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" onChange={event => choose(event.target.files?.[0] || null)} /></label><Button loading={busy === 'preview'} disabled={!file || Boolean(busy)} onClick={previewFile}>Kiểm tra dữ liệu</Button></div>
    {error && <p className="editor-error" role="alert">{error}</p>}
    {preview && <><div className="bulk-import-summary"><strong>Tổng: {preview.total}</strong><span>Hợp lệ: {preview.validCount}</span><span>Lỗi: {preview.errorCount}</span><span>Trùng: {preview.duplicateCount}</span></div><div className="bulk-import-preview"><table><thead><tr><th>Dòng</th><th>Mã GV</th><th>Họ tên</th><th>Email</th><th>Khoa</th><th>Kết quả</th></tr></thead><tbody>{preview.rows.map(row => <tr key={row.rowNumber} className={row.status === 'VALID' ? '' : 'has-error'}><td>{row.rowNumber}</td><td>{row.lecturerCode || '—'}</td><td>{row.fullName || '—'}</td><td>{row.email || '—'}</td><td>{row.facultyCode || '—'}</td><td><strong>{row.status}</strong>{row.errors?.length > 0 && <small>{row.errors.join('; ')}</small>}</td></tr>)}</tbody></table></div></>}
    <p className="form-note">Tài khoản được tạo với role USER và trạng thái PENDING_APPROVAL. Sau khi được duyệt, giảng viên dùng luồng Quên mật khẩu hiện có để đặt mật khẩu; mật khẩu plaintext không đi qua RabbitMQ hoặc log.</p>
  </Dialog>
}
