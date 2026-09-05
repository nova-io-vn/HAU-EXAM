import {useEffect,useRef,useState} from 'react'
import {Link,useLocation,useParams} from 'react-router-dom'
import {Button,DataTable,Loading} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {questionsApi} from '../../questions/api/questionsApi'
import {formatDateTime} from '../../questions/model/questionModel'
import {examsApi} from '../api/examsApi'
import {useExamResource} from '../hooks/useExamResource'
import {MatrixEditor} from '../components/MatrixEditor'
import {DistributionTable} from '../components/DistributionTable'
import {ExamError} from '../components/ExamError'
import {matrixRows} from '../model/matrixModel'

export function MatrixListPage(){
  const {data,loading,error,reload}=useExamResource('matrices')
  return <section><PageHeader title="Ma trận đề" description="Thiết kế phân bố câu hỏi trong phạm vi khoa được phân công." actions={<Link className="button button-primary" to="/exam-matrices/new">Tạo ma trận</Link>}/><div className="surface exam-list">{loading?<Loading label="Đang tải ma trận"/>:error?<ExamError error={error} onRetry={reload}/>:<DataTable rows={data} emptyTitle="Chưa có ma trận trong khoa" columns={[{key:'name',header:'Ma trận',render:m=><Link to={`/exam-matrices/${m.id}`}>{m.name}</Link>},{key:'facultyId',header:'Khoa'},{key:'subjectId',header:'Môn học (ID)'},{key:'totalQuestions',header:'Số câu'},{key:'updatedAt',header:'Cập nhật',render:m=>formatDateTime(m.updatedAt)},{key:'actions',header:'Thao tác',render:m=><Link to={`/exams/generate?matrixId=${m.id}`}>Sinh bộ đề</Link>}]}/>}</div></section>
}
export function CreateMatrixPage(){return <section><PageHeader title="Tạo ma trận" description="Nhập số câu theo chương, topic và độ khó."/><MatrixEditor/></section>}
export function MatrixPage({edit=false}){const {id}=useParams();return <MatrixDetail key={`${id}:${edit}`} id={id} edit={edit}/>}
function MatrixDetail({id,edit}){
  const {data,loading,error,reload}=useExamResource('matrix',id)
  const [catalog,setCatalog]=useState({subjects:[],chapters:[]})
  const [validation,setValidation]=useState(null);const [busy,setBusy]=useState(false);const lock=useRef(false);const location=useLocation()
  useEffect(()=>{let active=true;if(data)Promise.all([questionsApi.subjects(),questionsApi.chapters(data.subjectId)]).then(([subjects,chapters])=>{if(active)setCatalog({subjects,chapters})}).catch(()=>{});return()=>{active=false}},[data])
  async function validate(){if(lock.current)return;lock.current=true;setBusy(true);setValidation(null);try{await examsApi.validate(id);setValidation({success:true})}catch(error){setValidation({error})}finally{lock.current=false;setBusy(false)}}
  if(loading)return <Loading label="Đang tải ma trận"/>
  if(error)return <ExamError error={error} onRetry={reload}/>
  if(edit)return <section><PageHeader title="Chỉnh sửa ma trận" description={data.name}/><MatrixEditor matrix={data}/></section>
  return <section><PageHeader title={data.name} description="Chi tiết ma trận và phân bố câu hỏi." actions={<div className="page-actions"><Link className="button button-secondary" to={`/exam-matrices/${id}/edit`}>Chỉnh sửa</Link><Link className="button button-primary" to={`/exams/generate?matrixId=${id}`}>Sinh bộ đề</Link></div>}/>{location.state?.saved&&<p className="review-success" role="status">Đã lưu ma trận.</p>}<section className="editor-section"><dl className="exam-metadata"><Info label="Khoa" value={data.facultyId}/><Info label="Môn học" value={catalog.subjects.find(s=>s.id===data.subjectId)?.name||data.subjectId}/><Info label="Tổng số câu" value={data.totalQuestions}/><Info label="Cập nhật" value={formatDateTime(data.updatedAt)}/></dl><DistributionTable readOnly rows={matrixRows(data.rules)} chapters={catalog.chapters}/><div className="matrix-toolbar"><Button variant="secondary" loading={busy} disabled={busy} onClick={validate}>Kiểm tra ma trận</Button><Link to="/exam-matrices">Về danh sách</Link></div>{validation?.success&&<p role="status">Ma trận hợp lệ. Khả năng đáp ứng câu hỏi APPROVED được kiểm tra khi sinh đề.</p>}{validation?.error&&<ExamError error={validation.error}/>}</section></section>
}
export function Info({label,value}){return <div><dt>{label}</dt><dd>{value??'—'}</dd></div>}
