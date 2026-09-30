const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

const items = [
  { id: 1, slug: 'a-very-long-article-slug-for-layout-verification', title: '最近值得记住的一些片段以及那些没有来得及整理清楚的想法', categoryCode: 'DAILY', topicCode: 'LIFE', status: 'PUBLISHED', publishedAt: '2026-08-08T10:00:00Z', updatedAt: '2026-08-10T12:30:00Z', version: 3 },
  { id: 2, slug: 'draft-note', title: '还在慢慢写的一篇草稿', categoryCode: 'ESSAY', topicCode: 'BUILDING', status: 'DRAFT', publishedAt: null, updatedAt: '2026-08-11T01:20:00Z', version: 1 },
  { id: 3, slug: 'archived-note', title: '暂时收起来的旧文章', categoryCode: 'READING', topicCode: 'BOOKS', status: 'ARCHIVED', publishedAt: null, updatedAt: '2026-07-21T08:20:00Z', version: 5 }
]

async function main() {
  const browser = await chromium.launch({ headless: true, executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe` })
  try {
    const page = await browser.newPage({ viewport: { width: 1280, height: 900 } })
    const errors = []
    page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
    page.on('pageerror', error => errors.push(error.message))
    await page.route('**/api/v1/**', route => {
      const path = new URL(route.request().url()).pathname
      if (path === '/api/v1/admin/session') return route.fulfill({ json: { authenticated: true, username: 'lekang', csrfToken: 'visual-only', csrfHeaderName: 'X-XSRF-TOKEN' } })
      if (path === '/api/v1/admin/articles') return route.fulfill({ json: { items, page: 0, size: 20, totalItems: 3, totalPages: 1, hasNext: false } })
      return route.fulfill({ status: 404, json: { title: 'Not mocked' } })
    })
    await page.goto('http://127.0.0.1:4174/admin/articles', { waitUntil: 'domcontentloaded' })
    await page.locator('.article-table article').first().waitFor()
    await page.waitForTimeout(1800)
    await page.locator('.row-check input').first().check()
    await page.locator('.more-menu summary').first().click()
    const result = {
      bulkText: (await page.locator('.bulk-bar').innerText()).replace(/\s+/g, ' ').trim(),
      menuItems: await page.locator('.more-menu[open] > div').first().innerText(),
      titleOverflow: await page.locator('.article-title h2').first().evaluate(el => el.scrollWidth > el.clientWidth),
      statuses: await page.locator('.status').allTextContents(),
      overflow: await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth),
      errors
    }
    await page.screenshot({ path: 'D:/lekang/.runtime/admin-list-refined.png', fullPage: true })
    await page.locator('.more-menu[open] summary').click()
    await page.setViewportSize({ width: 375, height: 812 })
    await page.waitForTimeout(500)
    result.mobile = {
      statusSelectVisible: await page.locator('.mobile-status-box').isVisible(),
      desktopTabsVisible: await page.locator('.status-filter').isVisible(),
      overflow: await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
    }
    await page.screenshot({ path: 'D:/lekang/.runtime/admin-list-refined-mobile.png', fullPage: true })
    process.stdout.write(JSON.stringify(result, null, 2))
  } finally { await browser.close() }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
