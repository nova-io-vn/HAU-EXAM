import Constants from 'expo-constants';

const configuredBaseUrl =
  process.env.EXPO_PUBLIC_API_BASE_URL?.trim() ||
  (Constants.expoConfig?.extra?.apiBaseUrl as string | undefined)?.trim() ||
  '';

export const API_BASE_URL = configuredBaseUrl.replace(/\/$/, '');
export const WS_BASE_URL = (
  process.env.EXPO_PUBLIC_WS_BASE_URL?.trim() || API_BASE_URL.replace(/^http/, 'ws')
).replace(/\/$/, '');
export const EXPO_PROJECT_ID = process.env.EXPO_PUBLIC_EXPO_PROJECT_ID?.trim() || (Constants.expoConfig?.extra?.expoProjectId as string | undefined)?.trim() || '';

export function assertApiConfigured() {
  if (!API_BASE_URL) throw new Error('Mobile configuration error: EXPO_PUBLIC_API_BASE_URL is required.');
  if (!/^https?:\/\/[^/]+(?::\d+)?$/i.test(API_BASE_URL)) throw new Error('Mobile configuration error: EXPO_PUBLIC_API_BASE_URL must contain only scheme, host and optional port.');
}
