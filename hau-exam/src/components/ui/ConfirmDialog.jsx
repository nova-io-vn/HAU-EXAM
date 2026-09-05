import {Dialog} from './Dialog'
import {Button} from './Button'
export function ConfirmDialog({open,title='Xác nhận thao tác',description,confirmLabel='Xác nhận',danger=false,onConfirm,onClose}){return <Dialog open={open} title={title} onClose={onClose} footer={<><Button variant="secondary" onClick={onClose}>Hủy</Button><Button variant={danger?'danger':'primary'} onClick={onConfirm}>{confirmLabel}</Button></>}><p>{description}</p></Dialog>}
