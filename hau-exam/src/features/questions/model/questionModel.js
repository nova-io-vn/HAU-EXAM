export const questionTypes=['SINGLE_CHOICE','MULTIPLE_CHOICE','TRUE_FALSE']
export const difficulties=['EASY','MEDIUM','HARD']
export const questionStatuses=['DRAFT','PENDING_REVIEW','APPROVED','NEED_REVISION','REJECTED','ARCHIVED']
export const questionSources=['MANUAL','AI']
export const optionLabels=['A','B','C','D']
export function emptyQuestion(){return{content:'',imageUrl:'',type:'SINGLE_CHOICE',difficulty:'MEDIUM',subjectId:'',chapterId:'',topicId:'',options:optionLabels.map(label=>({label,content:'',imageUrl:''})),correctAnswer:''}}
export function normalizePage(result){if(Array.isArray(result))return{items:result,page:0,totalPages:1,totalElements:result.length};return{items:result?.content||result?.items||[],page:result?.number??result?.page??0,totalPages:result?.totalPages??0,totalElements:result?.totalElements??0}}
export function normalizeCatalog(result){const items=Array.isArray(result)?result:result?.content||result?.items||[];return items.map(item=>({value:item.id,label:item.name||item.code||item.id}))}
export function formatDateTime(value){if(!value)return'—';const date=new Date(value);return Number.isNaN(date.getTime())?'—':new Intl.DateTimeFormat('vi-VN',{dateStyle:'short',timeStyle:'short'}).format(date)}
export function editableStatus(status){return status==='DRAFT'||status==='NEED_REVISION'}
export function editorPayload(form){return {facultyId:form.facultyId,subjectId:form.subjectId,chapterId:form.chapterId,topicId:form.topicId||null,content:form.content.trim(),imageUrl:form.imageUrl||null,storageKey:form.storageKey||null,type:form.type,difficulty:form.difficulty,options:form.options.map((option,index)=>({label:option.label,content:option.content.trim(),imageUrl:option.imageUrl||null,storageKey:option.storageKey||null,correct:Array.isArray(form.correctAnswer)?form.correctAnswer.includes(option.label):form.correctAnswer===option.label,sortOrder:index}))}}
export function questionForm(question){return {...question,options:[...question.options].sort((a,b)=>a.sortOrder-b.sortOrder),correctAnswer:question.type==='MULTIPLE_CHOICE'?question.options.filter(o=>o.correct).map(o=>o.label):question.options.find(o=>o.correct)?.label||''}}
export function canEdit(q,auth){return auth.role==='USER'&&q.createdBy===auth.currentUser?.id&&editableStatus(q.status)}
export function canArchive(q,auth){return q.status==='APPROVED'&&((auth.role==='USER'&&q.createdBy===auth.currentUser?.id)||(auth.role==='SUBJECT_ADMIN'&&q.facultyId===auth.facultyId))}
export function validateQuestion(form){if(!form.content.trim())return 'Vui lòng nhập nội dung câu hỏi.';if(!form.facultyId||!form.subjectId||!form.chapterId)return 'Vui lòng chọn khoa, môn học và chương.';if(form.options.some(o=>!o.content.trim()))return 'Vui lòng nhập nội dung cho mọi phương án.';if(Array.isArray(form.correctAnswer)?!form.correctAnswer.length:!form.correctAnswer)return 'Vui lòng chọn đáp án đúng.';return null}
