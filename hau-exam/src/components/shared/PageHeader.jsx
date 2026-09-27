import {Breadcrumb} from '../layout/Breadcrumb'
import {useLocation} from 'react-router-dom'
import {getRouteMeta} from '../../app/router/routeConfig'
export function PageHeader({title,description,actions}){const{pathname}=useLocation();const meta=getRouteMeta(pathname);return <header className="page-header"><div><Breadcrumb/><h1 data-tour="page-heading">{title||meta?.title||'Trang'}</h1>{description&&<p>{description}</p>}</div>{actions&&<div className="page-actions">{actions}</div>}</header>}
