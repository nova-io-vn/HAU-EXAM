import { useState } from 'react'
import { useAuth } from '../../features/auth/hooks/useAuth'
import { CURRENT_WHATS_NEW, hasSeenWhatsNew, markWhatsNewSeen } from '../../features/onboarding/whatsNew'
import { Button, Dialog } from '../ui'

export function WhatsNewModal() {
  const { currentUser, bootstrapping } = useAuth()
  const [closedVersion, setClosedVersion] = useState('')
  const [dismiss, setDismiss] = useState(false)
  const userId = currentUser?.id

  const instance = `${userId || ''}:${CURRENT_WHATS_NEW.version}`
  const open = Boolean(!bootstrapping && userId && CURRENT_WHATS_NEW.enabled && closedVersion !== instance && !hasSeenWhatsNew(userId))

  function close({ seen = false, startGuide = false } = {}) {
    if (seen || dismiss || startGuide) markWhatsNewSeen(userId)
    setClosedVersion(instance)
    window.dispatchEvent(new CustomEvent('hau:whats-new-closed', { detail: { startGuide } }))
    if (startGuide) window.setTimeout(() => window.dispatchEvent(new Event('hau:restart-onboarding')), 120)
  }

  return (
    <Dialog
      open={open}
      size="large"
      title="MỚI TRÊN HAU EXAM"
      subtitle={`Phiên bản ${CURRENT_WHATS_NEW.version}`}
      onClose={() => close({ seen: dismiss })}
      footer={<><Button variant="secondary" onClick={() => close({ seen: true })}>Bỏ qua</Button><Button onClick={() => close({ seen: true, startGuide: true })}>Bắt đầu hướng dẫn</Button></>}
    >
      <div className="whats-new-layout">
        <div className="whats-new-visual">
          <img src={CURRENT_WHATS_NEW.image} alt="Minh họa động cho các tính năng mới của HAU Exam" width="372" height="346" />
        </div>
        <div className="whats-new-content">
          <span className="eyebrow">CẬP NHẬT SẢN PHẨM</span>
          <h3>{CURRENT_WHATS_NEW.title}</h3>
          <p>{CURRENT_WHATS_NEW.subtitle}</p>
          <ol>
            {CURRENT_WHATS_NEW.items.map(([title, description], index) => (
              <li key={title}><b>{index + 1}</b><span><strong>{title}</strong><small>{description}</small></span></li>
            ))}
          </ol>
          <label className="whats-new-dismiss"><input type="checkbox" checked={dismiss} onChange={(event) => setDismiss(event.target.checked)} /><span>Không hiển thị lại thông báo này</span></label>
        </div>
      </div>
    </Dialog>
  )
}
