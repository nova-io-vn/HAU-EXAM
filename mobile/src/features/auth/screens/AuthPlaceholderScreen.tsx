import { AppScreen, Card, StatusBadge } from '@/src/components/ui';
import { EmptyState } from '@/src/components/shared';
export function AuthPlaceholderScreen({ title }: { title: string }) { return <AppScreen><Card><StatusBadge label="AUTH FOUNDATION" /><EmptyState title={title} description="Màn hình nghiệp vụ sẽ được triển khai theo API contract ở phase tiếp theo." /></Card></AppScreen>; }
