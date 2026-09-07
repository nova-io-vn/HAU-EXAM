import {api} from '../../../services/api/client'
const path='/api/v1/faculties'
function query(params){const value=new URLSearchParams(Object.entries(params||{}).filter(([,v])=>v!==''&&v!==undefined&&v!==null));return value.size?'?'+value:''}
export const facultiesApi={list:params=>api.get(path+query(params)),get:id=>api.get(path+'/'+id),create:body=>api.post(path,body),update:(id,body)=>api.put(path+'/'+id,body),status:(id,active)=>api.patch(path+'/'+id+'/status',{active})}
