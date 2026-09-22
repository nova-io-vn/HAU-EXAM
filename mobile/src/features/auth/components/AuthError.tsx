import { StyleSheet, Text, View } from 'react-native';
import { colors, radius, spacing, typography } from '@/src/theme/tokens';
export function AuthError({ message }: { message?: string }) { return message ? <View accessibilityRole="alert" style={styles.box}><Text style={styles.text}>{message}</Text></View> : null; }
const styles = StyleSheet.create({ box: { backgroundColor: '#FDECEC', borderRadius: radius.sm, padding: spacing.md }, text: { color: colors.danger, fontSize: typography.label } });
