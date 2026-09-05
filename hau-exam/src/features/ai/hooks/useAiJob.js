import {useEffect,useState} from 'react'
import {aiApi} from '../api/aiApi'
import {isActiveJob} from '../model/aiModel'

// Poll only after the previous request finishes; stop on terminal status or error.
export function useAiJob(id) {
  const [state,setState]=useState({loading:true})
  const [attempt,setAttempt]=useState(0)
  useEffect(()=>{
    let active=true
    let timer
    async function poll() {
      try {
        const job=await aiApi.job(id)
        if(!active)return
        setState({job})
        if(isActiveJob(job))timer=setTimeout(poll,3000)
        else if(job.status==='COMPLETED') {
          const result=await aiApi.result(id)
          if(active)setState({job,result})
        }
      } catch(error) {if(active)setState(previous=>({...previous,loading:false,error}))}
    }
    poll()
    return ()=>{active=false;clearTimeout(timer)}
  },[id,attempt])
  return {...state,retry:()=>{setState({loading:true});setAttempt(value=>value+1)}}
}
