import test from 'node:test'
import assert from 'node:assert/strict'

test('version range count is inclusive and bounded by the server maximum',()=>{
  const count=(start,end)=>end>=start?end-start+1:0
  assert.equal(count(101,105),5)
  assert.equal(count(101,101),1)
  assert.equal(count(105,101),0)
  assert.ok(count(1,20)<=20)
  assert.ok(count(1,21)>20)
})
