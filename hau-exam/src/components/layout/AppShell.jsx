import {classNames} from '../../utils/classNames'

export function AppShell({children,className}){
  return <div className={classNames('app-shell',className)}>{children}</div>
}

