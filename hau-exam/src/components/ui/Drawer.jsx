import {useEffect,useId,useRef} from 'react'

const focusable='button,[href],input,select,textarea,[tabindex]:not([tabindex="-1"])'

export function Drawer({open,title,children,onClose}){
  const drawerRef=useRef(null)
  const previousFocus=useRef(null)
  const titleId=`drawer-title-${useId().replaceAll(':','')}`
  useEffect(()=>{
    if(open){
      previousFocus.current=document.activeElement
      const first=drawerRef.current?.querySelector(focusable)
      first?.focus()
      return
    }
    if(previousFocus.current?.focus)previousFocus.current.focus()
  },[open])
  useEffect(()=>{
    if(!open)return
    function handleKeyDown(event){
      if(event.key==='Escape'){event.preventDefault();onClose();return}
      if(event.key!=='Tab')return
      const items=[...drawerRef.current?.querySelectorAll(focusable)||[]].filter(item=>!item.hasAttribute('disabled'))
      if(!items.length)return
      const first=items[0],last=items.at(-1)
      if(event.shiftKey&&document.activeElement===first){event.preventDefault();last.focus()}
      else if(!event.shiftKey&&document.activeElement===last){event.preventDefault();first.focus()}
    }
    document.addEventListener('keydown',handleKeyDown)
    return()=>document.removeEventListener('keydown',handleKeyDown)
  },[onClose,open])
  return <><button className={`drawer-backdrop ${open?'open':''}`} hidden={!open} aria-hidden="true" tabIndex={-1} onClick={onClose}/><aside ref={drawerRef} className={`drawer ${open?'open':''}`} role="dialog" aria-modal="true" aria-hidden={!open} aria-labelledby={titleId} hidden={!open} inert={!open}><div className="dialog-head"><h2 id={titleId}>{title}</h2><button type="button" onClick={onClose} aria-label="Đóng">×</button></div>{children}</aside></>
}
