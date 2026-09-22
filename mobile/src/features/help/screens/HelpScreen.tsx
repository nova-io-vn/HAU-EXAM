import { useMemo, useState } from 'react';
import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text } from 'react-native';
import { AppScreen, Button, Card, Input } from '@/src/components/ui';
import { AppHeader, EmptyState } from '@/src/components/shared';
import { useAuth } from '@/src/app/providers/AppProviders';
import { colors, spacing, typography } from '@/src/theme/tokens';
import { useOnboarding } from '@/src/features/onboarding/MobileOnboardingTour';

const common = [
  ['Bắt đầu sử dụng', 'Đăng nhập bằng mã giảng viên và sử dụng các chức năng theo vai trò được phân công.'],
  ['Thông báo', 'Mở mục Thông báo để xem cập nhật mới và đánh dấu thông báo đã đọc.'],
];
const roleSections: Record<string, [string, string][]> = {
  USER: [['Câu hỏi của tôi', 'Tạo câu hỏi, lưu bản nháp và gửi phê duyệt khi nội dung đã hoàn thiện.'], ['Tạo câu hỏi bằng AI', 'Chọn tài liệu và tạo câu hỏi. Câu hỏi do AI tạo vẫn là bản nháp và cần gửi phê duyệt.'], ['Tài liệu của tôi', 'Theo dõi tài liệu đã tải lên và trạng thái xử lý của AI.'], ['Xử lý yêu cầu chỉnh sửa', 'Mở phản hồi của Quản trị viên chuyên môn, cập nhật câu hỏi và gửi lại.']],
  SUBJECT_ADMIN: [['Môn học và cấu trúc kiến thức', 'Quản lý nội dung trong phạm vi Khoa được phân công.'], ['Phê duyệt câu hỏi', 'Kiểm tra câu hỏi cùng Khoa, sau đó phê duyệt, yêu cầu chỉnh sửa hoặc từ chối.'], ['Độ bao phủ và ma trận đề', 'Xem số liệu thật từ hệ thống khi dữ liệu tương ứng được hỗ trợ.']],
  SYSTEM_ADMIN: [['Quản lý người dùng', 'Tìm kiếm tài khoản, gán Khoa, thay đổi vai trò và khóa/mở khóa theo quyền hệ thống.'], ['Phê duyệt tài khoản', 'Chọn Khoa và vai trò trước khi phê duyệt tài khoản đăng ký.'], ['Quản lý Khoa', 'Tạo, cập nhật và thay đổi trạng thái Khoa bằng dữ liệu từ User Service.']],
};

export function HelpScreen() {
  const role = useAuth().session?.role || 'USER'; const { replay } = useOnboarding(); const [query, setQuery] = useState(''); const [open, setOpen] = useState<string | null>(null);
  const sections = useMemo(() => [...common, ...(roleSections[role] || [])].filter(([title, text]) => `${title} ${text}`.toLowerCase().includes(query.trim().toLowerCase())), [query, role]);
  return <AppScreen scroll><AppHeader title="Trung tâm trợ giúp" subtitle={role === 'SYSTEM_ADMIN' ? 'Hướng dẫn quản trị hệ thống, Khoa và tài khoản.' : role === 'SUBJECT_ADMIN' ? 'Hướng dẫn quản lý nội dung và phê duyệt câu hỏi.' : 'Hướng dẫn tạo câu hỏi, AI, tài liệu và phê duyệt.'} showIdentity={false} /><Input label="Tìm kiếm" value={query} onChangeText={setQuery} placeholder="Tìm kiếm hướng dẫn..." /><Card><Text style={styles.title}>Hướng dẫn tương tác</Text><Text style={styles.body}>Khởi động lại hướng dẫn theo vai trò hiện tại.</Text><Button title="Xem lại hướng dẫn hệ thống" variant="secondary" onPress={replay} /></Card>{sections.length ? sections.map(([title, text]) => <Card key={title}><Pressable accessibilityRole="button" onPress={() => setOpen(open === title ? null : title)} style={styles.row}><Text style={styles.title}>{title}</Text><Ionicons name={open === title ? 'chevron-up-outline' : 'chevron-down-outline'} size={20} color={colors.accent} /></Pressable>{open === title && <Text style={styles.body}>{text}</Text>}</Card>) : <EmptyState title="Không tìm thấy hướng dẫn" description="Thử một từ khóa khác." />}</AppScreen>;
}

const styles = StyleSheet.create({ row: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' }, title: { color: colors.text, flex: 1, fontSize: typography.body, fontWeight: '700' }, chevron: { color: colors.accent, fontSize: typography.heading }, body: { color: colors.textSecondary, fontSize: typography.body, lineHeight: 22, marginTop: spacing.md } });
