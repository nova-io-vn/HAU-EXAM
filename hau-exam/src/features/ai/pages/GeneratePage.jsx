import {useRef,useState} from 'react'
import {Link} from 'react-router-dom'
import {Button,Input,Select} from '../../../components/ui'
import {DocumentPicker} from '../components/DocumentPicker'
import {AiError,AiJobPanel} from '../components/AiShared'
import {aiApi} from '../api/aiApi'
import {generationPayload} from '../model/aiModel'

export function GeneratePage({analysis=false}) {
  const [form,setForm]=useState({documentId:'',count:10,difficulty:'',topicId:'',analysisType:''})
  const [busy,setBusy]=useState(false)
  const [error,setError]=useState(null)
  const [job,setJob]=useState(null)
  const lock=useRef(false)
  function field(name,value){setForm(previous=>({...previous,[name]:value}))}
  async function submit(event) {
    event.preventDefault();if(lock.current)return
    lock.current=true;setBusy(true);setError(null)
    try {const body=analysis?{documentId:form.documentId,analysisType:form.analysisType.trim()}:generationPayload(form);setJob(await (analysis?aiApi.analyze(body):aiApi.generate(body)))}catch(reason){setError(reason)}finally{lock.current=false;setBusy(false)}
  }
  return <div className="ai-stack"><form className="editor-section" onSubmit={submit}><h2>{analysis?'Phân tích tài liệu':'Tạo câu hỏi từ tài liệu'}</h2><fieldset disabled={busy||Boolean(job)} className="ai-form">
    <DocumentPicker value={form.documentId} onChange={value=>field('documentId',value)} disabled={busy||Boolean(job)}/>
    {analysis?<Input label="Yêu cầu phân tích" required value={form.analysisType} placeholder="Ví dụ: Phân tích độ phủ kiến thức" onChange={event=>field('analysisType',event.target.value)}/>:<><Input label="Số câu hỏi (1–100)" type="number" required min={1} max={100} step={1} value={form.count} onChange={event=>field('count',event.target.value)}/><Select label="Độ khó" value={form.difficulty} options={[{value:'',label:'AI đề xuất'},{value:'EASY',label:'Dễ'},{value:'MEDIUM',label:'Trung bình'},{value:'HARD',label:'Khó'}]} onChange={event=>field('difficulty',event.target.value)}/><Input label="Topic ID (UUID, không bắt buộc)" value={form.topicId} onChange={event=>field('topicId',event.target.value.trim())}/></>}
    <Button type="submit" loading={busy} disabled={busy||!form.documentId||(analysis&&!form.analysisType.trim())||Boolean(job)}>{analysis?'Bắt đầu phân tích':'Tạo AI job'}</Button>
  </fieldset>{error&&<AiError error={error}/>}</form>
    {job&&<><div className="ai-inline"><p role="status">Đã tiếp nhận tác vụ · {job.status}</p><Link to={`/ai/jobs/${job.jobId}`}>Mở chi tiết job</Link><Button variant="secondary" onClick={()=>setJob(null)}>Tạo yêu cầu mới</Button></div><AiJobPanel key={job.jobId} id={job.jobId}/></>}
  </div>
}
