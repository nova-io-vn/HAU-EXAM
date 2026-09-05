export function Loading({label='Đang tải'}){return <div className="loading" role="status"><span className="spinner"/><span>{label}</span></div>}
export function Skeleton({lines=3}){return <div className="skeleton" aria-label="Đang tải">{Array.from({length:lines},(_,i)=><span key={i}/>)}</div>}
