import {Client} from '@stomp/stompjs'
import {authStore} from '../../stores/authStore'
import {notificationDestination,websocketEndpoint} from './config'

export function connectNotificationSocket({onMessage,onConnect,onStatus}){
  const client=new Client({brokerURL:websocketEndpoint,reconnectDelay:5000,connectionTimeout:10000,heartbeatIncoming:10000,heartbeatOutgoing:10000,debug:()=>{}})
  client.beforeConnect=()=>{const token=authStore.getAccessToken();client.connectHeaders=token?{Authorization:`Bearer ${token}`}:{}}
  client.onConnect=()=>{onStatus('connected');client.subscribe(notificationDestination,frame=>{try{onMessage(JSON.parse(frame.body))}catch{onStatus('message-error')}});onConnect()}
  client.onWebSocketClose=()=>onStatus('disconnected')
  client.onWebSocketError=()=>onStatus('disconnected')
  client.onStompError=()=>onStatus('error')
  onStatus('connecting')
  client.activate()
  return()=>{client.deactivate();onStatus('disconnected')}
}
