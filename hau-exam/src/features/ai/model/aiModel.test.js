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
test('generation enforces backend count and maps only contract fields',()=>{
  assert.deepEqual(generationPayload({documentId:'doc',count:'10',difficulty:'',topicId:''}),{documentId:'doc',count:10,difficulty:null,topicId:null})
  for(const count of [0,101,1.5,'invalid'])assert.throws(()=>generationPayload({documentId:'doc',count}))
  assert.throws(()=>generationPayload({documentId:'doc',count:1,topicId:'bad'}))
})
test('polling is required only for pending and processing jobs',()=>{
  for(const status of ['PENDING','PROCESSING'])assert.equal(isActiveJob({status}),true)
  for(const status of ['COMPLETED','FAILED',undefined])assert.equal(isActiveJob({status}),false)
})
