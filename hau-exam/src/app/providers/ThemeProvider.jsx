import {createContext,useContext,useEffect,useMemo,useState} from 'react'
import {normalizeUiPreferences,readUiPreferences,UI_PREFERENCES_KEY,writeUiPreferences} from '../preferences/uiPreferences'

const ThemeContext=createContext(null)

function systemTheme(){return window.matchMedia?.('(prefers-color-scheme: dark)').matches?'dark':'light'}
export function ThemeProvider({children}){
  const[preferences,setPreferences]=useState(()=>readUiPreferences(window.localStorage))
  const[systemDark,setSystemDark]=useState(()=>systemTheme()==='dark')
  const preference=preferences.theme
  const resolved=preference==='system'?(systemDark?'dark':'light'):preference
  useEffect(()=>{document.documentElement.dataset.theme=resolved;document.documentElement.style.colorScheme=resolved;writeUiPreferences(window.localStorage,preferences)},[preferences,resolved])
  useEffect(()=>{if(preference!=='system')return undefined;const media=window.matchMedia?.('(prefers-color-scheme: dark)');const update=event=>setSystemDark(event.matches);media?.addEventListener?.('change',update);return()=>media?.removeEventListener?.('change',update)},[preference])
  useEffect(()=>{const sync=event=>{if(event.key===UI_PREFERENCES_KEY&&event.newValue){try{setPreferences(normalizeUiPreferences(JSON.parse(event.newValue)))}catch{/* Ignore malformed preferences from another tab. */}}};window.addEventListener('storage',sync);return()=>window.removeEventListener('storage',sync)},[])
  const value=useMemo(()=>({
    preference,
    resolved,
    cursorEffect:preferences.cursorEffect,
    kuteVisible:preferences.kuteVisible,
    setPreference:theme=>{if(theme==='system')setSystemDark(systemTheme()==='dark');setPreferences(current=>normalizeUiPreferences({...current,theme}))},
    setCursorEffect:cursorEffect=>setPreferences(current=>normalizeUiPreferences({...current,cursorEffect})),
    setKuteVisible:kuteVisible=>setPreferences(current=>normalizeUiPreferences({...current,kuteVisible:Boolean(kuteVisible)})),
  }),[preference,preferences.cursorEffect,preferences.kuteVisible,resolved])
  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
}
// eslint-disable-next-line react-refresh/only-export-components
export function useTheme(){return useContext(ThemeContext)}
// eslint-disable-next-line react-refresh/only-export-components
export function useUiPreferences(){return useContext(ThemeContext)}
