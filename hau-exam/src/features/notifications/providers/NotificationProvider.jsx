import {useEffect} from 'react'
import {connectNotificationSocket} from '../../../services/websocket/notificationSocket'
import {notificationStore} from '../store/notificationStore'
import {NotificationToastHost} from '../components/NotificationToastHost'

export function NotificationProvider({children}){useEffect(()=>{const syncTask=setTimeout(()=>notificationStore.sync(),0);const disconnect=connectNotificationSocket({onMessage:message=>notificationStore.receive(message),onConnect:()=>notificationStore.sync(),onStatus:status=>notificationStore.setConnectionStatus(status)});return()=>{clearTimeout(syncTask);disconnect()}},[]);return <>{children}<NotificationToastHost/></>}
