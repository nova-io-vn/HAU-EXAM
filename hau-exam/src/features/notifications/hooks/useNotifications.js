import {useSyncExternalStore} from 'react'
import {notificationStore} from '../store/notificationStore'
export function useNotifications(){return useSyncExternalStore(notificationStore.subscribe,notificationStore.getSnapshot)}
