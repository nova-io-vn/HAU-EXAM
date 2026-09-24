import { useEffect, useState } from 'react'
import { PublicLayout } from '../../components/layout/PublicLayout'
import { Icon } from '../../components/ui/Icon'
import { DOWNLOAD_CONFIG } from '../../config/download'
import { LANDING_MEDIA } from '../../config/landingMedia'
import { MediaImage } from '../../components/shared/MediaImage'

const features = [
  { icon: 'clock', title: 'Truy cập mọi lúc', description: 'Truy cập hệ thống ngay trên điện thoại.' },
  { icon: 'check', title: 'Quản lý thuận tiện', description: 'Theo dõi câu hỏi và bài thi thuận tiện hơn.' },
  { icon: 'bell', title: 'Thông báo nhanh', description: 'Nhận các thông báo quan trọng từ hệ thống.' },
  { icon: 'refresh', title: 'Đồng bộ tài khoản', description: 'Sử dụng cùng tài khoản HAU-EXAM trên Web và Mobile.' },
]

const installationSteps = {
  android: [
    ['01', 'Tải xuống APK', 'Nhấn “Tải xuống APK” để tải file HAU-EXAM.apk khi phiên bản được phát hành.'],
    ['02', 'Mở file APK', 'Sau khi tải hoàn tất, mở file vừa tải trên thiết bị.'],
    ['03', 'Cho phép cài đặt', 'Nếu Android yêu cầu, cho phép trình duyệt hoặc trình quản lý tệp cài ứng dụng từ nguồn này.'],
    ['04', 'Chọn Cài đặt', 'Xác nhận cài đặt HAU-EXAM trên thiết bị.'],
    ['05', 'Đăng nhập', 'Mở HAU-EXAM và đăng nhập bằng tài khoản của bạn.'],
  ],
  ios: [
    ['01', 'Cài Expo Go', 'Cài Expo Go từ App Store trên iPhone.'],
    ['02', 'Mở Expo Go', 'Mở Expo Go và sẵn sàng quét mã.'],
    ['03', 'Quét QR', 'Quét mã QR HAU-EXAM được hiển thị ở trên.'],
    ['04', 'Tải project', 'Chờ project HAU-EXAM được tải và mở.'],
    ['05', 'Bắt đầu trải nghiệm', 'Đăng nhập và bắt đầu sử dụng HAU-EXAM.'],
  ],
}

function useDownloadMetadata() {
  useEffect(() => {
    const previousTitle = document.title
    let description = document.querySelector('meta[name="description"]')
    const previousDescription = description?.getAttribute('content') ?? null
    const createdDescription = !description

    document.title = 'HAU-EXAM Mobile | Tải ứng dụng'
    if (!description) {
      description = document.createElement('meta')
      description.setAttribute('name', 'description')
      document.head.appendChild(description)
    }
    description.setAttribute('content', 'Tải ứng dụng HAU-EXAM dành cho thiết bị di động.')

    return () => {
      document.title = previousTitle
      if (createdDescription) description.remove()
      else if (previousDescription === null) description.removeAttribute('content')
      else description.setAttribute('content', previousDescription)
    }
  }, [])
}

function PlatformAction({ platform, className = '' }) {
  const config = DOWNLOAD_CONFIG[platform]

  if (config.enabled && config.downloadUrl) {
    return (
      <a className={`button button-primary ${className}`} href={config.downloadUrl}>
        <Icon name="download" size={18} />
        {platform === 'android' ? 'Tải xuống APK' : 'Mở bằng Expo Go'}
      </a>
    )
  }

  if (platform === 'ios' && config.expoGoStoreUrl) {
    return <a className={`button button-secondary ${className}`} href={config.expoGoStoreUrl} target="_blank" rel="noreferrer">Cài Expo Go</a>
  }

  return (
    <button
      className={`button button-secondary ${className}`}
      type="button"
      disabled
      aria-label={`${platform === 'android' ? 'Tải xuống APK' : 'Mở bằng Expo Go'} - ${config.releaseStatus}`}
    >
      {config.releaseStatus}
    </button>
  )
}

