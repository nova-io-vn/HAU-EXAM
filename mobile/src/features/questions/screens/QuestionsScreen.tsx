/* eslint-disable react-hooks/set-state-in-effect -- preserve existing async catalog-loading behavior. */
import { useCallback, useEffect, useMemo, useState } from 'react';
import { FlatList, Modal, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { AppScreen, Button, Card, Input, Select, StatusBadge } from '@/src/components/ui';
import { AppHeader, EmptyState, ErrorState, LoadingIndicator } from '@/src/components/shared';
import { normalizeError } from '@/src/services/api/errors';
import { colors, radius, spacing, typography } from '@/src/theme/tokens';
import { questionApi, type CatalogItem, type Question, type QuestionFilters } from '../api/questionApi';

type FilterKey = 'subjectId' | 'chapterId' | 'topicId' | 'difficulty' | 'status' | 'source';
const fixedChoices: Record<Exclude<FilterKey, 'subjectId' | 'chapterId' | 'topicId'>, string[]> = {
  difficulty: ['EASY', 'MEDIUM', 'HARD'],
  status: ['DRAFT', 'PENDING_REVIEW', 'APPROVED', 'NEED_REVISION', 'REJECTED'],
  source: ['MANUAL', 'AI'],
};

export function QuestionsScreen() {
  const router = useRouter();
  const [items, setItems] = useState<Question[]>([]);
  const [subjects, setSubjects] = useState<CatalogItem[]>([]);
  const [chapters, setChapters] = useState<CatalogItem[]>([]);
  const [topics, setTopics] = useState<CatalogItem[]>([]);
  const [filters, setFilters] = useState<QuestionFilters>({});
  const [keyword, setKeyword] = useState('');
  const [filterOpen, setFilterOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (next: QuestionFilters = filters, isRefresh = false) => {
    if (isRefresh) setRefreshing(true); else setLoading(true);
    setError(null);
    try { setItems((await questionApi.search(next)).items); }
    catch (cause) { setError(normalizeError(cause).message); }
    finally { setLoading(false); setRefreshing(false); }
  }, [filters]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => { void questionApi.subjects().then(setSubjects).catch(() => undefined); }, []);
  useEffect(() => { if (filters.subjectId) void questionApi.chapters(filters.subjectId).then(setChapters).catch(() => setChapters([])); else setChapters([]); }, [filters.subjectId]);
  useEffect(() => { if (filters.chapterId) void questionApi.topics(filters.chapterId).then(setTopics).catch(() => setTopics([])); else setTopics([]); }, [filters.chapterId]);

  const subjectName = useMemo(() => new Map(subjects.map(item => [item.id, item.name])), [subjects]);
  const chapterName = useMemo(() => new Map(chapters.map(item => [item.id, item.name])), [chapters]);
  const topicName = useMemo(() => new Map(topics.map(item => [item.id, item.name])), [topics]);
  function applyFilters() { setFilters(current => ({ ...current, keyword: keyword.trim() || undefined, page: 0 })); setFilterOpen(false); }
  function clearFilters() { setKeyword(''); setFilters({}); setFilterOpen(false); }

  if (loading && items.length === 0) return <AppScreen><AppHeader title="Câu hỏi" subtitle="Câu hỏi của tôi" /><LoadingIndicator label="Đang tải câu hỏi" /></AppScreen>;
  return <>
    <AppScreen>
      <AppHeader title="Câu hỏi" subtitle="Câu hỏi của tôi" />
      <View style={styles.toolbar}>
        <Input label="Tìm kiếm" value={keyword} onChangeText={setKeyword} placeholder="Tìm theo nội dung" returnKeyType="search" onSubmitEditing={applyFilters} />
        <View style={styles.toolbarActions}><Button title="Bộ lọc" variant="secondary" onPress={() => setFilterOpen(true)} /><Button title="Tạo câu hỏi" onPress={() => router.push('/question/create')} /></View>
      </View>
      {error ? <ErrorState title={error} onRetry={() => void load(filters)} /> : <FlatList data={items} keyExtractor={item => item.id} contentContainerStyle={items.length ? styles.list : styles.emptyList} refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => void load(filters, true)} tintColor={colors.accent} />} renderItem={({ item }) => <QuestionCard question={item} subject={subjectName.get(item.subjectId)} chapter={chapterName.get(item.chapterId)} topic={item.topicId ? topicName.get(item.topicId) : undefined} onPress={() => router.push({ pathname: '/question/[id]', params: { id: item.id } })} />} ListEmptyComponent={<EmptyState title="Chưa có câu hỏi" description="Tạo câu hỏi đầu tiên hoặc điều chỉnh bộ lọc." />} />}
    </AppScreen>
    <FilterSheet visible={filterOpen} filters={filters} subjects={subjects} chapters={chapters} topics={topics} onChange={setFilters} onApply={applyFilters} onClear={clearFilters} onClose={() => setFilterOpen(false)} />
  </>;
}

