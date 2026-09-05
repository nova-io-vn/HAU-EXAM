import {useCallback,useEffect,useState} from 'react'
import {useNavigate,useParams} from 'react-router-dom'
import {Loading} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {questionsApi} from '../api/questionsApi'
import {QuestionEditor} from '../components/QuestionEditor'
import {editableStatus,editorPayload,emptyQuestion} from '../model/questionModel'

export function EditQuestionPage(){const{id}=useParams();const[form,setForm]=useState(emptyQuestion);const[status,setStatus]=useState(null);const[loading,setLoading]=useState(true);const[saving,setSaving]=useState(false);const[error,setError]=useState(null);const navigate=useNavigate();const load=useCallback(async()=>{setLoading(true);try{const question=await questionsApi.get(id);setStatus(question.status);setForm({...emptyQuestion(),...question,correctAnswer:question.correctAnswer||'',options:question.options||emptyQuestion().options})}catch(reason){setError(reason)}finally{setLoading(false)}},[id]);useEffect(()=>{const task=setTimeout(load,0);return()=>clearTimeout(task)},[load]);async function submit(event){event.preventDefault();setSaving(true);setError(null);try{await questionsApi.update(id,editorPayload(form));navigate(`/questions/${id}`)}catch(reason){setError(reason)}finally{setSaving(false)}}if(loading)return <div className="question-state"><Loading label="Đang tải trình soạn câu hỏi"/></div>;return <section><PageHeader title="Chỉnh sửa câu hỏi" description={editableStatus(status)?'Cập nhật nội dung; trạng thái chỉ thay đổi qua action nghiệp vụ.':'Trạng thái hiện tại không cho phép chỉnh sửa từ giao diện.'}/><QuestionEditor form={form} onChange={setForm} onSubmit={submit} saving={saving} readonly={!editableStatus(status)} error={error}/></section>}
