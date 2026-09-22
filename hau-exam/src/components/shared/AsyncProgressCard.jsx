import {useElapsedTime} from '../../hooks/useElapsedTime'
import {formatElapsedTime,realProgress} from '../../utils/asyncProgress'
import {Button,StatusBadge} from '../ui'

export function AsyncProgressCard({title,status,progress,startedAt,completedAt,currentStep,steps,message,canLeave=false,onCancel,onRetry,compact=false}){
  const active=['PENDING','PROCESSING','RETRYING'].includes(status)
  const elapsed=useElapsedTime(startedAt,completedAt,active)
  const percent=realProgress(progress)
  return <article className={`async-progress-card ${compact?'async-progress-card-compact':''} status-${String(status||'').toLowerCase()}`} aria-live="polite">
    <header><div><h3>{title}</h3>{currentStep&&<span className="async-step-badge">{currentStep}</span>}</div><StatusBadge status={status}/></header>
    {message&&<p className="async-progress-message">{message}</p>}
    {active&&<div className="async-progress-track" role="progressbar" aria-label={percent===null?'Tiến trình không xác định':'Tiến trình xử lý'} {...(percent===null?{}:{'aria-valuenow':percent,'aria-valuemin':0,'aria-valuemax':100})}><span className={percent===null?'is-indeterminate':''} style={percent===null?undefined:{width:`${percent}%`}}/></div>}
    {steps?.length>0&&<ol className="async-steps">{steps.map(step=><li key={step.id||step.label} className={`async-step-${step.status||'PENDING'}`}><span aria-hidden="true">{step.status==='COMPLETED'?'✓':step.status==='PROCESSING'?'●':'○'}</span>{step.label}</li>)}</ol>}
    <footer><span>{status==='COMPLETED'?'Hoàn tất sau':'Đã xử lý'} <strong>{formatElapsedTime(elapsed)}</strong></span>{canLeave&&active&&<span>Bạn có thể rời trang; tác vụ vẫn tiếp tục.</span>}{percent!==null&&active&&<strong>{Math.round(percent)}%</strong>}</footer>
    {(onCancel||onRetry)&&<div className="async-progress-actions">{onCancel&&active&&<Button variant="secondary" onClick={onCancel}>Hủy</Button>}{onRetry&&['FAILED','CANCELLED'].includes(status)&&<Button variant="secondary" onClick={onRetry}>Thử lại</Button>}</div>}
  </article>
}
