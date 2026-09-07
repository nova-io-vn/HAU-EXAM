import { Link } from 'react-router-dom'
import { routes } from '../../constants/routes'
import { Button } from '../../components/ui'

export function ErrorPage({ code='500', title='Đã xảy ra lỗi', description='Hệ thống gặp sự cố khi xử lý yêu cầu. Vui lòng thử lại sau.', onRetry, correlationId }) {
  return <main className="system-page" role="main"><span className="system-code">{code}</span><h1>{title}</h1><p>{description}</p>{correlationId&&<small className="system-reference">Mã tham chiếu: {correlationId}</small>}<div className="system-actions">{onRetry&&<Button onClick={onRetry}>Thử lại</Button>}<Link to={routes.dashboard}><Button variant={onRetry?'secondary':'primary'}>Về trang tổng quan</Button></Link>{code!=='401'&&<Button variant="ghost" onClick={() => window.history.back()}>Quay lại</Button>}</div></main>
}
