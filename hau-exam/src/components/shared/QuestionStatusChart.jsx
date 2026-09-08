import {Cell,Legend,Pie,PieChart,ResponsiveContainer,Tooltip} from 'recharts'
import {questionStatusLabels} from '../../utils/enumLabels'
import {ChartSkeleton} from '../ui'

const colors={APPROVED:'var(--hau-success)',PENDING_REVIEW:'var(--hau-warning)',NEED_REVISION:'var(--hau-primary)',DRAFT:'var(--text-muted)',REJECTED:'var(--hau-danger)'}
const order=['APPROVED','PENDING_REVIEW','NEED_REVISION','DRAFT','REJECTED']

export function QuestionStatusChart({counts,loading=false,error=false}){
  if(loading)return <ChartSkeleton/>
  if(error)return <div className="chart-error" role="alert"><strong>Không thể tải biểu đồ</strong><span>Dữ liệu biểu đồ chưa sẵn sàng. Các khu vực khác vẫn hoạt động.</span></div>
  const data=order.map(status=>({status,name:questionStatusLabels[status]||status,value:counts?.[status]||0})).filter(item=>item.value>0)
  const total=data.reduce((sum,item)=>sum+item.value,0)
  if(!total)return <div className="chart-empty">Chưa có dữ liệu để hiển thị biểu đồ.</div>
  return <div className="question-status-chart" aria-label={`Phân bố trạng thái câu hỏi, tổng ${total} câu hỏi`}>
    <div className="chart-visual"><ResponsiveContainer width="100%" height={250}><PieChart><Pie data={data} dataKey="value" nameKey="name" innerRadius={72} outerRadius={94} paddingAngle={2} stroke="var(--surface)" strokeWidth={3}>{data.map(item=><Cell key={item.status} fill={colors[item.status]||'var(--hau-primary)'}/>)}</Pie><Tooltip formatter={(value,name)=>[`${value} câu hỏi`,name]} contentStyle={{background:'var(--surface)',border:'1px solid var(--border)',borderRadius:6,color:'var(--text)'}}/><Legend verticalAlign="bottom" height={32} formatter={value=><span className="chart-legend-label">{value}</span>}/></PieChart></ResponsiveContainer><div className="chart-center"><strong>{total.toLocaleString('vi-VN')}</strong><span>Tổng câu hỏi</span></div></div>
    <div className="chart-summary">{data.map(item=><div key={item.status}><span className="chart-dot" style={{background:colors[item.status]}}/><span>{item.name}</span><strong>{item.value.toLocaleString('vi-VN')} · {Math.round(item.value/total*100)}%</strong></div>)}</div>
  </div>
}
