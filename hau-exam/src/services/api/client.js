import {authStore} from '../../stores/authStore'
import {ApiError} from './ApiError'

const baseUrl=(import.meta.env.VITE_API_BASE_URL||'').replace(/\/$/,'')
let refreshPromise=null

function notify(name,detail){window.dispatchEvent(new CustomEvent(name,{detail}))}
async function parseJson(response){try{return await response.json()}catch{return null}}
function unwrap(payload){return payload&&typeof payload.success==='boolean'?payload.data:payload}

async function refreshSession(){
  const refreshToken=authStore.getRefreshToken()
  if(!refreshToken)throw new Error('Missing refresh token')
  if(!refreshPromise){
    refreshPromise=fetch(`${baseUrl}/api/v1/auth/refresh`,{method:'POST',credentials:'include',headers:{Accept:'application/json','Content-Type':'application/json'},body:JSON.stringify({refreshToken})})
      .then(async response=>{const payload=await parseJson(response);if(!response.ok||payload?.success===false)throw new Error('Refresh failed');const session=unwrap(payload);authStore.setSession(session);return session.accessToken})
      .finally(()=>{refreshPromise=null})
  }
  return refreshPromise
}

export async function apiRequest(path,{body,headers={},skipRefresh=false,...options}={}){
  const token=authStore.getAccessToken()
  let response
  try{
    response=await fetch(`${baseUrl}${path.startsWith('/')?path:`/${path}`}`,{...options,credentials:'include',body:body instanceof FormData?body:body===undefined?undefined:JSON.stringify(body),headers:{Accept:'application/json',...(body!==undefined&&!(body instanceof FormData)?{'Content-Type':'application/json'}:{}),...(token?{Authorization:`Bearer ${token}`}:{}) ,...headers}})
  }catch{throw new ApiError({message:'Không thể kết nối đến máy chủ'})}

  if(response.status===401&&!skipRefresh&&authStore.getRefreshToken()){
    try{await refreshSession();return apiRequest(path,{body,headers,skipRefresh:true,...options})}catch{authStore.clear();notify('hau:unauthorized',{reason:'SESSION_EXPIRED'})}
  }

  const payload=await parseJson(response)
  const correlationId=response.headers.get('X-Correlation-Id')||payload?.correlationId
  if(response.status===401){authStore.clear();notify('hau:unauthorized',{correlationId,reason:'SESSION_EXPIRED'})}
  if(response.status===403)notify('hau:forbidden',{correlationId})
  if(!response.ok||payload?.success===false)throw new ApiError({status:response.status,code:payload?.code||`HTTP_${response.status}`,message:payload?.message||'Yêu cầu không thành công',correlationId,errors:payload?.errors})
  return unwrap(payload)
}

export const api={get:(path,options)=>apiRequest(path,{...options,method:'GET'}),post:(path,body,options)=>apiRequest(path,{...options,method:'POST',body}),put:(path,body,options)=>apiRequest(path,{...options,method:'PUT',body}),patch:(path,body,options)=>apiRequest(path,{...options,method:'PATCH',body}),delete:(path,options)=>apiRequest(path,{...options,method:'DELETE'})}
export const apiClient=api
export const request=apiRequest
