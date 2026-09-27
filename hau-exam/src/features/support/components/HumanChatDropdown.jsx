/* eslint-disable react-hooks/set-state-in-effect */
import {useCallback,useEffect,useMemo,useRef,useState} from 'react'
import {useAuth} from '../../auth/hooks/useAuth'
import {api} from '../../../services/api/client'
import {connectNotificationSocket} from '../../../services/websocket/notificationSocket'
import {Icon,Button} from '../../../components/ui'
import {toast} from '../../notifications/store/notificationStore'
import {supportChatApi} from '../api/supportChatApi'
import '../support-chat.css'

const roleLabels={SYSTEM_ADMIN:'Quản trị viên hệ thống',SUBJECT_ADMIN:'Quản trị viên chuyên môn',USER:'Giảng viên'}
function identity(contact){return contact?.displayName?.trim()||contact?.fullName?.trim()||contact?.lecturerCode||'Tài khoản HAU'}
function initials(value){return String(value||'HAU').trim().split(/\s+/).slice(-2).map(part=>part[0]).join('').toUpperCase()}
function time(value){return value?new Intl.DateTimeFormat('vi-VN',{day:'2-digit',month:'2-digit',hour:'2-digit',minute:'2-digit'}).format(new Date(value)):''}
function subtitle(contact,fallbackFaculty){const faculty=contact?.facultyId||fallbackFaculty;return `${roleLabels[contact?.role]||'Thành viên'}${faculty?` · Khoa ${faculty}`:''}`}

