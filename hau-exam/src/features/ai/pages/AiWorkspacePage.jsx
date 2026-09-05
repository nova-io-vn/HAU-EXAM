import {NavLink,useLocation,useParams} from 'react-router-dom'
import {PageHeader} from '../../../components/shared/PageHeader'
import {useAuth} from '../../auth/hooks/useAuth'
import {DocumentsPage} from './DocumentsPage'
import {GeneratePage} from './GeneratePage'
import {JobsPage} from './JobsPage'
import {ChatPage} from './ChatPage'
import {AiJobPanel} from '../components/AiShared'

const tabs=[['/ai/documents','Tài liệu'],['/ai/generate','Tạo câu hỏi'],['/ai/jobs','AI Jobs'],['/ai/analysis','Phân tích'],['/chat','Chatbot']]
export function AiWorkspacePage(){
  const {pathname}=useLocation();const {id}=useParams();const auth=useAuth()
  return <section className="ai-workspace"><PageHeader title="Không gian AI" description="Khai thác học liệu, tạo câu hỏi và theo dõi kết quả xử lý."/>
    <nav className="ai-tabs" aria-label="Khu vực AI">{tabs.filter(([to])=>auth.role==='USER'||!['/ai/documents','/chat'].includes(to)).map(([to,label])=><NavLink key={to} to={to}>{label}</NavLink>)}</nav>
    <div key={`${auth.currentUser?.id}:${pathname}`}>
      {id?<AiJobPanel id={id}/>:pathname==='/ai/documents'?<DocumentsPage/>:pathname==='/ai/jobs'?<JobsPage/>:pathname==='/ai/analysis'?<GeneratePage analysis/>:pathname==='/chat'?<ChatPage/>:<GeneratePage/>}
    </div>
  </section>
}
