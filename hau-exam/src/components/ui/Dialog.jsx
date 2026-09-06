import {useEffect,useId,useRef} from 'react'

const focusable='button,[href],input,select,textarea,[tabindex]:not([tabindex="-1"])'

export function Dialog({open,title,children,onClose,footer}){
  const ref=useRef(null)
  const previousFocus=useRef(null)
  const titleId=`dialog-title-${useId().replaceAll(':','')}`
  useEffect(()=>{
    const dialog=ref.current
    if(open){
      previousFocus.current=document.activeElement
      if(!dialog.open)dialog.showModal()
      const first=dialog.querySelector(focusable)
      first?.focus()
    }else if(dialog.open)dialog.close()
    if(!open&&previousFocus.current?.focus)previousFocus.current.focus()
  },[open])
  useEffect(()=>{
    if(!open)return
    function handleKeyDown(event){
      if(event.key!=='Tab')return
      const items=[...ref.current?.querySelectorAll(focusable)||[]].filter(item=>!item.hasAttribute('disabled'))
      if(!items.length)return
      const first=items[0],last=items.at(-1)
      if(event.shiftKey&&document.activeElement===first){event.preventDefault();last.focus()}
      else if(!event.shiftKey&&document.activeElement===last){event.preventDefault();first.focus()}
    }
    document.addEventListener('keydown',handleKeyDown)
    return()=>document.removeEventListener('keydown',handleKeyDown)
  },[open])
  function cancel(event){event.preventDefault();onClose()}
  return <dialog ref={ref} className="dialog" onCancel={cancel} aria-labelledby={titleId}><div className="dialog-head"><h2 id={titleId}>{title}</h2><button type="button" onClick={onClose} aria-label="Đóng">×</button></div><div className="dialog-body">{children}</div>{footer&&<div className="dialog-footer">{footer}</div>}</dialog>
}
