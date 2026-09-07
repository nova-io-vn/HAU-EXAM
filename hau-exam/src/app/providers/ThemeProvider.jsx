import {createContext,useContext,useEffect,useMemo,useState} from 'react'

const ThemeContext=createContext(null)
const STORAGE_KEY='hau-qm-theme'

function systemTheme(){return window.matchMedia?.('(prefers-color-scheme: dark)').matches?'dark':'light'}
export function ThemeProvider({children}){
  const[preference,setPreference]=useState(()=>localStorage.getItem(STORAGE_KEY)||'system')
  const[systemDark,setSystemDark]=useState(()=>systemTheme()==='dark')
  const resolved=preference==='system'?(systemDark?'dark':'light'):preference
  useEffect(()=>{document.documentElement.dataset.theme=resolved;document.documentElement.style.colorScheme=resolved;localStorage.setItem(STORAGE_KEY,preference)},[preference,resolved])
  useEffect(()=>{if(preference!=='system')return undefined;const media=window.matchMedia?.('(prefers-color-scheme: dark)');const update=event=>setSystemDark(event.matches);media?.addEventListener?.('change',update);return()=>media?.removeEventListener?.('change',update)},[preference])
  const value=useMemo(()=>({preference,resolved,setPreference}),[preference,resolved])
  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
}
// eslint-disable-next-line react-refresh/only-export-components
export function useTheme(){return useContext(ThemeContext)}
