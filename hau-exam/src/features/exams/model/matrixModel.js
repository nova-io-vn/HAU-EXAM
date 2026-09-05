export const levels=['EASY','MEDIUM','HARD']
export const uuidPattern=/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
export const emptyRow=()=>({key:crypto.randomUUID(),chapterId:'',topicId:'',EASY:0,MEDIUM:0,HARD:0})
export const rowTotal=row=>levels.reduce((sum,level)=>sum+(Number(row[level])||0),0)
export const matrixTotal=rows=>rows.reduce((sum,row)=>sum+rowTotal(row),0)
export function matrixRows(rules){
  const rows=new Map()
  for(const rule of rules){const key=`${rule.chapterId}:${rule.topicId||''}`;if(!rows.has(key))rows.set(key,{key,chapterId:rule.chapterId,topicId:rule.topicId||'',EASY:0,MEDIUM:0,HARD:0});rows.get(key)[rule.difficulty]=rule.questionCount}
  return [...rows.values()]
}
export function matrixPayload(form){
  if(!form.name.trim())throw new Error('Vui lòng nhập tên ma trận.')
  if(!form.facultyId||!form.subjectId)throw new Error('Cần khoa và môn học hợp lệ.')
  const scopes=new Set()
  for(const [index,row] of form.rows.entries()){
    if(!row.chapterId)throw new Error(`Dòng ${index+1}: chọn chương.`)
    for(const level of levels)if(row[level]===''||!Number.isInteger(Number(row[level]))||Number(row[level])<0||Number(row[level])>2147483647)throw new Error(`Dòng ${index+1}: số câu ${level} phải là số nguyên không âm hợp lệ.`)
    const scope=`${row.chapterId}:${row.topicId||''}`
    if(scopes.has(scope))throw new Error(`Dòng ${index+1}: chương/topic đã có trong bảng phân bố.`)
    scopes.add(scope)
  }
  const total=Number(form.totalQuestions)
  if(!Number.isInteger(total)||total<1||total>2147483647)throw new Error('Tổng số câu phải là số nguyên dương hợp lệ.')
  if(total!==matrixTotal(form.rows))throw new Error(`Tổng phân bố ${matrixTotal(form.rows)} chưa khớp tổng mục tiêu ${total}.`)
  return {name:form.name.trim(),facultyId:form.facultyId,subjectId:form.subjectId,totalQuestions:total,rules:form.rows.flatMap(row=>levels.filter(level=>Number(row[level])>0).map(difficulty=>({chapterId:row.chapterId,topicId:row.topicId||null,difficulty,questionCount:Number(row[difficulty])})))}
}

// Current API exposes shortage fields in this documented domain message.
export function shortageDetails(error){
  if(error?.code!=='INSUFFICIENT_APPROVED_QUESTIONS')return null
  const match=/chapter=([0-9a-f-]+), topic=(null|[0-9a-f-]+), difficulty=(EASY|MEDIUM|HARD): required=(\d+), available=(\d+)/i.exec(error.message||'')
  return match?{chapterId:match[1],topicId:match[2]==='null'?null:match[2],difficulty:match[3],required:Number(match[4]),available:Number(match[5])}:null
}
