/* eslint-disable react-hooks/set-state-in-effect */
import {useCallback,useEffect,useMemo,useRef,useState} from 'react'
import {useAuth} from '../../auth/hooks/useAuth'
import {api} from '../../../services/api/client'
import {connectNotificationSocket} from '../../../services/websocket/notificationSocket'
import {Icon,Button} from '../../../components/ui'
import {toast} from '../../notifications/store/notificationStore'
import {supportChatApi} from '../api/supportChatApi'
import {hasConversationMessage,mergeMessageById,shouldSendOnKeyDown,uniqueMessages} from '../model/chatModel'
import '../support-chat.css'

const roleLabels={SYSTEM_ADMIN:'Quản trị viên hệ thống',SUBJECT_ADMIN:'Quản trị viên chuyên môn',USER:'Giảng viên'}
function identity(contact){const fullName=contact?.displayName?.trim()||contact?.fullName?.trim();const title=[contact?.academicRank,contact?.academicDegree].filter(value=>value&&value!=='NONE').join(' · ');return fullName?`${title?`${title} · `:''}${fullName}`:contact?.lecturerCode||'Không xác định'}
function initials(value){return String(value||'HAU').trim().split(/\s+/).slice(-2).map(part=>part[0]).join('').toUpperCase()}
function time(value){return value?new Intl.DateTimeFormat('vi-VN',{day:'2-digit',month:'2-digit',hour:'2-digit',minute:'2-digit'}).format(new Date(value)):''}
function subtitle(contact,fallbackFaculty){const faculty=contact?.facultyId||fallbackFaculty;return `${roleLabels[contact?.role]||'Thành viên'}${faculty?` · Khoa ${faculty}`:''}`}

