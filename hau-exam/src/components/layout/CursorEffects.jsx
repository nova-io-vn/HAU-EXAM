import {useEffect,useRef} from 'react'
import {useUiPreferences} from '../../app/providers/ThemeProvider'

const MAX_PARTICLES=42
const isFormControl=target=>target instanceof Element&&Boolean(target.closest('input, textarea, select, [contenteditable="true"]'))

export function CursorEffects(){
  const canvasRef=useRef(null)
  const preferences=useUiPreferences()

  useEffect(()=>{
    const canvas=canvasRef.current
    const effect=preferences?.cursorEffect||'none'
    const reduced=window.matchMedia?.('(prefers-reduced-motion: reduce)')
    const finePointer=window.matchMedia?.('(hover: hover) and (pointer: fine)')
    if(!canvas||effect==='none')return undefined

    const context=canvas.getContext('2d')
    const particles=[]
    let frame=0
    let lastSpawn=0
    let pointer=null
    let dpr=1
    let enabled=!reduced?.matches&&Boolean(finePointer?.matches)

    const resize=()=>{
      if(!enabled)return
      dpr=Math.min(window.devicePixelRatio||1,2)
      canvas.width=Math.round(window.innerWidth*dpr)
      canvas.height=Math.round(window.innerHeight*dpr)
      canvas.style.width=`${window.innerWidth}px`
      canvas.style.height=`${window.innerHeight}px`
      context.setTransform(dpr,0,0,dpr,0,0)
    }
    const addParticles=(x,y,time)=>{
      if(time-lastSpawn<22)return
      lastSpawn=time
      const count=effect==='particles'?2:1
      for(let index=0;index<count;index+=1){
        particles.push({x,y,vx:(Math.random()-.5)*.8,vy:(Math.random()-.5)*.8,life:1,size:effect==='stars'?3.2:effect==='trail'?5:2.4})
      }
      if(particles.length>MAX_PARTICLES)particles.splice(0,particles.length-MAX_PARTICLES)
    }
    const schedule=()=>{if(!frame)frame=window.requestAnimationFrame(render)}
    const move=event=>{
      if(!enabled)return
      if(event.pointerType&&event.pointerType!=='mouse')return
      if(isFormControl(event.target)){pointer=null;schedule();return}
      pointer={x:event.clientX,y:event.clientY}
      if(effect!=='glow')addParticles(event.clientX,event.clientY,event.timeStamp)
      schedule()
    }
    const leave=()=>{pointer=null;schedule()}
    const drawStar=(x,y,r)=>{
      context.beginPath()
      for(let point=0;point<8;point+=1){const radius=point%2?r*.42:r;const angle=(Math.PI/4)*point-Math.PI/2;const px=x+Math.cos(angle)*radius;const py=y+Math.sin(angle)*radius;if(point===0)context.moveTo(px,py);else context.lineTo(px,py)}
      context.closePath();context.fill()
    }
    const render=()=>{
      frame=0
      context.clearRect(0,0,window.innerWidth,window.innerHeight)
      if(effect==='glow'&&pointer){const gradient=context.createRadialGradient(pointer.x,pointer.y,0,pointer.x,pointer.y,34);gradient.addColorStop(0,'rgba(36,112,206,.28)');gradient.addColorStop(1,'rgba(36,112,206,0)');context.fillStyle=gradient;context.beginPath();context.arc(pointer.x,pointer.y,34,0,Math.PI*2);context.fill()}
      for(let index=particles.length-1;index>=0;index-=1){const particle=particles[index];particle.life-=effect==='trail'?.055:.035;particle.x+=particle.vx;particle.y+=particle.vy;if(particle.life<=0){particles.splice(index,1);continue}context.fillStyle=`rgba(36,112,206,${Math.max(0,particle.life)*.62})`;if(effect==='stars')drawStar(particle.x,particle.y,particle.size*particle.life);else{context.beginPath();context.arc(particle.x,particle.y,particle.size*particle.life,0,Math.PI*2);context.fill()}}
      if(particles.length) schedule()
    }
    const capabilityChanged=()=>{
      enabled=!reduced?.matches&&Boolean(finePointer?.matches)
      if(enabled)resize()
      else{pointer=null;particles.length=0;if(frame)window.cancelAnimationFrame(frame);frame=0;context.clearRect(0,0,canvas.width,canvas.height)}
    }
    const observeMedia=(media,listener)=>media?.addEventListener?media.addEventListener('change',listener):media?.addListener?.(listener)
    const forgetMedia=(media,listener)=>media?.removeEventListener?media.removeEventListener('change',listener):media?.removeListener?.(listener)
    if(enabled)resize()
    window.addEventListener('resize',resize)
    window.addEventListener('pointermove',move,{passive:true})
    window.addEventListener('pointerleave',leave)
    observeMedia(reduced,capabilityChanged)
    observeMedia(finePointer,capabilityChanged)
    return()=>{if(frame)window.cancelAnimationFrame(frame);window.removeEventListener('resize',resize);window.removeEventListener('pointermove',move);window.removeEventListener('pointerleave',leave);forgetMedia(reduced,capabilityChanged);forgetMedia(finePointer,capabilityChanged);context.clearRect(0,0,canvas.width,canvas.height)}
  },[preferences?.cursorEffect])

  return <canvas ref={canvasRef} className="cursor-effects" aria-hidden="true"/>
}