function QuestionCard({ question, subject, chapter, topic, onPress }: { question: Question; subject?: string; chapter?: string; topic?: string; onPress: () => void }) {
  return <Pressable accessibilityRole="button" onPress={onPress}><Card><Text numberOfLines={3} style={styles.content}>{question.content}</Text><Text style={styles.meta}>{[subject, chapter, topic].filter(Boolean).join(' • ') || 'Chưa có thông tin phân loại'}</Text><View style={styles.badges}><StatusBadge label={question.difficulty} /><StatusBadge label={question.status} tone={question.status === 'APPROVED' ? 'success' : question.status === 'REJECTED' ? 'danger' : 'warning'} /><StatusBadge label={question.source} /><Text style={styles.updated}>{new Date(question.updatedAt).toLocaleDateString('vi-VN')}</Text></View></Card></Pressable>;
}

function FilterSheet({ visible, filters, subjects, chapters, topics, onChange, onApply, onClear, onClose }: { visible: boolean; filters: QuestionFilters; subjects: CatalogItem[]; chapters: CatalogItem[]; topics: CatalogItem[]; onChange: (filters: QuestionFilters) => void; onApply: () => void; onClear: () => void; onClose: () => void }) {
  const [picker, setPicker] = useState<{ key: FilterKey; title: string; items: { value: string; label: string }[] } | null>(null);
  const catalog = (key: 'subjectId' | 'chapterId' | 'topicId', title: string, items: CatalogItem[]) => setPicker({ key, title, items: items.map(item => ({ value: item.id, label: item.name })) });
  const fixed = (key: Exclude<FilterKey, 'subjectId' | 'chapterId' | 'topicId'>, title: string) => setPicker({ key, title, items: fixedChoices[key].map(value => ({ value, label: value })) });
  function choose(value?: string) { if (!picker) return; const next: QuestionFilters = { ...filters, [picker.key]: value }; if (picker.key === 'subjectId') { next.chapterId = undefined; next.topicId = undefined; } if (picker.key === 'chapterId') next.topicId = undefined; onChange(next); setPicker(null); }
  return <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}><Pressable style={styles.backdrop} onPress={onClose}><View style={styles.sheet} onStartShouldSetResponder={() => true}><Text style={styles.sheetTitle}>Bộ lọc câu hỏi</Text><Select label="Môn học" value={subjects.find(item => item.id === filters.subjectId)?.name} onPress={() => catalog('subjectId', 'Môn học', subjects)} /><Select label="Chương" value={chapters.find(item => item.id === filters.chapterId)?.name} onPress={() => catalog('chapterId', 'Chương', chapters)} /><Select label="Chủ đề" value={topics.find(item => item.id === filters.topicId)?.name} onPress={() => catalog('topicId', 'Chủ đề', topics)} /><Select label="Độ khó" value={filters.difficulty} onPress={() => fixed('difficulty', 'Độ khó')} /><Select label="Trạng thái" value={filters.status} onPress={() => fixed('status', 'Trạng thái')} /><Select label="Nguồn" value={filters.source} onPress={() => fixed('source', 'Nguồn')} /><View style={styles.sheetActions}><Button title="Xóa lọc" variant="secondary" onPress={onClear} /><Button title="Áp dụng" onPress={onApply} /></View><ChoiceModal picker={picker} onPick={choose} onClose={() => setPicker(null)} /></View></Pressable></Modal>;
}

function ChoiceModal({ picker, onPick, onClose }: { picker: { title: string; items: { value: string; label: string }[] } | null; onPick: (value?: string) => void; onClose: () => void }) { return <Modal visible={Boolean(picker)} transparent animationType="slide" onRequestClose={onClose}><Pressable style={styles.backdrop} onPress={onClose}><View style={styles.choiceSheet} onStartShouldSetResponder={() => true}>{picker && <><Text style={styles.sheetTitle}>{picker.title}</Text><Pressable onPress={() => onPick()} style={styles.choice}><Text style={styles.choiceText}>Tất cả</Text></Pressable>{picker.items.map(item => <Pressable key={item.value} onPress={() => onPick(item.value)} style={styles.choice}><Text style={styles.choiceText}>{item.label}</Text></Pressable>)}<Button title="Hủy" variant="secondary" onPress={onClose} /></>}</View></Pressable></Modal>; }

const styles = StyleSheet.create({ toolbar: { gap: spacing.md, marginBottom: spacing.md }, toolbarActions: { flexDirection: 'row', gap: spacing.md }, list: { gap: spacing.md, paddingBottom: spacing.xl }, emptyList: { flexGrow: 1 }, content: { color: colors.text, fontSize: typography.body, fontWeight: '600' }, meta: { color: colors.textSecondary, fontSize: typography.caption, marginTop: spacing.sm }, badges: { alignItems: 'center', flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm, marginTop: spacing.md }, updated: { color: colors.textSecondary, fontSize: typography.caption, marginLeft: 'auto' }, backdrop: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(0,0,0,0.25)' }, sheet: { gap: spacing.md, backgroundColor: colors.surface, borderTopLeftRadius: radius.lg, borderTopRightRadius: radius.lg, padding: spacing.xl }, choiceSheet: { gap: spacing.sm, backgroundColor: colors.surface, borderTopLeftRadius: radius.lg, borderTopRightRadius: radius.lg, maxHeight: '80%', padding: spacing.xl }, sheetTitle: { color: colors.text, fontSize: typography.heading, fontWeight: '700' }, sheetActions: { flexDirection: 'row', gap: spacing.md, marginTop: spacing.sm }, choice: { borderBottomColor: colors.border, borderBottomWidth: StyleSheet.hairlineWidth, paddingVertical: spacing.md }, choiceText: { color: colors.text, fontSize: typography.body } });
