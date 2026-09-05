import {useCallback,useEffect,useState} from 'react'
import {useNavigate,useParams} from 'react-router-dom'
import {Loading} from '../../../components/ui'
import {PageHeader} from '../../../components/shared/PageHeader'
import {questionsApi} from '../api/questionsApi'
import {QuestionEditor} from '../components/QuestionEditor'
import {canEdit,editorPayload,emptyQuestion,questionForm,validateQuestion} from '../model/questionModel'

import {useAuth} from '../../auth/hooks/useAuth'

export function EditQuestionPage(){const auth=useAuth();const{id}=useParams();const[form,setForm]=useState(emptyQuestion);const[loading,setLoading]=useState(true);const[saving,setSaving]=useState(false);const[error,setError]=useState(null);const navigate=useNavigate();const load=useCallback(async()=>{setLoading(true);setError(null);try{const question=await questionsApi.get(id);setForm(questionForm(question))}catch(reason){setError(reason)}finally{setLoading(false)}},[id]);useEffect(()=>{const task=setTimeout(load,0);return()=>clearTimeout(task)},[load]);async function submit(event){event.preventDefault();if(saving||!canEdit(form,auth))return;const message=validateQuestion(form);if(message){setError(new Error(message));return}setSaving(true);setError(null);try{await questionsApi.update(id,editorPayload(form));if(event.nativeEvent.submitter?.value==='resubmit')await questionsApi.submit(id);navigate(`/questions/${id}`)}catch(reason){setError(reason)}finally{setSaving(false)}}if(loading)return <div className="question-state"><Loading label="Đang tải trình soạn câu hỏi"/></div>;return <section><PageHeader title="Chỉnh sửa câu hỏi" description={canEdit(form,auth)?'Cập nhật nội dung; trạng thái chỉ thay đổi qua action nghiệp vụ.':'Trạng thái hiện tại không cho phép chỉnh sửa từ giao diện.'}/><QuestionEditor form={form} onChange={setForm} onSubmit={submit} editing saving={saving} readonly={!canEdit(form,auth)} error={error}/></section>}
