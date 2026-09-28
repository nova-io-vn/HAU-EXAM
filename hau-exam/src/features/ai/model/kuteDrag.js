const SIZE={width:78,height:92}
export function clampKutePosition(position,viewport){const screen=viewport||{width:window.innerWidth,height:window.innerHeight};const margin=10,top=70,bottom=screen.width<=640?82:14;return{x:Math.max(margin,Math.min(position.x,screen.width-SIZE.width-margin)),y:Math.max(top,Math.min(position.y,screen.height-SIZE.height-bottom))}}
export function exceededDragThreshold(dx,dy,threshold=7){return Math.hypot(dx,dy)>=threshold}
