import { useState } from 'react';
import { View } from 'react-native';
import { useRouter } from 'expo-router';
import { AuthLayout } from '@/src/features/auth/components/AuthLayout';
import { AuthError } from '@/src/features/auth/components/AuthError';
import { authApi } from '@/src/features/auth/api/authApi';
import { authErrorMessage } from '@/src/features/auth/model/authMessages';
import { Button, Input } from '@/src/components/ui';
import { spacing } from '@/src/theme/tokens';

export function ForgotPasswordScreen() { const router = useRouter(); const [lecturerCode, setLecturerCode] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(false); async function submit() { setError(''); setLoading(true); try { await authApi.forgotPassword(lecturerCode.trim()); router.push({ pathname: '/(auth)/verify-otp', params: { lecturerCode: lecturerCode.trim() } }); } catch (reason) { setError(authErrorMessage(reason, 'Không thể gửi yêu cầu OTP.')); } finally { setLoading(false); } } return <AuthLayout title="Quên mật khẩu" description="Nhập mã giảng viên để yêu cầu mã OTP."><View style={{ gap: spacing.lg }}><AuthError message={error} /><Input label="Mã giảng viên" value={lecturerCode} onChangeText={setLecturerCode} autoCapitalize="characters" autoComplete="username" /><Button title="Gửi yêu cầu OTP" loading={loading} disabled={!lecturerCode.trim() || loading} onPress={() => void submit()} /></View></AuthLayout>; }
