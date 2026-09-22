import {Ionicons} from '@expo/vector-icons';
import {Pressable,StyleSheet,Text,View} from 'react-native';
import {useRouter} from 'expo-router';
import {AppScreen,Card} from '@/src/components/ui';
import {AppHeader} from '@/src/components/shared';
import {useAuth} from '@/src/app/providers/AppProviders';
import {spacing,typography} from '@/src/theme/tokens';
import {useThemeColors} from '@/src/theme/useThemeColors';
import type {MobileRole} from '@/src/app/navigation/roleNavigation';

type Item={title:string;subtitle?:string;icon:keyof typeof Ionicons.glyphMap;path:string};
const common:Item[]=[
  {title:'Trợ lý HAU QM',subtitle:'Hỏi đáp cách sử dụng hệ thống',icon:'chatbubbles-outline',path:'/system-help'},
  {title:'Hồ sơ',icon:'person-outline',path:'/profile'},
  {title:'Giao diện',icon:'contrast-outline',path:'/appearance'},
  {title:'Trợ giúp',icon:'help-circle-outline',path:'/help'},
  {title:'Đăng xuất',icon:'log-out-outline',path:'/logout'},
];
const itemsByRole:Record<MobileRole,Item[]>={
  USER:[{title:'AI',subtitle:'Tạo câu hỏi bằng AI',icon:'sparkles-outline',path:'/ai'},{title:'Tài liệu AI',subtitle:'Quản lý học liệu đã tải lên',icon:'folder-open-outline',path:'/ai'},...common],
  SUBJECT_ADMIN:[{title:'Chuyên môn',icon:'library-outline',path:'/review'},{title:'Môn học',icon:'book-outline',path:'/subjects'},{title:'Chủ đề',icon:'git-branch-outline',path:'/topics'},{title:'Ma trận',icon:'grid-outline',path:'/exams'},...common],
  SYSTEM_ADMIN:[{title:'Khoa',icon:'school-outline',path:'/admin/faculties'},{title:'Chờ phê duyệt',icon:'checkmark-done-outline',path:'/admin/approvals'},{title:'Yêu cầu liên hệ',icon:'mail-outline',path:'/admin/contact'},{title:'Cấu hình Email',icon:'settings-outline',path:'/admin/email-settings'},...common],
};

export default function MoreScreen(){const router=useRouter();const role=(useAuth().session?.role||'USER')as MobileRole;const colors=useThemeColors();return <AppScreen scroll><AppHeader title="Thêm" subtitle="Truy cập các chức năng khác"/><View style={styles.list}>{itemsByRole[role].map(item=><Pressable key={item.title} accessibilityRole="button" onPress={()=>router.push(item.path as never)}><Card style={styles.item}><View style={[styles.icon,{backgroundColor:colors.accentSoft}]}><Ionicons name={item.icon} size={22} color={colors.accent}/></View><View style={styles.copy}><Text style={[styles.title,{color:colors.text}]}>{item.title}</Text>{item.subtitle&&<Text style={[styles.subtitle,{color:colors.textSecondary}]}>{item.subtitle}</Text>}</View><Ionicons name="chevron-forward" size={20} color={colors.textSecondary}/></Card></Pressable>)}</View></AppScreen>}
const styles=StyleSheet.create({list:{gap:spacing.sm},item:{alignItems:'center',flexDirection:'row',gap:spacing.md,paddingVertical:spacing.md},icon:{alignItems:'center',borderRadius:10,height:42,justifyContent:'center',width:42},copy:{flex:1,gap:spacing.xs},title:{fontSize:typography.body,fontWeight:'700'},subtitle:{fontSize:typography.caption}});
