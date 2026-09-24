import { useState } from 'react'

export function MediaImage({ src, alt, className = '', loading = 'lazy', width, height }) {
  const [failed, setFailed] = useState(false)
  if (failed) return <div className={`${className} media-image-fallback`} role="img" aria-label={alt}>Hình ảnh minh họa</div>
  return <img className={className} src={src} alt={alt} loading={loading} width={width} height={height} onError={() => setFailed(true)} />
}

