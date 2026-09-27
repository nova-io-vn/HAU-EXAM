import test from 'node:test'
import assert from 'node:assert/strict'
import { getRouteMeta } from './routeConfig.js'

test('localized route metadata drives question, profile, exam and settings identity', () => {
  assert.deepEqual(getRouteMeta('/questions/new').breadcrumb.map(item => item.label), ['Ngân hàng câu hỏi', 'Tạo câu hỏi'])
  assert.equal(getRouteMeta('/profile').documentTitle, 'Hồ sơ cá nhân')
  assert.equal(getRouteMeta('/exams').documentTitle, 'Quản lý đề thi')
  assert.equal(getRouteMeta('/admin/settings').title, 'Cấu hình hệ thống')
})

test('dynamic paths resolve without stealing static routes', () => {
  assert.equal(getRouteMeta('/questions/mine').title, 'Câu hỏi của tôi')
  assert.equal(getRouteMeta('/questions/123/edit').title, 'Chỉnh sửa câu hỏi')
  assert.equal(getRouteMeta('/admin/users/123').title, 'Chi tiết giảng viên')
})
