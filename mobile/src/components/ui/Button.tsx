import { ActivityIndicator, Pressable, StyleSheet, Text, PressableProps } from 'react-native';
import type React from 'react';
import { radius, spacing, typography } from '@/src/theme/tokens';
import {useThemeColors} from '@/src/theme/useThemeColors';

export function Button({ title, loading = false, variant = 'primary', icon, ...props }: PressableProps & { title: string; loading?: boolean; variant?: 'primary' | 'secondary' | 'destructive'; icon?: React.ReactNode }) {
  const colors=useThemeColors();const variantStyle=variant==='secondary'?{backgroundColor:colors.surface,borderWidth:1,borderColor:colors.border}:variant==='destructive'?{backgroundColor:colors.danger}:{backgroundColor:colors.accent};
  return <Pressable accessibilityRole="button" accessibilityState={{ busy: loading, disabled: props.disabled ?? false }} style={({ pressed }) => [styles.base,variantStyle, pressed && styles.pressed, props.disabled && styles.disabled]} {...props}>{loading ? <ActivityIndicator color={variant === 'secondary' ? colors.accent : colors.white} /> : <>{icon}<Text style={[styles.label,{color:variant==='secondary'?colors.text:colors.white}]}>{title}</Text></>}</Pressable>;
}
const styles = StyleSheet.create({ base: { minHeight: 44, borderRadius: radius.sm, flexDirection: 'row', gap: spacing.sm, alignItems: 'center', justifyContent: 'center', paddingHorizontal: spacing.lg }, label: { fontSize: typography.label, fontWeight: '700' }, pressed: { opacity: 0.8 }, disabled: { opacity: 0.45 } });
