export const questionTypes=['SINGLE_CHOICE','MULTIPLE_CHOICE','TRUE_FALSE']
export const difficulties=['EASY','MEDIUM','HARD']
export const questionStatuses=['DRAFT','PENDING_REVIEW','APPROVED','NEED_REVISION','REJECTED','ARCHIVED']
export const questionSources=['MANUAL','AI']
export const optionLabels=['A','B','C','D']
export function emptyQuestion(){return{content:'',imageUrl:'',type:'SINGLE_CHOICE',difficulty:'MEDIUM',subjectId:'',chapterId:'',topicId:'',explanation:'',options:optionLabels.map(label=>({label,content:'',imageUrl:''})),correctAnswer:''}}
export function normalizePage(result){if(Array.isArray(result))return{items:result,page:0,totalPages:1,totalElements:result.length};return{items:result?.content||result?.items||[],page:result?.number??result?.page??0,totalPages:result?.totalPages??0,totalElements:result?.totalElements??0}}
export function normalizeCatalog(result){const items=Array.isArray(result)?result:result?.content||result?.items||[];return items.map(item=>({value:item.id,label:item.name||item.title||item.code||item.id}))}
export function formatDateTime(value){if(!value)return'—';const date=new Date(value);return Number.isNaN(date.getTime())?'—':new Intl.DateTimeFormat('vi-VN',{dateStyle:'short',timeStyle:'short'}).format(date)}
export function editableStatus(status){return status==='DRAFT'||status==='NEED_REVISION'}
export function editorPayload(form){return{content:form.content,imageUrl:form.imageUrl||null,type:form.type,difficulty:form.difficulty,subjectId:form.subjectId,chapterId:form.chapterId||null,topicId:form.topicId||null,explanation:form.explanation||null,options:form.options.map(option=>({label:option.label,content:option.content,imageUrl:option.imageUrl||null})),correctAnswer:form.type==='MULTIPLE_CHOICE'?form.correctAnswer:form.correctAnswer}}