export function HumanChatDropdown(){
 const {role,currentUser,facultyId}=useAuth()
 const root=useRef(null)
 const[current,setCurrent]=useState(null)
 const[open,setOpen]=useState(false)
 const[contacts,setContacts]=useState([])
 const[list,setList]=useState([])
 const[messages,setMessages]=useState([])
 const[file,setFile]=useState(null)
 const[text,setText]=useState('')
 const[unread,setUnread]=useState(0)
 const[busy,setBusy]=useState(false)
 const[error,setError]=useState('')
 const[files,setFiles]=useState([])
 const[filesOpen,setFilesOpen]=useState(false)
 const[loading,setLoading]=useState(true)
 const isAdmin=role==='SYSTEM_ADMIN'
 const isSubject=role==='SUBJECT_ADMIN'
 const byId=useMemo(()=>new Map(contacts.map(contact=>[contact.userId,contact])),[contacts])

 const person=useCallback(conversation=>{
   const otherId=conversation.createdByUserId===currentUser?.id?conversation.assignedAdminId:conversation.createdByUserId
   return byId.get(otherId)||{userId:otherId,displayName:null,role:conversation.createdByRole,facultyId:conversation.facultyId||facultyId}
 },[byId,currentUser?.id,facultyId])

 const load=useCallback(async()=>{
   setLoading(true)
   try{
     const[people,conversations,count]=await Promise.all([
       api.get('/api/v1/users/me/chat-contacts'),
       isAdmin?supportChatApi.adminList({size:40}):isSubject?supportChatApi.assigned({size:40}):supportChatApi.mine({size:40}),
       supportChatApi.unread(),
     ])
     setContacts(people||[])
     setList(conversations?.content||[])
     setUnread(Number(count)||0)
     setError('')
   }catch(reason){setError(reason.message||'Không thể tải tin nhắn.')}
   finally{setLoading(false)}
 },[isAdmin,isSubject])

 useEffect(()=>{
   void load()
   const stop=connectNotificationSocket({onMessage:()=>{},onConnect:()=>{},onStatus:()=>{},onSupport:event=>{
     if(event.message&&event.conversationId===current?.id)setMessages(items=>items.some(message=>message.id===event.message.id)?items:[...items,event.message])
     void load()
   }})
   return stop
 },[load,current?.id])
 useEffect(()=>{const outside=event=>{if(!root.current?.contains(event.target))setOpen(false)};document.addEventListener('pointerdown',outside);return()=>document.removeEventListener('pointerdown',outside)},[])

 async function openConversation(conversation){
   setCurrent(conversation)
   setMessages([])
   try{
     const page=await supportChatApi.messages(conversation.id,{size:100})
     setMessages((page?.content||[]).reverse())
     await supportChatApi.read(conversation.id)
     setUnread(Number(await supportChatApi.unread())||0)
     setError('')
   }catch(reason){setError(reason.message||'Không thể tải cuộc trò chuyện.')}
 }

 async function start(contact){
   const existing=list.find(conversation=>person(conversation).userId===contact.userId)
   if(existing)return openConversation(existing)
   setBusy(true)
   try{
     const created=await supportChatApi.create(`Trao đổi với ${identity(contact)}`,'',contact.userId)
     setList(items=>[created,...items.filter(item=>item.id!==created.id)])
     await openConversation(created)
   }catch(reason){toast.error(reason.message||'Không thể mở tin nhắn.')}
   finally{setBusy(false)}
 }

 async function send(event){
   event.preventDefault()
   if(!current||busy||(!text.trim()&&!file))return
   const body=text
   setText('')
   setBusy(true)
   try{
     const message=await supportChatApi.send(current.id,body,file)
     setMessages(items=>[...items,message])
     setFile(null)
     setList(items=>items.map(conversation=>conversation.id===current.id?{...conversation,lastMessage:message.content||'Đã gửi tệp',lastMessageAt:message.createdAt}:conversation))
   }catch(reason){setText(body);setError(reason.message||'Không thể gửi tin nhắn.')}
   finally{setBusy(false)}
 }

 async function showFiles(){try{setFiles(await supportChatApi.attachments(current.id));setFilesOpen(true)}catch(reason){setError(reason.message||'Không thể tải file đã gửi.')}}
 async function remove(){if(!current||!window.confirm('Xóa đoạn chat khỏi danh sách của bạn?'))return;await supportChatApi.remove(current.id);setList(items=>items.filter(conversation=>conversation.id!==current.id));setCurrent(null);setMessages([])}

 const contactsToStart=contacts.filter(contact=>isSubject?['USER','SYSTEM_ADMIN'].includes(contact.role):['SUBJECT_ADMIN','SYSTEM_ADMIN'].includes(contact.role))
 return <div className="human-chat" ref={root}>
   <button className="human-chat-button" type="button" aria-label="Mở tin nhắn" aria-expanded={open} onClick={()=>setOpen(value=>!value)}><Icon name="messages" size={23}/>{unread>0&&<b>{unread>99?'99+':unread}</b>}</button>
   {open&&<section className="human-chat-panel human-chat-blue-frame" aria-label="Tin nhắn">
     {!current?<>
       <header><div><strong>Tin nhắn</strong><small>Trao đổi với giảng viên và quản trị viên</small></div><button type="button" onClick={()=>setOpen(false)} aria-label="Đóng">×</button></header>
       {!isAdmin&&<div className="human-chat-contacts"><strong>Liên hệ mới</strong>{contactsToStart.slice(0,5).map(contact=><button key={contact.userId} type="button" disabled={busy} onClick={()=>void start(contact)}><span className="human-avatar">{contact.avatarUrl?<img src={contact.avatarUrl} alt=""/>:initials(identity(contact))}</span><span><b>{identity(contact)}</b><small>{subtitle(contact,facultyId)}</small></span><Icon name="plus" size={16}/></button>)}</div>}
       <div className="human-chat-list">
         {loading&&<p className="human-chat-state">Đang tải cuộc trò chuyện…</p>}
         {!loading&&list.map(conversation=>{const contact=person(conversation);return <button key={conversation.id} type="button" onClick={()=>void openConversation(conversation)}><span className="human-avatar">{contact.avatarUrl?<img src={contact.avatarUrl} alt=""/>:initials(identity(contact))}</span><span className="human-chat-list-copy"><span className="human-chat-name-row"><strong>{identity(contact)}</strong><time>{time(conversation.lastMessageAt)}</time></span><small>{subtitle(contact,conversation.facultyId)}</small><span className="human-chat-preview">{conversation.lastMessage||'Chưa có tin nhắn'}</span></span>{conversation.unreadCount>0&&<b className="human-chat-unread">{conversation.unreadCount}</b>}</button>})}
         {!loading&&!list.length&&<p className="human-chat-state">Chưa có cuộc trò chuyện.</p>}
       </div>
     </>:<>
       <header className="human-chat-conversation-head"><button type="button" onClick={()=>setCurrent(null)} aria-label="Quay lại">‹</button><span className="human-avatar">{person(current).avatarUrl?<img src={person(current).avatarUrl} alt=""/>:initials(identity(person(current)))}</span><div><strong>{identity(person(current))}</strong><small>{subtitle(person(current),current.facultyId)}</small></div><button type="button" aria-label="Tùy chọn cuộc trò chuyện" onClick={()=>void showFiles()}>⋮</button></header>
       <div className="human-chat-messages">{!messages.length&&<p className="human-chat-state">Chưa có tin nhắn.</p>}{messages.map(message=><article key={message.id} className={message.senderId===currentUser?.id?'mine':'theirs'}><small>{message.senderId===currentUser?.id?'Bạn':identity(person(current))}</small>{message.content&&<p>{message.content}</p>}{message.attachments?.map(attachment=>attachment.contentType?.startsWith('image/')?<a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer"><img src={attachment.url} alt={attachment.fileName}/></a>:<a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer">📎 {attachment.fileName}</a>)}<time>{time(message.createdAt)}</time></article>)}</div>
       <form className="human-chat-composer" onSubmit={send}>{file&&<div className="human-chat-attachment"><span>{file.name}</span><button type="button" onClick={()=>setFile(null)}>×</button></div>}<div className="human-chat-input"><textarea value={text} onChange={event=>setText(event.target.value)} placeholder="Nhập tin nhắn..." aria-label="Nhập tin nhắn"/><label className="human-chat-file-button" aria-label="Đính kèm file">📎<input type="file" hidden accept="image/*,.pdf,.txt,.md,.doc,.docx,.xls,.xlsx,.ppt,.pptx" onChange={event=>setFile(event.target.files?.[0]||null)}/></label><button className="human-chat-send" type="submit" disabled={busy||(!text.trim()&&!file)} aria-label="Gửi">➤</button></div></form>
       {error&&<p className="support-error" role="alert">{error}</p>}
     </>}
     {filesOpen&&<div className="human-chat-files" role="dialog" aria-label="File đã gửi"><header><strong>File đã gửi</strong><button type="button" onClick={()=>setFilesOpen(false)}>×</button></header>{files.length?files.map(attachment=><a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer">{attachment.contentType?.startsWith('image/')?<img src={attachment.url} alt={attachment.fileName}/>:<span>📎</span>}<span>{attachment.fileName}<small>{Math.round(attachment.fileSize/1024)} KB · {time(attachment.createdAt)}</small></span></a>):<p>Chưa có file đã gửi.</p>}<footer><Button variant="danger" onClick={()=>void remove()}>Xóa đoạn chat</Button></footer></div>}
   </section>}
 </div>
}
