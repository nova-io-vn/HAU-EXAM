import { PropsWithChildren } from 'react';
import { ScrollView, StyleSheet, View, ViewProps } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { spacing } from '@/src/theme/tokens';
import { useThemeColors } from '@/src/theme/useThemeColors';

export function AppScreen({ children, scroll = false, ...props }: PropsWithChildren<ViewProps & { scroll?: boolean }>) {
  const colors = useThemeColors();
  const content = scroll ? <ScrollView contentContainerStyle={styles.content}>{children}</ScrollView> : <View style={styles.content}>{children}</View>;
  return <SafeAreaView style={[styles.safe, { backgroundColor: colors.background }]} {...props}>{content}</SafeAreaView>;
}
const styles = StyleSheet.create({ safe: { flex: 1 }, content: { flexGrow: 1, padding: spacing.lg } });
