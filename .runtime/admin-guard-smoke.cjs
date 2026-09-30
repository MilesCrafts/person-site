const { chromium } = require(String.raw`C:\Users\lenovo\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\playwright`)

async function main() {
  const browser = await chromium.launch({
    headless: true,
    executablePath: String.raw`C:\Program Files\Google\Chrome\Application\chrome.exe`,
    args: ['--disable-dev-shm-usage']
  })
  try {
    const page = await browser.newPage({ viewport: { width: 1280, height: 800 } })
    const sessionStatuses = []
    page.on('response', response => {
      if (response.url().includes('/api/v1/admin/session')) sessionStatuses.push(response.status())
    })
    await page.goto('http://127.0.0.1:4174/admin', { waitUntil: 'domcontentloaded', timeout: 15000 })
    await page.waitForURL(url => url.pathname === '/admin/login', { timeout: 15000 })
    await page.getByRole('heading', { name: /回来啦/ }).waitFor({ timeout: 10000 })
    if (!sessionStatuses.includes(200)) throw new Error(`Unexpected session statuses: ${sessionStatuses.join(',')}`)
    process.stdout.write(JSON.stringify({ route: page.url(), sessionStatuses, result: 'passed' }, null, 2))
  } finally {
    await browser.close()
  }
}

main().catch(error => {
  console.error(error)
  process.exitCode = 1
})
