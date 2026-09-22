import { PropsWithChildren, createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { Session } from '@/src/types/api';
import { authStore } from '@/src/stores/authStore';
import { bootstrapAuth } from '@/src/app/bootstrap/bootstrapAuth';
import { OnboardingTourProvider } from '@/src/features/onboarding/MobileOnboardingTour';
import { registerSessionExpiredHandler } from '@/src/services/api';

type AuthContextValue = { session: Session | null; bootstrapping: boolean; refresh: () => Promise<Session | null>; clear: () => Promise<void> };
const AuthContext = createContext<AuthContextValue | null>(null);

export function AppProviders({ children }: PropsWithChildren) {
  const [session, setSession] = useState(authStore.get());
  const [bootstrapping, setBootstrapping] = useState(true);
  useEffect(() => { const unsubscribe = authStore.subscribe(() => setSession(authStore.get())); return () => { unsubscribe(); }; }, []);
  useEffect(() => registerSessionExpiredHandler(() => { void authStore.clear(); }), []);
  useEffect(() => { bootstrapAuth().finally(() => setBootstrapping(false)); }, []);
  const value = useMemo(() => ({ session, bootstrapping, refresh: bootstrapAuth, clear: authStore.clear }), [session, bootstrapping]);
  return <AuthContext.Provider value={value}><OnboardingTourProvider>{children}</OnboardingTourProvider></AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AppProviders');
  return context;
}
