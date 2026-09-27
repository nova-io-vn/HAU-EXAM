import test from 'node:test'
import assert from 'node:assert/strict'
import {
  defaultUiPreferences,
  normalizeUiPreferences,
  readUiPreferences,
  UI_PREFERENCES_KEY,
  writeUiPreferences,
} from '../src/app/preferences/uiPreferences.js'

function memoryStorage(initial = {}) {
  const values = new Map(Object.entries(initial))
  return {
    getItem: key => values.has(key) ? values.get(key) : null,
    setItem: (key, value) => values.set(key, String(value)),
  }
}

test('normalizes theme, cursor effect and Kute visibility', () => {
  assert.deepEqual(normalizeUiPreferences({theme:'system',cursorEffect:'stars',kuteVisible:false}), {
    theme:'system',cursorEffect:'stars',kuteVisible:false,
  })
  assert.deepEqual(normalizeUiPreferences({theme:'invalid',cursorEffect:'heavy',kuteVisible:'no'}), defaultUiPreferences)
})

test('migrates the existing theme preference and persists one preference document', () => {
  const storage = memoryStorage({'hau-qm-theme':'dark'})
  assert.equal(readUiPreferences(storage).theme, 'dark')
  writeUiPreferences(storage, {theme:'system',cursorEffect:'trail',kuteVisible:false})
  assert.deepEqual(JSON.parse(storage.getItem(UI_PREFERENCES_KEY)), {
    theme:'system',cursorEffect:'trail',kuteVisible:false,
  })
})
