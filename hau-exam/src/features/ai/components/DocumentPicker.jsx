import {useEffect,useState} from 'react'
import {Button,Loading,Select} from '../../../components/ui'
import {aiApi} from '../api/aiApi'
import {AiError} from './AiShared'

export function DocumentPicker({value,onChange,optional=false,disabled=false}) {
  const [page,setPage]=useState(0)
  const [state,setState]=useState({loading:true})
  const [attempt,setAttempt]=useState(0)
  useEffect(()=>{let active=true;aiApi.documents(page).then(data=>{if(active)setState({data})}).catch(error=>{if(active)setState({error})});return()=>{active=false}},[page,attempt])
  function move(next){setState({loading:true});setPage(next);onChange('')}
  if(state.loading)return <Loading label="Đang tải tài liệu"/>
  if(state.error)return <AiError error={state.error} onRetry={()=>{setState({loading:true});setAttempt(v=>v+1)}}/>
  return <div className="ai-document-picker"><Select label={optional?'Ngữ cảnh tài liệu (không bắt buộc)':'Tài liệu nguồn'} required={!optional} disabled={disabled} value={value} onChange={event=>onChange(event.target.value)} options={[{value:'',label:optional?'Không dùng tài liệu — chỉ nội dung tin nhắn':'Chọn tài liệu'},...state.data.items.map(doc=>({value:doc.id,label:doc.originalName}))]}/>
    {!state.data.items.length&&<p>Chưa có tài liệu. Hãy upload trong khu vực Tài liệu.</p>}
    {(page>0||state.data.totalPages>1)&&<div className="ai-inline"><Button type="button" variant="secondary" disabled={disabled||page===0} onClick={()=>move(page-1)}>Trước</Button><span>Trang {page+1}</span><Button type="button" variant="secondary" disabled={disabled||page+1>=state.data.totalPages} onClick={()=>move(page+1)}>Sau</Button></div>}
  </div>
}
