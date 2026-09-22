import {Pressable,StyleSheet,Text,View} from 'react-native';
import {Ionicons} from '@expo/vector-icons';
import {AppScreen,Card} from '@/src/components/ui';
import {AppHeader} from '@/src/components/shared';
import {spacing,typography} from '@/src/theme/tokens';
import {ThemePreference,useAppTheme} from '@/src/theme/ThemePreferenceProvider';

const options:[ThemePreference,string,string][]=[['light','Sáng','Luôn dùng giao diện sáng'],['dark','Tối','Luôn dùng giao diện tối'],['system','Theo hệ thống','Tự động theo cài đặt thiết bị']];
export function AppearanceScreen(){const{preference,setPreference,palette}=useAppTheme();return <AppScreen scroll><AppHeader title="Giao diện" subtitle="Tùy chọn tài khoản" showIdentity={false}/><Card><View style={styles.list}>{options.map(([value,label,description])=><Pressable accessibilityRole="radio" accessibilityState={{checked:preference===value}} key={value} style={[styles.option,{borderBottomColor:palette.border}]} onPress={()=>setPreference(value)}><View style={styles.copy}><Text style={[styles.label,{color:palette.text}]}>{label}</Text><Text style={[styles.description,{color:palette.textSecondary}]}>{description}</Text></View><Ionicons name={preference===value?'radio-button-on':'radio-button-off'} size={22} color={preference===value?palette.accent:palette.textSecondary}/></Pressable>)}</View></Card></AppScreen>}
const styles=StyleSheet.create({list:{gap:spacing.xs},option:{alignItems:'center',borderBottomWidth:StyleSheet.hairlineWidth,flexDirection:'row',paddingVertical:spacing.lg},copy:{flex:1,gap:spacing.xs},label:{fontSize:typography.body,fontWeight:'700'},description:{fontSize:typography.caption}});
