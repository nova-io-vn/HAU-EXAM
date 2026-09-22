import * as Notifications from 'expo-notifications';
import Constants from 'expo-constants';
import { Platform } from 'react-native';
import { EXPO_PROJECT_ID } from '@/src/config/env';
import { pushApi, type PushPlatform } from '@/src/features/notifications/api/pushApi';
import { secureStorage } from '@/src/services/storage/secureStorage';

Notifications.setNotificationHandler({ handleNotification: async () => ({ shouldShowAlert: true, shouldShowBanner: true, shouldShowList: true, shouldPlaySound: false, shouldSetBadge: true }) });

function platform(): PushPlatform { return Platform.OS === 'ios' ? 'IOS' : 'ANDROID'; }
const pushTokenKey = 'hau-exam.push-token';
export async function registerPushToken(token: string) { await pushApi.register({ token, platform: platform(), deviceIdentifier: Constants.deviceId || undefined }); await secureStorage.setItem(pushTokenKey, token); }
export async function enablePushNotifications() {
  if (Platform.OS === 'web') throw new Error('OS push notifications require a native Expo build.');
  if (!EXPO_PROJECT_ID) throw new Error('EXPO_PUBLIC_EXPO_PROJECT_ID is required for Expo Push Token registration.');
  const current = await Notifications.getPermissionsAsync();
  const permission = (current as unknown as { granted: boolean }).granted ? current : await Notifications.requestPermissionsAsync();
  if (!(permission as unknown as { granted: boolean }).granted) throw new Error('Notification permission was not granted.');
  const token = (await Notifications.getExpoPushTokenAsync({ projectId: EXPO_PROJECT_ID })).data;
  await registerPushToken(token);
  return token;
}

export async function disablePushNotifications(token: string) {
  await pushApi.unregister({ token, platform: platform(), deviceIdentifier: Constants.deviceId || undefined });
  await secureStorage.deleteItem(pushTokenKey);
}
export async function disableCurrentPushNotifications() { const token = await secureStorage.getItem(pushTokenKey); if (token) await disablePushNotifications(token); }
