import { useEffect } from 'react';
import * as Notifications from 'expo-notifications';
import { useRouter } from 'expo-router';
import { useAuth } from './AppProviders';
import { Platform } from 'react-native';
import { registerPushToken } from '@/src/services/notifications/pushService';

function openReference(router: ReturnType<typeof useRouter>, data: Record<string, unknown>) {
  const id = typeof data.referenceId === 'string' ? data.referenceId : undefined;
  if (!id) return;
  const type = typeof data.referenceType === 'string' ? data.referenceType.toUpperCase() : '';
  const notificationType = typeof data.type === 'string' ? data.type : '';
  if (type.includes('AI') || notificationType.startsWith('AI_')) router.push({ pathname: '/ai/job/[id]', params: { id } });
  else if (type.includes('QUESTION') || notificationType.startsWith('QUESTION_')) router.push({ pathname: '/question/[id]', params: { id } });
}

export function PushNotificationManager() {
  const router = useRouter(); const { session } = useAuth();
  useEffect(() => { if (!session) return; const handleResponse = (response: Notifications.NotificationResponse) => { const data = response.notification.request.content.data; if (data && typeof data === 'object') openReference(router, data as Record<string, unknown>); }; const responseSubscription = Notifications.addNotificationResponseReceivedListener(handleResponse); void Notifications.getLastNotificationResponseAsync().then(response => { if (response) handleResponse(response); }); const tokenSubscription = Platform.OS === 'web' ? undefined : Notifications.addPushTokenListener(token => { void registerPushToken(token.data); }); return () => { responseSubscription.remove(); tokenSubscription?.remove(); }; }, [router, session]);
  return null;
}
