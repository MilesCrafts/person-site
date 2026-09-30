const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)
const mobile = process.argv.includes('--mobile')
const errorState = process.argv.includes('--error')
const screenshot = errorState
  ? String.raw`C:\Users\lenovo\.codex\visualizations\2026\08\10\019fec40-685f-7bb0-b2b4-86158ba7efeb\admin-login-error-state.png`
  : mobile
  ? String.raw`C:\Users\lenovo\.codex\visualizations\2026\08\10\019fec40-685f-7bb0-b2b4-86158ba7efeb\admin-login-companion-mobile.png`
  : String.raw`C:\Users\lenovo\.codex\visualizations\2026\08\10\019fec40-685f-7bb0-b2b4-86158ba7efeb\admin-login-companion-password.png`

async function main() {
  const browser = await chromium.launch({ headless: true, executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe` })
  try {
    const page = await browser.newPage({ viewport: mobile ? { width: 375, height: 812 } : { width: 1280, height: 900 } })
    const errors = []
    page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
    page.on('pageerror', error => errors.push(error.message))
    await page.route('**/api/v1/admin/session', route => route.fulfill({ json: { authenticated: false, username: null, csrfToken: '', csrfHeaderName: 'X-XSRF-TOKEN' } }))
    await page.goto('http://127.0.0.1:4174/admin/login', { waitUntil: 'domcontentloaded' })
    await page.locator('.login-companion').waitFor()
    await page.waitForTimeout(900)
    const pupil = page.locator('.companion-pupil').first()
    const before = await pupil.evaluate(element => getComputedStyle(element).transform)
    await page.mouse.move(mobile ? 340 : 1170, 180)
    await page.waitForTimeout(450)
    const afterPointer = await pupil.evaluate(element => getComputedStyle(element).transform)
    await page.locator('input[type="password"]').focus()
    await page.waitForTimeout(650)
    const hand = await page.locator('.companion-hand-left').evaluate(element => getComputedStyle(element).transform)
    const secretOpacity = await page.locator('.companion-secret').evaluate(element => getComputedStyle(element).opacity)
    let passwordType = 'password'
    let errorVisible = false
    if (errorState) {
      await page.locator('input[autocomplete="username"]').fill('wrong-user')
      await page.locator('input[autocomplete="current-password"]').fill('wrong-password')
      await page.locator('.password-toggle').click()
      passwordType = await page.locator('input[autocomplete="current-password"]').getAttribute('type') || ''
      await page.locator('button[type="submit"]').click()
      await page.locator('.login-error').waitFor()
      errorVisible = await page.locator('.login-error').isVisible()
    }
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
    await page.screenshot({ path: screenshot, fullPage: true })
    process.stdout.write(JSON.stringify({ before, afterPointer, hand, secretOpacity, passwordType, errorVisible, overflow, errors, screenshot }, null, 2))
  } finally { await browser.close() }
}
main().catch(error => { console.error(error); process.exitCode = 1 })
