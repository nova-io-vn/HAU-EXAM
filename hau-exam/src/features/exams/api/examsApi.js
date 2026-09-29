import {api} from '../../../services/api/client'
export const examsApi={
  matrices:()=>api.get('/api/v1/exam-matrices'),
  matrix:id=>api.get(`/api/v1/exam-matrices/${id}`),
  saveMatrix:(id,body)=>id?api.put(`/api/v1/exam-matrices/${id}`,body):api.post('/api/v1/exam-matrices',body),
  validate:id=>api.post(`/api/v1/exam-matrices/${id}/validate`),
  capacity:(id,startCode,endCode)=>api.get(`/api/v1/exam-matrices/${id}/capacity?startCode=${startCode}&endCode=${endCode}`),
  generate:body=>api.post('/api/v1/exams/generate',body),
  exams:()=>api.get('/api/v1/exams'),
  exam:id=>api.get(`/api/v1/exams/${id}`),
  version:id=>api.post(`/api/v1/exams/${id}/versions`),
  downloadPdf:(id,version,type='STUDENT_EXAM')=>api.get(`/api/v1/exams/${id}/pdf?version=${version}&type=${type}`,{responseType:'blob'}),
  downloadAll:(id,type='STUDENT_EXAM')=>api.get(`/api/v1/exams/${id}/pdf/all?type=${type}`,{responseType:'blob'}),
}
