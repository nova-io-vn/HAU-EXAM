import test from 'node:test'
import assert from 'node:assert/strict'
import {canReview,reviewActions} from './reviewModel.js'

const question={facultyId:'CNTT',status:'PENDING_REVIEW'}
test('review visibility excludes system admins, users, other faculties and missing faculty',()=>{
  assert.equal(canReview(question,{role:'SUBJECT_ADMIN',facultyId:'CNTT'}),true)
  for(const auth of [
    {role:'SYSTEM_ADMIN',facultyId:'CNTT'},
    {role:'USER',facultyId:'CNTT'},
    {role:'SUBJECT_ADMIN',facultyId:'ARCH'},
    {role:'SUBJECT_ADMIN'},
  ])assert.equal(canReview(question,auth),false)
})
test('only pending questions expose review actions',()=>{
  for(const status of ['DRAFT','APPROVED','REJECTED','NEED_REVISION','ARCHIVED']) {
    assert.equal(canReview({...question,status},{role:'SUBJECT_ADMIN',facultyId:'CNTT'}),false)
  }
})
test('reject and revision require reason; approve allows optional comment',()=>{
  assert.equal(reviewActions.reject.required,true)
  assert.equal(reviewActions.requestRevision.required,true)
  assert.equal(Boolean(reviewActions.approve.required),false)
})
