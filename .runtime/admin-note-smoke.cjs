const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

async function main() {
  const browser = await chromium.launch({ headless: true, executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe` })
  const results = []
  try {
    for (const viewport of [{ width: 1280, height: 900, name: 'desktop' }, { width: 375, height: 812, name: 'mobile' }]) {
      const page = await browser.newPage({ viewport })
      const errors = []
      page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
      page.on('pageerror', error => errors.push(error.message))
      await page.route('**/api/v1/**', route => {
        const path = new URL(route.request().url()).pathname
        if (path === '/api/v1/admin/session') return route.fulfill({ json: { authenticated: true, username: 'lekang', csrfToken: 'visual-only', csrfHeaderName: 'X-XSRF-TOKEN' } })
        if (path === '/api/v1/admin/overview') return route.fulfill({ json: { drafts: 0, published: 3, archived: 1, dailyNote: '先写下来，好文章不必一次完成。' } })
        return route.fulfill({ status: 404, json: { title: 'Not mocked' } })
      })
      await page.goto('http://127.0.0.1:4174/admin', { waitUntil: 'domcontentloaded', timeout: 15000 })
      await page.locator('.desk-note').waitFor()
      await page.waitForTimeout(3200)
      const data = await page.locator('.desk-note').evaluate(element => {
        const style = getComputedStyle(element)
        return { background: style.backgroundColor, color: style.color, transform: style.transform }
      })
      const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
      await page.screenshot({ path: `D:/lekang/.runtime/admin-note-${viewport.name}.png`, fullPage: true })
      results.push({ viewport: viewport.name, ...data, overflow, errors })
      await page.close()
    }
    process.stdout.write(JSON.stringify(results, null, 2))
  } finally { await browser.close() }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
