import {useCallback,useEffect,useState} from 'react'
import {Button,DataTable,Dialog,Select,StatusBadge} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {adminSupportApi} from '../api/adminSupportApi'
const statuses=['NEW','IN_PROGRESS','REPLIED','CLOSED']
export function ContactAdminPage(){
  const[data,setData]=useState(null);const[error,setError]=useState('');const[selected,setSelected]=useState(null);const[reply,setReply]=useState('');const[busy,setBusy]=useState(false)
  const load=useCallback(async()=>{try{setError('');setData(await adminSupportApi.list())}catch(reason){setError(reason.message||'Không thể tải yêu cầu liên hệ.')}},[])
  useEffect(()=>{const timer=setTimeout(()=>{void load()},0);return()=>clearTimeout(timer)},[load])
  async function sendReply(){if(!selected||!reply.trim())return;setBusy(true);try{await adminSupportApi.reply(selected.id,reply);setSelected(null);setReply('');await load()}catch(reason){setError(reason.message||'Không thể gửi phản hồi.')}finally{setBusy(false)}}
  async function setStatus(value){if(!selected)return;setBusy(true);try{await adminSupportApi.status(selected.id,value);setSelected(null);await load()}catch(reason){setError(reason.message||'Không thể cập nhật trạng thái.')}finally{setBusy(false)}}
  const rows=data?.content||data?.data?.content||data?.data||[]
  return <section><PageHeader title="Yêu cầu liên hệ" description="Tiếp nhận và phản hồi yêu cầu hỗ trợ từ người dùng."/><div className="surface admin-panel">{error&&<p className="editor-error" role="alert">{error}</p>}{!data?<p>Đang tải dữ liệu…</p>:<DataTable rows={rows} emptyTitle="Chưa có yêu cầu liên hệ." columns={[{key:'name',header:'Người gửi'},{key:'email',header:'Email'},{key:'subject',header:'Chủ đề'},{key:'status',header:'Trạng thái',render:item=><StatusBadge status={item.status}/>},{key:'createdAt',header:'Ngày gửi'},{key:'id',header:'',render:item=><Button variant="secondary" onClick={()=>{setSelected(item);setReply('')}}>Xem chi tiết</Button>}]}/>}</div><Dialog open={Boolean(selected)} title={selected?.subject||'Chi tiết yêu cầu'} onClose={()=>{if(!busy)setSelected(null)}}>{selected&&<div className="contact-detail"><p><strong>{selected.name}</strong> · {selected.email}</p><p>{selected.message}</p><Select label="Trạng thái" value={selected.status} options={statuses.map(value=>({value,label:value}))} onChange={event=>void setStatus(event.target.value)}/><label className="field"><span>Phản hồi</span><textarea rows="5" value={reply} onChange={event=>setReply(event.target.value)}/></label><Button loading={busy} disabled={!reply.trim()||busy} onClick={()=>void sendReply()}>Gửi phản hồi</Button></div>}</Dialog></section>
}
