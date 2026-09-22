import { StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { AuthLayout } from '@/src/features/auth/components/AuthLayout';
import { Button, Card } from '@/src/components/ui';
import { colors, spacing, typography } from '@/src/theme/tokens';

export function PendingApprovalScreen() { const router = useRouter(); return <AuthLayout title="Đang chờ phê duyệt" description="Đăng ký của bạn đã được tiếp nhận."><Card><View style={styles.content}><Text style={styles.title}>Tài khoản đang chờ phê duyệt.</Text><Text style={styles.body}>Quản trị viên hệ thống sẽ kiểm tra thông tin trước khi bạn có thể đăng nhập vào HAU QM System.</Text><Button title="Về đăng nhập" onPress={() => router.replace('/(auth)/login')} /></View></Card></AuthLayout>; }
const styles = StyleSheet.create({ content: { gap: spacing.md }, title: { color: colors.text, fontSize: typography.body, fontWeight: '700' }, body: { color: colors.textSecondary, fontSize: typography.body, lineHeight: 24 } });
