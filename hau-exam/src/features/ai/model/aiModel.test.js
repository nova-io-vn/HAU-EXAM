import test from 'node:test'
import assert from 'node:assert/strict'
import {validateDocument,generationPayload,isActiveJob,DEFAULT_MAX_DOCUMENT_BYTES} from './aiModel.js'

test('upload accepts only nonempty text/plain within configured size',()=>{
  assert.equal(validateDocument({type:'text/plain',size:100}),null)
  assert.ok(validateDocument({type:'application/pdf',size:100}))
  assert.ok(validateDocument({type:'text/plain',size:0}))
  assert.ok(validateDocument({type:'text/plain',size:DEFAULT_MAX_DOCUMENT_BYTES+1}))
  assert.ok(validateDocument({type:'text/plain',size:101},100))
})
test('generation requires taxonomy, enforces count and maps only contract fields',()=>{
  const subjectId='11111111-1111-4111-8111-111111111111',chapterId='22222222-2222-4222-8222-222222222222'
  assert.deepEqual(generationPayload({documentId:'doc',subjectId,chapterId,count:'10',difficulty:'',topicId:''}),{documentId:'doc',count:10,difficulty:null,topicId:null,subjectId,chapterId,language:'VI',includeImages:false})
  assert.throws(()=>generationPayload({documentId:'doc',chapterId,count:1}))
  assert.throws(()=>generationPayload({documentId:'doc',subjectId,count:1}))
  for(const count of [0,101,1.5,'invalid'])assert.throws(()=>generationPayload({documentId:'doc',subjectId,chapterId,count}))
  assert.throws(()=>generationPayload({documentId:'doc',subjectId,chapterId,count:1,topicId:'bad'}))
})
test('polling is required only for pending and processing jobs',()=>{
  for(const status of ['PENDING','PROCESSING'])assert.equal(isActiveJob({status}),true)
  for(const status of ['COMPLETED','FAILED',undefined])assert.equal(isActiveJob({status}),false)
})
