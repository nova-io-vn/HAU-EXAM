import {Link} from 'react-router-dom'
import {Button} from '../../components/ui'
import {routes} from '../../constants/routes'
export function ForbiddenPage(){return <section className="system-page"><span className="system-code">403</span><h1>Không có quyền truy cập</h1><p>Tài khoản của bạn không được phép mở trang này. Việc phân quyền cuối cùng luôn được backend xác thực.</p><Link to={routes.dashboard}><Button>Về Dashboard</Button></Link></section>}
