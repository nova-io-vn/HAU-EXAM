import {useEffect,useState} from 'react'
import {getElapsedSeconds} from '../utils/asyncProgress'

export function useElapsedTime(startedAt,endedAt=null,active=true){
  const [now,setNow]=useState(0)
  useEffect(()=>{if(!active||endedAt)return undefined;const timer=setInterval(()=>setNow(Date.now()),1000);return()=>clearInterval(timer)},[active,endedAt])
  return endedAt?getElapsedSeconds(startedAt,endedAt):getElapsedSeconds(startedAt,null,now)
}
