import { apiPost, apiRequest } from './client'
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
  GuestMessageCreated,
  GuestMessageDraft,
  Taxonomy
} from '../types/api'

interface ArticleListOptions {
  page?: number
  size?: number
  category?: string | null
  topic?: string | null
  sort?: 'publishedAt,asc' | 'publishedAt,desc'
  signal?: AbortSignal
}

export const isMockApiEnabled =
  import.meta.env.DEV && import.meta.env.VITE_USE_MOCK_API === 'true'

const loadMockApi = () => import('./mock')

function queryString(values: Record<string, string | number | null | undefined>): string {
  const params = new URLSearchParams()
  Object.entries(values).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') params.set(key, String(value))
  })
  const query = params.toString()
  return query ? `?${query}` : ''
}

export const getHome = (signal?: AbortSignal) => isMockApiEnabled
  ? loadMockApi().then(mock => mock.mockGetHome(signal))
  : apiRequest<Home>('/home', signal)

export const getArticles = ({
  page = 0,
  size = 6,
  category,
  topic,
  sort = 'publishedAt,desc',
  signal
}: ArticleListOptions = {}) => isMockApiEnabled
  ? loadMockApi().then(mock => mock.mockGetArticles(page, size, category, topic, sort, signal))
  : apiRequest<PageResponse<ArticleSummary>>(
      `/articles${queryString({ page, size, category, topic, sort })}`,
      signal
    )

export async function getAllArticles(signal?: AbortSignal): Promise<ArticleSummary[]> {
  const first = await getArticles({ page: 0, size: 50, signal })
  if (first.totalPages <= 1) return first.items

  const remaining = await Promise.all(
    Array.from({ length: first.totalPages - 1 }, (_, index) =>
      getArticles({ page: index + 1, size: 50, signal })
    )
  )
  return [first, ...remaining].flatMap(page => page.items)
}

export const getArticle = (slug: string, signal?: AbortSignal) =>
  isMockApiEnabled
    ? loadMockApi().then(mock => mock.mockGetArticle(slug, signal))
    : apiRequest<ArticleDetail>(`/articles/${encodeURIComponent(slug)}`, signal)

export const getArticleNavigation = (slug: string, signal?: AbortSignal) =>
  isMockApiEnabled
    ? loadMockApi().then(mock => mock.mockGetArticleNavigation(slug, signal))
    : apiRequest<ArticleNavigation>(`/articles/${encodeURIComponent(slug)}/navigation`, signal)

export const getTaxonomies = (signal?: AbortSignal) =>
  isMockApiEnabled
    ? loadMockApi().then(mock => mock.mockGetTaxonomies(signal))
    : apiRequest<Taxonomy[]>('/taxonomies', signal)

export const getArchives = (signal?: AbortSignal) =>
  isMockApiEnabled
    ? loadMockApi().then(mock => mock.mockGetArchives(signal))
    : apiRequest<ArchiveItem[]>('/archives', signal)

export const searchArticles = (query: string, page = 0, size = 12, signal?: AbortSignal) =>
  isMockApiEnabled
    ? loadMockApi().then(mock => mock.mockSearchArticles(query, page, size, signal))
    : apiRequest<PageResponse<ArticleSummary>>(
        `/search${queryString({ q: query, page, size })}`,
        signal
      )

export const getAlbums = (page = 0, size = 20, signal?: AbortSignal) =>
  isMockApiEnabled
    ? loadMockApi().then(mock => mock.mockGetAlbums(page, size, signal))
    : apiRequest<PageResponse<Album>>(`/albums${queryString({ page, size })}`, signal)

export const getAlbumPhotos = (
  slug: string,
  topic: string | null,
  page = 0,
  size = 50,
  signal?: AbortSignal
) => isMockApiEnabled
  ? loadMockApi().then(mock => mock.mockGetAlbumPhotos(slug, topic, page, size, signal))
  : apiRequest<PageResponse<AlbumPhoto>>(
      `/albums/${encodeURIComponent(slug)}/photos${queryString({ topic, page, size })}`,
      signal
    )

export const getProfile = (signal?: AbortSignal) =>
  isMockApiEnabled
    ? loadMockApi().then(mock => mock.mockGetProfile(signal))
    : apiRequest<Profile>('/profile', signal)

export const createGuestMessage = (draft: GuestMessageDraft, signal?: AbortSignal) =>
  apiPost<GuestMessageCreated>('/messages', draft, signal)
