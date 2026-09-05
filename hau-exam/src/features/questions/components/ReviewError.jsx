import {Link} from 'react-router-dom'
import {Button} from '../../../components/ui'

export function ReviewError({error,onRetry}) {
  const forbidden = error.status === 403
  return <div className="question-state request-error" role="alert">
    <h2>{forbidden ? '403 · Forbidden' : error.status === 404 ? 'Không tìm thấy câu hỏi' : 'Không thể hoàn tất yêu cầu'}</h2>
    <p>{forbidden ? 'Bạn không có quyền truy cập hoặc xét duyệt câu hỏi này. Câu hỏi có thể thuộc khoa khác.' : error.status === 401 ? 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.' : error.status === 409 ? 'Câu hỏi đã thay đổi trạng thái. Hãy tải lại để xem quyết định mới nhất.' : 'Vui lòng thử lại. Nếu lỗi tiếp diễn, liên hệ quản trị viên với mã hỗ trợ.'}</p>
    {error.correlationId && <small>Mã hỗ trợ: {error.correlationId}</small>}
    <Link to="/review">Về hàng đợi xét duyệt</Link>
    {!forbidden && onRetry && <Button variant="secondary" onClick={onRetry}>Tải lại</Button>}
  </div>
}
