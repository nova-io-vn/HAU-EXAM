/* eslint-disable react-hooks/set-state-in-effect -- preserve existing async screen-loading behavior. */
import { useCallback, useEffect, useState } from 'react';
import { Ionicons } from '@expo/vector-icons';
import { Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { AppScreen, Button, Card, StatusBadge } from '@/src/components/ui';
import { AppHeader, DashboardMetricSkeleton, EmptyState, ErrorState, ListSkeleton } from '@/src/components/shared';
import { useAuth } from '@/src/app/providers/AppProviders';
import { questionApi, type Question, type QuestionStatistics } from '@/src/features/questions/api/questionApi';
import { colors, spacing, typography } from '@/src/theme/tokens';
import { formatAcademicName } from '@/src/features/profile/utils/academicName';

export function HomeScreen() {
  const router = useRouter();
  const { session } = useAuth();
  const [stats, setStats] = useState<QuestionStatistics | null>(null);
  const [recent, setRecent] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const load = useCallback(async (refresh = false) => {
    if (refresh) setRefreshing(true); else setLoading(true); setError(null);
    try { const [summary, list] = await Promise.all([questionApi.statistics(), questionApi.search({ page: 0, size: 3 })]); setStats(summary); setRecent(list.items); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Không thể tải tổng quan'); }
    finally { setLoading(false); setRefreshing(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);
  const identity = formatAcademicName(session || {});
  const faculty = session?.facultyName || session?.facultyId || 'Chưa cập nhật';
  if (loading) return <AppScreen><AppHeader title="Tổng quan" /><DashboardMetricSkeleton count={4} /><ListSkeleton count={3} /></AppScreen>;
  if (error) return <AppScreen><AppHeader title="Tổng quan" /><ErrorState title={error} onRetry={() => void load()} /></AppScreen>;
  const cards = [['Tổng câu hỏi', stats?.total ?? 0, 'document-text-outline'], ['Đã phê duyệt', stats?.approved ?? 0, 'checkmark-circle-outline'], ['Chờ duyệt', stats?.pendingReview ?? 0, 'time-outline'], ['Cần chỉnh sửa', stats?.needRevision ?? 0, 'create-outline']] as const;
  return <AppScreen><ScrollView refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => void load(true)} tintColor={colors.accent} />} contentContainerStyle={styles.content}>
    <AppHeader title={`Xin chào, ${identity}`} subtitle={`Khoa ${faculty}`} />
    <View style={styles.actions}><Button title="Tạo câu hỏi" icon={<Ionicons name="add-circle-outline" size={18} color={colors.white} />} onPress={() => router.push('/question/create')} /><Button title="Tạo bằng AI" variant="secondary" icon={<Ionicons name="sparkles-outline" size={18} color={colors.accent} />} onPress={() => router.push('/ai')} /></View>
    <View style={styles.grid}>{cards.map(([label, value, icon]) => <Card key={label} style={styles.stat}><Ionicons name={icon as keyof typeof Ionicons.glyphMap} size={20} color={colors.accent} /><Text style={styles.label}>{label}</Text><Text style={styles.value}>{value}</Text></Card>)}</View>
    <View style={styles.sectionHead}><Text style={styles.sectionTitle}>Câu hỏi gần đây</Text><Pressable onPress={() => router.push('/(tabs)/questions')}><Text style={styles.link}>Xem tất cả</Text></Pressable></View>
    {recent.length ? recent.map(item => <Pressable key={item.id} onPress={() => router.push({ pathname: '/question/[id]', params: { id: item.id } })}><Card><Text numberOfLines={2} style={styles.question}>{item.content}</Text><View style={styles.meta}><Text style={styles.muted}>{new Date(item.updatedAt).toLocaleDateString('vi-VN')}</Text><StatusBadge label={item.status} tone={item.status === 'APPROVED' ? 'success' : item.status === 'REJECTED' ? 'danger' : 'warning'} /></View></Card></Pressable>) : <EmptyState title="Chưa có câu hỏi" description="Tạo câu hỏi thủ công hoặc sử dụng AI hỗ trợ." />}
  </ScrollView></AppScreen>;
}

const styles = StyleSheet.create({ content: { gap: spacing.md, paddingBottom: spacing.xl }, actions: { flexDirection: 'row', gap: spacing.sm }, grid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm }, stat: { flexBasis: '48%', gap: spacing.xs }, label: { color: colors.textSecondary, fontSize: typography.caption }, value: { color: colors.text, fontSize: 24, fontWeight: '800', marginTop: spacing.xs }, sectionHead: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between', marginTop: spacing.md }, sectionTitle: { color: colors.text, fontSize: typography.body, fontWeight: '800' }, link: { color: colors.accent, fontSize: typography.caption, fontWeight: '700' }, question: { color: colors.text, fontSize: typography.label, fontWeight: '600' }, meta: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between', marginTop: spacing.md }, muted: { color: colors.textSecondary, fontSize: typography.caption } });
