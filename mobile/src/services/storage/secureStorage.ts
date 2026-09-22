import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

const webStorage = {
  get: (key: string) => (typeof sessionStorage === 'undefined' ? null : sessionStorage.getItem(key)),
  set: (key: string, value: string) => sessionStorage.setItem(key, value),
  remove: (key: string) => sessionStorage.removeItem(key),
};

export const secureStorage = {
  getItem: async (key: string) => (Platform.OS === 'web' ? webStorage.get(key) : SecureStore.getItemAsync(key)),
  setItem: async (key: string, value: string) => {
    if (Platform.OS === 'web') webStorage.set(key, value);
    else await SecureStore.setItemAsync(key, value, { keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY });
  },
  deleteItem: async (key: string) => {
    if (Platform.OS === 'web') webStorage.remove(key);
    else await SecureStore.deleteItemAsync(key);
  },
};
