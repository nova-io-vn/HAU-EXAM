import { useCallback, useEffect, useState } from 'react';
import { FlatList, Pressable, RefreshControl, StyleSheet, Text } from 'react-native';
import { useRouter } from 'expo-router';
import { AppScreen, Card, Input, StatusBadge } from '@/src/components/ui';
import { AppHeader, EmptyState, ErrorState, LoadingIndicator } from '@/src/components/shared';
import { RoleGate } from '@/src/app/navigation/RoleGate';
import { normalizeError } from '@/src/services/api/errors';
import { colors, spacing, typography } from '@/src/theme/tokens';
import { adminApi, type AdminUser } from '../api/adminApi';

export function UsersScreen() { return <RoleGate roles={['SYSTEM_ADMIN']}><UsersContent /></RoleGate>; }

function UsersContent() {
  const router = useRouter(); const [items, setItems] = useState<AdminUser[]>([]); const [search, setSearch] = useState(''); const [loading, setLoading] = useState(true); const [refreshing, setRefreshing] = useState(false); const [error, setError] = useState<string | null>(null);
  const load = useCallback(async (refresh = false, keyword = search) => { if (refresh) setRefreshing(true); else setLoading(true); setError(null); try { setItems((await adminApi.listUsers(0, 20, { keyword: keyword.trim() || undefined })).content); } catch (cause) { setError(normalizeError(cause).message); } finally { setLoading(false); setRefreshing(false); } }, [search]);
  useEffect(() => { const timer = setTimeout(() => void load(false, search), 350); return () => clearTimeout(timer); }, [search, load]);
  if (loading && items.length === 0) return <AppScreen><AppHeader title="Quản lý người dùng" subtitle="Tài khoản và phân quyền" /><LoadingIndicator label="Đang tải người dùng" /></AppScreen>;
  return <AppScreen><AppHeader title="Quản lý người dùng" subtitle="Tài khoản và phân quyền" /><Input label="Tìm kiếm" value={search} onChangeText={setSearch} placeholder="Mã giảng viên, họ tên, email" autoCapitalize="none" />{error && <ErrorState title={error} onRetry={() => void load()} />}{!error && <FlatList data={items} keyExtractor={item => item.id} contentContainerStyle={items.length ? styles.list : styles.empty} refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => void load(true)} tintColor={colors.accent} />} renderItem={({ item }) => <Pressable accessibilityRole="button" onPress={() => router.push({ pathname: '/admin/user/[id]', params: { id: item.id } })}><Card><Text style={styles.title}>{item.fullName || item.lecturerCode}</Text><Text style={styles.meta}>{item.lecturerCode} · {item.email}</Text><Text style={styles.meta}>{item.facultyId || 'Chưa phân công Khoa'} · {item.role === 'SUBJECT_ADMIN' ? 'Quản trị viên chuyên môn' : item.role === 'SYSTEM_ADMIN' ? 'Quản trị viên hệ thống' : 'Giảng viên'}</Text><StatusBadge label={item.status} tone={item.status === 'ACTIVE' ? 'success' : item.status === 'LOCKED' || item.status === 'REJECTED' ? 'danger' : 'warning'} /></Card></Pressable>} ListEmptyComponent={<EmptyState title="Không tìm thấy người dùng" description="Thử thay đổi từ khóa hoặc tải lại dữ liệu." />} />}</AppScreen>;
}

const styles = StyleSheet.create({ list: { gap: spacing.md, paddingBottom: spacing.xl }, empty: { flexGrow: 1 }, title: { color: colors.text, fontSize: typography.body, fontWeight: '700' }, meta: { color: colors.textSecondary, fontSize: typography.caption, marginTop: spacing.sm } });