export function HumanChatDropdown(){
 const {role,currentUser,facultyId}=useAuth()
 const root=useRef(null)
 const[current,setCurrent]=useState(null)
 const[open,setOpen]=useState(false)
 const[contacts,setContacts]=useState([])
 const[directory,setDirectory]=useState([])
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
 const[contactSearch,setContactSearch]=useState('')
 const[facultyFilter,setFacultyFilter]=useState('')
 const[roleFilter,setRoleFilter]=useState('')
 const isAdmin=role==='SYSTEM_ADMIN'
 const isSubject=role==='SUBJECT_ADMIN'
 const byId=useMemo(()=>new Map(contacts.map(contact=>[contact.userId,contact])),[contacts])

 const person=useCallback(conversation=>{
   if(conversation?.draftContact)return conversation.draftContact
   const otherId=conversation.createdByUserId===currentUser?.id?conversation.assignedAdminId:conversation.createdByUserId
   return byId.get(otherId)||{userId:otherId,displayName:null,role:conversation.createdByRole,facultyId:conversation.facultyId||facultyId}
 },[byId,currentUser?.id,facultyId])

 const unifiedList=useMemo(()=>{
   const existingIds=new Set(list.map(conversation=>person(conversation).userId).filter(Boolean))
   const drafts=directory
     .filter(contact=>!existingIds.has(contact.userId))
     .map(contact=>({id:`draft-${contact.userId}`,draftContact:contact,lastMessage:'Bắt đầu cuộc trò chuyện',lastMessageAt:null,unreadCount:0}))
   return [...list,...drafts]
 },[directory,list,person])

 const load=useCallback(async()=>{
   setLoading(true)
   try{
     const[people,conversations,count]=await Promise.all([
       api.get('/api/v1/users/me/chat-contacts'),
       isAdmin?supportChatApi.adminList({size:40}):isSubject?supportChatApi.assigned({size:40}):supportChatApi.mine({size:40}),
       supportChatApi.unread(),
     ])
     setContacts(people||[])
     setDirectory(people||[])
     setList((conversations?.content||[]).filter(hasConversationMessage))
     setUnread(Number(count)||0)
     setError('')
   }catch(reason){setError(reason.message||'Không thể tải tin nhắn.')}
   finally{setLoading(false)}
 },[isAdmin,isSubject])

 useEffect(()=>{
   void load()
   const stop=connectNotificationSocket({onMessage:()=>{},onConnect:()=>{},onStatus:()=>{},onSupport:event=>{
     if(event.eventType==='CHAT_MESSAGE_DELETED'&&event.messageId)setMessages(items=>items.map(message=>message.id===event.messageId?{...message,...event.message,deleted:true,content:null,attachments:[]}:message))
     if(event.message&&event.conversationId===current?.id)setMessages(items=>mergeMessageById(items,event.message))
     void load()
   }})
   return stop
 },[load,current?.id])
 useEffect(()=>{if(!isAdmin&&!isSubject)return undefined;let active=true;const timer=setTimeout(async()=>{try{const params=new URLSearchParams();if(contactSearch.trim())params.set('keyword',contactSearch.trim());if(facultyFilter)params.set('facultyId',facultyFilter);if(roleFilter)params.set('role',roleFilter);const result=await api.get(`/api/v1/users/me/chat-contacts?${params}`);if(active)setDirectory(result||[])}catch{if(active)setDirectory([])}},250);return()=>{active=false;clearTimeout(timer)}},[contactSearch,facultyFilter,roleFilter,isAdmin,isSubject])
 useEffect(()=>{const outside=event=>{if(!root.current?.contains(event.target))setOpen(false)};document.addEventListener('pointerdown',outside);return()=>document.removeEventListener('pointerdown',outside)},[])

 async function openConversation(conversation){
   setCurrent(conversation)
   setMessages([])
   try{
     const page=await supportChatApi.messages(conversation.id,{size:100})
     setMessages(uniqueMessages((page?.content||[]).reverse()))
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
     setMessages(items=>mergeMessageById(items,message))
     setFile(null)
     setList(items=>items.map(conversation=>conversation.id===current.id?{...conversation,lastMessage:message.content||'Đã gửi tệp',lastMessageAt:message.createdAt}:conversation))
   }catch(reason){setText(body);setError(reason.message||'Không thể gửi tin nhắn.')}
   finally{setBusy(false)}
 }

 async function showFiles(){try{setFiles(await supportChatApi.attachments(current.id));setFilesOpen(true)}catch(reason){setError(reason.message||'Không thể tải file đã gửi.')}}
 async function remove(){if(!current||!window.confirm('Xóa đoạn chat khỏi danh sách của bạn?'))return;await supportChatApi.remove(current.id);setList(items=>items.filter(conversation=>conversation.id!==current.id));setCurrent(null);setMessages([])}
 async function revoke(message){if(message.deleted||message.senderId!==currentUser?.id||!window.confirm('Thu hồi tin nhắn này?'))return;try{const value=await supportChatApi.revokeMessage(message.id);setMessages(items=>items.map(item=>item.id===message.id?value:item));setList(items=>items.map(conversation=>conversation.id===current.id?{...conversation,lastMessage:'Tin nhắn đã được thu hồi'}:conversation))}catch(reason){setError(reason.message||'Không thể thu hồi tin nhắn.')}}

 const visibleList=unifiedList.filter(conversation=>{
   const contact=person(conversation)
   const needle=contactSearch.trim().toLocaleLowerCase()
   const matchesText=!needle||[identity(contact),contact?.lecturerCode,contact?.email,contact?.facultyId].filter(Boolean).join(' ').toLocaleLowerCase().includes(needle)
   const matchesRole=!roleFilter||contact?.role===roleFilter
   const matchesFaculty=!facultyFilter||contact?.facultyId===facultyFilter
   return matchesText&&matchesRole&&matchesFaculty
 })
 return <div className="human-chat" ref={root}>
   <button className="human-chat-button" type="button" aria-label="Mở tin nhắn" aria-expanded={open} onClick={()=>setOpen(value=>!value)}><Icon name="messages" size={23}/>{unread>0&&<b>{unread>99?'99+':unread}</b>}</button>
   {open&&<section className="human-chat-panel human-chat-blue-frame" aria-label="Tin nhắn">
     {!current?<>
       <header><div><strong>Tin nhắn</strong><small>Trao đổi với giảng viên và quản trị viên</small></div><button type="button" onClick={()=>setOpen(false)} aria-label="Đóng">×</button></header>
       <div className="human-chat-contacts human-chat-directory-tools"><input value={contactSearch} onChange={event=>setContactSearch(event.target.value)} placeholder="Tìm kiếm…" aria-label="Tìm kiếm cuộc trò chuyện hoặc người dùng"/>{(isAdmin||isSubject)&&<><select value={roleFilter} onChange={event=>setRoleFilter(event.target.value)} aria-label="Lọc vai trò"><option value="">Vai trò</option><option value="SUBJECT_ADMIN">Quản trị chuyên môn</option><option value="USER">Giảng viên</option></select><select value={facultyFilter} onChange={event=>setFacultyFilter(event.target.value)} aria-label="Lọc khoa"><option value="">Khoa</option>{[...new Set(contacts.map(item=>item.facultyId).filter(Boolean))].map(item=><option key={item} value={item}>{item}</option>)}</select></>}</div>
       <div className="human-chat-list">
         {loading&&<p className="human-chat-state">Đang tải cuộc trò chuyện…</p>}
          {!loading&&visibleList.map(conversation=>{const contact=person(conversation);return <button className={conversation.unreadCount>0?'has-unread':undefined} key={conversation.id} type="button" onClick={()=>conversation.draftContact?void start(contact):void openConversation(conversation)}><span className="human-avatar">{contact.avatarUrl?<img src={contact.avatarUrl} alt=""/>:initials(identity(contact))}</span><span className="human-chat-list-copy"><span className="human-chat-name-row"><strong>{identity(contact)}</strong><time>{time(conversation.lastMessageAt)}</time></span><small>{subtitle(contact,conversation.facultyId)}</small><span className="human-chat-preview">{conversation.lastMessage}</span></span>{conversation.unreadCount>0&&<b className="human-chat-unread">{conversation.unreadCount}</b>}</button>})}
         {!loading&&!visibleList.length&&<p className="human-chat-state">Không tìm thấy người dùng hoặc cuộc trò chuyện.</p>}
       </div>
     </>:<>
       <header className="human-chat-conversation-head"><button type="button" onClick={()=>setCurrent(null)} aria-label="Quay lại">‹</button><span className="human-avatar">{person(current).avatarUrl?<img src={person(current).avatarUrl} alt=""/>:initials(identity(person(current)))}</span><div><strong>{identity(person(current))}</strong><small>{subtitle(person(current),current.facultyId)}</small></div><button type="button" aria-label="Tùy chọn cuộc trò chuyện" onClick={()=>void showFiles()}>⋮</button></header>
       <div className="human-chat-messages">{!messages.length&&<p className="human-chat-state">Chưa có tin nhắn.</p>}{messages.map(message=><article key={message.id} className={`${message.senderId===currentUser?.id?'mine':'theirs'} ${message.deleted?'is-deleted':''}`}><small>{message.senderId===currentUser?.id?'Bạn':identity(person(current))}</small>{message.deleted?<p className="human-chat-revoked">Tin nhắn đã được thu hồi</p>:<>{message.content&&<p>{message.content}</p>}{message.attachments?.map(attachment=>attachment.contentType?.startsWith('image/')?<a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer"><img src={attachment.url} alt={attachment.fileName}/></a>:<a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer">📎 {attachment.fileName}</a>)}</>}{message.senderId===currentUser?.id&&!message.deleted&&<button className="human-message-menu" type="button" aria-label="Xóa tin nhắn" title="Thu hồi tin nhắn" onClick={()=>void revoke(message)}>⋮</button>}<time>{time(message.deletedAt||message.createdAt)}</time></article>)}</div>
        <form className="human-chat-composer" onSubmit={send}>{file&&<div className="human-chat-attachment"><span>{file.name}</span><button type="button" onClick={()=>setFile(null)}>×</button></div>}<div className="human-chat-input"><textarea value={text} onChange={event=>setText(event.target.value)} onKeyDown={event=>{if(shouldSendOnKeyDown(event)){event.preventDefault();event.currentTarget.form?.requestSubmit()}}} placeholder="Nhập tin nhắn..." aria-label="Nhập tin nhắn"/><label className="human-chat-file-button" aria-label="Đính kèm file">📎<input type="file" hidden accept="image/*,.pdf,.txt,.md,.doc,.docx,.xls,.xlsx,.ppt,.pptx" onChange={event=>setFile(event.target.files?.[0]||null)}/></label><button className="human-chat-send" type="submit" disabled={busy||(!text.trim()&&!file)} aria-label="Gửi">➤</button></div></form>
       {error&&<p className="support-error" role="alert">{error}</p>}
     </>}
     {filesOpen&&<div className="human-chat-files" role="dialog" aria-label="File đã gửi"><header><strong>File đã gửi</strong><button type="button" onClick={()=>setFilesOpen(false)}>×</button></header>{files.length?files.map(attachment=><a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer">{attachment.contentType?.startsWith('image/')?<img src={attachment.url} alt={attachment.fileName}/>:<span>📎</span>}<span>{attachment.fileName}<small>{Math.round(attachment.fileSize/1024)} KB · {time(attachment.createdAt)}</small></span></a>):<p>Chưa có file đã gửi.</p>}<footer><Button variant="danger" onClick={()=>void remove()}>Xóa đoạn chat</Button></footer></div>}
   </section>}
 </div>
}
