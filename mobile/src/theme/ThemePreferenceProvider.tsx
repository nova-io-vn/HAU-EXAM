import {PropsWithChildren,createContext,useContext,useEffect,useMemo,useState} from 'react';
import {Appearance,Platform,useColorScheme} from 'react-native';
import * as SecureStore from 'expo-secure-store';
import {colors,darkColors} from './tokens';

export type ThemePreference='light'|'dark'|'system';
const STORAGE_KEY='hau-qm-theme';
type Value={preference:ThemePreference;resolved:'light'|'dark';palette:typeof colors|typeof darkColors;setPreference:(value:ThemePreference)=>void;ready:boolean};
const Context=createContext<Value|null>(null);

async function readPreference(){if(Platform.OS==='web')return typeof localStorage==='undefined'?null:localStorage.getItem(STORAGE_KEY);return SecureStore.getItemAsync(STORAGE_KEY)}
async function savePreference(value:ThemePreference){if(Platform.OS==='web'){localStorage.setItem(STORAGE_KEY,value);return}await SecureStore.setItemAsync(STORAGE_KEY,value)}

export function MobileThemeProvider({children}:PropsWithChildren){
  const systemScheme=useColorScheme();const[preference,setPreferenceState]=useState<ThemePreference>('system');const[ready,setReady]=useState(false);
  useEffect(()=>{readPreference().then(value=>{if(value==='light'||value==='dark'||value==='system')setPreferenceState(value)}).finally(()=>setReady(true))},[]);
  useEffect(()=>{if(!ready)return;Appearance.setColorScheme(preference==='system'?'unspecified':preference);void savePreference(preference)},[preference,ready]);
  const resolved: 'light'|'dark'=preference==='system'?(systemScheme==='dark'?'dark':'light'):preference;
  const value=useMemo(()=>({preference,resolved,palette:resolved==='dark'?darkColors:colors,setPreference:setPreferenceState,ready}),[preference,resolved,ready]);
  return <Context.Provider value={value}>{children}</Context.Provider>;
}
export function useAppTheme(){const value=useContext(Context);if(!value)throw new Error('useAppTheme must be used inside MobileThemeProvider');return value}
