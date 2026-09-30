const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

async function styles(button) {
  return button.evaluate(element => {
    const value = getComputedStyle(element)
    return { background: value.backgroundColor, color: value.color, border: value.borderColor, cursor: value.cursor }
  })
}

async function main() {
  const browser = await chromium.launch({ headless: true, executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe` })
  try {
    const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })
    const errors = []
    page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
    page.on('pageerror', error => errors.push(error.message))
    await page.route('**/api/v1/admin/session', async route => {
      if (route.request().method() === 'POST') await new Promise(resolve => setTimeout(resolve, 900))
      await route.fulfill({ json: { authenticated: false, username: null, csrfToken: '', csrfHeaderName: 'X-XSRF-TOKEN' } })
    })
    await page.goto('http://127.0.0.1:4174/admin/login', { waitUntil: 'domcontentloaded' })
    const button = page.locator('button[type="submit"]')
    await button.waitFor()
    await page.waitForTimeout(500)
    const normal = await styles(button)
    await button.hover()
    await page.waitForTimeout(250)
    const hover = await styles(button)
    await page.locator('input[autocomplete="username"]').fill('state-check')
    await page.locator('input[autocomplete="current-password"]').fill('state-check')
    await button.click()
    await page.waitForTimeout(180)
    const loading = await styles(button)
    const disabledDuringLoading = await button.isDisabled()
    await page.locator('.login-error').waitFor()
    const errorLinked = await page.locator('input[autocomplete="username"]').getAttribute('aria-describedby')
    process.stdout.write(JSON.stringify({ normal, hover, loading, disabledDuringLoading, errorLinked, errors }, null, 2))
  } finally { await browser.close() }
}
main().catch(error => { console.error(error); process.exitCode = 1 })
