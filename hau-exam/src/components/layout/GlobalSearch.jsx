import {useEffect,useMemo,useRef,useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {Icon} from '../ui'
import {searchFeatures} from '../../constants/searchRegistry'
import {roles} from '../../constants/roles'
import {usersApi} from '../../features/users/api/usersApi'
import {facultiesApi} from '../../features/users/api/facultiesApi'
import {questionsApi} from '../../features/questions/api/questionsApi'
import {catalogApi} from '../../features/questions/api/catalogApi'
import {aiApi} from '../../features/ai/api/aiApi'
import {normalizePage} from '../../features/users/model/userModel'

const groups={functions:'Chức năng',users:'Người dùng',faculties:'Khoa',subjects:'Môn học',questions:'Câu hỏi',documents:'Tài liệu AI'}
const normalize=value=>normalizePage(value).items
function Highlight({value,query}){const text=String(value||'');const needle=query.trim().toLocaleLowerCase('vi-VN');const index=text.toLocaleLowerCase('vi-VN').indexOf(needle);if(!needle||index<0)return text;return <>{text.slice(0,index)}<mark>{text.slice(index,index+needle.length)}</mark>{text.slice(index+needle.length)}</>}

export function GlobalSearch({role}){
  const navigate=useNavigate();const root=useRef(null);const input=useRef(null)
  const[query,setQuery]=useState('');const[open,setOpen]=useState(false);const[entities,setEntities]=useState({});const[loading,setLoading]=useState(false);const[error,setError]=useState('');const[active,setActive]=useState(0)
  const functions=useMemo(()=>searchFeatures(query,role),[query,role])
  useEffect(()=>{function outside(event){if(!root.current?.contains(event.target))setOpen(false)}document.addEventListener('pointerdown',outside);return()=>document.removeEventListener('pointerdown',outside)},[])
  useEffect(()=>{if(query.trim().length<2)return undefined;const controller=new AbortController();const timer=setTimeout(async()=>{setLoading(true);setError('');try{const q=query.trim();let requests=[]
    if(role===roles.SYSTEM_ADMIN)requests=[['users',usersApi.list({keyword:q,page:0,size:5})],['faculties',facultiesApi.list({keyword:q,active:true,page:0,size:5})]]
    else if(role===roles.SUBJECT_ADMIN)requests=[['questions',questionsApi.list({keyword:q,page:0,size:5})],['subjects',catalogApi.subjects()]]
    else requests=[['questions',questionsApi.list({keyword:q,page:0,size:5})],['documents',aiApi.documents(0)]]
    const settled=await Promise.allSettled(requests.map(([,promise])=>promise));if(controller.signal.aborted)return
    const next={};settled.forEach((result,index)=>{if(result.status!=='fulfilled')return;const key=requests[index][0];const items=normalize(result.value);next[key]=key==='subjects'||key==='documents'?items.filter(item=>(item.name||item.originalName||'').toLocaleLowerCase('vi-VN').includes(q.toLocaleLowerCase('vi-VN'))).slice(0,5):items});setEntities(next);if(settled.every(result=>result.status==='rejected'))setError('Không thể tải kết quả dữ liệu.')
  }catch{if(!controller.signal.aborted)setError('Không thể tải kết quả dữ liệu.')}finally{if(!controller.signal.aborted)setLoading(false)}},320);return()=>{controller.abort();clearTimeout(timer)}},[query,role])
  const sections=useMemo(()=>{const data={functions:functions.map(item=>({...item,key:`function:${item.route}`,primary:item.label,secondary:item.description,route:item.route}))};Object.entries(entities).forEach(([key,items])=>{data[key]=items.map(item=>({key:`${key}:${item.id||item.code}`,primary:item.fullName||item.name||item.originalName||item.content||item.lecturerCode,secondary:item.lecturerCode||item.code||item.email||item.status||'',route:key==='users'?`/admin/users/${item.id}`:key==='faculties'?`/admin/faculties/${item.id}`:key==='questions'?`/questions/${item.id}`:key==='documents'?'/ai/documents':'/subjects'}))});return Object.entries(data).filter(([,items])=>items.length)},[entities,functions])
  const flat=sections.flatMap(([,items])=>items)
  function choose(item){setOpen(false);setQuery('');navigate(item.route)}
  function keyDown(event){if(event.key==='Escape'){setOpen(false);input.current?.blur()}else if(event.key==='ArrowDown'){event.preventDefault();setActive(value=>flat.length?(value+1)%flat.length:0)}else if(event.key==='ArrowUp'){event.preventDefault();setActive(value=>flat.length?(value-1+flat.length)%flat.length:0)}else if(event.key==='Enter'&&flat[active]){event.preventDefault();choose(flat[active])}}
  let index=-1
  return <div className="global-search-wrap" ref={root}><label className="global-search"><Icon name="file" size={15}/><input ref={input} value={query} onFocus={()=>setOpen(true)} onChange={event=>{const value=event.target.value;setQuery(value);setOpen(true);setActive(0);if(value.trim().length<2){setEntities({});setLoading(false);setError('')}}} onKeyDown={keyDown} placeholder="Tìm kiếm chức năng, tài khoản, khoa, câu hỏi..." aria-label="Tìm kiếm toàn hệ thống" aria-expanded={open}/></label>{open&&query.trim()&&<div className="global-search-popover" role="listbox">{sections.map(([group,items])=><section key={group}><strong>{groups[group]}</strong>{items.map(item=>{index+=1;const itemIndex=index;return <button key={item.key} type="button" className={active===itemIndex?'is-active':''} onMouseEnter={()=>setActive(itemIndex)} onClick={()=>choose(item)}><span><Highlight value={item.primary} query={query}/></span>{item.secondary&&<small><Highlight value={item.secondary} query={query}/></small>}</button>})}</section>)}{loading&&<p>Đang tìm dữ liệu…</p>}{error&&<p className="search-error">{error}</p>}{!loading&&!error&&!flat.length&&<p>Không tìm thấy kết quả phù hợp.</p>}</div>}</div>
}
