import {useEffect,useState} from 'react'
import {Link,useSearchParams} from 'react-router-dom'
import {Button,DataTable,Input,Loading,Select} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {questionsApi} from '../api/questionsApi'
import {QuestionPagination} from '../components/QuestionPagination'
import {ReviewError} from '../components/ReviewError'
import {formatDateTime,normalizePage} from '../model/questionModel'

export function ReviewQueuePage() {
  const [params,setParams] = useSearchParams()
  return <ReviewQueue key={params.toString()} params={params} setParams={setParams}/>
}

function ReviewQueue({params,setParams}) {
  const keyword = params.get('keyword') || ''
  const source = ['AI','MANUAL'].includes(params.get('source')) ? params.get('source') : ''
  const page = Math.max(0,Number.parseInt(params.get('page'),10)||0)
  const [draft,setDraft] = useState(keyword)
  const [state,setState] = useState({loading:true})
  const [refresh,setRefresh] = useState(0)
  useEffect(()=>{
    let active = true
    questionsApi.list({status:'PENDING_REVIEW',keyword,source,page,size:10,sort:'createdAt,asc'})
      .then(data=>{if(active)setState({data:normalizePage(data)})})
      .catch(error=>{if(active)setState({error})})
    return ()=>{active=false}
  },[keyword,source,page,refresh])
  function query(values) {setState({loading:true});setRefresh(v=>v+1);setParams({...Object.fromEntries(params),...values})}
  return <section>
    <PageHeader title="Hàng đợi xét duyệt" description="Câu hỏi chờ quyết định trong phạm vi khoa được phân công." actions={<Button variant="secondary" onClick={()=>{setState({loading:true});setRefresh(v=>v+1)}}>Làm mới</Button>}/>
    <form className="review-filters" onSubmit={event=>{event.preventDefault();query({keyword:draft.trim(),page:'0'})}}>
      <Input label="Tìm nội dung câu hỏi" value={draft} onChange={event=>setDraft(event.target.value)}/>
      <Select label="Nguồn" value={source} options={[{value:'',label:'Tất cả nguồn'},{value:'MANUAL',label:'Thủ công'},{value:'AI',label:'AI'}]} onChange={event=>query({source:event.target.value,page:'0'})}/>
      <Button type="submit">Tìm kiếm</Button>
    </form>
    <div className="surface question-table-surface">
      {state.loading ? <div className="question-state"><Loading label="Đang tải hàng đợi"/></div> : state.error ? <ReviewError error={state.error} onRetry={()=>{setState({loading:true});setRefresh(v=>v+1)}}/> : <>
        <DataTable rows={state.data.items} emptyTitle="Không có câu hỏi chờ duyệt phù hợp" columns={[
          {key:'content',header:'Câu hỏi',render:q=><Link className="question-cell" to={`/review/${q.id}?${params}`}>{q.content}</Link>},
          {key:'subject',header:'Môn / Chương / Chủ đề',render:q=><span>{q.subjectName||q.subjectId}<small>{q.chapterName||q.chapterId} · {q.topicName||q.topicId||'—'}</small></span>},
          {key:'facultyId',header:'Khoa'},
          {key:'createdBy',header:'Tác giả',render:q=><span className="review-author">{q.createdBy}</span>},
          {key:'source',header:'Nguồn',render:q=><span className={`source-label source-${q.source}`}>{q.source==='AI'?'AI':'Thủ công'}</span>},
          {key:'difficulty',header:'Độ khó'},
          {key:'updatedAt',header:'Cập nhật',render:q=>formatDateTime(q.updatedAt)},
          {key:'action',header:'Thao tác',render:q=><Link to={`/review/${q.id}?${params}`}>Xét duyệt</Link>},
        ]}/>
        <QuestionPagination {...state.data} onChange={next=>query({page:String(next)})}/>
        {page>0 && state.data.items.length===0 && <div className="review-empty-actions"><p>Trang này không còn câu hỏi chờ duyệt. Danh sách có thể đã thay đổi.</p><Button variant="secondary" onClick={()=>query({page:'0'})}>Về trang đầu</Button></div>}
      </>}
    </div>
  </section>
}
