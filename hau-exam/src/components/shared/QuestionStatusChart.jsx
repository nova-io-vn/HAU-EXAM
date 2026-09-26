import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { questionStatusLabels } from '../../utils/enumLabels'
import { ChartSkeleton } from '../ui'

const colors = {
  APPROVED: 'var(--chart-approved)',
  PENDING_REVIEW: 'var(--chart-pending)',
  NEED_REVISION: 'var(--chart-revision)',
  DRAFT: 'var(--chart-draft)',
  REJECTED: 'var(--chart-rejected)',
  ARCHIVED: 'var(--chart-archived)',
}
const numberFormat = new Intl.NumberFormat('vi-VN')
const percentFormat = new Intl.NumberFormat('vi-VN', { style: 'percent', maximumFractionDigits: 1 })

export function QuestionStatusChart({ counts, loading = false, error = false }) {
  if (loading) return <ChartSkeleton />
  if (error) return <div className="chart-error" role="alert"><strong>Không thể tải biểu đồ</strong><span>Vui lòng thử tải lại số liệu.</span></div>
  const data = Object.keys(colors)
    .map(status => ({ status, name: questionStatusLabels[status], value: Number(counts?.[status] ?? 0) }))
    .filter(item => Number.isFinite(item.value) && item.value > 0)
  const total = data.reduce((sum, item) => sum + item.value, 0)
  if (!total) return <div className="chart-empty">Chưa có dữ liệu để hiển thị biểu đồ.</div>

  return <figure className="question-status-chart" aria-label={`Phân bố trạng thái câu hỏi, tổng ${numberFormat.format(total)} câu hỏi`}>
    <div className="chart-visual">
      <ResponsiveContainer width="100%" height="100%" minWidth={0}>
        <PieChart>
          <Pie data={data} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius="70%" outerRadius="90%" paddingAngle={data.length > 1 ? 2 : 0} stroke="var(--surface)" strokeWidth={2} isAnimationActive={false}>
            {data.map(item => <Cell key={item.status} fill={colors[item.status]} />)}
          </Pie>
          <Tooltip formatter={(value, name) => [`${numberFormat.format(value)} câu hỏi`, name]} contentStyle={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 6, color: 'var(--text)', fontSize: 14 }} itemStyle={{ color: 'var(--text)' }} />
        </PieChart>
      </ResponsiveContainer>
      <div className="chart-center"><strong>{numberFormat.format(total)}</strong><span>Tổng câu hỏi</span></div>
    </div>
    <figcaption className="chart-summary">
      {data.map(item => <div key={item.status}>
        <span className="chart-dot" style={{ background: colors[item.status] }} aria-hidden="true" />
        <span>{item.name}</span>
        <strong>{numberFormat.format(item.value)}</strong>
        <span className="chart-percentage">{percentFormat.format(item.value / total)}</span>
      </div>)}
    </figcaption>
  </figure>
}
