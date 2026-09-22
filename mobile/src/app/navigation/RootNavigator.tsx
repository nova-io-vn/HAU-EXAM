import { Stack } from 'expo-router';
import { AuthenticatedRoute } from './AuthenticatedRoute';
import {useThemeColors} from '@/src/theme/useThemeColors';

export function RootNavigator() {
  const colors=useThemeColors();
  return <AuthenticatedRoute><Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: colors.background }, headerTintColor: colors.text, headerShadowVisible: false }} /></AuthenticatedRoute>;
}
