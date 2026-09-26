import {test as base,expect} from '@playwright/test'

export const ids={user:'00000000-0000-0000-0000-000000000001',admin:'00000000-0000-0000-0000-000000000002',question:'00000000-0000-0000-0000-000000000010',subject:'00000000-0000-0000-0000-000000000020',chapter:'00000000-0000-0000-0000-000000000030',topic:'00000000-0000-0000-0000-000000000040',matrix:'00000000-0000-0000-0000-000000000050',exam:'00000000-0000-0000-0000-000000000060',job:'00000000-0000-0000-0000-000000000070',document:'00000000-0000-0000-0000-000000000080',conversation:'00000000-0000-0000-0000-000000000090'}
const initialState={questionStatus:'DRAFT',notificationRead:false,approved:false,jobStatus:'PROCESSING',jobReads:0,currentRole:'USER',profileUnauthorizedOnce:false,refreshCalls:0}
const state={...initialState}

function token(role){const encode=value=>Buffer.from(JSON.stringify(value)).toString('base64url');return `${encode({alg:'none',typ:'JWT'})}.${encode({sub:role==='USER'?ids.user:ids.admin,lecturerCode:role==='USER'?'E2E_USER':'E2E_ADMIN',role,facultyId:'CNTT'})}.signature`}
function session(role='USER'){const lecturerCode=role==='USER'?'E2E_USER':role==='SUBJECT_ADMIN'?'E2E_SUBJECT_ADMIN':'E2E_ADMIN';return {userId:role==='USER'?ids.user:ids.admin,lecturerCode,role,currentUser:{id:role==='USER'?ids.user:ids.admin,lecturerCode,role,facultyId:'CNTT'},facultyId:'CNTT',accessToken:token(role),refreshToken:`e2e-refresh-${role}`,accessTokenExpiresAt:'2099-01-01T00:00:00Z',refreshTokenExpiresAt:'2099-01-01T00:00:00Z'}}
function question(){return {id:ids.question,facultyId:'CNTT',subjectId:ids.subject,chapterId:ids.chapter,topicId:ids.topic,content:'E2E question content',type:'SINGLE_CHOICE',difficulty:'MEDIUM',source:'MANUAL',status:state.questionStatus,createdBy:ids.user,createdAt:'2026-01-01T00:00:00Z',updatedAt:'2026-01-01T00:00:00Z',options:['A','B','C','D'].map((label,index)=>({id:`${ids.question}-${label}`,label,content:`Option ${label}`,correct:index===0,sortOrder:index})),reviewHistory:[]}}
function ok(data,code='SUCCESS'){return {success:true,code,message:'Operation successful',data}}
function page(data){return ok({items:data,page:0,totalPages:1,totalElements:data.length})}

