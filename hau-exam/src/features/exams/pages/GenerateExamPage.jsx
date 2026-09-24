import {useRef,useState} from 'react'
import {Link,useNavigate,useSearchParams} from 'react-router-dom'
import {Button,DataTable,Input,Loading,Select} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {useExamResource} from '../hooks/useExamResource'
import {ExamError} from '../components/ExamError'
import {examsApi} from '../api/examsApi'
import {uuidPattern} from '../model/matrixModel'
import {formatDateTime} from '../../questions/model/questionModel'

export function GenerateExamPage(){
  const [params]=useSearchParams();const navigate=useNavigate()
  const {data,loading,error,reload}=useExamResource('matrices')
  const [name,setName]=useState('');const [matrixId,setMatrixId]=useState(params.get('matrixId')||'');const [templateId,setTemplateId]=useState('')
  const [busy,setBusy]=useState(false);const [failure,setFailure]=useState(null);const lock=useRef(false)
  async function generate(event){event.preventDefault();if(lock.current)return;setFailure(null);if(templateId&&!uuidPattern.test(templateId)){setFailure(new Error('Template ID phải có định dạng UUID.'));return}lock.current=true;setBusy(true);try{const result=await examsApi.generate({name:name.trim(),matrixId,templateId:templateId||null});navigate(`/exams/${result.id}`,{replace:true})}catch(reason){setFailure(reason)}finally{lock.current=false;setBusy(false)}}
  const selected=data?.find(m=>m.id===matrixId)
  return <section><PageHeader title="Sinh bộ đề" description="Tạo phiên bản đầu tiên từ câu hỏi đã được duyệt theo ma trận."/>{loading?<Loading label="Đang tải ma trận"/>:error?<ExamError error={error} onRetry={reload}/>:<form className="editor-section exam-generation" onSubmit={generate}><fieldset disabled={busy}><Input label="Tên bộ đề" required value={name} onChange={e=>setName(e.target.value)}/><Select label="Ma trận" required value={matrixId} options={[{value:'',label:'Chọn ma trận'},...data.map(m=>({value:m.id,label:`${m.name} · ${m.totalQuestions} câu`}))]} onChange={e=>setMatrixId(e.target.value)}/>{selected&&<p>Khoa: <strong>{selected.facultyId}</strong> · {selected.totalQuestions} câu · <Link to={`/exam-matrices/${selected.id}`}>Xem phân bố</Link></p>}{!data.length&&<Link to="/exam-matrices/new">Tạo ma trận trước khi sinh bộ đề</Link>}<Input label="Template ID (UUID, không bắt buộc)" value={templateId} onChange={e=>setTemplateId(e.target.value.trim())}/><p className="matrix-note">Nếu dùng template, template phải thuộc ma trận đã chọn.</p><Button type="submit" loading={busy} disabled={busy||!selected||!name.trim()}>Sinh bộ đề</Button></fieldset>{busy&&<p role="status">Đang chọn câu hỏi theo ma trận…</p>}{failure&&<ExamError error={failure}/>}</form>}</section>
}

export function ExamsPage(){const [id,setId]=useState('');const navigate=useNavigate();const {data,loading,error,reload}=useExamResource('exams');return <section><PageHeader title="Bộ đề" description="Danh sách bộ đề và các phiên bản đã sinh trong phạm vi khoa." actions={<Link className="button button-primary" to="/exams/generate">Sinh bộ đề</Link>}/><div className="surface exam-list">{loading?<Loading label="Đang tải bộ đề"/>:error?<ExamError error={error} onRetry={reload}/>:<DataTable rows={data} emptyTitle="Chưa có bộ đề" columns={[{key:'name',header:'Bộ đề',render:item=><Link to={`/exams/${item.id}`}>{item.name}</Link>},{key:'facultyId',header:'Khoa'},{key:'subjectId',header:'Môn học (ID)'},{key:'versions',header:'Phiên bản',render:item=>item.versions?.length||0},{key:'updatedAt',header:'Cập nhật',render:item=>formatDateTime(item.updatedAt)}]}/>}</div><form className="editor-section exam-generation" onSubmit={e=>{e.preventDefault();if(uuidPattern.test(id))navigate(`/exams/${id}`)}}><Input label="Mở nhanh bằng mã bộ đề" required value={id} placeholder="UUID của bộ đề" pattern="[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}" onChange={e=>setId(e.target.value.trim())}/><Button type="submit">Mở bộ đề và phiên bản</Button></form></section>}
