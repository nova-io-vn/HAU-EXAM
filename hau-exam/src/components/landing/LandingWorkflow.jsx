import { useEffect, useState } from 'react'
import workflowPlaceholder from '../../assets/Kute.gif'

// TODO: replace with dedicated workflow animation assets (step1.gif ... step4.gif).
const workflowSteps = [
  {
    id: 1,
    title: 'Tạo câu hỏi',
    description: 'Soạn câu hỏi thủ công hoặc sử dụng AI hỗ trợ.',
    image: workflowPlaceholder,
  },
  {
    id: 2,
    title: 'Phê duyệt chuyên môn',
    description: 'Gửi câu hỏi để Quản trị viên chuyên môn của Khoa xem xét.',
    image: workflowPlaceholder,
  },
  {
    id: 3,
    title: 'Xây dựng ma trận đề',
    description: 'Thiết lập số lượng câu hỏi theo chủ đề và độ khó.',
    image: workflowPlaceholder,
  },
  {
    id: 4,
    title: 'Tạo & quản lý đề thi',
    description: 'Sinh đề từ các câu hỏi đã được phê duyệt và quản lý phiên bản.',
    image: workflowPlaceholder,
  },
]

export function LandingWorkflow() {
  const [active, setActive] = useState(0)
  const [paused, setPaused] = useState(false)

  useEffect(() => {
    if (paused || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return undefined
    const timer = window.setInterval(() => setActive((value) => (value + 1) % workflowSteps.length), 5000)
    return () => window.clearInterval(timer)
  }, [paused])

  const current = workflowSteps[active]
  return (
    <section className="public-section landing-workflow" aria-labelledby="landing-workflow-title">
      <div className="landing-workflow-heading">
        <span className="eyebrow">QUY TRÌNH LÀM VIỆC</span>
        <h2 id="landing-workflow-title">Từ câu hỏi đến đề thi trong một quy trình thống nhất.</h2>
      </div>
      <div
        className="landing-workflow-grid"
        onMouseEnter={() => setPaused(true)}
        onMouseLeave={() => setPaused(false)}
        onFocusCapture={() => setPaused(true)}
        onBlurCapture={(event) => {
          if (!event.currentTarget.contains(event.relatedTarget)) setPaused(false)
        }}
      >
        <figure className="landing-workflow-visual">
          <img src={current.image} alt="Minh họa động cho quy trình quản lý câu hỏi và đề thi" width="372" height="346" loading="lazy" />
          <figcaption>Bước {current.id}: {current.title}</figcaption>
        </figure>
        <div className="landing-workflow-steps" role="tablist" aria-label="Các bước trong quy trình HAU Exam">
          {workflowSteps.map((step, index) => (
            <button
              key={step.id}
              type="button"
              role="tab"
              aria-selected={active === index}
              className={active === index ? 'is-active' : ''}
              onClick={() => setActive(index)}
              onMouseEnter={() => setActive(index)}
            >
              <span>0{step.id}</span>
              <span><strong>{step.title}</strong><small>{step.description}</small></span>
            </button>
          ))}
        </div>
      </div>
    </section>
  )
}
