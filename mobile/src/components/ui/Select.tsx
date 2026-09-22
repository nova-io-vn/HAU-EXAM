import { Pressable, StyleSheet, Text, View } from 'react-native';
import { radius, spacing, typography } from '@/src/theme/tokens';
import {useThemeColors} from '@/src/theme/useThemeColors';

export function Select({ label, value, placeholder = 'Chọn một mục', onPress }: { label: string; value?: string; placeholder?: string; onPress?: () => void }) {
  const colors=useThemeColors();return <View style={styles.wrap}><Text style={[styles.label,{color:colors.text}]}>{label}</Text><Pressable accessibilityRole="button" accessibilityLabel={label} onPress={onPress} style={[styles.control,{borderColor:colors.border,backgroundColor:colors.surface}]}><Text style={[styles.value,{color:value?colors.text:colors.textSecondary}]}>{value || placeholder}</Text><Text style={[styles.chevron,{color:colors.textSecondary}]}>⌄</Text></Pressable></View>;
}
const styles = StyleSheet.create({ wrap: { gap: spacing.sm }, label: { fontSize: typography.label, fontWeight: '600' }, control: { minHeight: 48, borderRadius: radius.sm, borderWidth: 1, paddingHorizontal: spacing.md, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, value: { fontSize: typography.body }, chevron: { fontSize: 20 } });
