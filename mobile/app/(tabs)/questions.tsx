import { QuestionsScreen } from '@/src/features/questions/screens/QuestionsScreen';
import {UsersScreen} from '@/src/features/admin/screens/UsersScreen';
import {ReviewScreen} from '@/src/features/review/screens/ReviewScreen';
import {useAuth} from '@/src/app/providers/AppProviders';
export default function RoleWorkScreen(){const role=useAuth().session?.role;return role==='SYSTEM_ADMIN'?<UsersScreen/>:role==='SUBJECT_ADMIN'?<ReviewScreen/>:<QuestionsScreen/>}
