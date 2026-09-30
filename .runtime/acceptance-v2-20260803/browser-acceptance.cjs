const fs = require('fs')
const path = require('path')
const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

const baseUrl = 'http://127.0.0.1:5173'
const outputDir = __dirname
const password = process.env.ACCEPTANCE_ADMIN_PASSWORD
if (!password) throw new Error('Missing temporary acceptance password')

const publicRoutes = [
  ['home', '/'],
  ['journal', '/journal'],
  ['article', '/article/messi-world-cup'],
  ['about', '/about'],
  ['archive', '/archive'],
  ['search', '/search?q=%E6%A2%85%E8%A5%BF']
]
const viewports = [
  ['desktop', { width: 1280, height: 800 }],
  ['tablet', { width: 768, height: 1024 }],
  ['mobile', { width: 375, height: 812 }]
]

async function inspectPage(page, label) {
  const metrics = await page.evaluate(() => ({
    title: document.title,
    bodyTextLength: document.body.innerText.length,
    viewportWidth: window.innerWidth,
    scrollWidth: document.documentElement.scrollWidth,
    hasHorizontalOverflow: document.documentElement.scrollWidth > window.innerWidth + 1
  }))
  if (metrics.bodyTextLength < 20) throw new Error(`${label}: page content is unexpectedly empty`)
  if (metrics.hasHorizontalOverflow) {
    const offenders = await page.evaluate(() => [...document.querySelectorAll('*')]
      .map(element => {
        const rect = element.getBoundingClientRect()
        return { tag: element.tagName, className: element.className, left: rect.left, right: rect.right, width: rect.width }
      })
      .filter(item => item.right > window.innerWidth + 1 || item.left < -1)
      .slice(0, 12))
    throw new Error(`${label}: horizontal overflow ${metrics.scrollWidth}/${metrics.viewportWidth}: ${JSON.stringify(offenders)}`)
  }
  return metrics
}

