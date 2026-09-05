import {Button} from '../../../components/ui'
import {shortageDetails} from '../model/matrixModel'
export function ExamError({error,onRetry}){
  const shortage=shortageDetails(error)
  return <div className="exam-error" role="alert"><h3>{error.code==='INSUFFICIENT_APPROVED_QUESTIONS'?'Không đủ câu hỏi đã duyệt':error.status===403?'403 · Forbidden':error.status===404?'Không tìm thấy dữ liệu':error.status===401?'Phiên đăng nhập đã hết hạn':'Không thể hoàn tất yêu cầu'}</h3>
    {shortage?<><p>Quy tắc thiếu: chương <strong>{shortage.chapterId}</strong> · topic <strong>{shortage.topicId||'Không gắn topic'}</strong> · <strong>{shortage.difficulty}</strong></p><p>Cần <strong>{shortage.required}</strong> câu · hiện có <strong>{shortage.available}</strong> câu phù hợp.</p><p>Điều chỉnh phân bố hoặc bổ sung câu hỏi đã duyệt rồi sinh lại bộ đề.</p></>:<p>{error.code==='INSUFFICIENT_APPROVED_QUESTIONS'||error.code==='INVALID_EXAM_MATRIX'||!error.status?error.message:error.status===403?'Bạn không được phép truy cập dữ liệu ngoài phạm vi khoa được phân công.':'Hãy tải lại dữ liệu trước khi thực hiện tiếp.'}</p>}
    {error.correlationId&&<small>Mã hỗ trợ: {error.correlationId}</small>}{onRetry&&error.status!==403&&<Button variant="secondary" onClick={onRetry}>Tải lại</Button>}
  </div>
}
