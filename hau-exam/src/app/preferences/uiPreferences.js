export const UI_PREFERENCES_KEY = 'hau-qm-ui-preferences-v1'
export const LEGACY_THEME_KEY = 'hau-qm-theme'

export const themeOptions = ['light', 'dark', 'system']
export const cursorEffectOptions = ['none', 'glow', 'stars', 'particles', 'trail']
export const defaultUiPreferences = Object.freeze({
  theme: 'light',
  cursorEffect: 'none',
  kuteVisible: true,
})

export function normalizeUiPreferences(value = {}, legacyTheme = null) {
  const requestedTheme = value.theme ?? legacyTheme
  return {
    theme: themeOptions.includes(requestedTheme) ? requestedTheme : defaultUiPreferences.theme,
    cursorEffect: cursorEffectOptions.includes(value.cursorEffect) ? value.cursorEffect : defaultUiPreferences.cursorEffect,
    kuteVisible: typeof value.kuteVisible === 'boolean' ? value.kuteVisible : defaultUiPreferences.kuteVisible,
  }
}

export function readUiPreferences(storage) {
  if (!storage) return defaultUiPreferences
  try {
    const raw = storage.getItem(UI_PREFERENCES_KEY)
    const parsed = raw ? JSON.parse(raw) : {}
    return normalizeUiPreferences(parsed, storage.getItem(LEGACY_THEME_KEY))
  } catch {
    return defaultUiPreferences
  }
}

export function writeUiPreferences(storage, preferences) {
  if (!storage) return
  storage.setItem(UI_PREFERENCES_KEY, JSON.stringify(normalizeUiPreferences(preferences)))
}
