import { AppScreen, Card } from '@/src/components/ui';
import { EmptyState } from './EmptyState';
export function NotAvailableScreen({ title = 'Không khả dụng',description="Tính năng này chưa có màn hình Mobile chuyên biệt. Bạn có thể sử dụng Web; backend vẫn quyết định quyền truy cập." }: { title?: string;description?:string }) { return <AppScreen><Card><EmptyState title={title} description={description}/></Card></AppScreen>; }
