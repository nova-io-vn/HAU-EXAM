import {DynamicColorIOS,Platform} from 'react-native';
const semantic=(light:string,dark:string)=>Platform.OS==='ios'?DynamicColorIOS({light,dark}):light;
export const colors = {
  background: semantic('#FBF8F8','#171416'),
  surface: semantic('#FFFFFF','#211D1F'),
  text: semantic('#211B1D','#F7EFF1'),
  textSecondary: semantic('#766F71','#C3B6B9'),
  accent: semantic('#9D1C2E','#C84B61'),
  accentHover: semantic('#7F1423','#DD6579'),
  accentSoft: semantic('#F8E9EC','#3C2028'),
  primaryDark: semantic('#211B1D','#F7EFF1'),
  border: semantic('#E7DFE1','#45383C'),
  muted: semantic('#F6F1F2','#2A2326'),
  success: semantic('#18864B','#63C98D'),
  warning: semantic('#9A6700','#E4B85A'),
  danger: semantic('#C62828','#F08080'),
  info: semantic('#356B93','#74AED5'),
  infoSoft: semantic('#E8F1F8','#20384A'),
  dangerSoft: semantic('#FDE9E7','#4A2525'),
  successSoft: semantic('#E8F4EB','#1F3C2A'),
  warningSoft: semantic('#FFF3DF','#4A3820'),
  white: '#FFFFFF',
} as const;
export const darkColors = { ...colors, background: '#171416', surface: '#211D1F', text: '#F7EFF1', textSecondary: '#C3B6B9', border: '#45383C', muted: '#2A2326', accentSoft: '#3C2028', dangerSoft: '#4A2525', successSoft: '#1F3C2A', warningSoft: '#4A3820', infoSoft: '#20384A' } as const;

export const spacing = { xs: 4, sm: 8, md: 12, lg: 16, xl: 24, xxl: 32 } as const;
export const radius = { sm: 5, md: 8, lg: 12, pill: 999 } as const;
export const typography = { body: 16, label: 14, title: 28, heading: 22, caption: 12 } as const;

export const shadows = {
  card: { shadowColor: '#211B1D', shadowOpacity: 0.04, shadowRadius: 8, shadowOffset: { width: 0, height: 2 }, elevation: 1 },
  modal: { shadowColor: '#211B1D', shadowOpacity: 0.12, shadowRadius: 18, shadowOffset: { width: 0, height: 8 }, elevation: 4 },
} as const;

export const theme = { colors, spacing, radius, typography } as const;
