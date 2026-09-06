import {api} from '../../../services/api/client'

const PATH='/api/v1/notifications'
export const notificationsApi={
  list:({page=0,size=20}={})=>api.get(`${PATH}?page=${page}&size=${size}`),
  unreadCount:()=>api.get(`${PATH}/unread-count`),
  markRead:id=>api.post(`${PATH}/${id}/read`),
  markAllRead:()=>api.post(`${PATH}/read-all`),
}
