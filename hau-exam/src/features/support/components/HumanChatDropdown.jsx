import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { authStore } from "../../../stores/authStore";
import { api } from "../../../services/api/client";
import { connectNotificationSocket } from "../../../services/websocket/notificationSocket";
import { Icon } from "../../../components/ui";
import { toast } from "../../notifications/store/notificationStore";
import { supportChatApi } from "../api/supportChatApi";
import "../support-chat.css";

const roleLabels = { SYSTEM_ADMIN: "Quản trị viên hệ thống", SUBJECT_ADMIN: "Quản trị viên chuyên môn", USER: "Giảng viên" };
function initials(value) { return String(value || "GV").trim().split(/\s+/).slice(-2).map(part => part[0]).join("").toUpperCase(); }
function timeLabel(value) { if (!value) return ""; const date = new Date(value); const sameDay = date.toDateString() === new Date().toDateString(); return new Intl.DateTimeFormat("vi-VN", sameDay ? { hour: "2-digit", minute: "2-digit" } : { day: "2-digit", month: "2-digit" }).format(date); }
function identity(contact) { return contact?.displayName?.trim() || contact?.lecturerCode || "Tài khoản HAU"; }
function subtitle(contact, fallbackFaculty) { const role = roleLabels[contact?.role] || "Thành viên"; const faculty = contact?.facultyId || fallbackFaculty; return faculty ? `${role} · Khoa ${faculty}` : role; }

