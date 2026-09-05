import {useEffect} from 'react'
import {useNotifications} from '../hooks/useNotifications'
import {notificationStore} from '../store/notificationStore'

export function NotificationToastHost(){const{toasts}=useNotifications();useEffect(()=>{const timers=toasts.map(item=>setTimeout(()=>notificationStore.dismissToast(item.id||item.eventId),5000));return()=>timers.forEach(clearTimeout)},[toasts]);return <div className="toast-region" aria-live="polite" aria-label="Thông báo realtime">{toasts.map(item=><div className="notification-toast" key={item.id||item.eventId}><div><strong>{item.title}</strong><span>{item.content}</span></div><button type="button" onClick={()=>notificationStore.dismissToast(item.id||item.eventId)} aria-label="Đóng thông báo">×</button></div>)}</div>}
