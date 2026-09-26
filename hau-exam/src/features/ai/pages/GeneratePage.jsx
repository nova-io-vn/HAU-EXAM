import {useRef,useState} from 'react'
import {Link} from 'react-router-dom'
import {Button,Input,Select} from '../../../components/ui'
import {DocumentPicker} from '../components/DocumentPicker'
import {AiError,AiJobPanel} from '../components/AiShared'
import {aiApi} from '../api/aiApi'
import {generationPayload} from '../model/aiModel'
import {useQuestionCatalogs} from '../../questions/hooks/useQuestionCatalogs'
import {notificationStore} from '../../notifications/store/notificationStore'

export function GeneratePage({analysis=false}){
 const [form,setForm]=useState({documentId:'',count:10,difficulty:'',topicId:'',subjectId:'',chapterId:'',analysisType:'',language:localStorage.getItem('hau-ai-language')||'VI',includeImages:false})
 const[busy,setBusy]=useState(false),[error,setError]=useState(null),[job,setJob]=useState(null)
 const lock=useRef(false)
 const catalogs=useQuestionCatalogs(form.subjectId,form.chapterId)
 const field=(name,value)=>{setForm(previous=>({...previous,[name]:value}));if(name==='language')localStorage.setItem('hau-ai-language',value)}
 function selectSubject(value){setForm(previous=>({...previous,subjectId:value,chapterId:'',topicId:''}))}
 function selectChapter(value){setForm(previous=>({...previous,chapterId:value,topicId:''}))}
 async function submit(event){
  event.preventDefault()
  if(lock.current)return
  lock.current=true;setBusy(true);setError(null)
  try{
   const body=analysis?{documentId:form.documentId,analysisType:form.analysisType.trim()}:generationPayload(form)
   const accepted=await(analysis?aiApi.analyze(body):aiApi.generate(body));setJob(accepted);notificationStore.pushToast({title:'Đã tiếp nhận AI job',content:'Bạn có thể theo dõi tiến trình xử lý.',actionUrl:`/ai/jobs/${accepted.jobId}`,actionLabel:'Theo dõi AI job →'})
  }catch(reason){setError(reason)}
  finally{lock.current=false;setBusy(false)}
 }
 const generationReady=form.documentId&&form.subjectId&&form.chapterId
 return <div className="ai-stack">
  <form className="editor-section ai-generation-card" onSubmit={submit}>
   <header className="ai-section-header"><div><span className="eyebrow">{analysis?'PHÂN TÍCH AI':'TẠO SINH AI'}</span><h2>{analysis?'Phân tích tài liệu':'Tạo câu hỏi từ tài liệu'}</h2><p>{analysis?'Chọn tài liệu và mô tả nội dung cần phân tích.':'Chọn đúng môn học và chương để câu hỏi được đưa vào ngân hàng sau khi AI hoàn tất.'}</p></div></header>
   <fieldset disabled={busy||Boolean(job)} className="ai-form">
    <section className="ai-form-section"><div className="ai-form-section-title"><strong>1. Tài liệu nguồn</strong><span>Tài liệu đã upload và thuộc tài khoản của bạn.</span></div><DocumentPicker value={form.documentId} onChange={value=>field('documentId',value)} disabled={busy||Boolean(job)}/></section>
    {analysis?<section className="ai-form-section"><div className="ai-form-section-title"><strong>2. Yêu cầu phân tích</strong><span>Mô tả ngắn gọn kết quả bạn muốn nhận.</span></div><Input label="Nội dung cần phân tích" required value={form.analysisType} onChange={event=>field('analysisType',event.target.value)} placeholder="Ví dụ: Phân tích độ phủ kiến thức theo từng chương"/></section>:<>
     <section className="ai-form-section"><div className="ai-form-section-title"><strong>2. Vị trí lưu câu hỏi</strong><span>Môn học và chương là bắt buộc; chủ đề có thể để trống.</span></div><div className="ai-form-grid"><Select label="Môn học *" required value={form.subjectId} options={[{value:'',label:'Chọn môn học'},...catalogs.subjects]} onChange={event=>selectSubject(event.target.value)}/><Select label="Chương *" required disabled={!form.subjectId} value={form.chapterId} options={[{value:'',label:form.subjectId?'Chọn chương':'Chọn môn học trước'},...catalogs.chapters]} onChange={event=>selectChapter(event.target.value)}/><Select label="Chủ đề" disabled={!form.chapterId} value={form.topicId} options={[{value:'',label:form.chapterId?'AI đề xuất hoặc không gắn chủ đề':'Chọn chương trước'},...catalogs.topics]} onChange={event=>field('topicId',event.target.value)}/></div>{catalogs.error&&<AiError error={catalogs.error}/>}</section>
     <section className="ai-form-section"><div className="ai-form-section-title"><strong>3. Cấu hình tạo sinh</strong><span>Điều chỉnh số lượng, độ khó và ngôn ngữ đầu ra.</span></div><div className="ai-form-grid"><Input label="Số lượng câu hỏi *" type="number" required min={1} max={100} value={form.count} onChange={event=>field('count',event.target.value)}/><Select label="Độ khó" value={form.difficulty} options={[{value:'',label:'AI đề xuất'},{value:'EASY',label:'Dễ'},{value:'MEDIUM',label:'Trung bình'},{value:'HARD',label:'Khó'}]} onChange={event=>field('difficulty',event.target.value)}/><Select label="Ngôn ngữ *" value={form.language} options={[{value:'VI',label:'Tiếng Việt'},{value:'EN',label:'English'}]} onChange={event=>field('language',event.target.value)}/></div><label className="ai-checkbox"><input type="checkbox" checked={form.includeImages} onChange={event=>field('includeImages',event.target.checked)}/><span><strong>Cho phép đề xuất ảnh minh họa</strong><small>AI chỉ đề xuất khi nội dung câu hỏi thực sự cần hình ảnh.</small></span></label></section>
    </>}
    <footer className="ai-form-actions"><p>{analysis?'Tác vụ sẽ chạy nền và có thể theo dõi trong danh sách công việc.':'Câu hỏi do AI tạo sẽ ở trạng thái nháp để bạn kiểm tra trước khi gửi duyệt.'}</p><Button type="submit" loading={busy} disabled={busy||Boolean(job)||(analysis?(!form.documentId||!form.analysisType.trim()):!generationReady)}>{analysis?'Bắt đầu phân tích':'Tạo câu hỏi bằng AI'}</Button></footer>
   </fieldset>
   {error&&<AiError error={error}/>}
  </form>
  {job&&<><div className="ai-job-created"><div><strong>Đã tiếp nhận tác vụ</strong><span>Trạng thái hiện tại: {job.status}</span></div><Link to={`/ai/jobs/${job.jobId}`}>Mở chi tiết tác vụ</Link><Button variant="secondary" onClick={()=>setJob(null)}>Tạo yêu cầu mới</Button></div><AiJobPanel key={job.jobId} id={job.jobId}/></>}
 </div>
}
