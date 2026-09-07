import {api} from '../../../services/api/client'
export const contactApi={submit:body=>api.post('/api/v1/public/contact',body)}
