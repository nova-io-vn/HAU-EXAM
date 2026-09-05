import {useState} from 'react'
import {Outlet} from 'react-router-dom'
import {AppShell} from '../../components/layout/AppShell'
import {Header} from '../../components/layout/Header'
import {Sidebar} from '../../components/layout/Sidebar'
import {NotificationProvider} from '../../features/notifications'
export function AppLayout(){const[collapsed,setCollapsed]=useState(false);const[mobileOpen,setMobileOpen]=useState(false);return <NotificationProvider><AppShell className={collapsed?'is-collapsed':''}><Sidebar collapsed={collapsed} mobileOpen={mobileOpen} onClose={()=>setMobileOpen(false)}/><div className="app-frame"><Header collapsed={collapsed} onToggle={()=>setCollapsed(value=>!value)} onMobileMenu={()=>setMobileOpen(true)}/><main className="app-content"><Outlet/></main></div></AppShell></NotificationProvider>}
