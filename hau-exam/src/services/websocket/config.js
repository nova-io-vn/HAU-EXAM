const apiBase=import.meta.env.VITE_API_BASE_URL||window.location.origin
export const websocketEndpoint=`${apiBase.replace(/^http/,'ws').replace(/\/$/,'')}/ws`
export const notificationDestination='/user/queue/notifications'
