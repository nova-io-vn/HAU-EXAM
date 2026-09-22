import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useRouter } from 'expo-router';
import { AuthLayout } from '@/src/features/auth/components/AuthLayout';
import { AuthError } from '@/src/features/auth/components/AuthError';
import { authApi } from '@/src/features/auth/api/authApi';
import { authErrorMessage } from '@/src/features/auth/model/authMessages';
import { Button, Input } from '@/src/components/ui';
import { spacing } from '@/src/theme/tokens';
import { authStore } from '@/src/stores/authStore';

export function ResetPasswordScreen() { const router = useRouter(); const { lecturerCode = '', otp = '' } = authStore.getResetData() || {}; const [newPassword, setNewPassword] = useState(''); const [confirmation, setConfirmation] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(false); async function submit() { if (newPassword.length < 8 || newPassword !== confirmation) { setError('Mật khẩu mới phải có tối thiểu 8 ký tự và khớp xác nhận.'); return; } if (!lecturerCode || !otp) { setError('Phiên đặt lại mật khẩu không còn hợp lệ.'); return; } setError(''); setLoading(true); try { await authApi.resetPassword({ lecturerCode, otp, newPassword }); authStore.clearResetData(); router.replace('/(auth)/login'); } catch (reason) { setError(authErrorMessage(reason, 'Không thể đặt lại mật khẩu.')); } finally { setLoading(false); } } return <AuthLayout title="Đặt lại mật khẩu" description="Tạo mật khẩu mới cho tài khoản của bạn."><View style={styles.form}><AuthError message={error} /><Input label="Mật khẩu mới" value={newPassword} onChangeText={setNewPassword} secureTextEntry autoComplete="new-password" /><Input label="Xác nhận mật khẩu" value={confirmation} onChangeText={setConfirmation} secureTextEntry autoComplete="new-password" /><Button title="Đặt lại mật khẩu" loading={loading} disabled={loading || newPassword.length < 8 || !confirmation} onPress={() => void submit()} /></View></AuthLayout>; }
const styles = StyleSheet.create({ form: { gap: spacing.lg } });