export function HumanChatDropdown() {
  const { role, currentUser, facultyId } = authStore.getSnapshot();
  const root = useRef(null), picker = useRef(null), messagesEnd = useRef(null), currentRef = useRef(null), openRef = useRef(false);
  const [open, setOpen] = useState(false), [contacts, setContacts] = useState([]), [list, setList] = useState([]), [current, setCurrent] = useState(null), [messages, setMessages] = useState([]), [text, setText] = useState(""), [file, setFile] = useState(null), [unread, setUnread] = useState(0), [error, setError] = useState(""), [busy, setBusy] = useState(false), [loading, setLoading] = useState(true);
  const isAdmin = role === "SYSTEM_ADMIN", isSubjectAdmin = role === "SUBJECT_ADMIN";
  const contactsById = useMemo(() => new Map(contacts.map(contact => [contact.userId, contact])), [contacts]);
  const startContacts = contacts.filter(contact => isSubjectAdmin ? contact.role === "SYSTEM_ADMIN" : ["SUBJECT_ADMIN", "SYSTEM_ADMIN"].includes(contact.role));
  const participant = useCallback(conversation => {
    const otherId = conversation?.createdByUserId === currentUser?.id ? conversation?.assignedAdminId : conversation?.createdByUserId;
    const known = contactsById.get(otherId);
    if (known) return known;
    return { userId: otherId, lecturerCode: null, displayName: null, avatarUrl: null, role: conversation?.createdByUserId === currentUser?.id ? (isSubjectAdmin ? "SYSTEM_ADMIN" : "SUBJECT_ADMIN") : conversation?.createdByRole, facultyId: conversation?.facultyId || facultyId };
  }, [contactsById, currentUser?.id, facultyId, isSubjectAdmin]);
  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [people, conversations, count] = await Promise.all([
        api.get("/api/v1/users/me/chat-contacts"),
        isAdmin ? supportChatApi.adminList({ size: 30 }) : isSubjectAdmin ? supportChatApi.assigned({ size: 30 }) : supportChatApi.mine({ size: 30 }),
        supportChatApi.unread(),
      ]);
      setContacts(people || []); setList(conversations?.content || []); setUnread(Number(count) || 0); setError("");
    } catch (reason) { setError(reason.message || "Không thể tải tin nhắn."); }
    finally { setLoading(false); }
  }, [isAdmin, isSubjectAdmin]);

  useEffect(() => { currentRef.current = current; }, [current]);
  useEffect(() => { openRef.current = open; }, [open]);
  useEffect(() => { messagesEnd.current?.scrollIntoView({ block: "end" }); }, [messages, busy]);
  useEffect(() => {
    const timer = setTimeout(() => void load(), 0);
    const stop = connectNotificationSocket({ onMessage: () => {}, onSupport: event => { const message = event.message; if (!message) return; if (event.conversationId === currentRef.current?.id) setMessages(items => items.some(item => item.id === message.id) ? items : [...items, message]); if (message.senderId !== authStore.getSnapshot().currentUser?.id && (!openRef.current || event.conversationId !== currentRef.current?.id)) setUnread(value => value + 1); void load(); }, onStatus: () => {} });
    const outside = event => { if (!root.current?.contains(event.target)) setOpen(false); };
    document.addEventListener("pointerdown", outside); return () => { clearTimeout(timer); stop(); document.removeEventListener("pointerdown", outside); };
  }, [load]);

  async function openConversation(conversation) {
    setCurrent(conversation); setMessages([]); setError("");
    try { const page = await supportChatApi.messages(conversation.id, { size: 50 }); setMessages((page?.content || []).reverse()); await supportChatApi.read(conversation.id); setUnread(Number(await supportChatApi.unread()) || 0); setList(items => items.map(item => item.id === conversation.id ? { ...item, unreadCount: 0 } : item)); }
    catch (reason) { setError(reason.message || "Không thể tải cuộc trò chuyện."); }
  }
  async function startConversation(contact) {
    const existing = list.find(item => participant(item).userId === contact.userId && item.status !== "CLOSED");
    if (existing) { await openConversation(existing); return; }
    setBusy(true); setError("");
    try { const conversation = await supportChatApi.create(`Trao đổi với ${identity(contact)}`, "", contact.userId); setList(items => [conversation, ...items]); await openConversation(conversation); }
    catch (reason) { const message = reason.message || "Không thể bắt đầu cuộc trò chuyện."; setError(message); toast.error(message, { title: "Không thể mở tin nhắn" }); }
    finally { setBusy(false); }
  }
  async function send(event) {
    event.preventDefault(); if (!current || busy || (!text.trim() && !file)) return;
    const body = text; setText(""); setBusy(true); setError("");
    try { const message = await supportChatApi.send(current.id, body, file); setMessages(items => items.some(item => item.id === message.id) ? items : [...items, message]); setFile(null); setList(items => items.map(item => item.id === current.id ? { ...item, lastMessage: message.content || "Đã gửi một hình ảnh", lastMessageAt: message.createdAt } : item)); }
    catch (reason) { const message = reason.message || "Không thể gửi tin nhắn."; setError(message); setText(body); toast.error(message, { title: "Gửi tin nhắn thất bại" }); }
    finally { setBusy(false); }
  }

  const activeParticipant = current ? participant(current) : null;
  return <div className="human-chat" ref={root}>
    <button type="button" className="human-chat-button" aria-label="Mở tin nhắn" aria-expanded={open} onClick={() => setOpen(value => !value)}><Icon name="messages" size={23} />{unread > 0 && <b>{unread > 99 ? "99+" : unread}</b>}</button>
    {open && <section className="human-chat-panel" aria-label="Tin nhắn">
      {!current ? <><header className="human-chat-title"><div><strong>Tin nhắn</strong><small>Trao đổi với giảng viên và quản trị viên</small></div><button type="button" onClick={() => setOpen(false)} aria-label="Đóng">×</button></header>
        {!isAdmin && startContacts.length > 0 && <div className="human-chat-contacts"><strong>Liên hệ mới</strong>{startContacts.slice(0, 4).map(contact => <button type="button" key={contact.userId} disabled={busy} onClick={() => void startConversation(contact)}><span className="human-avatar">{contact.avatarUrl ? <img src={contact.avatarUrl} alt="" /> : initials(identity(contact))}</span><span><b>{identity(contact)}</b><small>{subtitle(contact, facultyId)}</small></span><Icon name="plus" size={16} /></button>)}</div>}
        <div className="human-chat-list">{loading && <p className="human-chat-state">Đang tải cuộc trò chuyện…</p>}{!loading && list.map(conversation => { const person = participant(conversation); return <button type="button" key={conversation.id} onClick={() => void openConversation(conversation)}><span className="human-avatar">{person.avatarUrl ? <img src={person.avatarUrl} alt="" /> : initials(identity(person))}</span><span className="human-chat-list-copy"><span className="human-chat-name-row"><strong>{identity(person)}</strong><time>{timeLabel(conversation.lastMessageAt)}</time></span><small>{subtitle(person, conversation.facultyId)}</small><span className="human-chat-preview">{conversation.lastMessage || conversation.subject}</span></span>{conversation.unreadCount > 0 && <b className="human-chat-unread">{conversation.unreadCount > 9 ? "9+" : conversation.unreadCount}</b>}</button>; })}{!loading && !list.length && <p className="human-chat-state">Chưa có cuộc trò chuyện.</p>}</div>
      </> : <><header className="human-chat-conversation-head"><button type="button" onClick={() => setCurrent(null)} aria-label="Quay lại">‹</button><span className="human-avatar">{activeParticipant.avatarUrl ? <img src={activeParticipant.avatarUrl} alt="" /> : initials(identity(activeParticipant))}</span><div><strong>{identity(activeParticipant)}</strong><small>{subtitle(activeParticipant, current.facultyId)}</small></div><button type="button" aria-label="Tùy chọn cuộc trò chuyện">⋮</button></header>
        <div className="human-chat-messages" aria-live="polite">{!messages.length && <p className="human-chat-state">Chưa có tin nhắn. Hãy bắt đầu cuộc trò chuyện.</p>}{messages.map(message => { const mine = message.senderId === currentUser?.id; return <article key={message.id} className={mine ? "mine" : "theirs"}><small>{mine ? "Bạn" : identity(activeParticipant)}</small>{message.content && <p>{message.content}</p>}{message.attachments?.map(attachment => <a key={attachment.id} href={attachment.url} target="_blank" rel="noreferrer"><img src={attachment.url} alt={attachment.fileName} /></a>)}<time>{timeLabel(message.createdAt)}</time></article>; })}{busy && <p className="human-chat-typing">Đang gửi…</p>}<span ref={messagesEnd} /></div>
        <form className="human-chat-composer" onSubmit={send}>{file && <div className="human-chat-attachment"><span>{file.name}</span><button type="button" onClick={() => setFile(null)} aria-label="Xóa ảnh">×</button></div>}<div className="human-chat-input"><textarea value={text} onChange={event => setText(event.target.value)} onPaste={event => { const item = [...(event.clipboardData?.items || [])].find(entry => entry.type.startsWith("image/")); if (item) { event.preventDefault(); setFile(item.getAsFile()); } }} onKeyDown={event => { if (event.key === "Enter" && !event.shiftKey && !event.isComposing) { event.preventDefault(); void send(event); } }} placeholder="Nhập tin nhắn…" aria-label="Nội dung tin nhắn" rows="2" /><button type="button" onClick={() => picker.current?.click()} aria-label="Đính kèm ảnh"><Icon name="paperclip" size={18} /></button><input ref={picker} hidden type="file" accept="image/png,image/jpeg,image/webp" onChange={event => setFile(event.target.files?.[0] || null)} /><button type="submit" className="human-chat-send" disabled={busy || (!text.trim() && !file)} aria-label="Gửi tin nhắn">➤</button></div></form>
      </>}{error && <p className="support-error" role="alert">{error}</p>}
    </section>}
  </div>;
}
