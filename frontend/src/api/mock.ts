import {
  albumTopics,
  articles,
  images,
  topicMap,
  type Article
} from '../data/articles'
import type {
  Album,
  AlbumPhoto,
  ArchiveItem,
  ArticleDetail,
  ArticleNavigation,
  ArticleSummary,
  Home,
  PageResponse,
  Profile,
  Taxonomy
} from '../types/api'

const profile: Profile = {
  displayName: 'LEKANG',
  bio: '记录足球、电影、书与日常生活。',
  manifesto: '一份持续生长的个人数字文化档案。',
  email: null,
  nowWatchingTitle: 'SHAMELESS',
  nowWatchingDetail: '混乱家庭里的真实生活。'
}

const album: Album = {
  slug: 'personal-album',
  title: '一些没有写成文章的瞬间。',
  description: '城市、房间、球场和银幕。照片替我保留那些来不及描述的部分。',
  publishedAt: '2026-07-01T00:00:00+08:00'
}

const albumPhotos: AlbumPhoto[] = [
  { id: 1, topic: 'CITY', title: '城市醒来以前', location: 'SHANGHAI', shotAt: '2026-07-01', caption: null, sortOrder: 10, imageUrl: images.shanghai, imageAlt: '清晨的上海街景' },
  { id: 2, topic: 'DAILY LIFE', title: '房间里的一束光', location: 'HOME', shotAt: '2026-06-18', caption: null, sortOrder: 20, imageUrl: images.fragments, imageAlt: '房间里落下的一束光' },
  { id: 3, topic: 'CITY', title: '雨落下来之后', location: 'SHANGHAI', shotAt: '2026-06-05', caption: null, sortOrder: 30, imageUrl: images.rain, imageAlt: '雨后的城市' },
  { id: 4, topic: 'CINEMA', title: '散场', location: 'CINEMA', shotAt: '2026-05-22', caption: null, sortOrder: 40, imageUrl: images.poster, imageAlt: '电影散场后的海报' },
  { id: 5, topic: 'DAILY LIFE', title: '写作发生的地方', location: 'DESK', shotAt: '2026-04-16', caption: null, sortOrder: 50, imageUrl: images.website, imageAlt: '书桌与写作空间' },
  { id: 6, topic: 'FOOTBALL', title: '看台与夜色', location: 'STADIUM', shotAt: '2026-03-09', caption: null, sortOrder: 60, imageUrl: images.argentina, imageAlt: '夜色中的足球看台' }
]

function abortIfNeeded(signal?: AbortSignal): void {
  if (signal?.aborted) throw new DOMException('The request was aborted.', 'AbortError')
}

function isoDate(value: string): string {
  return `${value.replaceAll('.', '-')}T00:00:00+08:00`
}

function readMinutes(value: string): number {
  return Number.parseInt(value, 10)
}

function toSummary(article: Article): ArticleSummary {
  return {
    slug: article.id,
    category: article.category,
    topic: article.topic,
    title: article.title,
    excerpt: article.excerpt,
    publishedAt: isoDate(article.date),
    readMinutes: readMinutes(article.readTime),
    imageUrl: article.image,
    imageAlt: article.title
  }
}

function escapeHtml(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;')
}

function toDetail(article: Article): ArticleDetail {
  const bodyHtml = article.sections.map(section => [
    section.heading ? `<h2>${escapeHtml(section.heading)}</h2>` : '',
    ...section.paragraphs.map(paragraph => `<p>${escapeHtml(paragraph)}</p>`),
    section.quote ? `<blockquote>${escapeHtml(section.quote)}</blockquote>` : '',
    section.list ? `<ul>${section.list.map(item => `<li>${escapeHtml(item)}</li>`).join('')}</ul>` : ''
  ].join('')).join('')

  return {
    ...toSummary(article),
    bodyMarkdown: '',
    bodyHtml,
    updatedAt: isoDate(article.date)
  }
}

function normalizeTopic(value: string): string {
  return value.toUpperCase().replaceAll(' ', '_').replaceAll('-', '_')
}

function pageOf<T>(items: T[], page: number, size: number): PageResponse<T> {
  const start = page * size
  const totalPages = items.length === 0 ? 0 : Math.ceil(items.length / size)
  return {
    items: items.slice(start, start + size),
    page,
    size,
    totalItems: items.length,
    totalPages,
    hasNext: page + 1 < totalPages
  }
}

function sortedArticles(): Article[] {
  return [...articles].sort((left, right) => right.date.localeCompare(left.date))
}

