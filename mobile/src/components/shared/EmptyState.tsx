import { StyleSheet, Text, View } from 'react-native';
import { spacing, typography } from '@/src/theme/tokens';
import {useThemeColors} from '@/src/theme/useThemeColors';
export function EmptyState({ title, description }: { title: string; description?: string }) { const colors=useThemeColors();return <View style={styles.wrap}><Text style={[styles.title,{color:colors.text}]}>{title}</Text>{description && <Text style={[styles.description,{color:colors.textSecondary}]}>{description}</Text>}</View>; }
const styles = StyleSheet.create({ wrap: { alignItems: 'center', padding: spacing.xxl }, title: { fontSize: typography.heading, fontWeight: '700', textAlign: 'center' }, description: { fontSize: typography.body, marginTop: spacing.sm, textAlign: 'center' } });
