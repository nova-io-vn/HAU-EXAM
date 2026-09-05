import {api} from '../../../services/api/client'

function queryString(params){const query=new URLSearchParams(Object.entries(params).filter(([,value])=>value!==''&&value!==undefined&&value!==null));return query.size?`?${query}`:''}
export const questionsApi={
  list:params=>api.get(`/api/v1/questions${queryString(params)}`),
  get:id=>api.get(`/api/v1/questions/${id}`),
  create:question=>api.post('/api/v1/questions',question),
  update:(id,question)=>api.put(`/api/v1/questions/${id}`,question),
  submit:id=>api.post(`/api/v1/questions/${id}/submit`),
  archive:id=>api.post(`/api/v1/questions/${id}/archive`),
  subjects:()=>api.get('/api/v1/subjects'),
  chapters:subjectId=>api.get(`/api/v1/chapters${queryString({subjectId})}`),
  topics:chapterId=>api.get(`/api/v1/topics${queryString({chapterId})}`),
}
