import { StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { radius, spacing, typography } from '@/src/theme/tokens';
import { useAuth } from '@/src/app/providers/AppProviders';
import { roleLabelFor } from '@/src/utils/labels';
import { formatAcademicName } from '@/src/features/profile/utils/academicName';
import {useThemeColors} from '@/src/theme/useThemeColors';

type Props = { title: string; subtitle?: string; showIdentity?: boolean };

export function AppHeader({ title, subtitle, showIdentity = false }: Props) {
  const { session } = useAuth();
  const colors=useThemeColors();
  const roleLabel = roleLabelFor(session?.role);
  const identity = formatAcademicName(session || {});
  const faculty = session?.facultyName || session?.facultyId;

  return (
    <View accessibilityRole="header" style={styles.header}>
      <View style={styles.brandRow}><View style={[styles.brandMark,{backgroundColor:colors.accent}]}><Ionicons name="school-outline" size={18} color={colors.white} /></View><Text style={[styles.brand,{color:colors.accent}]}>HAU QM</Text></View>
      <View style={styles.copy}>
        <Text style={[styles.title,{color:colors.text}]}>{title}</Text>
        {subtitle && <Text style={[styles.subtitle,{color:colors.textSecondary}]}>{subtitle}</Text>}
        {showIdentity && <>
          <Text style={[styles.identity,{color:colors.text}]}>{identity}</Text>
          <Text accessibilityLabel={`Vai trò ${roleLabel}`} style={[styles.role,{color:colors.accent}]}>
            {roleLabel}{faculty ? ` • Khoa ${faculty}` : ''}
          </Text>
        </>}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  header: { minHeight: 70, gap: spacing.md, marginBottom: spacing.lg },
  brandRow: { alignItems: 'center', flexDirection: 'row', gap: spacing.sm },
  brandMark: { alignItems: 'center', borderRadius: radius.sm, height: 30, justifyContent: 'center', width: 30 },
  brand: { fontSize: typography.caption, fontWeight: '800', letterSpacing: 1 },
  copy: { flex: 1, gap: spacing.xs },
  title: { fontSize: typography.heading, fontWeight: '700' },
  subtitle: { fontSize: typography.label },
  identity: { fontSize: typography.label, fontWeight: '600' },
  role: { fontSize: typography.caption, fontWeight: '700' },
});
