import { Link } from 'react-router-dom'
import { PublicLayout } from '../../components/layout/PublicLayout'
import errorGif from '../../assets/404.gif'

export function NotFoundPage() {
  return <PublicLayout seo={{title:'Không tìm thấy trang | HAU Exam',description:'Trang bạn đang tìm kiếm không tồn tại hoặc đã được di chuyển.',noindex:true,path:'/404'}}><section className="not-found-page"><img src={errorGif} alt="Minh họa lỗi 404 - không tìm thấy trang" width="372" height="346"/><span className="system-code">404</span><h1>Không tìm thấy trang</h1><p>Trang bạn đang tìm kiếm có thể đã được di chuyển, đổi địa chỉ hoặc không còn tồn tại.</p><div className="system-actions"><Link className="button button-primary" to="/">Về trang chủ</Link><button className="button button-secondary" type="button" onClick={() => window.history.back()}>Quay lại</button></div></section></PublicLayout>
}
