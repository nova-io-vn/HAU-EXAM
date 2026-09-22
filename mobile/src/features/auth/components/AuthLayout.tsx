import { PropsWithChildren } from 'react';
import { Ionicons } from '@expo/vector-icons';
import { KeyboardAvoidingView, Platform, StyleSheet, Text, View } from 'react-native';
import { AppScreen } from '@/src/components/ui';
import { colors, spacing, typography } from '@/src/theme/tokens';

export function AuthLayout({ title, description, children }: PropsWithChildren<{ title: string; description: string }>) {
  return <AppScreen scroll><KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={styles.content}><View style={styles.brand}><View style={styles.brandRow}><View style={styles.brandMark}><Ionicons name="school-outline" size={24} color={colors.white} /></View><Text style={styles.brandText}>HAU QM</Text></View><Text style={styles.subtitle}>Hệ thống Quản lý Khảo thí</Text></View><Text style={styles.title}>{title}</Text><Text style={styles.description}>{description}</Text>{children}</KeyboardAvoidingView></AppScreen>;
}
const styles = StyleSheet.create({ content: { flex: 1, gap: spacing.lg, justifyContent: 'center', paddingVertical: spacing.xl }, brand: { gap: spacing.xs }, brandRow: { alignItems: 'center', flexDirection: 'row', gap: spacing.sm }, brandMark: { alignItems: 'center', backgroundColor: colors.accent, borderRadius: 8, height: 44, justifyContent: 'center', width: 44 }, brandText: { color: colors.accent, fontSize: typography.title, fontWeight: '800' }, subtitle: { color: colors.textSecondary, fontSize: typography.label }, title: { color: colors.text, fontSize: typography.heading, fontWeight: '700', marginTop: spacing.xl }, description: { color: colors.textSecondary, fontSize: typography.body, lineHeight: 24 } });
