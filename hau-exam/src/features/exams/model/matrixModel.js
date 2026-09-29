export const levels=['EASY','MEDIUM','HARD']
export const uuidPattern=/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
export const emptyRow=()=>({key:crypto.randomUUID(),chapterId:'',topicId:'',knowledgeItemId:'',EASY:0,MEDIUM:0,HARD:0})
export const rowTotal=row=>levels.reduce((sum,level)=>sum+(Number(row[level])||0),0)
export const matrixTotal=rows=>rows.reduce((sum,row)=>sum+rowTotal(row),0)
export function matrixRows(rules){
  const rows=new Map()
  for(const rule of rules){const key=`${rule.chapterId}:${rule.topicId||''}:${rule.knowledgeItemId||''}`;if(!rows.has(key))rows.set(key,{key,chapterId:rule.chapterId,topicId:rule.topicId||'',knowledgeItemId:rule.knowledgeItemId||'',EASY:0,MEDIUM:0,HARD:0});rows.get(key)[rule.difficulty]=rule.questionCount}
  return [...rows.values()]
}
export function matrixPayload(form){
  if(!form.name.trim())throw new Error('Vui lòng nhập tên ma trận.')
  if(!form.facultyId||!form.subjectId)throw new Error('Cần khoa và môn học hợp lệ.')
  const scopes=new Set()
  for(const [index,row] of form.rows.entries()){
    if(!row.chapterId)throw new Error(`Dòng ${index+1}: chọn chương.`)
    for(const level of levels)if(row[level]===''||!Number.isInteger(Number(row[level]))||Number(row[level])<0||Number(row[level])>2147483647)throw new Error(`Dòng ${index+1}: số câu ${level} phải là số nguyên không âm hợp lệ.`)
    if(row.knowledgeItemId&&!row.topicId)throw new Error(`Dòng ${index+1}: đơn vị kiến thức phải thuộc một topic.`)
    const scope=`${row.chapterId}:${row.topicId||''}:${row.knowledgeItemId||''}`
    if(scopes.has(scope))throw new Error(`Dòng ${index+1}: phạm vi kiến thức đã có trong bảng phân bố.`)
    scopes.add(scope)
  }
  const total=Number(form.totalQuestions)
  if(!Number.isInteger(total)||total<1||total>2147483647)throw new Error('Tổng số câu phải là số nguyên dương hợp lệ.')
  if(total!==matrixTotal(form.rows))throw new Error(`Tổng phân bố ${matrixTotal(form.rows)} chưa khớp tổng mục tiêu ${total}.`)
  return {name:form.name.trim(),facultyId:form.facultyId,subjectId:form.subjectId,totalQuestions:total,rules:form.rows.flatMap(row=>levels.filter(level=>Number(row[level])>0).map(difficulty=>({chapterId:row.chapterId,topicId:row.topicId||null,knowledgeItemId:row.knowledgeItemId||null,difficulty,questionCount:Number(row[difficulty])})))}
}

// Current API exposes shortage fields in this documented domain message.
export function shortageDetails(error){
  if(error?.code!=='INSUFFICIENT_APPROVED_QUESTIONS')return null
  const message=error.backendMessage||error.message||''
  const current=/chapter=([0-9a-f-]+), topic=(null|[0-9a-f-]+), knowledgeItem=(null|[0-9a-f-]+), difficulty=(EASY|MEDIUM|HARD): required=(\d+), available=(\d+), missing=(\d+)/i.exec(message)
  if(current)return {chapterId:current[1],topicId:current[2]==='null'?null:current[2],knowledgeItemId:current[3]==='null'?null:current[3],difficulty:current[4],required:Number(current[5]),available:Number(current[6]),missing:Number(current[7])}
  const legacy=/chapter=([0-9a-f-]+), topic=(null|[0-9a-f-]+), difficulty=(EASY|MEDIUM|HARD): required=(\d+), available=(\d+)/i.exec(message)
  return legacy?{chapterId:legacy[1],topicId:legacy[2]==='null'?null:legacy[2],difficulty:legacy[3],required:Number(legacy[4]),available:Number(legacy[5])}:null
}