function formatMetadata(value) {
  return value || 'Chưa xác định'
}

function formatDate(value) {
  if (!value) return 'Chưa xác định'
  return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(value))
}

function DownloadDetails({ platform, onPlatformChange }) {
  const config = DOWNLOAD_CONFIG[platform]
  const isAndroid = platform === 'android'
  const details = isAndroid
    ? [['Phiên bản', formatMetadata(config.version)], ['Build number', formatMetadata(config.build)], ['Dung lượng', formatMetadata(config.size)], ['Cập nhật', formatDate(config.updatedAt)], ['Yêu cầu', formatMetadata(config.minOs)], ['Định dạng', config.fileType]]
    : [['Phiên bản', formatMetadata(config.version)], ['Expo SDK', config.expoSdk], ['Cập nhật', formatDate(config.updatedAt)], ['Phương thức', config.method]]

  return <section className="download-section download-details-section" aria-labelledby="download-details-title">
    <div className="public-container">
      <div className="download-section-heading"><p className="download-kicker">THÔNG TIN PHIÊN BẢN</p><h2 id="download-details-title">Tải ứng dụng trực tiếp</h2><p>Thông tin phát hành được quản lý tập trung và chỉ mở liên kết tải khi phiên bản chính thức sẵn sàng.</p></div>
      <div className="download-tabs" role="tablist" aria-label="Chọn nền tảng ứng dụng">
        {['android', 'ios'].map((item) => <button key={item} id={`download-detail-tab-${item}`} type="button" role="tab" aria-selected={platform === item} aria-controls={`download-detail-panel-${item}`} onClick={() => onPlatformChange(item)}>{item === 'android' ? 'Android' : 'iOS'}</button>)}
      </div>
      <div className="download-detail-panel" id={`download-detail-panel-${platform}`} role="tabpanel" aria-labelledby={`download-detail-tab-${platform}`} key={platform}>
        <div className="download-detail-heading"><div><p className="download-card-platform">HAU-EXAM</p><h3>{isAndroid ? 'Android' : 'iOS'}</h3><span className={`download-status ${config.enabled ? 'download-status-ready' : ''}`}>{config.releaseStatus}</span></div><PlatformAction platform={platform} className="download-detail-action" /></div>
        <dl className="download-metadata">{details.map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl>
        {isAndroid ? <div className="download-whats-new"><h4>What's New</h4><ul>{config.whatsNew.map((item) => <li key={item}>{item}</li>)}</ul></div> : <div className="download-ios-expo"><div className="download-qr-shell"><img className="download-qr" src={config.qrImage} alt="Mã QR để mở HAU-EXAM bằng Expo Go" width="280" height="280" /></div><p className="download-qr-caption">Quét mã bằng Expo Go để trải nghiệm HAU-EXAM</p><div className="download-ios-actions"><a className="button button-primary" href={config.expoGoStoreUrl} target="_blank" rel="noreferrer">Cài Expo Go</a></div><p className="download-ios-warning">Phiên bản iOS hiện được cung cấp cho mục đích trải nghiệm thông qua Expo Go. QR yêu cầu phiên Expo/Metro tương ứng đang hoạt động.</p></div>}
      </div>
    </div>
  </section>
}

function InstallationGuide() {
  const [platform, setPlatform] = useState('android')
  const isIos = platform === 'ios'
  const selectPlatform = (nextPlatform) => {
    setPlatform(nextPlatform)
    requestAnimationFrame(() => document.getElementById(`installation-tab-${nextPlatform}`)?.focus())
  }
  const handleTabKeyDown = (event) => {
    if (!['ArrowLeft', 'ArrowRight'].includes(event.key)) return
    event.preventDefault()
    selectPlatform(platform === 'android' ? 'ios' : 'android')
  }

  return (
    <section className="download-section download-install" aria-labelledby="download-install-title">
      <div className="public-container">
        <div className="download-install-heading">
          <div className="download-section-heading">
            <p className="download-kicker">BẮT ĐẦU TRÊN THIẾT BỊ</p>
            <h2 id="download-install-title">Hướng dẫn cài đặt</h2>
            <p>Chọn nền tảng để xem các bước cài đặt phù hợp.</p>
          </div>
          <div className="download-tabs" role="tablist" aria-label="Chọn hướng dẫn cài đặt">
            {['android', 'ios'].map((item) => (
              <button
                key={item}
                id={`installation-tab-${item}`}
                type="button"
                role="tab"
                aria-selected={platform === item}
                aria-controls={`installation-panel-${item}`}
                tabIndex={platform === item ? 0 : -1}
                onClick={() => setPlatform(item)}
                onKeyDown={handleTabKeyDown}
              >
                {item === 'android' ? 'Android' : 'iOS'}
              </button>
            ))}
          </div>
        </div>

        <div
          className="download-install-panel"
          id={`installation-panel-${platform}`}
          role="tabpanel"
          aria-labelledby={`installation-tab-${platform}`}
          tabIndex="0"
          key={platform}
        >
          {isIos && (
            <div className="download-ios-note" role="note">
              <span className="download-status download-status-ready">Expo Go</span>
              <p>Quét QR bằng Expo Go để tải và mở project HAU-EXAM. Phiên Expo/Metro tương ứng cần đang hoạt động.</p>
            </div>
          )}
          <ol className="download-step-grid">
            {installationSteps[platform].map(([number, title, description]) => (
              <li key={number}>
                <span>{number}</span>
                <div>
                  <h3>{title}</h3>
                  <p>{description}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </div>
    </section>
  )
}

export function DownloadPage() {
  useDownloadMetadata()
  const [selectedPlatform, setSelectedPlatform] = useState('android')
  const androidAvailable = DOWNLOAD_CONFIG.android.enabled && Boolean(DOWNLOAD_CONFIG.android.downloadUrl)
  const iosAvailable = DOWNLOAD_CONFIG.ios.enabled && Boolean(DOWNLOAD_CONFIG.ios.qrImage)
  const selectPlatform = (platform) => setSelectedPlatform(platform)

  return (
    <PublicLayout>
      <div className="download-page">
        <section className="download-hero" aria-labelledby="download-hero-title">
          <div className="public-container download-hero-grid">
            <div className="download-hero-copy">
              <div className="download-platform-badges" aria-label="Tình trạng nền tảng">
                <span>Android · {androidAvailable ? 'Sẵn sàng' : 'Đang chuẩn bị'}</span>
                <span className="is-muted">iOS · {iosAvailable ? 'Sẵn sàng' : 'Sắp ra mắt'}</span>
              </div>
              <p className="download-kicker">HAU-EXAM MOBILE</p>
              <h1 id="download-hero-title">HAU-EXAM trên thiết bị di động</h1>
              <p className="download-lead">
                Truy cập ngân hàng câu hỏi, quản lý bài thi và nhận thông báo mọi lúc, mọi nơi.
              </p>
              <div className="download-hero-actions">
                <PlatformAction platform="android" className="download-primary-action" />
                <a className="button button-ghost download-guide-action" href="#installation-guide">
                  Xem hướng dẫn cài đặt
                </a>
              </div>
              <p className="download-release-note">
                <Icon name="check" size={15} />
                {androidAvailable
                  ? 'APK được tải trực tiếp từ bản phát hành chính thức.'
                  : 'APK chưa được phát hành; nút tải sẽ mở khi bản chính thức sẵn sàng.'}
              </p>
            </div>

            <aside className="download-availability" aria-label="Thông tin phát hành ứng dụng">
              <div className="download-phone-visual"><div className="download-phone-notch" /><MediaImage src={LANDING_MEDIA.mobile} alt="Hình ảnh minh họa trải nghiệm HAU-EXAM trên điện thoại" width={700} height={900} /></div>
              <div className="download-availability-heading">
                <span className="download-availability-icon" aria-hidden="true">
                  <Icon name="smartphone" size={22} />
                </span>
                <div>
                  <span>Ứng dụng di động</span>
                  <strong>{androidAvailable ? 'Sẵn sàng cho Android' : 'Đang chuẩn bị bản Android'}</strong>
                </div>
              </div>
              <div className="download-availability-list">
                <div>
                  <span className="platform-mark platform-mark-android" aria-hidden="true">A</span>
                  <p><strong>Android</strong><span>APK trực tiếp · Phiên bản mới nhất</span></p>
                  <span className={`download-status ${androidAvailable ? 'download-status-ready' : ''}`}>
                    {androidAvailable ? 'Sẵn sàng' : 'Đang chuẩn bị'}
                  </span>
                </div>
                <div>
                  <span className="platform-mark" aria-hidden="true">i</span>
                  <p><strong>iOS</strong><span>iPhone &amp; iPad</span></p>
                  <span className={`download-status ${iosAvailable ? 'download-status-ready' : ''}`}>
                    {iosAvailable ? 'Sẵn sàng' : 'Sắp ra mắt'}
                  </span>
                </div>
              </div>
              <p className="download-availability-caption">
                Một tài khoản HAU-EXAM, liền mạch giữa Web và Mobile.
              </p>
            </aside>
          </div>
        </section>

        <section className="download-section" id="download" aria-labelledby="download-title">
          <div className="public-container">
            <div className="download-section-heading">
              <p className="download-kicker">CHỌN NỀN TẢNG</p>
              <h2 id="download-title">Tải ứng dụng</h2>
              <p>Chọn phiên bản phù hợp với thiết bị của bạn.</p>
            </div>
            <div className="download-card-grid">
              <article className={`download-card download-card-android ${selectedPlatform === 'android' ? 'is-selected' : ''}`} role="button" tabIndex="0" onClick={() => selectPlatform('android')} onKeyDown={(event) => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); selectPlatform('android') } }}>
                <div className="download-card-topline">
                  <span className="platform-mark platform-mark-android" aria-hidden="true">A</span>
                  <span className={`download-status ${androidAvailable ? 'download-status-ready' : ''}`}>
                    {androidAvailable ? 'Sẵn sàng' : 'Đang chuẩn bị'}
                  </span>
                </div>
                <div>
                  <p className="download-card-platform">Android</p>
                  <h3>APK trực tiếp</h3>
                  <p>File APK chính thức, tải trực tiếp khi phiên bản sẵn sàng.</p>
                </div>
                <PlatformAction platform="android" className="download-card-button" />
              </article>

              <article className={`download-card download-card-ios ${selectedPlatform === 'ios' ? 'is-selected' : ''}`} role="button" tabIndex="0" onClick={() => selectPlatform('ios')} onKeyDown={(event) => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); selectPlatform('ios') } }}>
                <div className="download-card-topline">
                  <span className="platform-mark" aria-hidden="true">i</span>
                  <span className={`download-status ${iosAvailable ? 'download-status-ready' : ''}`}>
                    {iosAvailable ? 'Sẵn sàng' : 'Sắp ra mắt'}
                  </span>
                </div>
                <div>
                  <p className="download-card-platform">iOS</p>
                  <h3>iPhone &amp; iPad</h3>
                  <p>Trải nghiệm project HAU-EXAM thông qua Expo Go và QR.</p>
                </div>
                <PlatformAction platform="ios" className="download-card-button" />
              </article>
            </div>
          </div>
        </section>

        <section className="download-section download-features" aria-labelledby="download-features-title">
          <div className="public-container">
            <div className="download-section-heading">
              <p className="download-kicker">HAU-EXAM MOBILE</p>
              <h2 id="download-features-title">Làm việc liền mạch khi di chuyển</h2>
            </div>
            <div className="download-feature-grid">
              {features.map((feature) => (
                <article className="download-feature-card" key={feature.title}>
                  <span className="download-feature-icon" aria-hidden="true">
                    <Icon name={feature.icon} size={20} />
                  </span>
                  <h3>{feature.title}</h3>
                  <p>{feature.description}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        <DownloadDetails platform={selectedPlatform} onPlatformChange={selectPlatform} />

        <div id="installation-guide">
          <InstallationGuide />
        </div>
      </div>
    </PublicLayout>
  )
}
