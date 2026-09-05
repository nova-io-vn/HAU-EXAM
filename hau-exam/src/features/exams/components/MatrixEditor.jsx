import {useEffect,useRef,useState} from 'react'
import {Link,useNavigate} from 'react-router-dom'
import {Button,Input,Select} from '../../../components/ui'
import {useAuth} from '../../auth/hooks/useAuth'
import {questionsApi} from '../../questions/api/questionsApi'
import {examsApi} from '../api/examsApi'
import {emptyRow,matrixRows,matrixTotal,matrixPayload} from '../model/matrixModel'
import {DistributionTable} from './DistributionTable'
import {ExamError} from './ExamError'

export function MatrixEditor({matrix}){
  const auth=useAuth();const navigate=useNavigate();const lock=useRef(false)
  const [form,setForm]=useState(()=>({name:matrix?.name||'',facultyId:matrix?.facultyId||auth.facultyId||'',subjectId:matrix?.subjectId||'',totalQuestions:matrix?.totalQuestions||10,rows:matrix?matrixRows(matrix.rules):[emptyRow()]}))
  const [catalog,setCatalog]=useState({subjects:[],chapters:[]})
  const [busy,setBusy]=useState(false);const [error,setError]=useState(null)
  useEffect(()=>{let active=true;Promise.all([questionsApi.subjects(),form.subjectId?questionsApi.chapters(form.subjectId):[]]).then(([subjects,chapters])=>{if(active)setCatalog({subjects,chapters,subjectId:form.subjectId})}).catch(error=>{if(active)setCatalog({subjects:[],chapters:[],error})});return()=>{active=false}},[form.subjectId])
  const chapters=catalog.subjectId===form.subjectId?catalog.chapters:[]
  const subjects=[{value:'',label:'Chọn môn học'},...catalog.subjects.map(s=>({value:s.id,label:s.name}))]
  if(form.subjectId&&!catalog.subjects.some(s=>s.id===form.subjectId))subjects.push({value:form.subjectId,label:form.subjectId})
  const total=matrixTotal(form.rows)
  function update(key,changes){setForm(previous=>({...previous,rows:previous.rows.map(row=>row.key===key?{...row,...changes}:row)}))}
  async function save(event){event.preventDefault();if(lock.current)return;setError(null);let payload;try{payload=matrixPayload(form)}catch(reason){setError(reason);return}lock.current=true;setBusy(true);try{const result=await examsApi.saveMatrix(matrix?.id,payload);navigate(`/exam-matrices/${result.id}`,{replace:true,state:{saved:true}})}catch(reason){setError(reason)}finally{lock.current=false;setBusy(false)}}
  return <form className="matrix-editor" onSubmit={save}><fieldset disabled={busy}><section className="editor-section"><h2>Thông tin ma trận</h2><div className="editor-grid"><Input label="Tên ma trận" required value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/><Input label="Khoa được phân công" readOnly value={form.facultyId}/><Select label="Môn học" required value={form.subjectId} options={subjects} onChange={e=>setForm({...form,subjectId:e.target.value,rows:[emptyRow()]})}/><Input label="Tổng số câu mục tiêu" type="number" min="1" max="2147483647" step="1" required value={form.totalQuestions} onChange={e=>setForm({...form,totalQuestions:e.target.value})}/></div><p className="matrix-note">Thay đổi môn học sẽ đặt lại bảng phân bố.</p>{catalog.error&&<ExamError error={catalog.error}/>}</section>
    <section className="editor-section"><h2>Phân bố câu hỏi</h2><p className="matrix-note">Mỗi dòng xác định chương và topic. “Không gắn topic” chọn câu hỏi chưa gắn topic theo quy tắc hiện tại của backend.</p><DistributionTable rows={form.rows} chapters={chapters} onChange={update} onRemove={key=>setForm({...form,rows:form.rows.filter(row=>row.key!==key)})}/><div className="matrix-toolbar"><Button type="button" variant="secondary" disabled={!form.subjectId} onClick={()=>setForm({...form,rows:[...form.rows,emptyRow()]})}>Thêm dòng</Button><p aria-live="polite">Đã phân bố <strong>{total}</strong> / {form.totalQuestions||0} câu {total===Number(form.totalQuestions)&&total>0?'· Khớp tổng':'· Chưa khớp tổng'}</p></div></section>
  </fieldset>{error&&<ExamError error={error}/>}<footer className="editor-actions"><Link className="button button-secondary" to={matrix?`/exam-matrices/${matrix.id}`:'/exam-matrices'}>Quay lại</Link><Button type="submit" loading={busy} disabled={busy||!form.facultyId}>Lưu ma trận</Button></footer></form>
}
