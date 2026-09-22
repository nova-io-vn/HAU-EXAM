import { StyleSheet, Text } from 'react-native';
import { radius, spacing, typography } from '@/src/theme/tokens';
import { labelFor } from '@/src/utils/labels';
import {useThemeColors} from '@/src/theme/useThemeColors';
export function StatusBadge({ label, tone = 'neutral' }: { label: string; tone?: 'neutral' | 'success' | 'warning' | 'danger' }) { const colors=useThemeColors();const tones={neutral:{backgroundColor:colors.muted,color:colors.textSecondary},success:{backgroundColor:colors.successSoft,color:colors.success},warning:{backgroundColor:colors.warningSoft,color:colors.warning},danger:{backgroundColor:colors.dangerSoft,color:colors.danger}};return <Text style={[styles.base,tones[tone]]}>{labelFor(label)}</Text>; }
const styles = StyleSheet.create({ base: { alignSelf: 'flex-start', borderRadius: radius.pill, paddingHorizontal: spacing.md, paddingVertical: spacing.xs, fontSize: typography.caption, fontWeight: '600' } });
