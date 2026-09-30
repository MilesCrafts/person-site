const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

const output = String.raw`C:\Users\lenovo\.codex\visualizations\2026\08\10\019fec40-685f-7bb0-b2b4-86158ba7efeb`
const articles = [
  { id: 1, slug: 'a-small-personal-site', title: '做一个真正属于自己的小网站', categoryCode: 'ESSAY', topicCode: 'BUILDING', status: 'PUBLISHED', publishedAt: '2026-08-08T10:00:00Z', updatedAt: '2026-08-10T12:30:00Z', version: 3 },
  { id: 2, slug: 'notes-from-a-rainy-night', title: '雨夜里记下的一些碎片', categoryCode: 'DAILY', topicCode: 'LIFE', status: 'DRAFT', publishedAt: null, updatedAt: '2026-08-11T01:20:00Z', version: 1 },
  { id: 3, slug: 'reading-without-a-list', title: '不列书单，也可以好好阅读', categoryCode: 'READING', topicCode: 'BOOKS', status: 'ARCHIVED', publishedAt: null, updatedAt: '2026-07-21T08:20:00Z', version: 5 }
]
const detail = { ...articles[0], excerpt: '一次关于个人网站、长期记录和慢慢打磨的实践。', bodyMarkdown: '## 从一个小念头开始\n\n个人网站不必一次完成。它可以边写、边改，也边记录自己。\n\n> 这里没有最终版本，只有下一次修改。', bodyHtml: '<h2>从一个小念头开始</h2><p>个人网站不必一次完成。它可以边写、边改，也边记录自己。</p><blockquote>这里没有最终版本，只有下一次修改。</blockquote>', coverAssetId: null, coverImageUrl: null, readMinutes: 4, createdAt: '2026-08-01T10:00:00Z', archivedAt: null }
const session = { authenticated: true, username: 'lekang', csrfToken: 'visual-only', csrfHeaderName: 'X-XSRF-TOKEN' }
const taxonomy = [{ code: 'ESSAY', displayName: '文章', contentType: 'ARTICLE', topics: [{ code: 'BUILDING', displayName: '造物', slug: 'building' }] }, { code: 'DAILY', displayName: '日常', contentType: 'ARTICLE', topics: [{ code: 'LIFE', displayName: '生活', slug: 'life' }] }]

async function mockAdmin(page, authenticated = true) {
  await page.route('**/api/v1/**', async route => {
    const url = new URL(route.request().url())
    if (url.pathname === '/api/v1/admin/session') return route.fulfill({ json: authenticated ? session : { ...session, authenticated: false, username: null, csrfToken: '' } })
    if (url.pathname === '/api/v1/admin/overview') return route.fulfill({ json: { drafts: 1, published: 1, archived: 1, dailyNote: '好文章不必一次完成。' } })
    if (url.pathname === '/api/v1/taxonomies') return route.fulfill({ json: taxonomy })
    if (url.pathname === '/api/v1/admin/articles/preview') return route.fulfill({ json: { bodyHtml: detail.bodyHtml, readMinutes: 4 } })
    if (/\/api\/v1\/admin\/articles\/\d+\/archive$/.test(url.pathname)) return route.fulfill({ json: { ...detail, status: 'ARCHIVED', version: detail.version + 1 } })
    if (/\/api\/v1\/admin\/articles\/\d+$/.test(url.pathname)) return route.fulfill({ json: detail })
    if (url.pathname === '/api/v1/admin/articles') {
      const status = url.searchParams.get('status')
      const items = status ? articles.filter(item => item.status === status) : articles
      return route.fulfill({ json: { items, page: 0, size: 20, totalItems: items.length, totalPages: 1, hasNext: false } })
    }
    return route.fulfill({ status: 404, json: { title: 'Not mocked' } })
  })
}

async function main() {
  const browser = await chromium.launch({ headless: true, executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe`, args: ['--disable-dev-shm-usage'] })
  const results = []
  try {
    for (const viewport of [{ width: 1280, height: 900, label: 'desktop' }, { width: 375, height: 812, label: 'mobile' }]) {
      for (const item of [
        { path: '/admin/login', name: 'login', auth: false },
        { path: '/admin', name: 'home', auth: true },
        { path: '/admin/articles', name: 'articles', auth: true },
        { path: '/admin/articles/new', name: 'new', auth: true },
        { path: '/admin/articles/1/edit', name: 'edit', auth: true }
      ]) {
        const page = await browser.newPage({ viewport })
        const errors = []
        page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
        page.on('pageerror', error => errors.push(error.message))
        await mockAdmin(page, item.auth)
        await page.goto(`http://127.0.0.1:4174${item.path}`, { waitUntil: 'domcontentloaded', timeout: 12000 })
        await page.waitForTimeout(650)
        const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
        const heading = await page.locator('h1').first().textContent().catch(() => '')
        if (!require('fs').existsSync(`${output}/admin-${item.name}-${viewport.label}.png`)) {
          await page.screenshot({ path: `${output}/admin-${item.name}-${viewport.label}.png`, fullPage: true })
        }
        results.push({ ...item, viewport: viewport.label, url: page.url(), heading: heading?.replace(/\s+/g, ' ').trim(), overflow, errors })
        await page.close()
      }
    }
    process.stdout.write(JSON.stringify(results, null, 2))
  } finally { await browser.close() }
}
main().catch(error => { console.error(error); process.exitCode = 1 })
