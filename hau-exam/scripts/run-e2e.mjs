import {spawn,spawnSync} from 'node:child_process'
import process from 'node:process'

const host='127.0.0.1'
const port='4173'
const baseUrl=process.env.E2E_BASE_URL||`http://${host}:${port}`
const server=spawn(process.execPath,['node_modules/vite/bin/vite.js','--host',host,'--port',port,'--strictPort'],{
  cwd:process.cwd(),env:process.env,stdio:'ignore',windowsHide:true,
})

async function waitForServer(){
  const deadline=Date.now()+30_000
  while(Date.now()<deadline){
    if(server.exitCode!==null)throw new Error(`Vite exited before E2E startup (code ${server.exitCode})`)
    try{const response=await fetch(baseUrl);if(response.ok)return}catch{/* server is still starting */}
    await new Promise(resolve=>setTimeout(resolve,200))
  }
  throw new Error(`Timed out waiting for Vite at ${baseUrl}`)
}

function stopServer(){
  if(server.exitCode!==null)return
  if(process.platform==='win32'){
    spawnSync('taskkill',['/PID',String(server.pid),'/T','/F'],{stdio:'ignore',windowsHide:true})
    try{process.kill(server.pid,'SIGKILL')}catch{/* process tree already stopped */}
  }
  else{try{server.kill('SIGTERM')}catch{/* process already stopped */}}
}

let exitCode=1
try{
  await waitForServer()
  const test=spawn(process.execPath,['node_modules/@playwright/test/cli.js','test'],{
    cwd:process.cwd(),env:{...process.env,E2E_EXTERNAL_SERVER:'1',E2E_BASE_URL:baseUrl},stdio:'inherit',windowsHide:true,
  })
  exitCode=await new Promise((resolve,reject)=>{test.once('error',reject);test.once('exit',code=>resolve(code??1))})
}finally{
  stopServer()
}
process.exit(exitCode)
