const MAX_SLUG_LENGTH = 120

function shortTitleHash(value: string) {
  let hash = 2166136261
  for (let index = 0; index < value.length; index += 1) {
    hash ^= value.charCodeAt(index)
    hash = Math.imul(hash, 16777619)
  }
  return (hash >>> 0).toString(36)
}

export function createArticleSlug(title: string) {
  const trimmed = title.trim()
  if (!trimmed) return ''

  const latinSlug = trimmed
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^\x00-\x7F]/g, ' ')
    .toLowerCase()
    .replace(/['’]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')

  if (latinSlug) return latinSlug.slice(0, MAX_SLUG_LENGTH).replace(/-+$/g, '')

  // 后端公开地址保持 ASCII；纯中文标题使用稳定短码，重复编辑同一标题不会改变结果。
  return `article-${shortTitleHash(trimmed)}`
}
