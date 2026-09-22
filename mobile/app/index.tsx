import { Redirect } from 'expo-router';
import { useAuth } from '@/src/app/providers/AppProviders';
import { LoadingIndicator } from '@/src/components/shared';

export default function Index() {
  const { session, bootstrapping } = useAuth();
  if (bootstrapping) return <LoadingIndicator label="Đang khôi phục phiên đăng nhập" />;
  return <Redirect href={session ? '/(tabs)' : '/(auth)/login'} />;
}
