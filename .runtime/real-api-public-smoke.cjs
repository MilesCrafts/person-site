const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

const baseUrl = 'http://127.0.0.1:4174'
const routes = [
  '/',
  '/works',
  '/capabilities',
  '/journal',
  '/article/messi-world-cup',
  '/search?q=%E6%A2%85%E8%A5%BF',
  '/archive',
  '/about'
]
const viewports = [
  ['desktop', { width: 1280, height: 800 }],
  ['tablet', { width: 768, height: 1024 }],
  ['mobile', { width: 375, height: 812 }]
]

async function main() {
  const browser = await chromium.launch({
    headless: true,
    executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe`,
    args: ['--disable-dev-shm-usage']
  })
  const report = []
  const errors = []
  let apiResponses = 0

  for (const [viewportName, viewport] of viewports) {
    const context = await browser.newContext({ viewport })
    const page = await context.newPage()
    page.on('console', message => {
      if (message.type() === 'error') errors.push(`${viewportName}:console:${message.text()}`)
    })
    page.on('pageerror', error => errors.push(`${viewportName}:pageerror:${error.message}`))
    page.on('requestfailed', request => errors.push(`${viewportName}:request:${request.url()}:${request.failure()?.errorText}`))
    page.on('response', response => {
      if (response.url().includes('/api/v1/')) {
        apiResponses += 1
        if (response.status() >= 400) errors.push(`${viewportName}:api:${response.status()}:${response.url()}`)
      }
    })

    for (const route of routes) {
      const response = await page.goto(baseUrl + route, { waitUntil: 'networkidle' })
      await page.waitForTimeout(700)
      const metrics = await page.evaluate(() => ({
        title: document.title,
        textLength: document.body.innerText.length,
        viewportWidth: window.innerWidth,
        scrollWidth: document.documentElement.scrollWidth,
        footerPresent: Boolean(document.querySelector('#site-footer'))
      }))
      if (!response || response.status() >= 400) throw new Error(`${viewportName}:${route}:navigation failed`)
      if (metrics.textLength < 80) throw new Error(`${viewportName}:${route}:content too short`)
      if (metrics.scrollWidth > metrics.viewportWidth + 1) throw new Error(`${viewportName}:${route}:horizontal overflow`)
      if (!metrics.footerPresent) throw new Error(`${viewportName}:${route}:footer missing`)
      report.push({ viewport: viewportName, route, status: response.status(), ...metrics })
    }
    await context.close()
  }

  await browser.close()
  if (apiResponses === 0) throw new Error('No /api/v1 network responses observed; frontend may still be using Mock data')
  if (errors.length) throw new Error(errors.join('\n'))
  process.stdout.write(JSON.stringify({ pages: report.length, apiResponses, errors }, null, 2))
}

main().catch(error => {
  console.error(error)
  process.exitCode = 1
})
