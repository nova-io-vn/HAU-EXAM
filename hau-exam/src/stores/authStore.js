const emptyState={currentUser:null,accessToken:null,refreshToken:null,authenticated:false,role:null,facultyId:null}
let state={...emptyState}
const listeners=new Set()

function decodeClaims(token){
  try{
    const encoded=token.split('.')[1]
    const normalized=encoded.replace(/-/g,'+').replace(/_/g,'/')
    return JSON.parse(decodeURIComponent(Array.from(atob(normalized),character=>`%${character.charCodeAt(0).toString(16).padStart(2,'0')}`).join('')))
  }catch{return {}}
}

function publish(next){state=next;listeners.forEach(listener=>listener())}

export const authStore={
  getSnapshot:()=>state,
  getAccessToken:()=>state.accessToken,
  getRefreshToken:()=>state.refreshToken,
  setSession({accessToken,refreshToken,currentUser}){
    const claims=accessToken?decodeClaims(accessToken):{}
    const user=currentUser||{id:claims.sub||null,lecturerCode:claims.lecturerCode||null}
    publish({currentUser:user,accessToken:accessToken||null,refreshToken:refreshToken||state.refreshToken,authenticated:Boolean(accessToken),role:user.role||claims.role||null,facultyId:user.facultyId||claims.facultyId||null})
  },
  clear(){publish({...emptyState})},
  subscribe(listener){listeners.add(listener);return()=>listeners.delete(listener)},
}
