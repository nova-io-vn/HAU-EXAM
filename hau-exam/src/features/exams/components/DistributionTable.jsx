import {useEffect,useState} from 'react'
import {Button,Select} from '../../../components/ui'
import {questionsApi} from '../../questions/api/questionsApi'
import {levels,rowTotal,matrixTotal} from '../model/matrixModel'

export function DistributionTable({rows,chapters=[],onChange,onRemove,readOnly=false}){
  return <div className="table-wrap matrix-table"><table><thead><tr><th scope="col">Phạm vi kiến thức</th>{levels.map(level=><th scope="col" key={level}>{level==='EASY'?'Easy':level==='MEDIUM'?'Medium':'Hard'}</th>)}<th scope="col">Total</th>{!readOnly&&<th scope="col">Thao tác</th>}</tr></thead><tbody>{rows.map((row,index)=><DistributionRow key={row.key} {...{row,index,chapters,onChange,onRemove,readOnly}}/>)}</tbody><tfoot><tr><th scope="row">Tổng phân bố</th>{levels.map(level=><td key={level}>{rows.reduce((sum,row)=>sum+(Number(row[level])||0),0)}</td>)}<td aria-live="polite"><strong>{matrixTotal(rows)}</strong></td>{!readOnly&&<td/>}</tr></tfoot></table></div>
}

function DistributionRow({row,index,chapters,onChange,onRemove,readOnly}){
  const [catalog,setCatalog]=useState({chapterId:null,topics:[],topicId:null,items:[],failed:false})
  useEffect(()=>{let active=true;if(row.chapterId)questionsApi.topics(row.chapterId).then(value=>{if(active)setCatalog(current=>({...current,chapterId:row.chapterId,topics:value,failed:false}))}).catch(()=>{if(active)setCatalog(current=>({...current,chapterId:row.chapterId,topics:[],failed:true}))});return()=>{active=false}},[row.chapterId])
  useEffect(()=>{let active=true;if(row.topicId)questionsApi.knowledgeItems(row.topicId).then(value=>{if(active)setCatalog(current=>({...current,topicId:row.topicId,items:value,failed:false}))}).catch(()=>{if(active)setCatalog(current=>({...current,topicId:row.topicId,items:[],failed:true}))});return()=>{active=false}},[row.topicId])
  const topics=catalog.chapterId===row.chapterId?catalog.topics:[],items=catalog.topicId===row.topicId?catalog.items:[]
  const chapterOptions=withCurrent([{value:'',label:'Chọn chương'},...chapters.map(c=>({value:c.id,label:c.name}))],row.chapterId)
  const topicOptions=withCurrent([{value:'',label:'Không giới hạn topic'},...topics.map(t=>({value:t.id,label:t.name}))],row.topicId)
  const itemOptions=withCurrent([{value:'',label:'Không giới hạn đơn vị kiến thức'},...items.map(item=>({value:item.id,label:item.name}))],row.knowledgeItemId)
  const names=[chapters.find(c=>c.id===row.chapterId)?.name||row.chapterId,topics.find(t=>t.id===row.topicId)?.name||row.topicId||'Mọi topic',items.find(item=>item.id===row.knowledgeItemId)?.name||row.knowledgeItemId||'Mọi đơn vị kiến thức']
  return <tr><td>{readOnly?<span>{names[0]}<small>{names.slice(1).join(' · ')}</small></span>:<div className="matrix-scope"><Select label={`Chương dòng ${index+1}`} required value={row.chapterId} options={chapterOptions} onChange={e=>onChange(row.key,{chapterId:e.target.value,topicId:'',knowledgeItemId:''})}/><Select label={`Topic dòng ${index+1}`} disabled={!row.chapterId} value={row.topicId} options={topicOptions} onChange={e=>onChange(row.key,{topicId:e.target.value,knowledgeItemId:''})}/><Select label={`Đơn vị kiến thức dòng ${index+1}`} disabled={!row.topicId} value={row.knowledgeItemId||''} options={itemOptions} onChange={e=>onChange(row.key,{knowledgeItemId:e.target.value})}/>{catalog.failed&&<small role="status">Không tải được đầy đủ tên danh mục; ID hiện có được giữ nguyên.</small>}</div>}</td>{levels.map(level=><td key={level}>{readOnly?row[level]:<input aria-label={`Dòng ${index+1} ${level}`} type="number" min="0" max="2147483647" step="1" required value={row[level]} onChange={e=>onChange(row.key,{[level]:e.target.value})}/>}</td>)}<td><strong>{rowTotal(row)}</strong></td>{!readOnly&&<td><Button type="button" variant="secondary" aria-label={`Xóa dòng ${index+1}`} onClick={()=>onRemove(row.key)}>Xóa dòng</Button></td>}</tr>
}

function withCurrent(options,value){if(value&&!options.some(option=>option.value===value))return [...options,{value,label:value}];return options}
