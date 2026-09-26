import {useCallback,useEffect,useMemo,useRef,useState} from 'react'
import {authStore} from '../../../stores/authStore'
import {api} from '../../../services/api/client'
import {supportChatApi} from '../api/supportChatApi'
import {connectNotificationSocket} from '../../../services/websocket/notificationSocket'
import {Icon} from '../../../components/ui'
import {notificationStore} from '../../notifications/store/notificationStore'
import '../support-chat.css'

const statusLabels={OPEN:'Đang mở',IN_PROGRESS:'Đang xử lý',RESOLVED:'Đã giải quyết',CLOSED:'Đã đóng'}

function initials(value){
 return String(value||'ND').trim().split(/\s+/).slice(-2).map(part=>part[0]).join('').toUpperCase()
}

function timeLabel(value){
 if(!value)return ''
 return new Intl.DateTimeFormat('vi-VN',{day:'2-digit',month:'2-digit',hour:'2-digit',minute:'2-digit'}).format(new Date(value))
}

export function HumanChatDropdown(){
 const {role,currentUser}=authStore.getSnapshot()
 const root=useRef(null),picker=useRef(null),messagesEnd=useRef(null),currentRef=useRef(null),openRef=useRef(false)
 const[open,setOpen]=useState(false),[contacts,setContacts]=useState([]),[list,setList]=useState([]),[current,setCurrent]=useState(null),[messages,setMessages]=useState([]),[text,setText]=useState(''),[file,setFile]=useState(null),[unread,setUnread]=useState(0),[error,setError]=useState(''),[busy,setBusy]=useState(false),[loading,setLoading]=useState(true)
 const isAdmin=role==='SYSTEM_ADMIN',isSubjectAdmin=role==='SUBJECT_ADMIN'
 const contactsById=useMemo(()=>new Map(contacts.map(contact=>[contact.userId,contact])),[contacts])

 const participant=useCallback(conversation=>{
  if(!isAdmin){const contact=contactsById.get(isSubjectAdmin?conversation?.createdByUserId:conversation?.assignedAdminId);return contact||{displayName:isSubjectAdmin?'Giảng viên':'Quản trị viên chuyên môn',avatarUrl:null,role:isSubjectAdmin?'USER':'SUBJECT_ADMIN',facultyId:conversation?.facultyId||currentUser?.facultyId}}
  return contactsById.get(conversation?.createdByUserId)||{displayName:`Người dùng ${String(conversation?.createdByUserId||'').slice(0,8)}`,avatarUrl:null,role:conversation?.createdByRole}
 },[contactsById,isAdmin])

 const load=useCallback(async()=>{
  setLoading(true)
  try{
   const [people,conversations,count]=await Promise.all([
    isAdmin?api.get('/api/v1/users/me/chat-contacts'):Promise.resolve([]),
    isAdmin?supportChatApi.adminList({size:30}):isSubjectAdmin?supportChatApi.assigned({size:20}):supportChatApi.mine({size:2}),
    supportChatApi.unread(),
   ])
   setContacts(people||[])
   setList(conversations?.content||[])
   setUnread(Number(count)||0)
   setError('')
  }catch(reason){setError(reason.message||'Không thể tải tin nhắn.')}
  finally{setLoading(false)}
 },[isAdmin])

 useEffect(()=>{currentRef.current=current},[current])
 useEffect(()=>{openRef.current=open},[open])
 useEffect(()=>{messagesEnd.current?.scrollIntoView({block:'end'})},[messages,busy])
 useEffect(()=>{
  queueMicrotask(()=>void load())
  const stop=connectNotificationSocket({onMessage:()=>{},onSupport:event=>{
   const message=event.message
   if(!message)return
   if(event.conversationId===currentRef.current?.id){
    setMessages(items=>items.some(item=>item.id===message.id)?items:[...items,message])
   }
   if(message.senderId!==authStore.getSnapshot().currentUser?.id&&(!openRef.current||event.conversationId!==currentRef.current?.id))setUnread(value=>value+1)
   void load()
  },onStatus:()=>{}})
  const outside=event=>{if(!root.current?.contains(event.target))setOpen(false)}
  document.addEventListener('pointerdown',outside)
  return()=>{stop();document.removeEventListener('pointerdown',outside)}
 },[load])

 async function openConversation(conversation){
  setCurrent(conversation);setMessages([]);setError('')
  try{
   const page=await supportChatApi.messages(conversation.id,{size:50})
   setMessages((page?.content||[]).reverse())
   await supportChatApi.read(conversation.id)
   setUnread(Number(await supportChatApi.unread())||0)
  }catch(reason){setError(reason.message||'Không thể tải cuộc trò chuyện.')}
 }

 async function startConversation(){
  const available=list.find(item=>item.status!=='CLOSED')
  if(available){await openConversation(available);return}
  setBusy(true);setError('')
  try{
   const contact=contacts[0]
   if(!contact){setError('Chưa có quản trị viên chuyên môn cùng khoa để chat.');return}
   const conversation=await supportChatApi.create(`Trao đổi với ${contact.displayName}`,'',contact.userId)
   setList(items=>[conversation,...items])
   await openConversation(conversation)
  }catch(reason){setError(reason.message||'Không thể bắt đầu cuộc trò chuyện.')}
  finally{setBusy(false)}
 }

 async function send(event){
  event.preventDefault()
  if(!current||busy||(!text.trim()&&!file))return
  const body=text
  setText('');setBusy(true);setError('')
  try{
   const message=await supportChatApi.send(current.id,body,file)
   setMessages(items=>items.some(item=>item.id===message.id)?items:[...items,message])
   setFile(null)
   notificationStore.pushToast({title:'Đã gửi tin nhắn',content:'Tin nhắn đã được gửi thành công.'})
  }catch(reason){setError(reason.message||'Không thể gửi tin nhắn.');setText(body)}
  finally{setBusy(false)}
 }

 async function updateStatus(value){
  try{
   const updated=await supportChatApi.status(current.id,value)
   setCurrent(updated)
   setList(items=>items.map(item=>item.id===updated.id?updated:item))
  }catch(reason){setError(reason.message||'Không thể cập nhật trạng thái.')}
 }

 const activeParticipant=current?participant(current):null
 return <div className="human-chat" ref={root}>
  <button type="button" className="human-chat-button" aria-label="Mở tin nhắn với quản trị viên" aria-expanded={open} onClick={()=>setOpen(value=>!value)}><Icon name="messages" size={19}/>{unread>0&&<b>{unread>99?'99+':unread}</b>}</button>
  {open&&<section className="human-chat-panel" aria-label="Tin nhắn hỗ trợ">
   {!current?<>
    <header className="human-chat-title"><div><strong>{isAdmin?'Tin nhắn người dùng':'Hỗ trợ trực tuyến'}</strong><small>{isAdmin?'Trao đổi trực tiếp với người dùng':'Trao đổi trực tiếp với quản trị viên'}</small></div><button type="button" onClick={()=>setOpen(false)} aria-label="Đóng">×</button></header>
    {!isAdmin&&!isSubjectAdmin&&<div className="human-chat-intro"><span className="human-avatar human-avatar-large">{contacts[0]?.avatarUrl?<img src={contacts[0].avatarUrl} alt=""/>:initials(contacts[0]?.displayName)}</span><div><strong>{contacts[0]?.displayName||'Quản trị viên chuyên môn'}</strong><p>{contacts[0]?.facultyId?`Khoa ${contacts[0].facultyId}`:'Quản trị viên cùng khoa'}</p></div><button type="button" className="human-chat-primary" disabled={busy||!contacts.length} onClick={()=>void startConversation()}>{list.length?'Tiếp tục trò chuyện':'Bắt đầu trò chuyện'}</button></div>}
    <div className="human-chat-list">
     {loading&&<p className="human-chat-state">Đang tải cuộc trò chuyện…</p>}
     {!loading&&list.map(conversation=>{const person=participant(conversation);return <button type="button" key={conversation.id} onClick={()=>void openConversation(conversation)}><span className="human-avatar">{person.avatarUrl?<img src={person.avatarUrl} alt=""/>:initials(person.displayName)}</span><span className="human-chat-list-copy"><strong>{person.displayName}</strong><small>{statusLabels[conversation.status]||conversation.status}<time>{timeLabel(conversation.lastMessageAt)}</time></small></span><Icon name="chevron" size={15} className="human-chat-chevron"/></button>})}
     {!loading&&isAdmin&&!list.length&&<p className="human-chat-state">Chưa có người dùng nhắn đến.</p>}
     {!loading&&isSubjectAdmin&&!list.length&&<p className="human-chat-state">Chưa có giảng viên cùng khoa nhắn đến.</p>}
    </div>
   </>:<>
    <header className="human-chat-conversation-head"><button type="button" onClick={()=>setCurrent(null)} aria-label="Quay lại">‹</button><span className="human-avatar">{activeParticipant.avatarUrl?<img src={activeParticipant.avatarUrl} alt=""/>:initials(activeParticipant.displayName)}</span><div><strong>{activeParticipant.displayName}</strong><small>{statusLabels[current.status]||current.status}</small></div><button type="button" onClick={()=>setOpen(false)} aria-label="Đóng">×</button></header>
    {isAdmin&&<div className="human-chat-status"><label htmlFor="support-status">Trạng thái</label><select id="support-status" value={current.status} onChange={event=>void updateStatus(event.target.value)}><option value="OPEN">Đang mở</option><option value="IN_PROGRESS">Đang xử lý</option><option value="RESOLVED">Đã giải quyết</option><option value="CLOSED">Đã đóng</option></select></div>}
    <div className="human-chat-messages" aria-live="polite">
     {!messages.length&&<p className="human-chat-state">Chưa có tin nhắn. Hãy bắt đầu cuộc trò chuyện.</p>}
     {messages.map(message=>{const mine=message.senderId===currentUser?.id;return <article key={message.id} className={mine?'mine':'theirs'}><small>{mine?'Bạn':activeParticipant.displayName}</small>{message.content&&<p>{message.content}</p>}{message.attachments?.map(attachment=><a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer"><img src={attachment.url} alt={attachment.fileName}/></a>)}<time>{timeLabel(message.createdAt)}</time></article>})}
     {busy&&<p className="human-chat-typing">Đang gửi…</p>}<span ref={messagesEnd}/>
    </div>
    <form className="human-chat-composer" onSubmit={send}>
     {file&&<div className="human-chat-attachment"><span>{file.name}</span><button type="button" onClick={()=>setFile(null)} aria-label="Xóa ảnh">×</button></div>}
     <div className="human-chat-input"><textarea value={text} onChange={event=>setText(event.target.value)} onPaste={event=>{const item=[...(event.clipboardData?.items||[])].find(entry=>entry.type.startsWith('image/'));if(item){event.preventDefault();setFile(item.getAsFile())}}} onKeyDown={event=>{if(event.key==='Enter'&&!event.shiftKey&&!event.isComposing){event.preventDefault();void send(event)}}} placeholder="Nhập tin nhắn…" aria-label="Nội dung tin nhắn" rows="2"/><button type="button" onClick={()=>picker.current?.click()} aria-label="Đính kèm ảnh"><Icon name="paperclip" size={18}/></button><input ref={picker} hidden type="file" accept="image/png,image/jpeg,image/webp" onChange={event=>setFile(event.target.files?.[0]||null)}/><button type="submit" className="human-chat-send" disabled={busy||(!text.trim()&&!file)} aria-label="Gửi tin nhắn">➤</button></div>
    </form>
   </>}
   {error&&<p className="support-error" role="alert">{error}</p>}
  </section>}
 </div>
}
