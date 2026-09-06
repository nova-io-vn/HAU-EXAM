import {defineConfig,devices} from '@playwright/test'

export default defineConfig({
  testDir:'./e2e',
  timeout:30000,
  fullyParallel:false,
  forbidOnly:Boolean(process.env.CI),
  retries:process.env.CI?2:0,
  reporter:process.env.CI?[['line'],['html',{open:'never'}]]:[['list']],
  use:{baseURL:process.env.E2E_BASE_URL||'http://127.0.0.1:4173',trace:'retain-on-failure',screenshot:'only-on-failure',video:'retain-on-failure'},
  projects:[{name:'chromium',use:{...devices['Desktop Chrome']}}],
  webServer:{command:'npm run dev -- --host 127.0.0.1 --port 4173',url:process.env.E2E_BASE_URL||'http://127.0.0.1:4173',reuseExistingServer:false,timeout:120000}
})
