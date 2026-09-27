import { useEffect, useState } from 'react'
import { Button, Input } from '../../../components/ui'
import { PageHeader } from '../../../components/shared/PageHeader'
import { toast } from '../store/notificationStore'
import { telegramApi } from '../api/telegramApi'
import { useAuth } from '../../auth/hooks/useAuth'

const adminKeys = ['newUsers', 'actionable', 'systemEvents', 'loginEvents']
const subjectKeys = ['questionPending', 'questionResubmitted', 'subjectEvents']
const labels = {
  newUsers: 'Người dùng mới', actionable: 'Yêu cầu cần xử lý', systemEvents: 'Thông báo hệ thống', loginEvents: 'Đăng nhập mới',
  questionPending: 'Câu hỏi chờ phê duyệt', questionResubmitted: 'Câu hỏi gửi lại', subjectEvents: 'Thông báo chuyên môn',
}

export function TelegramPage() {
  const { role } = useAuth()
  const [status, setStatus] = useState(null), [prefs, setPrefs] = useState(null), [link, setLink] = useState(null)
  const [admin, setAdmin] = useState(null), [token, setToken] = useState(''), [username, setUsername] = useState(''), [busy, setBusy] = useState('')
  useEffect(() => { Promise.all([telegramApi.status(), telegramApi.preferences(), role === 'SYSTEM_ADMIN' ? telegramApi.admin() : Promise.resolve(null)]).then(([s, p, a]) => { setStatus(s); setPrefs(p); if (a) { setAdmin(a); setUsername(a.botUsername || '') } }).catch(() => toast.error('Không thể tải tích hợp Telegram.')) }, [role])
  async function createLink() { setBusy('link'); try { setLink(await telegramApi.link()); toast.success('Đã tạo liên kết Telegram.') } catch { toast.error('Không thể tạo liên kết Telegram.') } finally { setBusy('') } }
  async function savePreferences() { setBusy('prefs'); try { await telegramApi.savePreferences(prefs); toast.success('Đã lưu tùy chọn thông báo.') } catch { toast.error('Không thể lưu tùy chọn Telegram.') } finally { setBusy('') } }
  async function saveAdmin() { setBusy('admin'); try { const value = await telegramApi.saveAdmin({ botUsername: username, botToken: token || undefined, enabled: true }); setAdmin(value); setToken(''); toast.success('Đã lưu cấu hình Telegram.') } catch { toast.error('Không thể lưu cấu hình Telegram.') } finally { setBusy('') } }
  async function testAdmin() { setBusy('admin-test'); try { await telegramApi.testAdmin(); toast.success('Kết nối bot hoạt động bình thường.') } catch { toast.error('Không thể kết nối Telegram.') } finally { setBusy('') } }
  async function unlink() { try { await telegramApi.unlink(); setStatus({ connected: false }); toast.success('Đã ngắt liên kết Telegram.') } catch { toast.error('Không thể ngắt liên kết Telegram.') } }
  const keys = role === 'SYSTEM_ADMIN' ? adminKeys : subjectKeys
  return <section><PageHeader title="Tích hợp Telegram" description="Nhận thông báo HAU QM trực tiếp trên Telegram khi có công việc cần xử lý." /><div className="telegram-grid">
    <article className="surface telegram-hero"><div className="telegram-icon">✈</div><h2>Telegram Notifications</h2><p>Liên kết an toàn bằng nút START. HAU QM không yêu cầu nhập thủ công Telegram Chat ID.</p>{status?.connected ? <><p className="telegram-connected">✓ Telegram đã được liên kết</p><p>@{status.username || 'người dùng Telegram'}</p><div className="settings-actions"><Button variant="secondary" loading={busy === 'test'} onClick={async () => { setBusy('test'); try { await telegramApi.test(); toast.success('Đã gửi thông báo thử.') } catch { toast.error('Không thể gửi thông báo thử.') } finally { setBusy('') } }}>Gửi thông báo thử</Button><Button variant="ghost" onClick={unlink}>Ngắt liên kết</Button></div></> : <><p>Telegram chưa được liên kết.</p><Button loading={busy === 'link'} onClick={createLink}>Tạo liên kết Telegram</Button>{link && <p className="telegram-link"><a href={link.url} target="_blank" rel="noreferrer">Mở HAU QM Bot</a><small>Liên kết có hiệu lực trong 10 phút.</small></p>}</>}</article>
    <article className="surface telegram-guide"><span className="eyebrow">HƯỚNG DẪN</span><h2>Liên kết trong 5 bước</h2><ol><li>Mở Telegram.</li><li>Bấm “Mở HAU QM Bot”.</li><li>Nhấn START.</li><li>Liên kết tài khoản HAU QM.</li><li>Quay lại trang này để kiểm tra trạng thái.</li></ol></article>
    <article className="surface telegram-preferences"><span className="eyebrow">THÔNG BÁO MUỐN NHẬN</span><h2>Tùy chọn Telegram</h2>{prefs && keys.map((key) => <label className="settings-toggle" key={key}><input type="checkbox" checked={Boolean(prefs[key])} onChange={(e) => setPrefs({ ...prefs, [key]: e.target.checked })} /><span>{labels[key]}</span></label>)}<Button loading={busy === 'prefs'} disabled={!prefs} onClick={savePreferences}>Lưu tùy chọn thông báo</Button></article>
    {role === 'SYSTEM_ADMIN' && <article className="surface telegram-admin"><span className="eyebrow">CẤU HÌNH TELEGRAM BOT</span><h2>Bot HAU QM</h2><Input label="Bot username" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="@hau_qm_bot" /><Input label="Bot token" type="password" value={token} onChange={(e) => setToken(e.target.value)} placeholder={admin?.tokenConfigured ? 'Đã cấu hình · nhập mới để thay đổi' : 'Nhập token qua secret manager'} /><small>Token chỉ được gửi khi lưu và không bao giờ trả về trình duyệt.</small><div className="settings-actions"><Button loading={busy === 'admin'} disabled={!username.trim()} onClick={saveAdmin}>Lưu cấu hình bot</Button><Button variant="secondary" loading={busy === 'admin-test'} onClick={testAdmin}>Kiểm tra kết nối</Button></div></article>}
  </div></section>
}
