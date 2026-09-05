import {Link,useLocation} from 'react-router-dom'
import {routes} from '../../constants/routes'
import {getRouteMeta} from '../../app/router/routeConfig'
export function Breadcrumb(){const{pathname}=useLocation();const current=getRouteMeta(pathname);return <nav className="breadcrumb" aria-label="Breadcrumb"><ol><li><Link to={routes.dashboard}>HAU-EXAM</Link></li>{pathname!==routes.dashboard&&<li aria-current="page">{current?.title||'Trang'}</li>}</ol></nav>}
