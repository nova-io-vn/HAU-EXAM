import {useEffect,useRef,useState} from 'react'
import {Link,useParams,useSearchParams} from 'react-router-dom'
import {Button,ConfirmDialog,Loading} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {QuestionPreview} from '../../questions/components/QuestionPreview'
import {questionsApi} from '../../questions/api/questionsApi'
import {formatDateTime} from '../../questions/model/questionModel'
import {examsApi} from '../api/examsApi'
import {useExamResource} from '../hooks/useExamResource'
import {ExamError} from '../components/ExamError'
import {Info} from './MatrixPages'

export function ExamDetailPage(){const {id}=useParams();return <ExamDetail key={id} id={id}/>}
function ExamDetail({id}){
  const {data,loading,error,reload,setData}=useExamResource('exam',id)
  const [params,setParams]=useSearchParams();const [confirm,setConfirm]=useState(false);const [busy,setBusy]=useState(false);const [failure,setFailure]=useState(null);const lock=useRef(false)
  async function regenerate(){if(lock.current)return;lock.current=true;setBusy(true);setFailure(null);try{const result=await examsApi.version(id);setData(result);setParams({version:String(Math.max(...result.versions.map(v=>v.version)))})}catch(reason){setFailure(reason)}finally{lock.current=false;setBusy(false);setConfirm(false)}}
  if(loading)return <Loading label="Đang tải bộ đề"/>
  if(error)return <ExamError error={error} onRetry={reload}/>
  if(failure?.status===403)return <ExamError error={failure}/>
  const versions=[...data.versions].sort((a,b)=>b.version-a.version)
  const chosen=params.has('version')?versions.find(v=>v.version===Number(params.get('version'))):versions[0]
  return <section><PageHeader title={data.name} description="Thành phần bộ đề và lịch sử phiên bản." actions={<Button disabled={busy} onClick={()=>setConfirm(true)}>Tạo phiên bản mới</Button>}/><section className="editor-section"><dl className="exam-metadata"><Info label="Mã bộ đề" value={data.id}/><Info label="Khoa" value={data.facultyId}/><Info label="Môn học (ID)" value={data.subjectId}/><Info label="Ma trận" value={<Link to={`/exam-matrices/${data.matrixId}`}>{data.matrixId}</Link>}/><Info label="Sinh lần đầu" value={formatDateTime(data.createdAt)}/><Info label="Template" value={data.templateId||'Không dùng template'}/></dl></section>
    {failure&&<ExamError error={failure}/>}<section className="editor-section exam-versions"><h2>Phiên bản bộ đề</h2><div className="version-tabs" aria-label="Chọn phiên bản">{versions.map(v=><Button variant={chosen?.id===v.id?'primary':'secondary'} key={v.id} aria-pressed={chosen?.id===v.id} onClick={()=>setParams({version:String(v.version)})}>Phiên bản {v.version} · {v.questions.length} câu</Button>)}</div>
      {chosen?<><p>Sinh lúc <strong>{formatDateTime(chosen.createdAt)}</strong> · Phiên bản {chosen.version}</p><p className="matrix-note">Danh sách và thứ tự thuộc phiên bản đã lưu. Xem trước tải nội dung câu hỏi hiện tại từ ngân hàng; đây không phải bản chụp nội dung tại thời điểm sinh.</p><ol className="exam-questions" key={chosen.id}>{[...chosen.questions].sort((a,b)=>a.position-b.position).map(reference=><QuestionReference key={reference.id} reference={reference}/>)}</ol></>:<p role="status">Không tìm thấy phiên bản được yêu cầu. Chọn một phiên bản ở trên.</p>}
    </section><ConfirmDialog open={confirm} title="Tạo phiên bản mới?" description="Backend sẽ chọn câu hỏi theo ma trận hiện tại và lưu thêm phiên bản. Các phiên bản đã có được giữ nguyên." confirmLabel="Tạo phiên bản" loading={busy} onConfirm={regenerate} onClose={event=>{if(busy){event?.preventDefault();return}setConfirm(false)}}/></section>
}
function QuestionReference({reference}){
  const [open,setOpen]=useState(false);const [state,setState]=useState({loading:true});const [attempt,setAttempt]=useState(0)
  useEffect(()=>{let active=true;if(open)questionsApi.get(reference.questionId).then(question=>{if(active)setState({question})}).catch(error=>{if(active)setState({error})});return()=>{active=false}},[open,reference.questionId,attempt])
  return <li><div className="exam-question-heading"><strong>Câu {reference.position}</strong><Link to={`/questions/${reference.questionId}`}>{reference.questionId}</Link><Button variant="secondary" aria-expanded={open} onClick={()=>setOpen(value=>!value)}>{open?'Thu gọn':'Xem nội dung'}</Button></div><small>Quy tắc ma trận: {reference.matrixRuleId}</small>{open&&<div className="exam-question-preview">{state.loading?<Loading label="Đang tải câu hỏi"/>:state.error?<ExamError error={state.error} onRetry={()=>{setState({loading:true});setAttempt(v=>v+1)}}/>:<QuestionPreview question={state.question}/>}</div>}</li>
}
