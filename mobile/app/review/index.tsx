import { ReviewScreen } from '@/src/features/review/screens/ReviewScreen';
import { RoleGate } from '@/src/app/navigation/RoleGate';
export default function ReviewRoute() { return <RoleGate roles={['SUBJECT_ADMIN']}><ReviewScreen /></RoleGate>; }