export function mockGetHome(signal?: AbortSignal): Promise<Home> {
  abortIfNeeded(signal)
  const summaries = sortedArticles().map(toSummary)
  return Promise.resolve({
    heroCopy: {
      eyebrow: 'LEKANG · 产品、开发与记录',
      headlinePrimary: '把想法',
      headlineEmphasis: '理清楚，',
      headlineAccent: '再做出来。',
      description: '关注产品体验、前端开发和内容系统，也持续记录阅读、电影与工作之外的日常。',
      paperLabel: '正在搭建',
      paperLineOne: '让文章有地方住，',
      paperLineTwo: '让作品慢慢长，',
      paperLineThree: '也让我持续更新。',
      paperFooter: 'BUILDING IN PUBLIC'
    },
    feature: summaries[0] ?? null,
    features: summaries.slice(0, 5),
    latestStories: summaries.slice(0, 4),
    profile
  })
}

export function mockGetArticles(
  page: number,
  size: number,
  category: string | null | undefined,
  topic: string | null | undefined,
  sort: 'publishedAt,asc' | 'publishedAt,desc',
  signal?: AbortSignal
): Promise<PageResponse<ArticleSummary>> {
  abortIfNeeded(signal)
  const filtered = articles.filter(article =>
    (!category || article.category === category)
    && (!topic || normalizeTopic(article.topic) === normalizeTopic(topic))
  )
  filtered.sort((left, right) =>
    sort === 'publishedAt,asc'
      ? left.date.localeCompare(right.date)
      : right.date.localeCompare(left.date)
  )
  return Promise.resolve(pageOf(filtered.map(toSummary), page, size))
}

export function mockGetArticle(slug: string, signal?: AbortSignal): Promise<ArticleDetail> {
  abortIfNeeded(signal)
  const article = articles.find(item => item.id === slug)
  if (!article) return Promise.reject(new Error('Mock article not found'))
  return Promise.resolve(toDetail(article))
}

export function mockGetArticleNavigation(slug: string, signal?: AbortSignal): Promise<ArticleNavigation> {
  abortIfNeeded(signal)
  const sorted = sortedArticles()
  const index = sorted.findIndex(item => item.id === slug)
  const item = (article: Article | undefined) => article ? { slug: article.id, title: article.title } : null
  return Promise.resolve({
    previous: item(index > 0 ? sorted[index - 1] : undefined),
    next: item(index >= 0 ? sorted[index + 1] : undefined)
  })
}

export function mockGetTaxonomies(signal?: AbortSignal): Promise<Taxonomy[]> {
  abortIfNeeded(signal)
  const articleTaxonomies: Taxonomy[] = Object.entries(topicMap).map(([code, topics]) => ({
    code,
    displayName: code[0] + code.slice(1).toLowerCase(),
    contentType: 'ARTICLE',
    topics: topics.map(topic => ({
      code: normalizeTopic(topic),
      displayName: topic,
      slug: topic.toLowerCase().replaceAll(' ', '-')
    }))
  }))
  return Promise.resolve([
    ...articleTaxonomies,
    {
      code: 'ALBUM',
      displayName: 'Album',
      contentType: 'ALBUM',
      topics: albumTopics.map(topic => ({
        code: normalizeTopic(topic),
        displayName: topic,
        slug: topic.toLowerCase().replaceAll(' ', '-')
      }))
    }
  ])
}

export function mockGetArchives(signal?: AbortSignal): Promise<ArchiveItem[]> {
  abortIfNeeded(signal)
  const counts = new Map<string, number>()
  articles.forEach(article => {
    const key = article.date.slice(0, 7)
    counts.set(key, (counts.get(key) ?? 0) + 1)
  })
  return Promise.resolve(
    [...counts.entries()]
      .sort(([left], [right]) => right.localeCompare(left))
      .map(([key, articleCount]) => {
        const [year, month] = key.split('.').map(Number)
        return { year, month, articleCount }
      })
  )
}

export function mockSearchArticles(
  query: string,
  page: number,
  size: number,
  signal?: AbortSignal
): Promise<PageResponse<ArticleSummary>> {
  abortIfNeeded(signal)
  const term = query.trim().toLocaleLowerCase()
  const matches = sortedArticles().filter(article =>
    `${article.title} ${article.excerpt} ${article.category} ${article.topic}`
      .toLocaleLowerCase()
      .includes(term)
  )
  return Promise.resolve(pageOf(matches.map(toSummary), page, size))
}

export function mockGetAlbums(page: number, size: number, signal?: AbortSignal): Promise<PageResponse<Album>> {
  abortIfNeeded(signal)
  return Promise.resolve(pageOf([album], page, size))
}

export function mockGetAlbumPhotos(
  slug: string,
  topic: string | null,
  page: number,
  size: number,
  signal?: AbortSignal
): Promise<PageResponse<AlbumPhoto>> {
  abortIfNeeded(signal)
  const matches = slug === album.slug
    ? albumPhotos.filter(photo => !topic || normalizeTopic(photo.topic ?? '') === normalizeTopic(topic))
    : []
  return Promise.resolve(pageOf(matches, page, size))
}

export function mockGetProfile(signal?: AbortSignal): Promise<Profile> {
  abortIfNeeded(signal)
  return Promise.resolve(profile)
}
