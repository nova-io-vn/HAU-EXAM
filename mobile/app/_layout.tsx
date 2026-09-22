import { ThemeProvider, DefaultTheme } from 'expo-router/react-navigation';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';

import { AppProviders } from '@/src/app/providers/AppProviders';
import { PushNotificationManager } from '@/src/app/providers/PushNotificationManager';
import { RootNavigator } from '@/src/app/navigation/RootNavigator';
import { MobileThemeProvider, useAppTheme } from '@/src/theme/ThemePreferenceProvider';

export const unstable_settings = {
  anchor: '(auth)',
};

export default function RootLayout() {
  return <MobileThemeProvider><ThemedRoot/></MobileThemeProvider>;
}

function ThemedRoot() {
  const {resolved,palette}=useAppTheme();
  return (
    <ThemeProvider value={{ ...DefaultTheme, dark: resolved === 'dark', colors: { ...DefaultTheme.colors, background: palette.background, card: palette.surface, text: palette.text, primary: palette.accent, border: palette.border } }}>
      <AppProviders><PushNotificationManager /><RootNavigator /></AppProviders>
      <StatusBar style={resolved === 'dark' ? 'light' : 'dark'} />
    </ThemeProvider>
  );
}
