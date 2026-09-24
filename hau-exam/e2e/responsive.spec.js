import {test,expect} from './fixtures.js'

const roleRoutes={SYSTEM_ADMIN:'/admin/settings',SUBJECT_ADMIN:'/exams/generate',USER:'/questions/new'}
const auditedViewports=[320,360,375,390,412,430,768,820,834,1024,1180,1280,1366,1440,1920]

async function assertNoPageOverflow(page){
  await expect.poll(()=>page.evaluate(()=>document.documentElement.scrollWidth-document.documentElement.clientWidth)).toBeLessThanOrEqual(1)
}

for(const role of Object.keys(roleRoutes)){
  test(`${role} keeps responsive shell and role navigation`,async({page,gateway})=>{
    await page.setViewportSize({width:375,height:812})
    await gateway.login(role)
    await page.goto(roleRoutes[role])
    await assertNoPageOverflow(page)
    await expect(page.locator('.mobile-toggle')).toBeVisible()
    await page.locator('.mobile-toggle').click()
    await expect(page.locator('.sidebar.is-open')).toBeVisible()
    await expect(page.locator('.sidebar-backdrop.is-open')).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(page.locator('.sidebar.is-open')).toHaveCount(0)
    await assertNoPageOverflow(page)
  })
}

for(const width of [768,820,1024,1366,1920]){
  test(`authenticated workspace has no page overflow at ${width}px`,async({page,gateway})=>{
    await page.setViewportSize({width,height:width<1100?1024:900})
    await gateway.login('USER')
    await page.goto('/dashboard')
    await expect(page.locator('.app-content')).toBeVisible()
    await assertNoPageOverflow(page)
  })
}

test('viewport audit matrix stays within the viewport',async({page,gateway})=>{
  test.setTimeout(120000)
  await gateway.login('USER')
  for(const width of auditedViewports){
    await page.setViewportSize({width,height:width<768?812:width<1200?1024:900})
    await page.goto('/dashboard')
    await expect(page.locator('.app-content')).toBeVisible()
    await assertNoPageOverflow(page)
  }
})

test('question editor stacks controls on a phone',async({page,gateway})=>{
  await page.setViewportSize({width:360,height:800})
  await gateway.login('USER')
  await page.goto('/questions/new')
  await expect(page.locator('.question-editor')).toBeVisible()
  await assertNoPageOverflow(page)
  const editorSections=page.locator('.question-editor > fieldset > .editor-section')
  await expect(editorSections.first()).toBeVisible()
})
