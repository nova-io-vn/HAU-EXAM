import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';
import { radius, spacing, typography } from '@/src/theme/tokens';
import { Button } from './Button';
import {useThemeColors} from '@/src/theme/useThemeColors';

export function ConfirmModal({ visible, title, description, onCancel, onConfirm }: { visible: boolean; title: string; description?: string; onCancel: () => void; onConfirm: () => void }) {
  const colors=useThemeColors();return <Modal visible={visible} transparent animationType="slide" onRequestClose={onCancel}><Pressable style={styles.backdrop} onPress={onCancel}><View accessibilityViewIsModal style={[styles.sheet,{backgroundColor:colors.surface}]} onStartShouldSetResponder={() => true}><Text style={[styles.title,{color:colors.text}]}>{title}</Text>{description && <Text style={[styles.description,{color:colors.textSecondary}]}>{description}</Text>}<View style={styles.actions}><Button title="Hủy" variant="secondary" onPress={onCancel} /><Button title="Xác nhận" onPress={onConfirm} /></View></View></Pressable></Modal>;
}
const styles = StyleSheet.create({ backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.45)', justifyContent: 'flex-end' }, sheet: { borderTopLeftRadius: radius.lg, borderTopRightRadius: radius.lg, padding: spacing.xl, gap: spacing.md, maxHeight:'85%' }, title: { fontSize: typography.heading, fontWeight: '700' }, description: { fontSize: typography.body }, actions: { flexDirection: 'row', gap: spacing.md } });
