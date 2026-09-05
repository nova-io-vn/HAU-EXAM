import {useEffect,useState} from 'react'
import {examsApi} from '../api/examsApi'
export function useExamResource(kind,id){
  const [state,setState]=useState({loading:true})
  const [attempt,setAttempt]=useState(0)
  useEffect(()=>{let active=true;examsApi[kind](id).then(data=>{if(active)setState({data})}).catch(error=>{if(active)setState({error})});return()=>{active=false}},[kind,id,attempt])
  return {...state,setData:data=>setState({data}),reload:()=>{setState({loading:true});setAttempt(v=>v+1)}}
}
