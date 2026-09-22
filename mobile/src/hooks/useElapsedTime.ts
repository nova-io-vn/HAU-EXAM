import { useEffect, useState } from 'react';
import { elapsedSeconds } from '@/src/utils/asyncProgress';
export function useElapsedTime(startedAt?: string | null, endedAt?: string | null, active = true) { const [now, setNow] = useState(0); useEffect(() => { if (!active || endedAt) return undefined; const timer = setInterval(() => setNow(Date.now()), 1000); return () => clearInterval(timer); }, [active, endedAt]); return endedAt ? elapsedSeconds(startedAt, endedAt) : elapsedSeconds(startedAt, null, now); }
