import { useCallback, useEffect, useState } from 'react'
import { PageHeader } from '../../../components/shared/PageHeader'
import { Button, Input, Select } from '../../../components/ui'
import { PasswordInput } from '../../auth/components/PasswordInput'
import { api } from '../../../services/api/client'
import { toast } from '../../notifications/store/notificationStore'
import { AiKnowledgeSettingsCard } from '../components/AiKnowledgeSettingsCard'

const initial = { provider: 'GEMINI', model: 'gemini-2.5-flash', apiKey: '', persona: 'FRIENDLY', personaInstructions: '' }

export function KuteAdminPage() {
  const [form, setForm] = useState(initial)
  const [meta, setMeta] = useState({ status: 'NOT_CONFIGURED', apiKeyConfigured: false })
  const [busy, setBusy] = useState('load')
  const [error, setError] = useState('')
  const load = useCallback(async () => {
    const value = await api.get('/api/v1/admin/ai-settings')
    setMeta(value)
    setForm(current => ({ ...current, provider: value.provider || current.provider, model: value.model || current.model, persona: value.persona || 'FRIENDLY', personaInstructions: value.personaInstructions || '', apiKey: '' }))
  }, [])
  useEffect(() => { const timer = setTimeout(() => load().catch(reason => setError(reason.message)).finally(() => setBusy('')), 0); return () => clearTimeout(timer) }, [load])
  async function save(section) {
    if (!form.model.trim()) return
    if (form.persona === 'CUSTOM' && !form.personaInstructions.trim()) { setError('Persona tùy chỉnh cần có hướng dẫn.'); return }
    setBusy(section); setError('')
    try {
      const value = await api.put('/api/v1/admin/ai-settings', { provider: form.provider, model: form.model.trim(), apiKey: form.apiKey || undefined, persona: form.persona, personaInstructions: form.personaInstructions || undefined })
      setMeta(value); setForm(current => ({ ...current, apiKey: '' })); toast.success('Đã lưu cấu hình Kute.')
    } catch (reason) { setError(reason.message || 'Không thể lưu cấu hình Kute.') }
    finally { setBusy('') }
  }
  async function test() {
    setBusy('test'); setError('')
    try { const value = await api.post('/api/v1/admin/ai-settings/test', {}); setMeta(value); value.status === 'WORKING' ? toast.success('Kết nối AI thành công.') : toast.warning('Provider chưa sẵn sàng.') }
    catch (reason) { setError(reason.message || 'Không thể kiểm tra provider.') }
    finally { setBusy('') }
  }
  return <section className="admin-settings kute-admin-page">
    <PageHeader title="Trợ lý Kute" description="Cấu hình model thực thi, kho tri thức RAG và persona của Kute tại một nơi." />
    {error && <p className="editor-error" role="alert">{error}</p>}
    <div className="settings-grid">
      <article className="surface settings-card">
        <span className="eyebrow">MODEL</span><h2>Provider và model runtime</h2>
        <p>Trạng thái: <strong>{meta.status}</strong>{meta.lastErrorCode ? ` · ${meta.lastErrorCode}` : ''}</p>
        <div className="settings-form">
          <Select label="Nhà cung cấp" value={form.provider} options={[{ value: 'GEMINI', label: 'Google Gemini' }, { value: 'OPENAI', label: 'OpenAI' }, { value: 'MISTRAL', label: 'Mistral' }]} onChange={event => setForm({ ...form, provider: event.target.value })} />
          <Input label="Model" value={form.model} onChange={event => setForm({ ...form, model: event.target.value })} />
          <PasswordInput label="API key" value={form.apiKey} onChange={event => setForm({ ...form, apiKey: event.target.value })} placeholder={meta.apiKeyConfigured ? '•••••••••••• · nhập mới để thay đổi' : 'Nhập API key'} />
          <small>Secret chỉ được gửi để lưu mã hóa và không được trả lại frontend.</small>
          <div className="settings-actions"><Button variant="secondary" loading={busy === 'test'} disabled={Boolean(busy)} onClick={test}>Kiểm tra</Button><Button loading={busy === 'model'} disabled={Boolean(busy)} onClick={() => save('model')}>Lưu model</Button></div>
        </div>
      </article>
      <article className="surface settings-card">
        <span className="eyebrow">PERSONA</span><h2>Phong cách trả lời</h2>
        <div className="settings-form">
          <Select label="Persona" value={form.persona} options={[{ value: 'FRIENDLY', label: 'Thân thiện' }, { value: 'FORMAL', label: 'Học thuật trang trọng' }, { value: 'CONCISE', label: 'Ngắn gọn, hành động' }, { value: 'CUSTOM', label: 'Tùy chỉnh' }]} onChange={event => setForm({ ...form, persona: event.target.value })} />
          <label className="field"><span>Hướng dẫn persona</span><textarea rows="7" maxLength="4000" disabled={form.persona !== 'CUSTOM'} value={form.personaInstructions} onChange={event => setForm({ ...form, personaInstructions: event.target.value })} placeholder="Mô tả giọng điệu và cách trả lời mong muốn" /></label>
          <Button loading={busy === 'persona'} disabled={Boolean(busy)} onClick={() => save('persona')}>Lưu persona</Button>
        </div>
      </article>
      <div className="kute-knowledge-section"><AiKnowledgeSettingsCard /></div>
    </div>
  </section>
}
