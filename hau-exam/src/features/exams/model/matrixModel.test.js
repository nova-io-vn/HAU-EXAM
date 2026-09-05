import test from 'node:test'
import assert from 'node:assert/strict'
import {matrixPayload,matrixRows,matrixTotal,shortageDetails} from './matrixModel.js'

const form=()=>({name:' Ma trận mẫu ',facultyId:'CNTT',subjectId:'subject',totalQuestions:6,rows:[{key:'a',chapterId:'chapter',topicId:'',EASY:'1',MEDIUM:'2',HARD:'3'}]})
test('inline totals and payload follow backend distribution contract',()=>{
  const input=form()
  assert.equal(matrixTotal(input.rows),6)
  const payload=matrixPayload(input)
  assert.equal(payload.name,'Ma trận mẫu')
  assert.equal(payload.rules.length,3)
  assert.deepEqual(payload.rules[0],{chapterId:'chapter',topicId:null,difficulty:'EASY',questionCount:1})
  assert.equal(matrixTotal(matrixRows(payload.rules)),6)
})
test('negative, fractional, blank and oversized counts are rejected',()=>{
  for(const value of [-1,0.5,'','bad',2147483648]){const input=form();input.rows[0].EASY=value;assert.throws(()=>matrixPayload(input))}
})
test('matrix requires positive matching total and a chapter for each row',()=>{
  for(const total of [0,-1,7,1.5]){const input=form();input.totalQuestions=total;assert.throws(()=>matrixPayload(input))}
  const input=form();input.rows[0].chapterId='';assert.throws(()=>matrixPayload(input))
})
test('duplicate chapter/topic rows are invalid; different topics are allowed',()=>{
  const input=form();input.rows.push({...input.rows[0],key:'b'});input.totalQuestions=12
  assert.throws(()=>matrixPayload(input))
  input.rows[1].topicId='topic';assert.equal(matrixPayload(input).rules.length,6)
})
test('zero cells do not create unnecessary selection rules',()=>{
  const input=form();input.rows[0].EASY=0;input.totalQuestions=5
  assert.equal(matrixPayload(input).rules.length,2)
})
test('shortage details show the exact rule and server counts, without guessing',()=>{
  const details=shortageDetails({code:'INSUFFICIENT_APPROVED_QUESTIONS',message:'Insufficient APPROVED questions for chapter=1234-abcd, topic=null, difficulty=HARD: required=7, available=2'})
  assert.deepEqual(details,{chapterId:'1234-abcd',topicId:null,difficulty:'HARD',required:7,available:2})
  assert.equal(shortageDetails({code:'INSUFFICIENT_APPROVED_QUESTIONS',message:'Not enough questions'}),null)
  assert.equal(shortageDetails({code:'OTHER',message:'required=7, available=2'}),null)
})
