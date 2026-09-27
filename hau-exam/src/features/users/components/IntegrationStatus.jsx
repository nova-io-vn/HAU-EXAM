import { StatusBadge } from '../../../components/ui/StatusBadge';

export function IntegrationStatus({ status, checkedAt, errorCode, className = '' }) {
  return <div className={`integration-status ${className}`}>
    <StatusBadge status={status || 'NOT_CONFIGURED'} />
    {checkedAt && <small>Kiểm tra gần nhất: {new Date(checkedAt).toLocaleString('vi-VN')}</small>}
    {errorCode && status === 'ERROR' && <small role="alert">Mã lỗi: {errorCode}</small>}
  </div>;
}
