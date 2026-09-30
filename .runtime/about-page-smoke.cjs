const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

const output = String.raw`C:\Users\lenovo\.codex\visualizations\2026\08\10\019fec40-685f-7bb0-b2b4-86158ba7efeb`

async function inspect(browser, viewport) {
  const page = await browser.newPage({ viewport })
  const errors = []
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()) })
  page.on('pageerror', error => errors.push(error.message))
  await page.goto('http://127.0.0.1:4174/about', { waitUntil: 'domcontentloaded', timeout: 12000 })
  await page.waitForSelector('.about-playboard')
  await page.waitForTimeout(900)

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
  const board = page.locator('.about-playboard')
  const box = await board.boundingBox()
  const beforePointer = await page.locator('.about-play-card').evaluate(element => getComputedStyle(element).transform)
  if (box) await page.mouse.move(box.x + box.width * .82, box.y + box.height * .25)
  await page.waitForTimeout(550)
  const afterPointer = await page.locator('.about-play-card').evaluate(element => getComputedStyle(element).transform)
  const toggle = board.getByRole('button')
  await toggle.click()
  await page.waitForTimeout(800)
  const pressed = await toggle.getAttribute('aria-pressed')
  const tagTransforms = await page.locator('.about-play-tag').evaluateAll(elements => elements.map(element => getComputedStyle(element).transform))

  await page.evaluate(() => window.scrollTo(0, 0))
  await page.waitForTimeout(120)
  await page.screenshot({ path: `${output}/about-${viewport.label}-top.png` })
  await page.locator('.about-profile-card').scrollIntoViewIfNeeded()
  await page.waitForTimeout(500)
  await page.screenshot({ path: `${output}/about-${viewport.label}-profile.png` })
  await page.locator('.about-tool-card').scrollIntoViewIfNeeded()
  await page.waitForTimeout(500)
  await page.screenshot({ path: `${output}/about-${viewport.label}-bottom.png` })

  const results = {
    viewport: viewport.label,
    overflow,
    errors,
    pressed,
    pointerMoved: beforePointer !== afterPointer,
    tagsMoved: tagTransforms.every(transform => transform !== 'none'),
    nowItems: await page.locator('.about-now-strip article').count(),
    nextLinks: (await page.locator('.about-next-card nav a').allTextContents()).map(text => text.replace(/\s+/g, ' ').trim())
  }
  await page.close()
  return results
}

async function main() {
  const browser = await chromium.launch({ headless: true, executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe`, args: ['--disable-dev-shm-usage'] })
  try {
    const results = []
    for (const viewport of [{ width: 1280, height: 900, label: 'desktop' }, { width: 768, height: 900, label: 'tablet' }, { width: 375, height: 812, label: 'mobile' }]) results.push(await inspect(browser, viewport))
    process.stdout.write(JSON.stringify(results, null, 2))
  } finally {
    await browser.close()
  }
}

main().catch(error => { console.error(error); process.exitCode = 1 })
