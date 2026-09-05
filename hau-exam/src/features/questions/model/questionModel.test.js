import test from 'node:test'
import assert from 'node:assert/strict'
import {canArchive,canEdit,editorPayload,questionForm,validateQuestion} from './questionModel.js'

const question={id:'q',facultyId:'F',subjectId:'s',chapterId:'c',topicId:null,content:'Question',imageUrl:null,storageKey:'image-key',type:'MULTIPLE_CHOICE',difficulty:'MEDIUM',status:'NEED_REVISION',source:'AI',createdBy:'owner',options:[{label:'B',content:'Second',correct:true,sortOrder:1,storageKey:'option-key'},{label:'A',content:'First',correct:true,sortOrder:0}]}

test('editing AI data preserves source and maps correct flags without inventing request fields',()=>{
  const form=questionForm(question)
  assert.equal(form.source,'AI')
  assert.equal(form.status,'NEED_REVISION')
  assert.deepEqual(form.options.map(o=>o.label),['A','B'])
  const payload=editorPayload(form)
  assert.deepEqual(payload.options.map(o=>o.correct),[true,true])
  assert.equal(payload.options[1].storageKey,'option-key')
  assert.equal(payload.storageKey,'image-key')
  for(const field of ['source','status','correctAnswer','explanation','createdBy'])assert.equal(field in payload,false)
  assert.equal(payload.facultyId,'F')
  assert.equal(validateQuestion(form),null)
})

test('single choice roundtrip and missing answer validation',()=>{
  const form=questionForm({...question,type:'SINGLE_CHOICE',options:question.options.map(o=>({...o,correct:o.label==='A'}))})
  assert.equal(form.correctAnswer,'A')
  assert.deepEqual(editorPayload(form).options.map(o=>o.correct),[true,false])
  assert.ok(validateQuestion({...form,correctAnswer:''}))
  assert.ok(validateQuestion({...form,chapterId:''}))
})

test('ownership, faculty and workflow control visible actions',()=>{
  const owner={role:'USER',currentUser:{id:'owner'},facultyId:'F'}
  assert.equal(canEdit(question,owner),true)
  assert.equal(canEdit(question,{...owner,currentUser:{id:'other'}}),false)
  for(const status of ['PENDING_REVIEW','APPROVED','REJECTED','ARCHIVED'])assert.equal(canEdit({...question,status},owner),false)
  const approved={...question,status:'APPROVED'}
  assert.equal(canArchive(approved,owner),true)
  assert.equal(canArchive(approved,{role:'SUBJECT_ADMIN',facultyId:'F'}),true)
  assert.equal(canArchive(approved,{role:'SUBJECT_ADMIN',facultyId:'OTHER'}),false)
  assert.equal(canArchive(approved,{role:'SYSTEM_ADMIN',facultyId:'F'}),false)
  assert.equal(canArchive({...question,status:'PENDING_REVIEW'},owner),false)
})
