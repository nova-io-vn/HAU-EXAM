import {useCallback,useEffect,useMemo,useRef,useState} from 'react'
import {useLocation,useNavigate} from 'react-router-dom'
import introJs from 'intro.js'
import {useAuth} from '../../features/auth/hooks/useAuth'
import {onboardingApi} from '../../features/onboarding/api/onboardingApi'
import {CURRENT_ONBOARDING_VERSION,onboardingTours} from '../../features/onboarding/onboardingTours'

export function OnboardingTour(){
 const{role,currentUser,bootstrapping}=useAuth();const location=useLocation();const navigate=useNavigate();const list=useMemo(()=>onboardingTours[role]||[],[role]);const[introIndex,setIntroIndex]=useState(-1);const[state,setState]=useState(null);const[saveError,setSaveError]=useState('');const introRef=useRef(null);const startedRef=useRef(false)
 const closeIntro=useCallback(()=>{if(introRef.current){introRef.current.exit();introRef.current=null}},[])
 const complete=useCallback(async()=>{closeIntro();setIntroIndex(-1);setSaveError('');try{await onboardingApi.complete(CURRENT_ONBOARDING_VERSION)}catch{setSaveError('Không thể lưu trạng thái hướng dẫn. Hướng dẫn có thể hiển thị lại ở lần đăng nhập sau.')}},[closeIntro])
 const move=useCallback((next)=>{closeIntro();if(next<0){setIntroIndex(0);return}if(next>=list.length){void complete();return}setIntroIndex(next)},[closeIntro,complete,list.length])
 const skip=useCallback(()=>{if(window.confirm('Bạn có muốn bỏ qua hướng dẫn?'))void complete()},[complete])
 useEffect(()=>{if(bootstrapping||!role||!currentUser?.id||window.__HAU_DISABLE_ONBOARDING__)return undefined;let active=true;onboardingApi.current().then(value=>{if(active)setState(value)}).catch(()=>{if(active)setState({versionCompleted:0})});return()=>{active=false}},[bootstrapping,currentUser?.id,role])
 useEffect(()=>{if(!state||startedRef.current||window.__HAU_DISABLE_ONBOARDING__)return;if((state.versionCompleted||0)<CURRENT_ONBOARDING_VERSION){startedRef.current=true;const timer=setTimeout(()=>setIntroIndex(0),450);return()=>clearTimeout(timer)}},[state])
 useEffect(()=>{const replay=()=>{startedRef.current=true;setSaveError('');setIntroIndex(0)};window.addEventListener('hau:restart-onboarding',replay);return()=>window.removeEventListener('hau:restart-onboarding',replay)},[])
 useEffect(()=>{if(introIndex<0||!list[introIndex])return undefined;const current=list[introIndex];if(location.pathname!==current.route){navigate(current.route);return undefined}let timer;const target=document.querySelector(current.target);if(!target){timer=setTimeout(()=>move(introIndex+1),500);return()=>clearTimeout(timer)}introRef.current=introJs().setOptions({steps:[{element:target,title:current.title,intro:current.description,position:current.position}],showButtons:false,showBullets:false,showProgress:false,exitOnOverlayClick:false,exitOnEsc:false,scrollToElement:true,scrollTo:'tooltip'});introRef.current.start();return()=>{clearTimeout(timer);closeIntro()}},[closeIntro,introIndex,list,location.pathname,move,navigate])
 if(introIndex<0||!list[introIndex])return saveError?<div className="onboarding-save-error" role="status">{saveError}</div>:null
 const current=list[introIndex];return <div className="onboarding-controls" role="dialog" aria-label="Hướng dẫn sử dụng"><strong>{current.title}</strong><small>{introIndex+1} / {list.length}</small><div><button type="button" onClick={skip}>Bỏ qua</button>{introIndex>0&&<button type="button" onClick={()=>move(introIndex-1)}>Quay lại</button>}<button type="button" onClick={()=>move(introIndex+1)}>{introIndex===list.length-1?'Hoàn tất':'Tiếp tục'}</button></div>{saveError&&<small role="status">{saveError}</small>}</div>
}
