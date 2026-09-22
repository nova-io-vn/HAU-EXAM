import {useEffect} from 'react';
import {useRouter} from 'expo-router';
import {LoadingIndicator} from '@/src/components/shared';
import {useAuth} from '@/src/app/providers/AppProviders';
import {authApi} from '@/src/features/auth/api/authApi';
import {getRefreshToken} from '@/src/services/api';
export default function LogoutScreen(){const router=useRouter();const{clear}=useAuth();useEffect(()=>{async function logout(){try{const token=await getRefreshToken();if(token)await authApi.logout(token)}catch{/* local session is still cleared */}finally{await clear();router.replace('/(auth)/login')}}void logout()},[clear,router]);return <LoadingIndicator label="Đang đăng xuất"/>}
