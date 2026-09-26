import {useEffect,useRef,useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {systemHelpApi} from '../../help/systemHelpApi'
import {Icon} from '../../../components/ui'
import './ai-assistant.css'

const quick=['Hướng dẫn tạo câu hỏi','Cách tạo câu hỏi từ tài liệu','Xem trạng thái câu hỏi','Quản lý thông báo']
const routesByKey={PROFILE:'/profile',NOTIFICATIONS:'/notifications',QUESTION_CREATE:'/questions/new',MY_QUESTIONS:'/questions/mine',AI_GENERATE:'/ai/generate',AI_DOCUMENTS:'/ai/documents',QUESTION_REVIEW:'/review',SUBJECTS:'/subjects',EXAM_MATRICES:'/exam-matrices',USERS:'/admin/users',FACULTIES:'/admin/faculties',SYSTEM_SETTINGS:'/admin/settings',CONTACT_REQUESTS:'/admin/contact'}

export function AiAssistantWidget(){
 const navigate=useNavigate()
 const[open,setOpen]=useState(false),[input,setInput]=useState(''),[messages,setMessages]=useState([]),[busy,setBusy]=useState(false),[error,setError]=useState('')
 const inputRef=useRef(null),endRef=useRef(null)
 useEffect(()=>{if(open)inputRef.current?.focus()},[open])
 useEffect(()=>{endRef.current?.scrollIntoView({block:'end'})},[messages,busy])
 async function send(value=input){
  const question=value.trim()
  if(!question||busy)return
  setInput('');setBusy(true);setError('')
  setMessages(items=>[...items,{role:'USER',content:question}])
  try{
   const response=await systemHelpApi.ask(question)
   setMessages(items=>[...items,{role:'ASSISTANT',content:response.answer||'Tôi chưa nhận được nội dung phản hồi.',actions:response.actions||[]}])
  }catch(reason){setError(reason.message||'Không thể nhận phản hồi từ trợ lý. Vui lòng thử lại.')}
  finally{setBusy(false)}
 }
 function act(action){const route=routesByKey[action.routeKey];if(route){navigate(route);setOpen(false)}}
 return <div className="ai-assistant-widget">
  <button type="button" className="ai-assistant-fab" aria-label="Mở Trợ lý HAU QM" aria-expanded={open} onClick={()=>setOpen(value=>!value)}><Icon name="sparkles" size={23}/></button>
  {open&&<section className="ai-assistant-window" aria-label="Trợ lý HAU QM">
   <header><span className="ai-assistant-mark"><Icon name="sparkles" size={19}/></span><div><strong>Trợ lý HAU QM</strong><span>Hướng dẫn sử dụng theo vai trò của bạn</span></div><button type="button" aria-label="Đóng trợ lý" onClick={()=>setOpen(false)}>×</button></header>
   <div className="ai-assistant-messages" aria-live="polite">
    {!messages.length&&<div className="ai-assistant-welcome"><strong>Xin chào, tôi có thể hỗ trợ gì?</strong><p>Hỏi về cách sử dụng chức năng, tài liệu, câu hỏi và quy trình duyệt trong HAU QM.</p><div>{quick.map(question=><button type="button" key={question} onClick={()=>void send(question)}>{question}</button>)}</div></div>}
    {messages.map((message,index)=><article key={`${message.role}-${index}`} className={message.role.toLowerCase()}><small>{message.role==='USER'?'Bạn':'Trợ lý HAU QM'}</small><p>{message.content}</p>{message.actions?.map(action=>routesByKey[action.routeKey]&&<button type="button" className="ai-assistant-action" key={`${action.routeKey}-${action.label}`} onClick={()=>act(action)}>{action.label}</button>)}</article>)}
    {busy&&<p className="ai-assistant-typing">Trợ lý đang chuẩn bị câu trả lời…</p>}<span ref={endRef}/>
   </div>
   <form onSubmit={event=>{event.preventDefault();void send()}}><textarea ref={inputRef} value={input} maxLength={1000} onChange={event=>setInput(event.target.value)} placeholder="Nhập câu hỏi về HAU QM…" aria-label="Câu hỏi cho trợ lý" rows="2" onKeyDown={event=>{if(event.key==='Enter'&&!event.shiftKey&&!event.isComposing){event.preventDefault();void send()}}}/><button type="submit" disabled={busy||!input.trim()} aria-label="Gửi câu hỏi">➤</button></form>
   {error&&<p className="ai-assistant-error" role="alert">{error}</p>}
  </section>}
 </div>
}
