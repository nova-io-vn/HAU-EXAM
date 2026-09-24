import { PublicLayout } from '../../components/layout/PublicLayout'
import { MediaImage } from '../../components/shared/MediaImage'
import { LANDING_MEDIA } from '../../config/landingMedia'

export function PublicInfoPage({ title }) {
  const isSupport = title === 'Trung tâm hỗ trợ'
  return <PublicLayout><section className={`public-info-card ${isSupport ? 'public-info-with-visual' : ''}`}>
    {isSupport && <MediaImage className="public-info-visual" src={LANDING_MEDIA.support} alt="Hình ảnh minh họa hỗ trợ và cộng tác trong môi trường giáo dục" width={1000} height={650} />}
    <div><span className="eyebrow">HAU-EXAM</span><h1>{title}</h1><p>Thông tin được trình bày rõ ràng để giảng viên và quản trị viên sử dụng hệ thống đúng quy trình.</p><h2>Hướng dẫn chung</h2><p>HAU-EXAM hỗ trợ quản lý học liệu, tạo câu hỏi, phê duyệt chuyên môn và theo dõi ngân hàng câu hỏi theo phạm vi Khoa.</p><h2>Cần hỗ trợ thêm?</h2><p>Gửi yêu cầu tới đội ngũ hỗ trợ qua biểu mẫu liên hệ. Không chia sẻ mật khẩu hoặc mã xác thực trong nội dung yêu cầu.</p></div>
  </section></PublicLayout>
}
