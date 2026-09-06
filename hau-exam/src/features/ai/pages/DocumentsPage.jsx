import {useEffect,useRef,useState} from 'react'
import {Button,DataTable,Loading} from '../../../components/ui'
import {aiApi} from '../api/aiApi'
import {AiError} from '../components/AiShared'
import {DEFAULT_MAX_DOCUMENT_BYTES,validateDocument} from '../model/aiModel'
import {formatDateTime} from '../../questions/model/questionModel'
import {AI_DOCUMENT_MAX_SIZE_BYTES} from '../../../config/env'

const maxBytes=AI_DOCUMENT_MAX_SIZE_BYTES||DEFAULT_MAX_DOCUMENT_BYTES
export function DocumentsPage() {
  const [file,setFile]=useState(null)
  const [error,setError]=useState(null)
  const [busy,setBusy]=useState(false)
  const [success,setSuccess]=useState('')
  const [page,setPage]=useState(0)
  const [version,setVersion]=useState(0)
  const [state,setState]=useState({loading:true})
  const lock=useRef(false)
  const input=useRef(null)
  useEffect(()=>{let active=true;aiApi.documents(page).then(data=>{if(active)setState({data})}).catch(error=>{if(active)setState({error})});return()=>{active=false}},[page,version])
  function choose(next){if(lock.current)return;setSuccess('');setFile(next);const message=validateDocument(next,maxBytes);setError(message?new Error(message):null)}
  async function upload(event) {
    event.preventDefault()
    if(lock.current)return
    const message=validateDocument(file,maxBytes)
    if(message){setError(new Error(message));return}
    lock.current=true;setBusy(true);setError(null);setSuccess('')
    try {
      const bytes=await file.arrayBuffer()
      try{new TextDecoder('utf-8',{fatal:true}).decode(bytes)}catch{throw new Error('Tài liệu phải được mã hóa UTF-8.')}
      const document=await aiApi.upload(file)
      setSuccess(`Đã lưu tài liệu ${document.originalName}.`);setFile(null);if(input.current)input.current.value=''
      setState({loading:true});setPage(0);setVersion(v=>v+1)
    } catch(reason){setError(reason)}finally{lock.current=false;setBusy(false)}
  }
  return <div className="ai-stack"><form className="editor-section" onSubmit={upload}><h2>Upload tài liệu</h2>
    <div className="ai-dropzone" onDragOver={event=>event.preventDefault()} onDrop={event=>{event.preventDefault();if(event.dataTransfer.files.length!==1){setError(new Error('Chọn một tài liệu mỗi lần.'));return}choose(event.dataTransfer.files[0])}}>
      <label htmlFor="ai-file">Kéo thả tài liệu vào đây hoặc chọn tệp</label><input ref={input} id="ai-file" type="file" accept="text/plain,.txt" disabled={busy} onChange={event=>choose(event.target.files[0])}/>
      <p>UTF-8 .txt · tối đa {(maxBytes/1048576).toFixed(1)} MiB. Chưa hỗ trợ PDF/DOCX.</p>{file&&<strong>{file.name} · {(file.size/1024).toFixed(1)} KiB</strong>}
    </div>{error&&<AiError error={error}/>}<Button type="submit" loading={busy} disabled={busy||!file||Boolean(validateDocument(file,maxBytes))}>Upload tài liệu</Button>{busy&&<p role="status">Đang gửi tài liệu…</p>}{success&&<p role="status" className="review-success">{success}</p>}
  </form><section className="surface ai-list"><h2>Tài liệu của tôi</h2><p>Tài liệu đã lưu sẽ được trích xuất khi tác vụ AI chạy.</p>
    {state.loading?<Loading label="Đang tải tài liệu"/>:state.error?<AiError error={state.error} onRetry={()=>{setState({loading:true});setVersion(v=>v+1)}}/>:<><DataTable rows={state.data.items} emptyTitle="Chưa có tài liệu" columns={[{key:'originalName',header:'Tên tài liệu'},{key:'size',header:'Kích thước',render:d=>`${(d.size/1024).toFixed(1)} KiB`},{key:'createdAt',header:'Upload lúc',render:d=>formatDateTime(d.createdAt)},{key:'status',header:'Trạng thái',render:()=> 'Đã lưu'}]}/><div className="ai-inline"><Button variant="secondary" disabled={page===0} onClick={()=>{setState({loading:true});setPage(p=>p-1)}}>Trước</Button><span>Trang {page+1} · {state.data.totalElements} tài liệu</span><Button variant="secondary" disabled={page+1>=state.data.totalPages} onClick={()=>{setState({loading:true});setPage(p=>p+1)}}>Sau</Button></div></>}
  </section></div>
}
