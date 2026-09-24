import {api} from '../../../services/api/client'

function contactQuery({status,page=0,size=20}={}){
  const query=new URLSearchParams({page:String(page),size:String(size)})
  if(status)query.set('status',status)
  return query
}

export const adminSupportApi={
  list:params=>api.get(`/api/v1/admin/contact?${contactQuery(params)}`),
  get:id=>api.get(`/api/v1/admin/contact/${id}`),
  status:(id,value)=>api.patch(`/api/v1/admin/contact/${id}/status?value=${encodeURIComponent(value)}`),
  reply:(id,replyMessage)=>api.post(`/api/v1/admin/contact/${id}/reply`,{replyMessage}),
  emailSettings:()=>api.get('/api/v1/admin/email-settings'),
  saveEmailSettings:settings=>api.put('/api/v1/admin/email-settings',settings),
  testEmail:recipient=>api.post('/api/v1/admin/email-settings/test',{recipient}),
}
