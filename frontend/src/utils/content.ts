export function formatPublishedDate(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('zh-CN', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(date).replaceAll('/', '.')
}

export function formatReadTime(minutes: number): string {
  return `${minutes} MIN READ`
}

export function formatCategory(category: string): string {
  const labels: Record<string, string> = {
    CINEMA: '日常',
    BOOKS: '阅读',
    NOTES: '笔记',
    FOOTBALL: '日常'
  }
  return labels[category.toUpperCase()] ?? category
}

export function getPublishedYearMonth(value: string): { year: string; month: string } {
  const parts = new Intl.DateTimeFormat('en', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: 'numeric'
  }).formatToParts(new Date(value))
  return {
    year: parts.find(part => part.type === 'year')?.value ?? '',
    month: parts.find(part => part.type === 'month')?.value ?? ''
  }
}

export function formatMonth(month: number): string {
  return new Intl.DateTimeFormat('en', { month: 'long', timeZone: 'UTC' })
    .format(new Date(Date.UTC(2020, month - 1, 1)))
    .toUpperCase()
}

export function setPageMeta(title: string, description: string): void {
  document.title = title === 'LEKANG' ? title : `${title} — LEKANG`
  const meta = document.querySelector<HTMLMetaElement>('meta[name="description"]')
  if (meta) meta.content = description
}

export function sanitizeArticleHtml(html: string): string {
  const documentFragment = new DOMParser().parseFromString(html, 'text/html')
  const allowedTags = new Set(['H2', 'H3', 'P', 'BLOCKQUOTE', 'UL', 'OL', 'LI', 'STRONG', 'EM', 'A', 'BR'])
  const blockedTags = new Set(['SCRIPT', 'STYLE', 'TEMPLATE', 'IFRAME', 'OBJECT', 'EMBED'])

  documentFragment.body.querySelectorAll('*').forEach(element => {
    if (!allowedTags.has(element.tagName)) {
      if (blockedTags.has(element.tagName)) {
        element.remove()
        return
      }
      element.replaceWith(...Array.from(element.childNodes))
      return
    }

    const href = element instanceof HTMLAnchorElement ? element.getAttribute('href') : null
    Array.from(element.attributes).forEach(attribute => element.removeAttribute(attribute.name))
    if (element instanceof HTMLAnchorElement) {
      if (href && /^(https?:|mailto:|\/)/i.test(href)) {
        element.setAttribute('href', href)
        if (/^https?:/i.test(href)) element.setAttribute('rel', 'noreferrer')
      }
    }
  })

  return documentFragment.body.innerHTML
}
