const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

const output = String.raw`C:\Users\lenovo\.codex\visualizations\2026\08\10\019fec40-685f-7bb0-b2b4-86158ba7efeb`
const session = { authenticated: true, username: 'lekang', csrfToken: 'visual-only', csrfHeaderName: 'X-XSRF-TOKEN' }
const taxonomy = [
  { code: 'ESSAY', displayName: '文章', contentType: 'ARTICLE', topics: [{ code: 'BUILDING', displayName: '造物', slug: 'building' }] },
  { code: 'DAILY', displayName: '日常', contentType: 'ARTICLE', topics: [{ code: 'LIFE', displayName: '生活', slug: 'life' }] }
]
const detail = {
  id: 1,
  slug: 'a-small-personal-site',
  title: '做一个真正属于自己的小网站',
  excerpt: '一次关于个人网站、长期记录和慢慢打磨的实践。',
  categoryCode: 'ESSAY',
  topicCode: 'BUILDING',
  status: 'DRAFT',
  bodyMarkdown: '## 从一个小念头开始\n\n个人网站不必一次完成。',
  bodyHtml: '<h2>从一个小念头开始</h2><p>个人网站不必一次完成。</p>',
  coverAssetId: null,
  coverImageUrl: null,
  readMinutes: 2,
  publishedAt: null,
  archivedAt: null,
  createdAt: '2026-08-01T10:00:00Z',
  updatedAt: '2026-08-11T06:32:00Z',
  version: 3
}

async function mockAdmin(page) {
  await page.route('**/api/v1/**', async route => {
    const url = new URL(route.request().url())
    if (url.pathname === '/api/v1/admin/session') return route.fulfill({ json: session })
    if (url.pathname === '/api/v1/taxonomies') return route.fulfill({ json: taxonomy })
    if (url.pathname === '/api/v1/admin/articles/preview') {
      return route.fulfill({ json: { bodyHtml: '<h2>实时预览</h2><p>正文已经同步到这里。</p>', readMinutes: 1 } })
    }
    if (/\/api\/v1\/admin\/articles\/\d+$/.test(url.pathname)) return route.fulfill({ json: detail })
    return route.fulfill({ status: 404, json: { title: 'Not mocked' } })
  })
}

async function inspect(page, path, viewport) {
  const errors = []
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
  page.on('pageerror', error => errors.push(error.message))
  await mockAdmin(page)
  await page.goto(`http://127.0.0.1:4174${path}`, { waitUntil: 'domcontentloaded', timeout: 12000 })
  await page.waitForSelector('.markdown-editor')
  await page.waitForTimeout(900)

  const isNew = path.endsWith('/new')
  const topButton = await page.locator(".publish-actions[data-placement='top'] .save").textContent()
  const bottomButton = await page.locator(".publish-actions[data-placement='bottom'] .save").textContent()
  const saveStatus = await page.locator('.save-state').textContent()
  const toolGroups = await page.locator('.tool-group').count()
  const advanced = page.locator('.cover-advanced')
  await advanced.locator('summary').click()
  const advancedOpen = await advanced.evaluate(element => element.hasAttribute('open'))
  const initialPreview = await page.locator('.preview-status').textContent()
  const initialState = await page.locator('.preview-status').getAttribute('data-state')

  if (isNew) {
    await page.locator('.markdown-editor textarea').fill('## 新的正文\n\n正文已经开始。')
    await page.waitForTimeout(900)
  }
  const finalPreview = await page.locator('.preview-status').textContent()
  const finalState = await page.locator('.preview-status').getAttribute('data-state')
  const layout = await page.locator('.editor-columns').evaluate(element => {
    const children = [...element.children]
    const widths = children.map(child => Math.round(child.getBoundingClientRect().width))
    return { widths, ratio: widths[1] ? Number((widths[0] / widths[1]).toFixed(2)) : null }
  })
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)

  await page.evaluate(() => window.scrollTo(0, 0))
  await page.waitForTimeout(100)
  await page.screenshot({ path: `${output}/admin-editor-${isNew ? 'new' : 'edit'}-${viewport.label}-top.png` })
  await page.locator('.markdown-editor').scrollIntoViewIfNeeded()
  await page.waitForTimeout(100)
  await page.screenshot({ path: `${output}/admin-editor-${isNew ? 'new' : 'edit'}-${viewport.label}-editor.png` })

  return {
    path,
    viewport: viewport.label,
    overflow,
    errors,
    topButton: topButton?.trim(),
    bottomButton: bottomButton?.trim(),
    saveStatus: saveStatus?.replace(/\s+/g, ' ').trim(),
    toolGroups,
    advancedOpen,
    initialPreview: initialPreview?.replace(/\s+/g, ' ').trim(),
    initialState,
    finalPreview: finalPreview?.replace(/\s+/g, ' ').trim(),
    finalState,
    layout
  }
}

async function main() {
  const browser = await chromium.launch({ headless: true, executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe`, args: ['--disable-dev-shm-usage'] })
  const results = []
  try {
    for (const viewport of [{ width: 1280, height: 900, label: 'desktop' }, { width: 375, height: 812, label: 'mobile' }]) {
      for (const path of ['/admin/articles/new', '/admin/articles/1/edit']) {
        const page = await browser.newPage({ viewport })
        results.push(await inspect(page, path, viewport))
        await page.close()
      }
    }
    process.stdout.write(JSON.stringify(results, null, 2))
  } finally {
    await browser.close()
  }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
