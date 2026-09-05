import {useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {PageHeader} from '../../../components/shared/PageHeader'
import {questionsApi} from '../api/questionsApi'
import {QuestionEditor} from '../components/QuestionEditor'
import {editorPayload,emptyQuestion} from '../model/questionModel'

export function CreateQuestionPage(){const[form,setForm]=useState(emptyQuestion);const[saving,setSaving]=useState(false);const[error,setError]=useState(null);const navigate=useNavigate();async function submit(event){event.preventDefault();if(Array.isArray(form.correctAnswer)?!form.correctAnswer.length:!form.correctAnswer){setError(new Error('Vui lòng chọn ít nhất một đáp án đúng.'));return}setSaving(true);setError(null);try{const created=await questionsApi.create(editorPayload(form));navigate(`/questions/${created.id}`)}catch(reason){setError(reason)}finally{setSaving(false)}}return <section><PageHeader title="Tạo câu hỏi" description="Tạo câu hỏi thủ công ở trạng thái do Question Service quyết định."/><QuestionEditor form={form} onChange={setForm} onSubmit={submit} saving={saving} error={error}/></section>}
