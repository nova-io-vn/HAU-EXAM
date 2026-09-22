import { ReactNode, useState } from 'react';
import { Pressable, Text, TextInput, TextInputProps, StyleSheet, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { radius, spacing, typography } from '@/src/theme/tokens';
import { useThemeColors } from '@/src/theme/useThemeColors';

export function Input({ label, error, rightElement, secureTextEntry, ...props }: TextInputProps & { label: string; error?: string; rightElement?: ReactNode }) {
  const colors=useThemeColors();const[passwordVisible,setPasswordVisible]=useState(false);const isPassword=secureTextEntry!==undefined;
  const accessory=rightElement||(isPassword?<Pressable accessibilityRole="button" accessibilityLabel={passwordVisible?'Ẩn mật khẩu':'Hiện mật khẩu'} hitSlop={8} style={styles.passwordButton} onPress={()=>setPasswordVisible(value=>!value)}><Ionicons name={passwordVisible?'eye-off-outline':'eye-outline'} size={22} color={colors.textSecondary}/></Pressable>:null);const hasAccessory=accessory!==null&&accessory!==undefined;
  return <View style={styles.wrap}><Text style={[styles.label,{color:colors.text}]}>{label}</Text><View style={styles.inputWrap}><TextInput accessibilityLabel={label} style={[styles.input,{borderColor:colors.border,backgroundColor:colors.surface,color:colors.text},hasAccessory&&styles.withRight,error&&{borderColor:colors.danger}]} placeholderTextColor={colors.textSecondary} secureTextEntry={isPassword?!passwordVisible:secureTextEntry} {...props}/>{hasAccessory&&<View style={styles.right}>{accessory}</View>}</View>{error&&<Text style={[styles.error,{color:colors.danger}]}>{error}</Text>}</View>;
}
const styles=StyleSheet.create({wrap:{gap:spacing.sm},label:{fontSize:typography.label,fontWeight:'600'},inputWrap:{position:'relative'},input:{minHeight:48,borderRadius:radius.sm,borderWidth:1,paddingHorizontal:spacing.md,fontSize:typography.body},withRight:{paddingRight:52},right:{position:'absolute',right:5,top:5},passwordButton:{alignItems:'center',height:38,justifyContent:'center',width:38},error:{fontSize:typography.caption}});
