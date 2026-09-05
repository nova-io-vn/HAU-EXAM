import {useEffect,useRef,useState} from 'react'
import {useNotifications} from '../hooks/useNotifications'
import {notificationStore} from '../store/notificationStore'
import {NotificationPopover} from './NotificationPopover'

export function NotificationBell(){const[open,setOpen]=useState(false);const{unreadCount}=useNotifications();const root=useRef(null);useEffect(()=>{function outside(event){if(!root.current?.contains(event.target))setOpen(false)}document.addEventListener('pointerdown',outside);return()=>document.removeEventListener('pointerdown',outside)},[]);function toggle(){setOpen(value=>{if(!value)notificationStore.sync();return!value})}return <div className="notification-bell" ref={root}><button type="button" onClick={toggle} aria-label={`Thông báo, ${unreadCount} chưa đọc`} aria-expanded={open}>🔔{unreadCount>0&&<span className="unread-badge">{unreadCount>99?'99+':unreadCount}</span>}</button>{open&&<NotificationPopover onClose={()=>setOpen(false)}/>}</div>}
