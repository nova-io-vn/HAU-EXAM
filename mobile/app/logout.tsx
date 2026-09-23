import {useEffect} from 'react';
import {useRouter} from 'expo-router';
import {LoadingIndicator} from '@/src/components/shared';
import {logoutCurrentSession} from '@/src/features/auth/services/logoutCurrentSession';
export default function LogoutScreen(){const router=useRouter();useEffect(()=>{async function logout(){try{await logoutCurrentSession()}finally{router.replace('/(auth)/login')}}void logout()},[router]);return <LoadingIndicator label="Đang đăng xuất"/>}
