import { useLocation } from 'react-router-dom'
import { PublicHeader } from './PublicHeader'
import { PublicFooter } from './PublicFooter'
import { Seo } from '../shared/Seo'

export function PublicLayout({ children, seo }) {
  const location = useLocation()
  return <div className="public-layout">
    <a className="public-skip-link" href="#public-main">Bỏ qua điều hướng</a>
    {seo && <Seo {...seo} />}
    <PublicHeader />
    <main className="public-page-transition" id="public-main" key={location.pathname}>{children}</main>
    <PublicFooter />
  </div>
}
