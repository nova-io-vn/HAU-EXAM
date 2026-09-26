import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, DataTable, DashboardMetricSkeleton, TableSkeleton, ChartSkeleton, StatusBadge } from '../../../components/ui'
import { PageHeader } from '../../../components/shared/PageHeader'
import { QuestionStatusChart } from '../../../components/shared/QuestionStatusChart'
import { routes } from '../../../constants/routes'
import { useAuth } from '../../auth/hooks/useAuth'
import { catalogApi } from '../../questions/api/catalogApi'
import { questionsApi } from '../../questions/api/questionsApi'
import { normalizePage } from '../../questions/model/questionModel'

export function SubjectAdminDashboardPage() {
  const { facultyId, currentUser } = useAuth()
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const faculty = currentUser?.facultyName || currentUser?.faculty?.name || facultyId || 'Chưa phân công'
  const load = useCallback(async () => {
    setError(null)
    try {
      const [subjects, all, pending, counts] = await Promise.all([
        catalogApi.subjects(),
        questionsApi.list({ page: 0, size: 1 }),
        questionsApi.list({ status: 'PENDING_REVIEW', page: 0, size: 5, sort: 'createdAt,asc' }),
        questionsApi.statusCounts(),
      ])
      setData({ subjects, all: normalizePage(all), pending: normalizePage(pending), counts })
    } catch (reason) { setError(reason) }
  }, [])
  useEffect(() => { const timer = setTimeout(load, 0); return () => clearTimeout(timer) }, [load])

  if (error) return <section>
    <PageHeader title="Tổng quan chuyên môn" description={'Theo dõi nội dung trong Khoa ' + faculty + '.'} />
    <div className="surface request-error" role="alert"><strong>Không thể tải tổng quan chuyên môn</strong><p>{error.message}</p><Button variant="secondary" onClick={load}>Thử lại</Button></div>
  </section>
  if (!data) return <section className="subject-dashboard">
    <PageHeader title="Tổng quan chuyên môn" description={'Theo dõi nội dung trong Khoa ' + faculty + '.'} />
    <DashboardMetricSkeleton count={4} />
    <div className="subject-dashboard-grid"><section className="surface admin-panel"><TableSkeleton rows={5} columns={3} /></section><section className="surface admin-panel"><ChartSkeleton /></section></div>
  </section>

  const stats = [
    ['Môn học phụ trách', data.subjects.length, routes.subjects],
    ['Tổng số câu hỏi', data.all.totalElements, routes.questions],
    ['Chờ phê duyệt', data.pending.totalElements, routes.review],
    ['Đã phê duyệt', data.counts.APPROVED, routes.questions],
  ]
  return <section className="subject-dashboard">
    <PageHeader title="Tổng quan chuyên môn" description={'Theo dõi ngân hàng câu hỏi và xét duyệt nội dung trong Khoa ' + faculty + '.'} actions={<Link className="button button-primary" to={routes.review}>Mở hàng đợi xét duyệt</Link>} />
    <div className="admin-kpi-grid subject-kpi-grid">
      {stats.map(([label, value, to]) => <Link className="admin-kpi" to={to} key={label}><small>{label}</small><strong>{value.toLocaleString('vi-VN')}</strong><span>Xem chi tiết →</span></Link>)}
    </div>
    <div className="subject-dashboard-grid">
      <section className="surface admin-panel chart-card">
        <header><div><span className="eyebrow">NGÂN HÀNG CÂU HỎI</span><h2>Trạng thái câu hỏi trong Khoa</h2></div></header>
        <div className="chart-card-body"><QuestionStatusChart counts={data.counts} /></div>
      </section>
      <section className="surface admin-panel">
        <header><div><span className="eyebrow">CHUYÊN MÔN</span><h2>Môn học phụ trách</h2></div><Link to={routes.subjects}>Quản lý môn →</Link></header>
        <DataTable rows={data.subjects} emptyTitle="Chưa có môn học trong Khoa." columns={[
          { key: 'code', header: 'Mã môn', render: item => <span className="mono">{item.code}</span> },
          { key: 'name', header: 'Môn học' },
        ]} />
        <div className="dashboard-shortcuts">
          <Link to={routes.coverage}><strong>Độ bao phủ kiến thức</strong><small>Kiểm tra phân bố câu hỏi theo chương và độ khó.</small><span aria-hidden="true">→</span></Link>
        </div>
      </section>
    </div>
    <section className="surface admin-panel">
      <header><div><span className="eyebrow">CẦN XỬ LÝ</span><h2>Câu hỏi chờ phê duyệt</h2></div><Link to={routes.review}>Xem tất cả →</Link></header>
      <DataTable rows={data.pending.items} emptyTitle="Không có câu hỏi đang chờ phê duyệt." columns={[
        { key: 'id', header: 'Mã câu hỏi', render: item => <span className="mono">{item.id?.slice(0, 8)}</span> },
        { key: 'content', header: 'Nội dung', render: item => <span className="truncate-cell">{item.content}</span> },
        { key: 'subjectName', header: 'Môn học', render: item => item.subjectName || '—' },
        { key: 'status', header: 'Trạng thái', render: item => <StatusBadge status={item.status} /> },
        { key: 'action', header: '', render: () => <Link to={routes.review}>Xét duyệt →</Link> },
      ]} />
    </section>
  </section>
}
