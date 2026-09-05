import {useCallback,useEffect,useState} from 'react'
import {PageHeader} from '../../../components/shared/PageHeader'
import {usersApi} from '../api/usersApi'
import {Pagination} from '../components/Pagination'
import {RequestState} from '../components/RequestState'
import {UserFilters} from '../components/UserFilters'
import {UserTable} from '../components/UserTable'
import {normalizePage} from '../model/userModel'

export function UserListPage(){const[draft,setDraft]=useState({keyword:'',role:'',status:'',facultyId:''});const[query,setQuery]=useState(draft);const[page,setPage]=useState(0);const[data,setData]=useState(normalizePage());const[loading,setLoading]=useState(true);const[error,setError]=useState(null);const load=useCallback(async()=>{setLoading(true);setError(null);try{setData(normalizePage(await usersApi.list({...query,page,size:10})))}catch(reason){setError(reason)}finally{setLoading(false)}},[page,query]);useEffect(()=>{const task=setTimeout(load,0);return()=>clearTimeout(task)},[load]);function submit(event){event.preventDefault();setPage(0);setQuery(draft)}return <section><PageHeader title="Quản lý người dùng" description="Quản lý hồ sơ, vai trò, khoa và trạng thái tài khoản."/><UserFilters filters={draft} onChange={setDraft} onSubmit={submit}/><div className="surface user-table-surface"><RequestState loading={loading} error={error} onRetry={load}/>{!loading&&!error&&<><UserTable users={data.items}/><Pagination {...data} onPageChange={setPage}/></>}</div></section>}
