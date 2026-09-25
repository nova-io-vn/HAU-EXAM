import {useState} from 'react'
import {initialsFromName} from '../../features/users/model/academic'
export function Avatar({user,size='md',className=''}){const[failedUrl,setFailedUrl]=useState('');const url=user?.avatar||user?.avatarUrl;const show=url&&failedUrl!==url;return <span className={`avatar avatar-${size} ${className}`} aria-label={show?'Ảnh đại diện':`Chữ viết tắt của ${user?.fullName||user?.lecturerCode||'giảng viên'}`}>{show?<img src={url} alt="" onError={()=>setFailedUrl(url)}/>:initialsFromName(user||{})}</span>}
