/* eslint-disable react-hooks/set-state-in-effect -- preserve existing async screen-loading behavior. */
import { useCallback, useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text } from 'react-native';
import { useRouter } from 'expo-router';
import { AppScreen, Card, StatusBadge } from '@/src/components/ui';
import { AppHeader, EmptyState, ErrorState, LoadingIndicator } from '@/src/components/shared';
import { normalizeError } from '@/src/services/api/errors';
import { colors, spacing, typography } from '@/src/theme/tokens';
import { examApi, type ExamMatrix, type GeneratedExam } from '../api/examApi';
import { RoleGate } from '@/src/app/navigation/RoleGate';

export function ExamWorkspaceScreen() {
  return <RoleGate roles={['SUBJECT_ADMIN']}><ExamWorkspaceContent /></RoleGate>;
}

function ExamWorkspaceContent() {
  const router = useRouter();
  const [matrices, setMatrices] = useState<ExamMatrix[]>([]);
  const [exams, setExams] = useState<GeneratedExam[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { const [matrixResult, examResult] = await Promise.all([examApi.listMatrices(), examApi.listExams()]); setMatrices(matrixResult); setExams(examResult); }
    catch (cause) { setError(normalizeError(cause).message); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);
  if (loading) return <AppScreen><AppHeader title="Ma trận và bộ đề" /><LoadingIndicator label="Đang tải dữ liệu đề thi" /></AppScreen>;
  if (error) return <AppScreen><AppHeader title="Ma trận và bộ đề" /><ErrorState title={error} onRetry={() => void load()} /></AppScreen>;
  return <AppScreen scroll><AppHeader title="Ma trận và bộ đề" subtitle="Chỉ xem và xem trước dữ liệu hiện có" />
    <Text style={styles.section}>Ma trận đề</Text>
    {matrices.length ? matrices.map(matrix => <Pressable key={matrix.id} accessibilityRole="button" onPress={() => router.push({ pathname: '/exams/matrix/[id]', params: { id: matrix.id } })}><Card><Text style={styles.title}>{matrix.name}</Text><Text style={styles.meta}>Môn học {matrix.subjectId} · {matrix.totalQuestions} câu</Text><StatusBadge label={`${matrix.rules.length} quy tắc`} tone="neutral" /></Card></Pressable>) : <EmptyState title="Chưa có ma trận đề" description="Ma trận sẽ hiển thị khi backend trả về dữ liệu trong phạm vi Khoa." />}
    <Text style={styles.section}>Bộ đề đã sinh</Text>
    {exams.length ? exams.map(exam => <Pressable key={exam.id} accessibilityRole="button" onPress={() => router.push({ pathname: '/exams/[id]', params: { id: exam.id } })}><Card><Text style={styles.title}>{exam.name}</Text><Text style={styles.meta}>Môn học {exam.subjectId} · {exam.versions.length} phiên bản</Text><Text style={styles.meta}>Tạo lúc {new Date(exam.createdAt).toLocaleString('vi-VN')}</Text></Card></Pressable>) : <EmptyState title="Chưa có bộ đề" description="Bộ đề đã sinh sẽ hiển thị tại đây." />}
  </AppScreen>;
}

const styles = StyleSheet.create({ section: { color: colors.text, fontSize: typography.heading, fontWeight: '700', marginBottom: spacing.sm, marginTop: spacing.md }, title: { color: colors.text, fontSize: typography.body, fontWeight: '700' }, meta: { color: colors.textSecondary, fontSize: typography.caption, marginTop: spacing.sm } });
