const configuredApiBaseUrl=(import.meta.env.VITE_API_BASE_URL||'').trim()
if(import.meta.env.PROD&&!configuredApiBaseUrl)throw new Error('Frontend configuration error: VITE_API_BASE_URL is required in production.')

const rawApiBaseUrl=configuredApiBaseUrl||'http://localhost:8080'
export const API_BASE_URL=rawApiBaseUrl.replace(/\/$/,'')
const configuredWsBaseUrl=(import.meta.env.VITE_WS_BASE_URL||'').trim()
export const WS_BASE_URL=(configuredWsBaseUrl||API_BASE_URL.replace(/^http/,'ws')).replace(/\/$/,'')
export const AI_DOCUMENT_MAX_SIZE_BYTES=Number(import.meta.env.VITE_AI_DOCUMENT_MAX_SIZE_BYTES)||10*1024*1024
