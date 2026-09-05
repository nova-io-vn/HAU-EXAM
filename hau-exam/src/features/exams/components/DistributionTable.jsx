import {useEffect,useState} from 'react'
import {Button,Select} from '../../../components/ui'
import {questionsApi} from '../../questions/api/questionsApi'
import {levels,rowTotal,matrixTotal} from '../model/matrixModel'

export function DistributionTable({rows,chapters=[],onChange,onRemove,readOnly=false}){
  return <div className="table-wrap matrix-table"><table><thead><tr><th scope="col">Chương / Topic</th>{levels.map(level=><th scope="col" key={level}>{level==='EASY'?'Easy':level==='MEDIUM'?'Medium':'Hard'}</th>)}<th scope="col">Total</th>{!readOnly&&<th scope="col">Thao tác</th>}</tr></thead><tbody>{rows.map((row,index)=><DistributionRow key={row.key} {...{row,index,chapters,onChange,onRemove,readOnly}}/>)}</tbody><tfoot><tr><th scope="row">Tổng phân bố</th>{levels.map(level=><td key={level}>{rows.reduce((sum,row)=>sum+(Number(row[level])||0),0)}</td>)}<td aria-live="polite"><strong>{matrixTotal(rows)}</strong></td>{!readOnly&&<td/>}</tr></tfoot></table></div>
}
function DistributionRow({row,index,chapters,onChange,onRemove,readOnly}){
  const [catalog,setCatalog]=useState({topics:[]})
  useEffect(()=>{let active=true;if(row.chapterId)questionsApi.topics(row.chapterId).then(topics=>{if(active)setCatalog({chapterId:row.chapterId,topics})}).catch(()=>{if(active)setCatalog({chapterId:row.chapterId,topics:[],error:true})});return()=>{active=false}},[row.chapterId])
  const topics=catalog.chapterId===row.chapterId?catalog.topics:[]
  const chapterOptions=[{value:'',label:'Chọn chương'},...chapters.map(c=>({value:c.id,label:c.name}))]
  if(row.chapterId&&!chapters.some(c=>c.id===row.chapterId))chapterOptions.push({value:row.chapterId,label:row.chapterId})
  const topicOptions=[{value:'',label:'Không gắn topic'},...topics.map(t=>({value:t.id,label:t.name}))]
  if(row.topicId&&!topics.some(t=>t.id===row.topicId))topicOptions.push({value:row.topicId,label:row.topicId})
  return <tr><td>{readOnly?<span>{chapters.find(c=>c.id===row.chapterId)?.name||row.chapterId}<small>{topics.find(t=>t.id===row.topicId)?.name||row.topicId||'Không gắn topic'}</small></span>:<div className="matrix-scope"><Select label={`Chương dòng ${index+1}`} required value={row.chapterId} options={chapterOptions} onChange={e=>onChange(row.key,{chapterId:e.target.value,topicId:''})}/><Select label={`Topic dòng ${index+1}`} disabled={!row.chapterId} value={row.topicId} options={topicOptions} onChange={e=>onChange(row.key,{topicId:e.target.value})}/>{catalog.error&&<small role="status">Không tải được tên topic; ID hiện có được giữ nguyên.</small>}</div>}</td>
    {levels.map(level=><td key={level}>{readOnly?row[level]:<input aria-label={`Dòng ${index+1} ${level}`} type="number" min="0" max="2147483647" step="1" required value={row[level]} onChange={e=>onChange(row.key,{[level]:e.target.value})}/>}</td>)}<td><strong>{rowTotal(row)}</strong></td>{!readOnly&&<td><Button type="button" variant="secondary" aria-label={`Xóa dòng ${index+1}`} onClick={()=>onRemove(row.key)}>Xóa dòng</Button></td>}</tr>
}
