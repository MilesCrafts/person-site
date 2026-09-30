const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

const output = String.raw`C:\Users\lenovo\.codex\visualizations\2026\08\10\019fec40-685f-7bb0-b2b4-86158ba7efeb`

async function inspect(browser, viewport) {
  const page = await browser.newPage({ viewport })
  const errors = []
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
  page.on('pageerror', error => errors.push(error.message))
  await page.goto('http://127.0.0.1:4174/works', { waitUntil: 'domcontentloaded', timeout: 12000 })
  await page.waitForSelector('.work-process-dial')
  await page.waitForTimeout(1000)

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
  const projectCards = await page.locator('.work-project-card').count()
  const proofValues = await page.locator('.work-proof strong').allTextContents()
  const nextLinks = await page.locator('.works-next nav a').allTextContents()

  await page.locator('.work-process-dial').scrollIntoViewIfNeeded()
  await page.waitForTimeout(250)
  const firstTitle = await page.locator('.dial-content h3').textContent()
  await page.getByRole('tab', { name: '03 / CHECK' }).click()
  await page.waitForTimeout(550)
  const switchedTitle = await page.locator('.dial-content h3').textContent()
  const selected = await page.getByRole('tab', { name: '03 / CHECK' }).getAttribute('aria-selected')
  const markerTransform = await page.locator('.dial-marker').evaluate(element => getComputedStyle(element).transform)
  await page.screenshot({ path: `${output}/works-${viewport.label}-process.png` })

  await page.locator('.work-project-grid').scrollIntoViewIfNeeded()
  await page.waitForTimeout(250)
  await page.screenshot({ path: `${output}/works-${viewport.label}-projects.png` })

  await page.evaluate(() => window.scrollTo(0, 0))
  await page.waitForTimeout(200)
  await page.screenshot({ path: `${output}/works-${viewport.label}-top.png` })

  await page.close()
  return { viewport: viewport.label, overflow, errors, projectCards, proofValues, nextLinks: nextLinks.map(text => text.replace(/\s+/g, ' ').trim()), firstTitle: firstTitle?.trim(), switchedTitle: switchedTitle?.trim(), selected, markerTransform }
}

async function main() {
  const browser = await chromium.launch({ headless: true, executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe`, args: ['--disable-dev-shm-usage'] })
  try {
    const results = []
    for (const viewport of [{ width: 1280, height: 900, label: 'desktop' }, { width: 768, height: 900, label: 'tablet' }, { width: 375, height: 812, label: 'mobile' }]) {
      results.push(await inspect(browser, viewport))
    }
    process.stdout.write(JSON.stringify(results, null, 2))
  } finally {
    await browser.close()
  }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
