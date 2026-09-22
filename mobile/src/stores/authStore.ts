import type { Session } from '@/src/types/api';
import { clearTokens, saveRefreshToken, setAccessToken } from '@/src/services/api/client';
import { disconnectGatewaySocket } from '@/src/services/websocket/client';

let session: Session | null = null;
let resetData: { lecturerCode: string; otp: string } | null = null;
const listeners = new Set<() => void>();

function notify() { listeners.forEach(listener => listener()); }

export const authStore = {
  get: () => session,
  getResetData: () => resetData,
  subscribe: (listener: () => void) => { listeners.add(listener); return () => listeners.delete(listener); },
  async set(next: Session) { session = next; setAccessToken(next.accessToken); await saveRefreshToken(next.refreshToken); notify(); },
  updateProfile(profile: unknown) {
    if (!session) return;
    const values = profile as { avatar?: string | null; avatarUrl?: string | null } & Record<string, unknown>;
    session = { ...session, ...values, avatar: values.avatar || values.avatarUrl || null } as Session;
    notify();
  },
  async clear() { session = null; resetData = null; disconnectGatewaySocket(); await clearTokens(); notify(); },
  setResetData: (value: { lecturerCode: string; otp: string }) => { resetData = value; },
  clearResetData: () => { resetData = null; },
};
