import {useEffect,useRef,useState} from 'react'
import {Link,useParams,useSearchParams} from 'react-router-dom'
import {Button,Dialog,Loading} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {useAuth} from '../../auth/hooks/useAuth'
import {questionsApi} from '../api/questionsApi'
import {QuestionPreview} from '../components/QuestionPreview'
import {ReviewError} from '../components/ReviewError'
import {formatDateTime} from '../model/questionModel'
import {canReview,reviewActions} from '../model/reviewModel'

export function QuestionReviewPage() {
  const {id} = useParams()
  return <ReviewDetail key={id} id={id}/>
}

function ReviewDetail({id}) {
  const auth = useAuth()
  const [params] = useSearchParams()
  const [state,setState] = useState({loading:true})
  const [refresh,setRefresh] = useState(0)
  const [action,setAction] = useState(null)
  const [reason,setReason] = useState('')
  const [busy,setBusy] = useState(false)
  const [success,setSuccess] = useState('')
  const submitting = useRef(false)
  const active = useRef(true)
  useEffect(()=>{active.current=true;return()=>{active.current=false}},[])
  useEffect(()=>{
    let current = true
    questionsApi.get(id).then(question=>{if(current)setState({question})})
      .catch(error=>{if(current)setState({error})})
    return ()=>{current=false}
  },[id,refresh])
  function reload() {setState({loading:true});setRefresh(v=>v+1)}
  async function confirm(event) {
    event.preventDefault()
    if(submitting.current || !canReview(state.question,auth) || !action || (reviewActions[action].required&&!reason.trim()))return
    submitting.current=true
    setBusy(true)
    try {
      const updated = await questionsApi[action](id,reason.trim())
      if(!active.current)return
      setState({question:{...state.question,...updated}})
      setSuccess(`${reviewActions[action].label} thành công.`)
      setAction(null)
    } catch(error) {
      if(active.current){setAction(null);setState({error})}
    } finally {
      submitting.current=false
      if(active.current)setBusy(false)
    }
  }
  if(state.loading)return <div className="question-state"><Loading label="Đang tải câu hỏi xét duyệt"/></div>
  if(state.error)return <ReviewError error={state.error} onRetry={reload}/>
  const q = state.question
  const selected = reviewActions[action]
  return <section className="review-workspace">
    <PageHeader title="Xét duyệt câu hỏi" description="Đối chiếu nội dung và đáp án trước khi đưa ra quyết định." actions={<Link to={`/review?${params}`}>← Về hàng đợi</Link>}/>
    {success&&<p className="review-success" role="status">{success} <Link to={`/review?${params}`}>Tiếp tục xét duyệt</Link></p>}
    <div className="surface question-detail">
      <div>
        <QuestionPreview question={{...q,options:[...q.options].sort((a,b)=>a.sortOrder-b.sortOrder)}}/>
        <section className="review-explanation"><h3>Giải thích</h3><p>Chưa có dữ liệu giải thích được cung cấp.</p></section>
      </div>
      <aside><h3>Thông tin câu hỏi</h3><dl>
        <Info label="Mã câu hỏi" value={q.id}/><Info label="Nguồn" value={q.source==='AI'?'AI':'Thủ công'}/>
        <Info label="Tác giả (ID)" value={q.createdBy}/><Info label="Khoa" value={q.facultyId}/>
        <Info label="Môn học" value={q.subjectName||q.subjectId}/><Info label="Chương" value={q.chapterName||q.chapterId}/>
        <Info label="Chủ đề" value={q.topicName||q.topicId}/><Info label="Loại câu hỏi" value={q.type}/>
        <Info label="Đáp án đúng" value={q.options.filter(o=>o.correct).map(o=>o.label).join(', ')}/>
        <Info label="Ngày tạo" value={formatDateTime(q.createdAt)}/><Info label="Cập nhật" value={formatDateTime(q.updatedAt)}/>
      </dl>{q.source==='AI'&&<section className="ai-review-note"><h3>Thông tin AI</h3><p>Câu hỏi được tạo bằng AI. Chưa có metadata chi tiết được cung cấp.</p><p>Cần kiểm tra tính chính xác của nội dung và đáp án trước khi duyệt.</p></section>}</aside>
    </div>
    <section className="surface review-history"><h2>Lịch sử xét duyệt</h2>
      {!q.reviewHistory?.length?<p>Chưa có quyết định xét duyệt.</p>:<ol>{q.reviewHistory.map(item=><li key={item.id}>
        <div><strong>{({APPROVED:'Phê duyệt',REJECTED:'Từ chối',REVISION_REQUESTED:'Yêu cầu chỉnh sửa'})[item.action]||item.action}</strong><time dateTime={item.timestamp}>{formatDateTime(item.timestamp)}</time></div>
        <small>Người xét duyệt: {item.reviewerId}</small><p>{item.comment||'Không có nhận xét.'}</p>
      </li>)}</ol>}
    </section>
    {canReview(q,auth)?<footer className="review-actions"><span>Chờ quyết định xét duyệt</span><div>{['requestRevision','reject','approve'].map(key=><Button key={key} disabled={busy} variant={key==='approve'?'primary':key==='reject'?'danger':'secondary'} onClick={()=>{setReason('');setAction(key)}}>{reviewActions[key].label}</Button>)}</div></footer>:<p className="review-readonly">Câu hỏi hiện không ở trạng thái hoặc phạm vi cho phép bạn xét duyệt.</p>}
    <Dialog open={Boolean(action)} title={selected?.title||'Xét duyệt'} onClose={event=>{if(busy){event?.preventDefault();return}setAction(null)}}>
      {selected&&<form onSubmit={confirm}>
        <p>{selected.description}</p>
        <div className="field"><label htmlFor="review-reason">{selected.required?'Lý do / nhận xét (bắt buộc)':'Nhận xét (không bắt buộc)'}</label>
          <textarea id="review-reason" rows={5} required={selected.required} disabled={busy} value={reason} onChange={event=>setReason(event.target.value)}/>
        </div>
        <div className="review-dialog-actions"><Button variant="secondary" disabled={busy} onClick={()=>setAction(null)} type="button">Hủy</Button><Button variant={action==='reject'?'danger':'primary'} type="submit" loading={busy} disabled={busy||(selected.required&&!reason.trim())}>{selected.label}</Button></div>
      </form>}
    </Dialog>
  </section>
}

function Info({label,value}) {return <div><dt>{label}</dt><dd>{value||'—'}</dd></div>}
