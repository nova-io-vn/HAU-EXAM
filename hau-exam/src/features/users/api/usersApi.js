import {api} from '../../../services/api/client'

const USERS_PATH='/api/v1/users'
function queryString(params){const query=new URLSearchParams(Object.entries(params).filter(([,value])=>value!==''&&value!==undefined&&value!==null));return query.size?`?${query}`:''}

export const usersApi={
  list:params=>api.get(`${USERS_PATH}${queryString(params)}`),
  get:id=>api.get(`${USERS_PATH}/${id}`),
  getMe:()=>api.get(`${USERS_PATH}/me`),
  updateMe:profile=>api.put(`${USERS_PATH}/me`,profile),
  approve:id=>api.post(`${USERS_PATH}/${id}/approve`),
  reject:id=>api.post(`${USERS_PATH}/${id}/reject`),
  assignRole:(id,role)=>api.put(`${USERS_PATH}/${id}/role`,{role}),
  assignFaculty:(id,facultyId)=>api.put(`${USERS_PATH}/${id}/faculty`,{facultyId}),
  lock:id=>api.post(`${USERS_PATH}/${id}/lock`),
  unlock:id=>api.post(`${USERS_PATH}/${id}/unlock`),
}
