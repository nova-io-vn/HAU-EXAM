import { useEffect } from 'react'
import banner from '../../assets/banner.jpg'

const SITE_URL = 'https://exam.nova.io.vn'

function setMeta(name, content, attribute = 'name') {
  if (!content) return
  let element = document.head.querySelector(`meta[${attribute}="${name}"]`)
  if (!element) { element = document.createElement('meta'); element.setAttribute(attribute, name); document.head.appendChild(element) }
  element.setAttribute('content', content)
}

export function Seo({ title, description, path = '/', image = banner, noindex = false, jsonLd }) {
  useEffect(() => {
    const canonicalUrl = `${SITE_URL}${path === '/' ? '/' : path}`
    const socialImage = image.startsWith('/src/assets/') ? banner : image
    document.title = title
    document.documentElement.lang = 'vi'
    setMeta('description', description)
    setMeta('robots', noindex ? 'noindex, nofollow' : 'index, follow')
    setMeta('og:type', 'website', 'property'); setMeta('og:title', title, 'property'); setMeta('og:description', description, 'property'); setMeta('og:url', canonicalUrl, 'property'); setMeta('og:image', socialImage, 'property'); setMeta('og:site_name', 'HAU Exam', 'property')
    setMeta('twitter:card', 'summary_large_image'); setMeta('twitter:title', title); setMeta('twitter:description', description); setMeta('twitter:image', socialImage)
    let canonical = document.head.querySelector('link[rel="canonical"]')
    if (!canonical) { canonical = document.createElement('link'); canonical.rel = 'canonical'; document.head.appendChild(canonical) }
    canonical.href = canonicalUrl
    document.head.querySelector('script[data-hau-jsonld]')?.remove()
    if (jsonLd) { const script = document.createElement('script'); script.type = 'application/ld+json'; script.dataset.hauJsonld = 'true'; script.textContent = JSON.stringify(jsonLd); document.head.appendChild(script) }
  }, [description, image, jsonLd, noindex, path, title])
  return null
}

export { SITE_URL }
