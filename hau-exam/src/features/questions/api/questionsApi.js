import {api} from '../../../services/api/client'

function queryString(params){const query=new URLSearchParams(Object.entries(params).filter(([,value])=>value!==''&&value!==undefined&&value!==null));return query.size?`?${query}`:''}
export const questionsApi={
  list:async params=>{const result=await api.get(`/api/v1/questions${queryString(params)}`);return {...result,items:await catalogNames(result.items)}} ,
  get:async id=>(await catalogNames([await api.get(`/api/v1/questions/${id}`)]))[0],
  create:question=>api.post('/api/v1/questions',question),
  update:(id,question)=>api.put(`/api/v1/questions/${id}`,question),
  submit:id=>api.post(`/api/v1/questions/${id}/submit`),
  archive:id=>api.post(`/api/v1/questions/${id}/archive`),
  approve:(id,reason)=>api.post(`/api/v1/questions/${id}/approve`,{reason:reason||null}),
  reject:(id,reason)=>api.post(`/api/v1/questions/${id}/reject`,{reason}),
  requestRevision:(id,reason)=>api.post(`/api/v1/questions/${id}/request-revision`,{reason}),
  subjects:()=>api.get('/api/v1/subjects'),
  chapters:subjectId=>api.get(`/api/v1/chapters${queryString({subjectId})}`),
  topics:chapterId=>api.get(`/api/v1/topics${queryString({chapterId})}`),
}

// Display names are resolved from catalog endpoints, not assumed response fields.
async function catalogNames(items=[]){
  if(!items.length)return items
  const results=await Promise.allSettled([
    questionsApi.subjects(),
    ...[...new Set(items.map(q=>q.subjectId))].map(id=>questionsApi.chapters(id)),
    ...[...new Set(items.map(q=>q.chapterId))].map(id=>questionsApi.topics(id)),
  ])
  const names=new Map(results.flatMap(result=>result.status==='fulfilled'?result.value:[]).map(item=>[item.id,item.name]))
  return items.map(q=>({...q,subjectName:names.get(q.subjectId),chapterName:names.get(q.chapterId),topicName:names.get(q.topicId)}))
}