export const test=base.extend({gateway:async({context},use)=>{
  Object.assign(state,initialState)
  await context.addInitScript(()=>{window.__HAU_DISABLE_ONBOARDING__=true})
  await context.route('**/api/v1/**',async route=>{
    const request=route.request(),url=new URL(request.url()),path=url.pathname,method=request.method()
    let body
    try{body=request.postDataJSON()}catch{body=null}
    const corsHeaders={'Access-Control-Allow-Origin':'http://127.0.0.1:4173','Access-Control-Allow-Credentials':'true','Access-Control-Allow-Headers':'Content-Type, Authorization, Accept','Access-Control-Allow-Methods':'GET, POST, PUT, PATCH, DELETE, OPTIONS'}
    if(method==='OPTIONS')return route.fulfill({status:204,headers:corsHeaders})
    const respond=(data,status=200)=>route.fulfill({status,headers:{...corsHeaders,'Content-Type':'application/json'},body:JSON.stringify(data)})
    if(path==='/api/v1/auth/login'&&method==='POST'){const code=String(body?.lecturerCode||'').toLowerCase();const role=code==='e2e_subject_admin'?'SUBJECT_ADMIN':code==='e2e_admin'?'SYSTEM_ADMIN':'USER';state.currentRole=role;return respond(ok(session(role),'LOGIN_SUCCESS'))}
    if(path==='/api/v1/public/faculties'&&method==='GET')return respond(ok({content:[{id:'faculty-cntt',code:'CNTT',name:'Công nghệ thông tin',active:true}],page:0,size:100,totalElements:1,totalPages:1}))
    if(path==='/api/v1/auth/refresh'&&method==='POST'){state.refreshCalls+=1;return respond(ok(session(state.currentRole),'TOKEN_REFRESHED'))}
    if(path==='/api/v1/auth/logout'&&method==='POST')return respond(ok(null,'LOGOUT_SUCCESS'))
    if(path==='/api/v1/auth/register'&&method==='POST'){
      if(!body?.lecturerCode||!body?.password||!body?.fullName||!body?.email)return respond({success:false,code:'VALIDATION_FAILED',message:'Missing registration profile',data:null},400)
      return respond(ok({status:'PENDING_APPROVAL'},'REGISTERED'),201)
    }
    if(path==='/api/v1/auth/forgot-password'&&method==='POST')return respond(ok({status:'OTP_REQUESTED'},'OTP_REQUESTED'))
    if(path==='/api/v1/auth/verify-otp'&&method==='POST')return respond(ok({verified:true,resetToken:'e2e-reset'},'OTP_VERIFIED'))
    if(path==='/api/v1/auth/reset-password'&&method==='POST')return respond(ok(null,'PASSWORD_RESET'))
    if(path==='/api/v1/users/me'&&method==='GET'&&state.profileUnauthorizedOnce){state.profileUnauthorizedOnce=false;return respond({success:false,code:'INVALID_ACCESS_TOKEN',message:'Expired',data:null},401)}
    if(path==='/api/v1/users/me'&&method==='GET'){
      const authorization=request.headers().authorization||''
      let role=state.currentRole
      try{role=JSON.parse(Buffer.from(authorization.split('.')[1]||'','base64url').toString()).role||role}catch{void 0}
      const current=session(role).currentUser
      return respond(ok({...current,fullName:role==='USER'?'E2E User':role==='SUBJECT_ADMIN'?'E2E Subject Admin':'E2E System Admin',email:'e2e@example.test',facultyName:'Công nghệ thông tin',status:'ACTIVE',updatedAt:'2026-01-01T00:00:00Z'}))
    }
    if(path==='/api/v1/users/me/chat-contacts'&&method==='GET')return respond(ok([{userId:ids.user,displayName:'Nguyễn Văn An',role:'USER',facultyId:'CNTT',avatarUrl:null}]))
    if(path.startsWith('/api/v1/users')&&method==='GET')return respond(page([{id:ids.user,lecturerCode:'E2E_USER',fullName:'E2E User',email:'e2e@example.test',facultyId:'CNTT',role:'USER',status:state.approved?'ACTIVE':'PENDING_APPROVAL',updatedAt:'2026-01-01T00:00:00Z'}]))
    if(path===`/api/v1/users/${ids.user}/approve`&&method==='POST'){state.approved=true;return respond(ok(null,'USER_APPROVED'))}
    if(path==='/api/v1/subjects'&&method==='GET')return respond([{id:ids.subject,name:'E2E Subject'}])
    if(path==='/api/v1/chapters'&&method==='GET')return respond([{id:ids.chapter,name:'E2E Chapter'}])
    if(path==='/api/v1/topics'&&method==='GET')return respond([{id:ids.topic,name:'E2E Topic'}])
    if(path==='/api/v1/questions'&&method==='GET')return respond(page(state.questionStatus==='PENDING_REVIEW'?[question()]:[]))
    if(path==='/api/v1/questions'&&method==='POST'){state.questionStatus='DRAFT';return respond(ok(question()),201)}
    if(path===`/api/v1/questions/${ids.question}`&&method==='GET')return respond(ok(question()))
    if(path===`/api/v1/questions/${ids.question}/submit`&&method==='POST'){state.questionStatus='PENDING_REVIEW';return respond(ok(question(),'QUESTION_SUBMITTED'))}
    if(path===`/api/v1/questions/${ids.question}/approve`&&method==='POST'){state.questionStatus='APPROVED';return respond(ok(question(),'QUESTION_APPROVED'))}
    if(path==='/api/v1/notifications'&&method==='GET')return respond(ok([{id:'notification-1',type:'QUESTION_APPROVED',title:'Question approved',content:'Your question was approved.',read:state.notificationRead,createdAt:'2026-01-01T00:00:00Z'}]))
    if(path==='/api/v1/notifications/unread-count'&&method==='GET')return respond(state.notificationRead?0:1)
    if(path.endsWith('/read')&&method==='POST'){state.notificationRead=true;return respond(ok(null,'NOTIFICATION_READ'))}
    if(path.endsWith('/read-all')&&method==='POST'){state.notificationRead=true;return respond(ok(null,'NOTIFICATIONS_READ'))}
    if(path==='/api/v1/support/unread-count'&&method==='GET')return respond(ok(0))
    if((path==='/api/v1/support/conversations/my'||path==='/api/v1/admin/support/conversations')&&method==='GET')return respond(ok({content:[{id:ids.conversation,createdByUserId:ids.user,createdByRole:'USER',facultyId:'CNTT',subject:'Trao đổi với quản trị viên',status:'OPEN',lastMessageAt:'2026-01-01T08:30:00Z'}],number:0,size:20,totalElements:1,totalPages:1}))
    if(path===`/api/v1/support/conversations/${ids.conversation}/messages`&&method==='GET')return respond(ok({content:[{id:'support-message-1',conversationId:ids.conversation,senderId:ids.user,senderRole:'USER',content:'Tôi cần hỗ trợ tạo câu hỏi.',createdAt:'2026-01-01T08:30:00Z',attachments:[]}],number:0,size:50,totalElements:1,totalPages:1}))
    if(path===`/api/v1/support/conversations/${ids.conversation}/read`&&method==='PATCH')return respond(ok(1))
    if(path==='/api/v1/ai/system-help'&&method==='POST')return respond(ok({answer:'Mở Tạo câu hỏi bằng AI, chọn tài liệu, môn học và chương.',actions:[{type:'NAVIGATE',label:'Mở tạo câu hỏi AI',routeKey:'AI_GENERATE'}],sources:[]}))
    if(path==='/api/v1/documents'&&method==='GET')return respond(page([{id:ids.document,originalName:'giao-trinh-kien-truc.txt',size:2048,createdAt:'2026-01-01T00:00:00Z'}]))
    if(path==='/api/v1/ai/generate/questions'&&method==='POST')return respond(ok({jobId:ids.job,type:'QUESTION_GENERATION',status:'PROCESSING'},'AI_JOB_ACCEPTED'),202)
    if(path==='/api/v1/admin/ai-settings'&&method==='GET')return respond(ok({provider:'GEMINI',model:'gemini-2.5-flash',apiKeyConfigured:true}))
    if(path==='/api/v1/admin/analytics/traffic'&&method==='GET')return respond(ok({configured:true,today:{visitors:14,pageviews:29},lastSevenDays:{visitors:82,pageviews:173},monthToDate:{visitors:241,pageviews:519},daily:[],generatedAt:'2026-09-26T00:00:00Z'}))
    if(path==='/api/v1/admin/platform/cloudinary'&&method==='GET')return respond(ok({configured:true,cloudName:'e2e-cloud',apiKeyConfigured:true,apiSecretConfigured:true}))
    if(path==='/api/v1/admin/ai-policy/documents'&&method==='GET')return respond(ok([]))
    if(path==='/api/v1/admin/ai-policy/documents'&&method==='POST')return respond(ok({id:'knowledge-1',title:'Quy chế đào tạo',originalFileName:'quy-che.txt',contentType:'text/plain',fileSize:19,status:'READY',enabled:true,chunkCount:3,createdAt:'2026-09-26T00:00:00Z',updatedAt:'2026-09-26T00:00:00Z'}))
    if(path==='/api/v1/admin/email-settings'&&method==='GET')return respond(ok({smtpHost:'smtp.gmail.com',smtpPort:587,smtpUsername:'smtp-user@example.test',passwordConfigured:true,fromEmail:'smtp-user@example.test',fromName:'HAU QM',security:'STARTTLS',enabled:true}))
    if(path==='/api/v1/admin/email-settings'&&method==='PUT')return respond(ok({...body,passwordConfigured:true}))
    if(path==='/api/v1/admin/email-settings/test'&&method==='POST')return respond(ok(null))
    if(path==='/api/v1/admin/contact'&&method==='GET')return respond(ok({content:[{id:'contact-1',name:'Nguyễn Văn A',email:'contact@example.test',subject:'Cần hỗ trợ',message:'Nội dung cần hỗ trợ chi tiết.',facultyId:'CNTT',status:'NEW',createdAt:'2026-01-01T00:00:00Z'}],number:0,size:20,totalElements:1,totalPages:1}))
    if(path==='/api/v1/admin/contact/contact-1/reply'&&method==='POST')return respond({success:false,code:'EMAIL_SEND_FAILED',message:'Delivery failed',data:null},502)
    if(path==='/api/v1/ai/jobs'&&method==='GET')return respond(page([{jobId:ids.job,type:'QUESTION_GENERATION',status:state.jobStatus,createdAt:'2026-01-01T00:00:00Z'}]))
    if(path===`/api/v1/ai/jobs/${ids.job}`&&method==='GET'){state.jobReads+=1;const status=state.jobReads>1?'COMPLETED':state.jobStatus;return respond(ok({jobId:ids.job,type:'QUESTION_GENERATION',status,createdAt:'2026-01-01T00:00:00Z'}))}
    if(path===`/api/v1/ai/jobs/${ids.job}/result`&&method==='GET')return respond(ok([{question:'Generated question',options:[{label:'A',content:'Answer A'}],correctAnswer:'A',difficulty:'MEDIUM'}]))
    if(path==='/api/v1/exam-matrices'&&method==='GET')return respond([{id:ids.matrix,name:'E2E Matrix',facultyId:'CNTT',subjectId:ids.subject,totalQuestions:1,rules:[{id:'rule-1',chapterId:ids.chapter,topicId:null,difficulty:'EASY',questionCount:1}],updatedAt:'2026-01-01T00:00:00Z'}])
    if(path==='/api/v1/exams/generate'&&method==='POST')return respond(ok({id:ids.exam},'EXAM_GENERATED'))
    if(path===`/api/v1/exams/${ids.exam}`&&method==='GET')return respond(ok({id:ids.exam,name:'E2E Exam',subjectId:ids.subject,matrixId:ids.matrix,versions:[{id:'version-1',version:1,generatedAt:'2026-01-01T00:00:00Z',questions:[]}]}))
    return respond(ok(null))
  })
  await use({
    login:async(role='USER',targetPage)=>{const activePage=targetPage||context.pages()[0];await activePage.goto('/login');await activePage.getByLabel('Mã giảng viên').fill(role==='USER'?'E2E_USER':role==='SUBJECT_ADMIN'?'E2E_SUBJECT_ADMIN':'E2E_ADMIN');await activePage.locator('input[name="password"]').fill('test-password');await activePage.getByRole('button',{name:'Đăng nhập'}).click();await expect(activePage).toHaveURL(/dashboard/)},
    expireNextProfile:()=>{state.profileUnauthorizedOnce=true},
    refreshCalls:()=>state.refreshCalls,
  })
}})
export {expect}
