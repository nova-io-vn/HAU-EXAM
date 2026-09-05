export function AuthAlert({children,tone='danger'}){if(!children)return null;return <div className={`auth-alert auth-alert-${tone}`} role={tone==='danger'?'alert':'status'}>{children}</div>}
