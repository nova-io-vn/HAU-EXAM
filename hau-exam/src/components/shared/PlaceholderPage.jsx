import {PageHeader} from './PageHeader'
import {EmptyState} from '../ui/EmptyState'
export function PlaceholderPage({title,description}){return <section><PageHeader title={title} description={description}/><div className="surface"><EmptyState title="Sẵn sàng cho phase nghiệp vụ" description="Foundation đã cung cấp layout, trạng thái và component dùng chung cho màn hình này."/></div></section>}
