import {NavLink,useLocation,useParams} from 'react-router-dom'
import {PageHeader} from '../../../components/shared/PageHeader'
import {useAuth} from '../../auth/hooks/useAuth'
import {DocumentsPage} from './DocumentsPage'
import {GeneratePage} from './GeneratePage'
import {JobsPage} from './JobsPage'
import {ChatPage} from './ChatPage'
import {AiJobPanel} from '../components/AiShared'
const tabs=[['/ai/generate','Tạo câu hỏi bằng AI'],['/ai/jobs','Tác vụ AI'],['/ai/analysis','Phân tích']]
export function AiWorkspacePage(){const{pathname}=useLocation();const{id}=useParams();const{role}=useAuth();const isUser=role==='USER';return <section className="ai-workspace"><PageHeader title="Không gian AI" description="Theo dõi tài liệu, tạo câu hỏi và trạng thái xử lý bằng AI."/><nav className="ai-tabs" aria-label="Khu vực AI">{isUser&&<NavLink to="/ai/documents" end>Tài liệu của tôi</NavLink>}{tabs.filter(([to])=>to!='/ai/analysis'||!isUser).map(([to,label])=><NavLink key={to} to={to} end>{label}</NavLink>)}{isUser&&<NavLink to="/chat" end>Trợ lý trao đổi</NavLink>}</nav><div key={`${role}:${pathname}`}>{id?<AiJobPanel id={id}/>:pathname==='/ai/documents'?<DocumentsPage/>:pathname==='/ai/jobs'?<JobsPage/>:pathname==='/ai/analysis'?<GeneratePage analysis/>:pathname==='/chat'?<ChatPage/>:<GeneratePage/>}</div></section>}
