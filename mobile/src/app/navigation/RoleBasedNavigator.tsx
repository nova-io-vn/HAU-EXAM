import { Tabs } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { HapticTab } from '@/components/haptic-tab';
import { useAuth } from '@/src/app/providers/AppProviders';
import type { MobileRole } from './roleNavigation';
import {useThemeColors} from '@/src/theme/useThemeColors';

const labels: Record<MobileRole, string> = { USER: 'Câu hỏi', SUBJECT_ADMIN: 'Duyệt', SYSTEM_ADMIN: 'Người dùng' };

export function RoleBasedNavigator() {
  const role = (useAuth().session?.role || 'USER') as MobileRole;
  const insets = useSafeAreaInsets();
  const colors=useThemeColors();
  return <Tabs screenOptions={{ headerShown: false, tabBarActiveTintColor: colors.accent, tabBarInactiveTintColor: colors.textSecondary, tabBarStyle: { backgroundColor: colors.surface, borderTopColor: colors.border, height: 62 + insets.bottom, paddingBottom: Math.max(insets.bottom, 8), paddingTop: 6 }, tabBarLabelStyle: { fontSize: 11, fontWeight: '600' }, tabBarButton: HapticTab }}>
    <Tabs.Screen name="index" options={{ title: 'Trang chủ', tabBarIcon: ({ color, size }) => <Ionicons name="home-outline" size={size} color={color} /> }} />
    <Tabs.Screen name="questions" options={{ title: labels[role], tabBarIcon: ({ color, size }) => <Ionicons name={role === 'USER' ? 'document-text-outline' : role === 'SUBJECT_ADMIN' ? 'checkmark-circle-outline' : 'people-outline'} size={size} color={color} /> }} />
    <Tabs.Screen name="notifications" options={{ title: 'Thông báo', tabBarIcon: ({ color, size }) => <Ionicons name="notifications-outline" size={size} color={color} /> }} />
    <Tabs.Screen name="more" options={{ title: 'Thêm', tabBarIcon: ({ color, size }) => <Ionicons name="menu-outline" size={size} color={color} /> }} />
  </Tabs>;
}
