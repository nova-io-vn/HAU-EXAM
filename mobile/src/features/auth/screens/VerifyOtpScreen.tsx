import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { AuthLayout } from '@/src/features/auth/components/AuthLayout';
import { AuthError } from '@/src/features/auth/components/AuthError';
import { authApi } from '@/src/features/auth/api/authApi';
import { authErrorMessage } from '@/src/features/auth/model/authMessages';
import { Button, Input } from '@/src/components/ui';
import { spacing } from '@/src/theme/tokens';
import { authStore } from '@/src/stores/authStore';

export function VerifyOtpScreen() { const router = useRouter(); const { lecturerCode = '' } = useLocalSearchParams<{ lecturerCode: string }>(); const [otp, setOtp] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(false); async function submit() { setError(''); setLoading(true); try { const identity = String(lecturerCode); const result = await authApi.verifyOtp({ lecturerCode: identity, otp }); if (!result.verified) throw new Error('INVALID_OTP'); authStore.setResetData({ lecturerCode: identity, otp }); router.push('/(auth)/reset-password'); } catch (reason) { setError(authErrorMessage(reason, 'Mã OTP không hợp lệ.')); } finally { setLoading(false); } } return <AuthLayout title="Xác minh OTP" description="Nhập mã OTP đã được gửi đến kênh liên hệ của bạn."><View style={styles.form}><AuthError message={error} /><Input label="Mã OTP" value={otp} onChangeText={value => setOtp(value.replace(/\D/g, '').slice(0, 6))} keyboardType="number-pad" maxLength={6} autoComplete="one-time-code" /><Button title="Xác minh" loading={loading} disabled={otp.length !== 6 || loading} onPress={() => void submit()} /></View></AuthLayout>; }
const styles = StyleSheet.create({ form: { gap: spacing.lg } });
