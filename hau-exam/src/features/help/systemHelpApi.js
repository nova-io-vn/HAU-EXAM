import {api} from '../../services/api/client'
export const systemHelpApi={ask:message=>api.post('/api/v1/ai/system-help',{message})}
