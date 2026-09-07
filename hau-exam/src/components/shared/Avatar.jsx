import {useState} from 'react'
import {initialsFromName} from '../../features/users/model/academic'
export function Avatar({user,size='md',className=''}){const[failed,setFailed]=useState(false);const show=user?.avatar&&!failed;return <span className={`avatar avatar-${size} ${className}`} aria-label={show?'Ảnh đại diện':`Chữ viết tắt của ${user?.fullName||user?.lecturerCode||'giảng viên'}`}>{show?<img src={user.avatar} alt="" onError={()=>setFailed(true)}/>:initialsFromName(user||{})}</span>}
