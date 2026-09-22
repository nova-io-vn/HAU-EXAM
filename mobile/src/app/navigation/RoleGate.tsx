import { PropsWithChildren } from 'react';
import { useAuth } from '@/src/app/providers/AppProviders';
import { NotAvailableScreen } from '@/src/components/shared';
import type { MobileRole } from './roleNavigation';

export function RoleGate({ roles, children }: PropsWithChildren<{ roles: readonly MobileRole[] }>) { const role = useAuth().session?.role; return role && roles.includes(role as MobileRole) ? children : <NotAvailableScreen />; }
