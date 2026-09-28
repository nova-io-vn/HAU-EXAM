import {useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {Button} from '../../components/ui'
import {PageHeader} from '../../components/shared/PageHeader'
import {systemHelpApi} from './systemHelpApi'

const suggestions=['Cách tạo câu hỏi?','Cách gửi câu hỏi để duyệt?','Cách sử dụng AI?','Cách quản lý thông báo?']
const routesByKey={DASHBOARD:'/dashboard',PROFILE:'/profile',NOTIFICATIONS:'/notifications',QUESTION_CREATE:'/questions/new',MY_QUESTIONS:'/questions/mine',AI_GENERATE:'/ai/generate',AI_DOCUMENTS:'/ai/documents',QUESTION_REVIEW:'/review',SUBJECTS:'/subjects',EXAM_MATRICES:'/exam-matrices',USERS:'/admin/users',FACULTIES:'/admin/faculties',SYSTEM_SETTINGS:'/admin/settings',CONTACT_REQUESTS:'/admin/contact'}

export function SystemHelpPage(){
  const navigate=useNavigate();const[messages,setMessages]=useState([]);const[input,setInput]=useState('');const[loading,setLoading]=useState(false);const[error,setError]=useState('')
  async function send(value=input){const message=value.trim();if(!message||loading)return;setInput('');setError('');setLoading(true);setMessages(current=>[...current,{role:'USER',content:message}]);try{const response=await systemHelpApi.ask(message);setMessages(current=>[...current,{role:'ASSISTANT',content:response.answer,actions:response.actions||[]}])}catch(reason){setError(reason.message||'Không thể nhận hướng dẫn lúc này.')}finally{setLoading(false)}}
  function act(routeKey){const route=routesByKey[routeKey];if(route)navigate(route)}
  return <section className="system-help-page"><PageHeader title="Kute" description="Trợ lý AI HAU QM hỗ trợ theo đúng vai trò của bạn."/><div className="surface system-help-panel"><div className="system-help-messages" aria-live="polite">{!messages.length&&<div className="system-help-welcome"><strong>Xin chào, mình là Kute ✨</strong><p>Mình có thể hướng dẫn quy trình HAU QM và trả lời các câu hỏi kiến thức phổ thông an toàn.</p><div>{suggestions.map(item=><button type="button" key={item} onClick={()=>send(item)}>{item}</button>)}</div></div>}{messages.map((message,index)=><article key={`${message.role}-${index}`} className={`system-help-message ${message.role.toLowerCase()}`}><small>{message.role==='USER'?'Bạn':'Kute'}</small><p>{message.content}</p>{message.actions?.map(action=>routesByKey[action.routeKey]&&<Button key={action.routeKey} variant="secondary" onClick={()=>act(action.routeKey)}>{action.label}</Button>)}</article>)}{loading&&<p className="system-help-loading">Kute đang chuẩn bị câu trả lời…</p>}</div><form className="system-help-composer" onSubmit={event=>{event.preventDefault();send()}}><label><span className="sr-only">Câu hỏi cho Kute</span><textarea rows="3" maxLength="1000" value={input} onChange={event=>setInput(event.target.value)} placeholder="Hỏi Kute..."/></label><Button type="submit" loading={loading} disabled={loading||!input.trim()}>Gửi</Button></form>{error&&<p className="ai-error" role="alert">{error}</p>}</div></section>
}
