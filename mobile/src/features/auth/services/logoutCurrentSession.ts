import { authApi } from '@/src/features/auth/api/authApi';
import { getRefreshToken } from '@/src/services/api';
import { disableCurrentPushNotifications } from '@/src/services/notifications/pushService';
import { authStore } from '@/src/stores/authStore';

export async function logoutCurrentSession() {
  try {
    await disableCurrentPushNotifications();
  } catch {
    // Push cleanup is best-effort and must not prevent logout.
  }

  try {
    const refreshToken = await getRefreshToken();
    if (refreshToken) await authApi.logout(refreshToken);
  } catch {
    // Server revocation failure must not retain the local session.
  }

  await authStore.clear();
}
