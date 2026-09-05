import {Breadcrumb} from '../layout/Breadcrumb'
export function PageHeader({title,description,actions}){return <header className="page-header"><div><Breadcrumb/><h1>{title}</h1>{description&&<p>{description}</p>}</div>{actions&&<div className="page-actions">{actions}</div>}</header>}
