import test from 'node:test'
import assert from 'node:assert/strict'
import { hasConversationMessage, mergeMessageById, shouldSendOnKeyDown, uniqueMessages } from './chatModel.js'

test('REST response and WebSocket echo merge by stable server message id', () => {
  const socket = { id: 'message-1', content: 'Xin chào', readAt: null }
  const rest = { id: 'message-1', content: 'Xin chào', readAt: '2026-09-28T00:00:00Z' }
  assert.deepEqual(mergeMessageById([socket], rest), [rest])
  assert.equal(uniqueMessages([socket, rest]).length, 1)
})

test('Enter sends while Shift+Enter and IME composition keep a newline', () => {
  assert.equal(shouldSendOnKeyDown({ key: 'Enter', shiftKey: false, isComposing: false }), true)
  assert.equal(shouldSendOnKeyDown({ key: 'Enter', shiftKey: true, isComposing: false }), false)
  assert.equal(shouldSendOnKeyDown({ key: 'Enter', shiftKey: false, isComposing: true }), false)
})

test('empty conversation shells stay out of the conversation list', () => {
  assert.equal(hasConversationMessage({ lastMessage: null }), false)
  assert.equal(hasConversationMessage({ lastMessage: 'Chưa có tin nhắn' }), false)
  assert.equal(hasConversationMessage({ lastMessage: 'Đã gửi một hình ảnh' }), true)
})
