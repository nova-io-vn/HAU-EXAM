import {api} from '../../../services/api/client'

export const aiApi = {
  documents: (page=0)=>api.get(`/api/v1/documents?page=${page}&size=20`),
  upload: file=>{const body=new FormData();body.append('file',file);return api.post('/api/v1/documents',body)},
  generate: body=>api.post('/api/v1/ai/generate/questions',body),
  analyze: body=>api.post('/api/v1/ai/analyze',body),
  chat: body=>api.post('/api/v1/chat',body),
  jobs: (page=0)=>api.get(`/api/v1/ai/jobs?page=${page}&size=20`),
  job: id=>api.get(`/api/v1/ai/jobs/${id}`),
  result: id=>api.get(`/api/v1/ai/jobs/${id}/result`),
}