async function main() {
  const browser = await chromium.launch({
    headless: true,
    executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe`,
    args: ['--disable-dev-shm-usage']
  })
  const report = { public: [], admin: [], errors: [] }

  for (const [viewportName, viewport] of viewports) {
    const context = await browser.newContext({ viewport })
    const page = await context.newPage()
    page.on('console', message => {
      if (message.type() === 'error') report.errors.push(`${viewportName}:console:${message.text()}`)
    })
    page.on('pageerror', error => report.errors.push(`${viewportName}:pageerror:${error.message}`))
    page.on('requestfailed', request => report.errors.push(`${viewportName}:requestfailed:${request.url()}:${request.failure()?.errorText}`))

    for (const [routeName, route] of publicRoutes) {
      const response = await page.goto(baseUrl + route, { waitUntil: 'networkidle' })
      if (!response || response.status() >= 400) throw new Error(`${viewportName}:${routeName}: navigation failed`)
      await page.locator('body').waitFor({ state: 'visible' })
      const metrics = await inspectPage(page, `${viewportName}:${routeName}`)
      if (routeName === 'journal') await page.getByRole('heading').first().waitFor()
      if (routeName === 'article') await page.getByRole('heading', { name: /梅西/ }).waitFor()
      await page.screenshot({ path: path.join(outputDir, `public-${viewportName}-${routeName}.png`), fullPage: true })
      report.public.push({ viewport: viewportName, route, status: response.status(), ...metrics })
    }
    await context.close()
  }

  const context = await browser.newContext({ viewport: { width: 1280, height: 900 } })
  const page = await context.newPage()
  let expectingNotFound = false
  page.on('console', message => {
    if (message.type() === 'error' && !expectingNotFound) report.errors.push(`admin:console:${message.text()}`)
  })
  page.on('pageerror', error => report.errors.push(`admin:pageerror:${error.message}`))
  page.on('requestfailed', request => {
    const expectedLogoutAbort = request.method() === 'DELETE'
      && request.url().endsWith('/api/v1/admin/session')
      && request.failure()?.errorText === 'net::ERR_ABORTED'
    if (!expectedLogoutAbort) {
      report.errors.push(`admin:requestfailed:${request.method()}:${request.url()}:${request.failure()?.errorText}`)
    }
  })
  page.on('dialog', dialog => dialog.accept())

  await page.goto(baseUrl + '/admin', { waitUntil: 'networkidle' })
  await page.waitForURL(/\/admin\/login/)
  report.admin.push('anonymous route guard redirected to login')

  await page.getByLabel('管理员账号').fill('acceptance-admin')
  await page.getByLabel('密码').fill(password)
  await page.getByRole('button', { name: '进入编辑部' }).click()
  await page.waitForURL(url => url.pathname === '/admin')
  await page.getByRole('heading', { name: '今天，写点什么？' }).waitFor()
  report.admin.push('login and authenticated dashboard passed')

  await page.goto(baseUrl + '/admin/articles/new', { waitUntil: 'networkidle' })
  await page.getByLabel('标题').fill('浏览器验收测试稿')
  await page.getByLabel('公开 SLUG').fill('acceptance-v2-browser-20260803')
  await page.getByLabel('摘要').fill('用于验证管理端完整文章生命周期，完成后删除。')
  await page.getByLabel('栏目').selectOption('FOOTBALL')
  await page.getByLabel('主题').selectOption('MESSI')
  await page.getByLabel('Markdown 正文').fill('## Browser Acceptance Body\n\n这是一段用于预览和发布流程验收的正文。')
  await page.getByRole('button', { name: '更新预览' }).click()
  await page.getByText('Browser Acceptance Body', { exact: true }).waitFor()
  report.admin.push('markdown preview passed')
  await page.screenshot({ path: path.join(outputDir, 'admin-new-preview.png'), fullPage: true })

  await page.getByRole('button', { name: '创建草稿' }).first().click()
  await page.waitForURL(/\/admin\/articles\/\d+\/edit/)
  await page.getByText('草稿已创建。').waitFor()
  const editUrl = page.url()
  report.admin.push('draft creation passed')

  await page.getByLabel('标题').fill('浏览器验收测试稿（已编辑）')
  await page.getByLabel('Markdown 正文').fill('## Browser Acceptance Body\n\n正文已经通过编辑页面修改。\n\n> Editorial preview')
  await page.getByRole('button', { name: '保存修改' }).first().click()
  await page.getByText('修改已保存。').waitFor()
  await page.reload({ waitUntil: 'networkidle' })
  await page.getByLabel('标题').waitFor()
  if (await page.getByLabel('标题').inputValue() !== '浏览器验收测试稿（已编辑）') {
    throw new Error('saved title did not persist after refresh')
  }
  report.admin.push('edit save and refresh persistence passed')

  await page.getByRole('button', { name: '发布文章' }).first().click()
  await page.getByText('文章已发布。').waitFor()
  report.admin.push('publish passed')

  const publicPage = await context.newPage()
  const publicResponse = await publicPage.goto(baseUrl + '/article/acceptance-v2-browser-20260803', { waitUntil: 'networkidle' })
  if (!publicResponse || publicResponse.status() >= 400) throw new Error('published public article route failed')
  await publicPage.getByRole('heading', { name: '浏览器验收测试稿（已编辑）' }).waitFor()
  report.admin.push('published article became publicly visible')

  await page.bringToFront()
  if (page.url() !== editUrl) await page.goto(editUrl, { waitUntil: 'networkidle' })
  await page.getByRole('button', { name: '撤回发布' }).first().click()
  await page.getByText('文章已撤回为草稿。').waitFor()
  report.admin.push('unpublish passed')

  expectingNotFound = true
  await publicPage.reload({ waitUntil: 'networkidle' })
  await publicPage.getByText('没有找到请求的内容。').waitFor()
  expectingNotFound = false
  report.admin.push('unpublished article disappeared from public API/page')

  await page.getByRole('button', { name: '归档' }).first().click()
  await page.getByText('文章已归档。').waitFor()
  await page.getByText('已归档').first().waitFor()
  await page.screenshot({ path: path.join(outputDir, 'admin-archived.png'), fullPage: true })
  report.admin.push('archive passed')

  await page.getByRole('button', { name: '退出' }).click()
  await page.waitForURL(/\/admin\/login/)
  report.admin.push('logout passed')

  report.adminEditUrl = editUrl
  fs.writeFileSync(path.join(outputDir, 'browser-report.json'), JSON.stringify(report, null, 2))
  await context.close()
  await browser.close()

  if (report.errors.length) throw new Error(`Browser errors detected:\n${report.errors.join('\n')}`)
  console.log(JSON.stringify(report, null, 2))
}

main().catch(error => {
  console.error(error.stack || error)
  process.exit(1)
})
