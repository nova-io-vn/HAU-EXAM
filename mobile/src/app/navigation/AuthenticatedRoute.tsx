import { PropsWithChildren, useEffect } from 'react';
import { useRouter, useSegments } from 'expo-router';
import { useAuth } from '@/src/app/providers/AppProviders';
import { LoadingIndicator } from '@/src/components/shared';

export function AuthenticatedRoute({ children }: PropsWithChildren) {
  const { session, bootstrapping } = useAuth();
  const router = useRouter();
  const segments = useSegments();
  const inAuth = segments[0] === '(auth)';

  useEffect(() => {
    if (bootstrapping) return;
    if (!session && !inAuth) router.replace('/(auth)/login');
    if (session && inAuth) router.replace('/(tabs)');
  }, [bootstrapping, inAuth, router, session]);

  if (bootstrapping) return <LoadingIndicator label="Đang khôi phục phiên đăng nhập" />;
  return <>{children}</>;
}
