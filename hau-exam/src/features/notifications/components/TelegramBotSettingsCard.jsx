import { useEffect, useState } from 'react'
import { Button, Input } from '../../../components/ui'
import { telegramApi } from '../api/telegramApi'
import { toast } from '../store/notificationStore'

export function TelegramBotSettingsCard() {
  const [state, setState] = useState(null), [username, setUsername] = useState(''), [token, setToken] = useState(''), [busy, setBusy] = useState(false)
  useEffect(() => { telegramApi.admin().then(value => { setState(value); setUsername(value?.botUsername || '') }).catch(() => {}) }, [])
  async function save() { setBusy(true); try { const value = await telegramApi.saveAdmin({ botUsername: username, botToken: token || undefined, enabled: true }); setState(value); setToken(''); toast.success('Đã lưu cấu hình Telegram.') } catch { toast.error('Không thể lưu cấu hình Telegram.') } finally { setBusy(false) } }
  async function test() { setBusy(true); try { await telegramApi.testAdmin(); toast.success('Webhook/bot hoạt động bình thường.') } catch { toast.error('Không thể kiểm tra Telegram.') } finally { setBusy(false) } }
  return <article className="surface settings-card"><span className="eyebrow">INTEGRATIONS</span><h2>Telegram Bot</h2><p>Trạng thái: {state?.configured ? 'Đã cấu hình' : 'Chưa cấu hình'} · Webhook: {state?.webhookStatus || '—'}</p><Input label="Bot username" value={username} onChange={e => setUsername(e.target.value)} placeholder="@hau_qm_bot" /><Input label="Token" type="password" value={token} onChange={e => setToken(e.target.value)} placeholder={state?.configured ? 'Đã cấu hình · nhập mới để thay đổi' : 'Nhập token mới'} /><small className="settings-note">Token không được trả về frontend. Để trống khi muốn giữ token hiện tại.</small><div className="settings-actions"><Button onClick={save} loading={busy} disabled={!username.trim()}>Lưu cấu hình</Button><Button variant="secondary" onClick={test} loading={busy}>Kiểm tra kết nối</Button></div></article>
}
