import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { spacing, typography } from '@/src/theme/tokens';
import { useThemeColors } from '@/src/theme/useThemeColors';
export function LoadingIndicator({ label = 'Đang tải' }: { label?: string }) { const colors = useThemeColors(); return <View accessibilityRole="progressbar" style={styles.wrap}><ActivityIndicator color={colors.accent} /><Text style={[styles.text, { color: colors.textSecondary }]}>{label}</Text></View>; }
const styles = StyleSheet.create({ wrap: { alignItems: 'center', gap: spacing.sm, padding: spacing.xl }, text: { fontSize: typography.label } });
