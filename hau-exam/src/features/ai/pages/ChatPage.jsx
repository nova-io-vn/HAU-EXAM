import {useRef,useState} from 'react'
import {Link} from 'react-router-dom'
import {Button} from '../../../components/ui'
import {DocumentPicker} from '../components/DocumentPicker'
import {AiError,AiJobPanel} from '../components/AiShared'
import {aiApi} from '../api/aiApi'

export function ChatPage(){
  const [documentId,setDocumentId]=useState('')
  const [message,setMessage]=useState('')
  const [turns,setTurns]=useState([])
  const [busy,setBusy]=useState(false)
  const [error,setError]=useState(null)
  const lock=useRef(false)
  async function send(event){event.preventDefault();if(lock.current||!message.trim())return;lock.current=true;setBusy(true);setError(null);const text=message.trim();try{const job=await aiApi.chat({documentId:documentId||null,message:text});setTurns(previous=>[...previous,{message:text,jobId:job.jobId}]);setMessage('')}catch(reason){setError(reason)}finally{lock.current=false;setBusy(false)}}
  return <div className="ai-stack"><section className="editor-section"><h2>Ngữ cảnh trò chuyện</h2><DocumentPicker optional value={documentId} onChange={setDocumentId} disabled={busy}/><p>Mỗi câu hỏi dùng tài liệu đã chọn và nội dung tin nhắn. Lịch sử hội thoại không được gửi tự động làm ngữ cảnh.</p></section>
    <section className="ai-conversation" aria-label="Hội thoại">{!turns.length?<p>Đặt câu hỏi về học liệu để bắt đầu.</p>:turns.map(turn=><article className="editor-section" key={turn.jobId}><h3>Bạn</h3><p className="ai-message">{turn.message}</p><h3>Trợ lý học liệu</h3><AiJobPanel id={turn.jobId}/><Link to={`/ai/jobs/${turn.jobId}`}>Mở job hội thoại</Link></article>)}</section>
    <form className="editor-section ai-composer" onSubmit={send}><div className="field"><label htmlFor="ai-message">Tin nhắn</label><textarea id="ai-message" rows={4} maxLength={4000} required disabled={busy} value={message} onChange={event=>setMessage(event.target.value)}/><small>{message.length}/4000 ký tự</small></div>{error&&<AiError error={error}/>}<Button type="submit" loading={busy} disabled={busy||!message.trim()}>Gửi câu hỏi</Button></form>
  </div>
}
