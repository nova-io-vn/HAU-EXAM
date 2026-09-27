import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import { useBranding } from '../../features/branding/brandingContext'
import { getRouteMeta } from './routeConfig'

export function RouteIdentity() {
  const { pathname } = useLocation()
  const branding = useBranding()
  const meta = getRouteMeta(pathname)

  useEffect(() => {
    document.title = meta?.documentTitle ? `${meta.documentTitle} | ${branding.shortName}` : branding.systemName
  }, [branding.shortName, branding.systemName, meta])

  return null
}
