import {Link} from 'react-router-dom'
import {Button,EmptyState,Loading} from '../../../components/ui'
import {routes} from '../../../constants/routes'
import {useNotifications} from '../hooks/useNotifications'
import {notificationStore} from '../store/notificationStore'
import {NotificationItem} from './NotificationItem'

export function NotificationPopover({onClose}){const state=useNotifications();return <section className="notification-popover" aria-label="Thông báo gần đây"><header><div><strong>Thông báo</strong><span className={`connection-state is-${state.connectionStatus}`}>{state.connectionStatus==='connected'?'Realtime đang kết nối':'Đang dùng dữ liệu REST'}</span></div>{state.unreadCount>0&&<Button variant="ghost" onClick={()=>notificationStore.markAllRead()}>Đọc tất cả</Button>}</header><div className="notification-popover-list">{state.loading&&!state.notifications.length?<Loading label="Đang tải thông báo"/>:state.notifications.length?state.notifications.slice(0,6).map(notification=><NotificationItem key={notification.id||notification.eventId} notification={notification} compact onSelected={onClose}/>):<EmptyState title="Chưa có thông báo" description="Thông báo mới sẽ xuất hiện tại đây."/>}</div><footer><Link to={routes.notifications} onClick={onClose}>Xem tất cả thông báo</Link></footer></section>}
