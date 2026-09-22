import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { AuthLayout } from '@/src/features/auth/components/AuthLayout';
import { AuthError } from '@/src/features/auth/components/AuthError';
import { authApi } from '@/src/features/auth/api/authApi';
import { authErrorMessage } from '@/src/features/auth/model/authMessages';
import { authStore } from '@/src/stores/authStore';
import { Button, Input } from '@/src/components/ui';
import { colors, spacing, typography } from '@/src/theme/tokens';

export function LoginScreen() {
  const router = useRouter(); const [lecturerCode, setLecturerCode] = useState(''); const [password, setPassword] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(false);
  async function submit() { setError(''); setLoading(true); try { await authStore.set(await authApi.login({ lecturerCode: lecturerCode.trim(), password })); router.replace('/(tabs)'); } catch (reason) { setError(authErrorMessage(reason, 'Đăng nhập không thành công.')); } finally { setLoading(false); } }
  return <AuthLayout title="Đăng nhập" description="Chào mừng trở lại. Đăng nhập bằng mã giảng viên để tiếp tục."><View style={styles.form}><AuthError message={error} /><Input label="Mã giảng viên" value={lecturerCode} onChangeText={setLecturerCode} autoCapitalize="characters" autoComplete="username" returnKeyType="next" /><Input label="Mật khẩu" value={password} onChangeText={setPassword} secureTextEntry autoComplete="password" returnKeyType="go" onSubmitEditing={() => void submit()} /><Button title="Đăng nhập" loading={loading} disabled={!lecturerCode.trim() || !password || loading} onPress={() => void submit()} /><Pressable accessibilityRole="button" onPress={() => router.push('/(auth)/forgot-password')}><Text style={styles.link}>Quên mật khẩu?</Text></Pressable><Text style={styles.registerPrompt}>Chưa có tài khoản?</Text><Pressable accessibilityRole="button" onPress={() => router.push('/(auth)/register')}><Text style={styles.link}>Đăng ký tài khoản</Text></Pressable></View></AuthLayout>;
}
const styles = StyleSheet.create({ form: { gap: spacing.lg }, link: { color: colors.accent, fontSize: typography.body, fontWeight: '600', textAlign: 'center' }, registerPrompt: { color: colors.textSecondary, fontSize: typography.label, textAlign: 'center', marginTop: spacing.md } });
