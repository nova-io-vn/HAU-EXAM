import {useSyncExternalStore} from 'react'
import {authStore} from '../../../stores/authStore'
export function useAuth(){return useSyncExternalStore(authStore.subscribe,authStore.getSnapshot)}
