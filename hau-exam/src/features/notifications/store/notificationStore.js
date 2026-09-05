import {notificationsApi} from '../api/notificationsApi'
import {normalizeNotification,normalizeNotificationPage} from '../model/notificationModel'

let state={notifications:[],unreadCount:0,connectionStatus:'disconnected',loading:false,error:null,toasts:[]}
const listeners=new Set()
const seen=new Set()
function publish(patch){state={...state,...patch};listeners.forEach(listener=>listener())}
function keyOf(item){return item.id||item.eventId}
function remember(key){if(!key)return true;if(seen.has(key))return false;seen.add(key);if(seen.size>500)seen.delete(seen.values().next().value);return true}

export const notificationStore={
  getSnapshot:()=>state,
  subscribe(listener){listeners.add(listener);return()=>listeners.delete(listener)},
  setConnectionStatus(connectionStatus){publish({connectionStatus})},
  async sync(){publish({loading:true,error:null});try{const[list,count]=await Promise.all([notificationsApi.list({page:0,size:20}),notificationsApi.unreadCount()]);const page=normalizeNotificationPage(list);page.items.forEach(item=>remember(keyOf(item)));publish({notifications:page.items,unreadCount:typeof count==='number'?count:count?.count??count?.unreadCount??0,loading:false})}catch(error){publish({error,loading:false})}},
  receive(message){const item=normalizeNotification(message);if(!remember(keyOf(item)))return;publish({notifications:[item,...state.notifications].slice(0,50),unreadCount:state.unreadCount+(item.isRead?0:1),toasts:[...state.toasts,item].slice(-3)})},
  async markRead(id){const item=state.notifications.find(notification=>notification.id===id);if(item?.isRead)return;await notificationsApi.markRead(id);publish({notifications:state.notifications.map(notification=>notification.id===id?{...notification,isRead:true}:notification),unreadCount:item?Math.max(0,state.unreadCount-1):state.unreadCount})},
  async markAllRead(){await notificationsApi.markAllRead();publish({notifications:state.notifications.map(item=>({...item,isRead:true})),unreadCount:0})},
  dismissToast(id){publish({toasts:state.toasts.filter(item=>keyOf(item)!==id)})},
}
