import { PropsWithChildren } from 'react';
import { StyleSheet, View, ViewProps } from 'react-native';
import { radius, spacing, shadows } from '@/src/theme/tokens';
import { useThemeColors } from '@/src/theme/useThemeColors';
export function Card({ children, style, ...props }: PropsWithChildren<ViewProps>) { const colors = useThemeColors(); return <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.border }, style]} {...props}>{children}</View>; }
const styles = StyleSheet.create({ card: { borderRadius: radius.md, padding: spacing.lg, borderWidth: StyleSheet.hairlineWidth, ...shadows.card } });
