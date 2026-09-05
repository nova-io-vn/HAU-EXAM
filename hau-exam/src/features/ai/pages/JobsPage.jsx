import {useEffect,useState} from 'react'
import {Link} from 'react-router-dom'
import {Button,DataTable,Loading,StatusBadge} from '../../../components/ui'
import {aiApi} from '../api/aiApi'
import {AiError} from '../components/AiShared'
import {isActiveJob,jobTypeLabels} from '../model/aiModel'
import {formatDateTime} from '../../questions/model/questionModel'

export function JobsPage(){
  const [page,setPage]=useState(0)
  const [state,setState]=useState({loading:true})
  const [version,setVersion]=useState(0)
  useEffect(()=>{let active=true;let timer;async function load(){try{const data=await aiApi.jobs(page);if(!active)return;setState({data});if(data.items.some(isActiveJob))timer=setTimeout(load,5000)}catch(error){if(active)setState({error})}}load();return()=>{active=false;clearTimeout(timer)}},[page,version])
  function move(next){setState({loading:true});setPage(next)}
  return <section className="surface ai-list"><div className="ai-inline"><h2>AI Jobs của tôi</h2><Button variant="secondary" onClick={()=>{setState({loading:true});setVersion(v=>v+1)}}>Làm mới</Button></div>
    {state.loading?<Loading label="Đang tải AI jobs"/>:state.error?<AiError error={state.error} onRetry={()=>{setState({loading:true});setVersion(v=>v+1)}}/>:<><DataTable rows={state.data.items} rowKey="jobId" emptyTitle="Chưa có tác vụ AI" columns={[{key:'jobId',header:'Job',render:j=><Link className="ai-id" to={`/ai/jobs/${j.jobId}`}>{j.jobId}</Link>},{key:'type',header:'Tác vụ',render:j=>jobTypeLabels[j.type]||j.type},{key:'status',header:'Trạng thái',render:j=><StatusBadge status={j.status}/>},{key:'createdAt',header:'Tạo lúc',render:j=>formatDateTime(j.createdAt)},{key:'completedAt',header:'Hoàn tất',render:j=>formatDateTime(j.completedAt)}]}/><div className="ai-inline"><Button variant="secondary" disabled={page===0} onClick={()=>move(page-1)}>Trước</Button><span>Trang {page+1} · {state.data.totalElements} jobs</span><Button variant="secondary" disabled={page+1>=state.data.totalPages} onClick={()=>move(page+1)}>Sau</Button></div></>}
  </section>
}
