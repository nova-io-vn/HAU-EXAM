const STORAGE_KEY='hau-exam.refresh-token'
const emptyState={currentUser:null,accessToken:null,refreshToken:null,authenticated:false,role:null,facultyId:null,bootstrapping:true}
let state={...emptyState}
const listeners=new Set()
let bootstrapPromise=null

function decodeClaims(token){
  try{
    const encoded=token.split('.')[1]
    const normalized=encoded.replace(/-/g,'+').replace(/_/g,'/')
    return JSON.parse(decodeURIComponent(Array.from(atob(normalized),character=>`%${character.charCodeAt(0).toString(16).padStart(2,'0')}`).join('')))
  }catch{return {}}
}

function publish(next){state=next;listeners.forEach(listener=>listener())}
function readRefreshToken(){try{return sessionStorage.getItem(STORAGE_KEY)}catch{return null}}
function writeRefreshToken(token){try{if(token)sessionStorage.setItem(STORAGE_KEY,token);else sessionStorage.removeItem(STORAGE_KEY);return true}catch{return false}}

export const authStore={
  getSnapshot:()=>state,
  getAccessToken:()=>state.accessToken,
  getRefreshToken:()=>state.refreshToken,
  setSession({accessToken,refreshToken,currentUser,userId,lecturerCode,role,facultyId}){
    const claims=accessToken?decodeClaims(accessToken):{}
    const user=currentUser||{id:userId||claims.sub||null,lecturerCode:lecturerCode||claims.lecturerCode||null}
    const nextRefreshToken=refreshToken||state.refreshToken||readRefreshToken()
    writeRefreshToken(nextRefreshToken)
    publish({currentUser:user,accessToken:accessToken||null,refreshToken:nextRefreshToken,authenticated:Boolean(accessToken),role:role||user.role||claims.role||null,facultyId:facultyId||user.facultyId||claims.facultyId||null,bootstrapping:false})
  },
  async bootstrapAuth(refresh){
    if(bootstrapPromise)return bootstrapPromise
    bootstrapPromise=(async()=>{
      const refreshToken=readRefreshToken()
      if(!refreshToken){publish({...emptyState,bootstrapping:false});return}
      try{this.setSession(await refresh(refreshToken))}catch{this.clear()}
      finally{publish({...state,bootstrapping:false})}
    })().finally(()=>{bootstrapPromise=null})
    return bootstrapPromise
  },
  clear(){writeRefreshToken(null);publish({...emptyState,bootstrapping:false})},
  subscribe(listener){listeners.add(listener);return()=>listeners.delete(listener)},
}
