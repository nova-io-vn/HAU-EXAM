/* eslint-disable react-hooks/set-state-in-effect -- preserve existing async screen-loading behavior. */
import { useCallback, useEffect, useState } from 'react';
import { StyleSheet, Text } from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import { AppScreen, Card, StatusBadge } from '@/src/components/ui';
import { AppHeader, EmptyState, ErrorState, LoadingIndicator } from '@/src/components/shared';
import { normalizeError } from '@/src/services/api/errors';
import { colors, spacing, typography } from '@/src/theme/tokens';
import { examApi, type ExamMatrix } from '../api/examApi';
import { RoleGate } from '@/src/app/navigation/RoleGate';

export function MatrixDetailScreen() { return <RoleGate roles={['SUBJECT_ADMIN']}><MatrixDetailContent /></RoleGate>; }
function MatrixDetailContent() {
  const { id } = useLocalSearchParams<{ id: string }>(); const [matrix, setMatrix] = useState<ExamMatrix | null>(null); const [error, setError] = useState<string | null>(null);
  const load = useCallback(async () => { if (!id) return; setError(null); try { setMatrix(await examApi.getMatrix(id)); } catch (cause) { setError(normalizeError(cause).message); } }, [id]);
  useEffect(() => { void load(); }, [load]);
  if (!matrix && !error) return <AppScreen><AppHeader title="Chi tiết ma trận" /><LoadingIndicator label="Đang tải ma trận" /></AppScreen>;
  if (error) return <AppScreen><AppHeader title="Chi tiết ma trận" /><ErrorState title={error} onRetry={() => void load()} /></AppScreen>;
  if (!matrix) return <AppScreen><AppHeader title="Chi tiết ma trận" /><EmptyState title="Không tìm thấy ma trận" /></AppScreen>;
  return <AppScreen scroll><AppHeader title={matrix.name} subtitle="Chế độ xem trước" /><Card><Text style={styles.label}>Môn học</Text><Text style={styles.value}>{matrix.subjectId}</Text><Text style={styles.label}>Khoa</Text><Text style={styles.value}>{matrix.facultyId}</Text><Text style={styles.label}>Tổng số câu</Text><Text style={styles.value}>{matrix.totalQuestions}</Text></Card><Text style={styles.section}>Quy tắc phân bổ</Text>{matrix.rules.map(rule => <Card key={rule.id}><Text style={styles.value}>{rule.chapterId}{rule.topicId ? ` · ${rule.topicId}` : ''}</Text><Text style={styles.meta}>{rule.difficulty} · {rule.questionCount} câu</Text><StatusBadge label="Chỉ xem trước" tone="neutral" /></Card>)}</AppScreen>;
}
const styles = StyleSheet.create({ section: { color: colors.text, fontSize: typography.heading, fontWeight: '700', marginBottom: spacing.sm, marginTop: spacing.md }, label: { color: colors.textSecondary, fontSize: typography.caption, marginTop: spacing.sm }, value: { color: colors.text, fontSize: typography.body, fontWeight: '600' }, meta: { color: colors.textSecondary, fontSize: typography.caption, marginVertical: spacing.sm } });
