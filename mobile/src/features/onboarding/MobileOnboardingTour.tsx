/* eslint-disable react-hooks/set-state-in-effect -- preserve existing onboarding hydration behavior. */
import { PropsWithChildren, createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { usePathname, useRouter } from 'expo-router';
import { useAuth } from '@/src/app/providers/AppProviders';
import { colors, radius, spacing, typography } from '@/src/theme/tokens';
import { CURRENT_ONBOARDING_VERSION, onboardingApi } from './api/onboardingApi';

type Step = { route: string; title: string; description: string };
type ContextValue = { replay: () => void };
const OnboardingContext = createContext<ContextValue | null>(null);
const steps: Record<string, Step[]> = {
  USER: [
    { route: '/(tabs)', title: 'Tổng quan', description: 'Theo dõi nhanh câu hỏi, trạng thái phê duyệt và các thao tác chính.' },
    { route: '/(tabs)/questions', title: 'Câu hỏi', description: 'Quản lý câu hỏi bạn đã tạo và theo dõi trạng thái thẩm định.' },
    { route: '/ai', title: 'Trợ lý AI', description: 'Dùng học liệu để tạo các câu hỏi nháp bằng AI.' },
    { route: '/(tabs)/notifications', title: 'Thông báo', description: 'Nhận phản hồi phê duyệt và cập nhật tác vụ trong hệ thống.' },
    { route: '/profile', title: 'Tài khoản', description: 'Quản lý hồ sơ, ảnh đại diện và thông tin học thuật.' },
    { route: '/help', title: 'Trợ giúp', description: 'Bạn có thể xem lại hướng dẫn này bất kỳ lúc nào.' },
  ],
  SUBJECT_ADMIN: [
    { route: '/(tabs)', title: 'Tổng quan chuyên môn', description: 'Theo dõi nội dung học thuật và câu hỏi trong Khoa được phân công.' },
    { route: '/review', title: 'Duyệt câu hỏi', description: 'Kiểm tra, yêu cầu chỉnh sửa hoặc phê duyệt câu hỏi.' },
    { route: '/(tabs)/questions', title: 'Cấu trúc nội dung', description: 'Truy cập ngân hàng câu hỏi và cấu trúc kiến thức.' },
    { route: '/(tabs)/notifications', title: 'Thông báo', description: 'Theo dõi các hoạt động chuyên môn mới nhất.' },
    { route: '/profile', title: 'Tài khoản', description: 'Quản lý hồ sơ quản trị chuyên môn.' },
    { route: '/help', title: 'Trợ giúp', description: 'Chạy lại hướng dẫn từ Trung tâm trợ giúp.' },
  ],
  SYSTEM_ADMIN: [
    { route: '/(tabs)', title: 'Tổng quan hệ thống', description: 'Theo dõi người dùng, Khoa và các hoạt động quản trị.' },
    { route: '/admin/users', title: 'Người dùng', description: 'Quản lý tài khoản, vai trò, Khoa và trạng thái.' },
    { route: '/admin/approvals', title: 'Tài khoản chờ duyệt', description: 'Xem đăng ký và quyết định Khoa, vai trò trước khi kích hoạt.' },
    { route: '/(tabs)/notifications', title: 'Thông báo', description: 'Theo dõi các sự kiện quản trị quan trọng.' },
    { route: '/profile', title: 'Tài khoản', description: 'Quản lý hồ sơ quản trị hệ thống.' },
    { route: '/help', title: 'Trợ giúp', description: 'Xem lại hướng dẫn sử dụng hệ thống từ đây.' },
  ],
};

export function useOnboarding() { const value = useContext(OnboardingContext); if (!value) throw new Error('useOnboarding must be used inside OnboardingTourProvider'); return value; }

export function OnboardingTourProvider({ children }: PropsWithChildren) {
  const { session, bootstrapping } = useAuth(); const router = useRouter(); const pathname = usePathname(); const role = session?.role || 'USER'; const tour = useMemo(() => steps[role] || steps.USER, [role]);
  const [index, setIndex] = useState(-1); const [state, setState] = useState<{ versionCompleted: number } | null>(null); const [error, setError] = useState('');
  const replay = useCallback(() => { setError(''); setIndex(0); }, []);
  const finish = useCallback(async () => { setIndex(-1); try { await onboardingApi.complete(CURRENT_ONBOARDING_VERSION); } catch { setError('Không thể lưu trạng thái hướng dẫn. Hướng dẫn có thể hiển thị lại ở lần đăng nhập sau.'); } }, []);
  useEffect(() => { if (bootstrapping || !session?.userId) return; let active = true; setState(null); onboardingApi.current().then(value => { if (active) setState(value); }).catch(() => { if (active) setState({ versionCompleted: 0 }); }); return () => { active = false; }; }, [bootstrapping, session?.userId, role]);
  useEffect(() => { if (state && state.versionCompleted < CURRENT_ONBOARDING_VERSION) { const timer = setTimeout(() => setIndex(0), 500); return () => clearTimeout(timer); } return undefined; }, [state]);
  useEffect(() => { if (index < 0 || !tour[index]) return; const target = tour[index].route; if (pathname !== target) router.push(target as never); }, [index, pathname, router, tour]);
  const current = index >= 0 ? tour[index] : null;
  const next = () => { if (index >= tour.length - 1) void finish(); else setIndex(value => value + 1); };
  const back = () => setIndex(value => Math.max(0, value - 1));
  const skip = () => void finish();
  return <OnboardingContext.Provider value={{ replay }}><>{children}{current && <Modal transparent visible animationType="fade" onRequestClose={skip}><View style={styles.overlay}><View style={styles.card}><Text style={styles.title}>{current.title}</Text><Text style={styles.description}>{current.description}</Text><Text style={styles.progress}>{index + 1} / {tour.length}</Text><View style={styles.actions}><Pressable accessibilityRole="button" onPress={skip}><Text style={styles.secondary}>Bỏ qua</Text></Pressable>{index > 0 && <Pressable accessibilityRole="button" onPress={back}><Text style={styles.secondary}>Quay lại</Text></Pressable>}<Pressable accessibilityRole="button" onPress={next} style={styles.primary}><Text style={styles.primaryText}>{index === tour.length - 1 ? 'Hoàn tất' : 'Tiếp tục'}</Text></Pressable></View></View></View></Modal>}{error && <View accessibilityRole="alert" style={styles.error}><Text style={styles.errorText}>{error}</Text></View>}</></OnboardingContext.Provider>;
}

const styles = StyleSheet.create({ overlay: { backgroundColor: 'rgba(20, 12, 16, 0.58)', flex: 1, justifyContent: 'flex-end', padding: spacing.lg }, card: { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: radius.lg, borderWidth: 1, gap: spacing.sm, padding: spacing.lg }, title: { color: colors.text, fontSize: typography.heading, fontWeight: '800' }, description: { color: colors.textSecondary, fontSize: typography.body, lineHeight: 22 }, progress: { color: colors.accent, fontSize: typography.caption, fontWeight: '700' }, actions: { alignItems: 'center', flexDirection: 'row', gap: spacing.md, justifyContent: 'flex-end', marginTop: spacing.sm }, secondary: { color: colors.accent, fontSize: typography.label, fontWeight: '700', padding: spacing.sm }, primary: { backgroundColor: colors.accent, borderRadius: radius.sm, paddingHorizontal: spacing.md, paddingVertical: spacing.sm }, primaryText: { color: colors.white, fontSize: typography.label, fontWeight: '700' }, error: { backgroundColor: colors.surface, bottom: spacing.lg, left: spacing.lg, padding: spacing.md, position: 'absolute', right: spacing.lg }, errorText: { color: colors.danger, fontSize: typography.caption } });
