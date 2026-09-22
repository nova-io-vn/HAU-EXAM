import { StyleSheet, Text, View } from 'react-native';
import { Button } from '@/src/components/ui/Button';
import { spacing, typography } from '@/src/theme/tokens';
import {useThemeColors} from '@/src/theme/useThemeColors';
export function ErrorState({ title = 'Không thể tải dữ liệu', onRetry }: { title?: string; onRetry?: () => void }) { const colors=useThemeColors();return <View style={styles.wrap}><Text style={[styles.title,{color:colors.danger}]}>{title}</Text>{onRetry && <Button title="Thử lại" variant="secondary" onPress={onRetry} />}</View>; }
const styles = StyleSheet.create({ wrap: { alignItems: 'center', gap: spacing.md, padding: spacing.xxl }, title: { fontSize: typography.body, textAlign: 'center' } });
