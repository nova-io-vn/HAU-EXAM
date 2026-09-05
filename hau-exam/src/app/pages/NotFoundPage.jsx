import {Link} from 'react-router-dom'
import {Button} from '../../components/ui'
import {routes} from '../../constants/routes'
export function NotFoundPage(){return <section className="system-page"><span className="system-code">404</span><h1>Không tìm thấy trang</h1><p>Đường dẫn bạn vừa mở không tồn tại hoặc đã được thay đổi.</p><Link to={routes.dashboard}><Button>Về Dashboard</Button></Link></section>}
