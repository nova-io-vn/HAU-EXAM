import {api} from '../../../services/api/client'

const PATH='/api/v1/notifications'
export const notificationsApi={
  list:({page=0,size=20}={})=>api.get(`${PATH}?page=${page}&size=${size}`),
  unreadCount:()=>api.get(`${PATH}/unread-count`),
  markRead:id=>api.patch(`${PATH}/${id}/read`),
  markAllRead:()=>api.patch(`${PATH}/read-all`),
}
