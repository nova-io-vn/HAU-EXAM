import {Button,Loading,StatusBadge} from '../../../components/ui'
import {useAiJob} from '../hooks/useAiJob'
import {isActiveJob,jobTypeLabels} from '../model/aiModel'
import {formatDateTime} from '../../questions/model/questionModel'

export function AiError({error,onRetry}) {
  return <div className="ai-error" role="alert"><strong>{error.status===403?'403 · Forbidden':error.status===404?'Không tìm thấy dữ liệu hoặc bạn không có quyền truy cập':error.status===401?'Phiên đăng nhập đã hết hạn':'Không thể hoàn tất yêu cầu'}</strong>
    <p>{!error.status?error.message:error.status===413?'Tài liệu vượt giới hạn upload của máy chủ.':error.status===400?'Dữ liệu không hợp lệ. Kiểm tra loại, kích thước tài liệu và các trường nhập.':'Vui lòng thử tải lại. Không tự gửi lại tác vụ để tránh tạo job trùng.'}</p>
    {error.correlationId&&<small>Mã hỗ trợ: {error.correlationId}</small>}{onRetry&&<Button variant="secondary" onClick={onRetry}>Tải lại</Button>}
  </div>
}

export function AiResult({type,result}) {
  if(type==='QUESTION_GENERATION') {
    const questions=Array.isArray(result)?result:result?.questions
    if(!Array.isArray(questions))return <p role="alert">Kết quả không có cấu trúc câu hỏi hợp lệ.</p>
    return <div className="ai-results"><p className="ai-notice">Bản xem trước kết quả AI. Câu hỏi chưa được coi là APPROVED; nội dung cần đi qua quy trình xét duyệt.</p>
      {questions.map((q,index)=><article className="editor-section question-preview" key={index}>
        <header><strong>Câu {index+1}</strong><p>Độ khó: {q.difficulty} · Topic: {q.topicId||'Chưa phân loại'}</p></header><h2>{q.question}</h2>
        <ol className="option-preview">{q.options?.map(option=><li key={option.label} className={option.label===q.correctAnswer?'is-correct':''}><strong>{option.label}</strong><span>{option.content}{option.label===q.correctAnswer&&<small> · Đáp án đúng</small>}</span></li>)}</ol>
        <p><strong>Đáp án đúng:</strong> {q.correctAnswer}</p><section className="explanation"><strong>Giải thích</strong><p>{q.explanation}</p></section>
      </article>)}
    </div>
  }
  return <section className="ai-output">{type==='CHAT'&&typeof result?.answer==='string'?<p>{result.answer}</p>:<pre>{JSON.stringify(result,null,2)}</pre>}
    {result?.references&&<details><summary>Nguồn tham chiếu</summary><pre>{JSON.stringify(result.references,null,2)}</pre></details>}
  </section>
}

export function AiJobPanel({id}) {
  const {job,result,loading,error,retry}=useAiJob(id)
  if(loading)return <Loading label="Đang tải AI job"/>
  if(error)return <AiError error={error} onRetry={retry}/>
  return <section className="ai-job-panel" aria-live="polite">
    <header><h2>{jobTypeLabels[job.type]||job.type}</h2><StatusBadge status={job.status}/></header>
    <p className="ai-id">Job ID: {job.jobId}</p><p>Tạo lúc {formatDateTime(job.createdAt)}</p>
    {isActiveJob(job)&&<Loading label={job.status==='PENDING'?'PENDING · Đang chờ xử lý':'PROCESSING · AI đang xử lý'}/>}
    {job.status==='FAILED'&&<div role="alert" className="ai-error"><strong>Tác vụ thất bại</strong><p>{job.errorMessage||'Không thể xử lý tác vụ AI.'}</p><small>{job.errorCode}</small></div>}
    {job.status==='COMPLETED'&&(result===undefined?<Loading label="Đang tải kết quả"/>:<AiResult type={job.type} result={result}/>)}
  </section>
}
