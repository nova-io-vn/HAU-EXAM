import {useCallback,useEffect,useState} from 'react'
import {Link} from 'react-router-dom'
import {Button,DataTable,Loading,StatusBadge} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {routes} from '../../../constants/routes'
import {useAuth} from '../../auth/hooks/useAuth'
import {questionsApi} from '../../questions/api/questionsApi'
import {aiApi} from '../../ai/api/aiApi'
import {notificationsApi} from '../../notifications/api/notificationsApi'
import {normalizePage,formatDateTime} from '../model/userModel'
import {formatAcademicName} from '../model/academic'

const statusLabels={APPROVED:'Đã duyệt',PENDING_REVIEW:'Đang chờ duyệt',NEED_REVISION:'Cần chỉnh sửa',DRAFT:'Bản nháp',REJECTED:'Từ chối'}
const sourceLabels={AI:'AI hỗ trợ',AI_GENERATED:'AI hỗ trợ',MANUAL:'Thủ công'}

export function UserDashboardPage(){
  const{currentUser,facultyId}=useAuth()
  const[data,setData]=useState(null)
  const[error,setError]=useState(null)
  const load=useCallback(async()=>{try{const[all,approved,pending,revision,draft,documents,notifications]=await Promise.all([questionsApi.list({page:0,size:5}),questionsApi.list({status:'APPROVED',page:0,size:1}),questionsApi.list({status:'PENDING_REVIEW',page:0,size:1}),questionsApi.list({status:'NEED_REVISION',page:0,size:1}),questionsApi.list({status:'DRAFT',page:0,size:1}),aiApi.documents(0),notificationsApi.list({page:0,size:5})]);setData({all:normalizePage(all),approved:normalizePage(approved),pending:normalizePage(pending),revision:normalizePage(revision),draft:normalizePage(draft),documents:normalizePage(documents),notifications:normalizePage(notifications)})}catch(e){setError(e)}},[])
  useEffect(()=>{const timer=setTimeout(load,0);return()=>clearTimeout(timer)},[load])
  const name=formatAcademicName(currentUser||{})
  const faculty=currentUser?.facultyName||currentUser?.faculty?.name||facultyId||'Khoa của bạn'
  const header=<PageHeader title={`Xin chào, ${name}`} description={`Theo dõi ngân hàng câu hỏi và học liệu trong ${faculty}.`}/>
  if(error)return <section>{header}<div className="surface admin-error"><p>Không thể tải dữ liệu tổng quan.</p><Button onClick={load}>Thử lại</Button></div></section>
  if(!data)return <section>{header}<div className="surface admin-loading"><Loading label="Đang tải không gian giảng viên"/></div></section>
  const stats=[['Tổng câu hỏi',data.all.totalElements,routes.myQuestions],['Đã phê duyệt',data.approved.totalElements,routes.myQuestions],['Đang chờ duyệt',data.pending.totalElements,routes.myQuestions],['Cần chỉnh sửa',data.revision.totalElements,routes.myQuestions],['Tài liệu học thuật',data.documents.totalElements,routes.documents]]
  const chart=[['approved','Đã phê duyệt',data.approved.totalElements],['pending','Đang chờ duyệt',data.pending.totalElements],['revision','Cần chỉnh sửa',data.revision.totalElements],['draft','Bản nháp',data.draft.totalElements]]
  const max=Math.max(...chart.map(([, ,value])=>value),1)
  return <section className="user-dashboard"><PageHeader title={`Xin chào, ${name}`} description={`Theo dõi ngân hàng câu hỏi và học liệu trong ${faculty}.`} actions={<><Link className="button button-secondary" to={routes.documents}>Tải tài liệu</Link><Link className="button button-secondary" to={routes.generate}>Tạo bằng AI</Link><Link className="button button-primary" to={routes.newQuestion}>Tạo câu hỏi mới</Link></>}/><div className="admin-kpi-grid user-kpi-grid">{stats.map(([label,value,to])=><Link className="admin-kpi" to={to} key={label}><span className="kpi-accent"/><small>{label}</small><strong>{value.toLocaleString('vi-VN')}</strong><span>Xem chi tiết →</span></Link>)}</div><div className="user-dashboard-grid"><section className="surface admin-panel"><header><div><span className="eyebrow">TỔNG QUAN</span><h2>Phân bố trạng thái câu hỏi</h2></div></header><div className="status-chart">{chart.map(([key,label,value])=><div className="status-chart-row" key={key}><span>{label}</span><div className="status-chart-track"><i className={`status-chart-bar ${key}`} style={{width:`${value?Math.max(8,value/max*100):0}%`}}/></div><strong>{value}</strong></div>)}</div></section><section className="surface admin-panel"><header><div><span className="eyebrow">THÔNG BÁO</span><h2>Thông báo mới</h2></div><Link to={routes.notifications}>Xem tất cả →</Link></header><DataTable rows={data.notifications.items} emptyTitle="Chưa có thông báo mới." columns={[{key:'title',header:'Thông báo'},{key:'createdAt',header:'Thời gian',render:n=>formatDateTime(n.createdAt)}]}/></section></div><section className="surface admin-panel"><header><div><span className="eyebrow">CÂU HỎI GẦN ĐÂY</span><h2>Câu hỏi gần đây</h2></div><Link to={routes.myQuestions}>Xem tất cả →</Link></header><DataTable rows={data.all.items} emptyTitle="Bạn chưa có câu hỏi nào." columns={[{key:'id',header:'Mã câu hỏi',render:q=><span className="mono">{q.id?.slice(0,8)}</span>},{key:'content',header:'Nội dung',render:q=><span className="truncate-cell">{q.content}</span>},{key:'subjectId',header:'Học phần',render:q=>q.subjectName||q.subjectId||'—'},{key:'difficulty',header:'Mức độ',render:q=><StatusBadge status={q.difficulty}/>},{key:'source',header:'Nguồn',render:q=>sourceLabels[q.source]||q.source||'Thủ công'},{key:'status',header:'Trạng thái',render:q=>statusLabels[q.status]||q.status},{key:'updatedAt',header:'Cập nhật',render:q=>formatDateTime(q.updatedAt)}]}/></section></section>
}
