import { ApiError } from './client'
import type { ProblemDetails } from '../types/api'
import type {
  AdminArticleAction,
  AdminArticleDetail,
  AdminArticleDraft,
  AdminArticlePage,
  AdminArticlePreview,
  AdminArticlePreviewRequest,
  AdminArticleStatus,
  AdminArticleSort,
  AdminArticleUpdate,
  AdminLoginRequest,
  AdminMediaAsset,
  AdminMediaPage,
  AdminOverview,
  AdminHomepageFeature,
  AdminHomepageCopy,
  AdminHomepageCopyUpdate,
  AdminMessage,
  AdminMessagePage,
  AdminMessageStatus,
  SortDirection,
  AdminSession
} from '../types/admin'

const ADMIN_BASE = '/api/v1/admin'

let csrfToken = ''
let csrfHeaderName = 'X-XSRF-TOKEN'
let unauthorizedHandler: (() => void) | undefined

export function setAdminSecurity(session: AdminSession | null): void {
  csrfToken = session?.csrfToken ?? ''
  csrfHeaderName = session?.csrfHeaderName || 'X-XSRF-TOKEN'
}

export function onAdminUnauthorized(handler: () => void): void {
  unauthorizedHandler = handler
}

interface AdminRequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  signal?: AbortSignal
  csrf?: boolean
}

async function adminRequest<T>(
  path: string,
  { method = 'GET', body, signal, csrf = method !== 'GET' }: AdminRequestOptions = {}
): Promise<T> {
  const headers: Record<string, string> = {
    Accept: 'application/json',
    'Cache-Control': 'no-store'
  }
  if (body !== undefined && !(body instanceof FormData)) headers['Content-Type'] = 'application/json'
  if (csrf && csrfToken) headers[csrfHeaderName] = csrfToken

  const response = await fetch(`${ADMIN_BASE}${path}`, {
    method,
    body: body === undefined ? undefined : body instanceof FormData ? body : JSON.stringify(body),
    credentials: 'same-origin',
    cache: 'no-store',
    headers,
    signal
  })

  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? ''
    const problem = contentType.includes('json')
      ? await response.json().catch(() => undefined) as ProblemDetails | undefined
      : undefined
    if (response.status === 401) unauthorizedHandler?.()
    throw new ApiError(
      response.status,
      problem?.detail || problem?.title || `管理请求失败（${response.status}）`,
      problem
    )
  }

  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

interface AdminArticleQuery {
  status?: AdminArticleStatus
  q?: string
  sort?: AdminArticleSort
  direction?: SortDirection
}

function articleQuery(page: number, size: number, query: AdminArticleQuery = {}): string {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (query.status) params.set('status', query.status)
  if (query.q?.trim()) params.set('q', query.q.trim())
  if (query.sort) params.set('sort', query.sort)
  if (query.direction) params.set('direction', query.direction)
  return `?${params.toString()}`
}

export const getAdminSession = (signal?: AbortSignal) =>
  adminRequest<AdminSession>('/session', { signal, csrf: false })

export const loginAdmin = (credentials: AdminLoginRequest) =>
  adminRequest<AdminSession>('/session', { method: 'POST', body: credentials, csrf: false })

export const logoutAdmin = () =>
  adminRequest<void>('/session', { method: 'DELETE' })

export const getAdminOverview = (signal?: AbortSignal) =>
  adminRequest<AdminOverview>('/overview', { signal })

export const getAdminArticles = (
  page = 0,
  size = 20,
  query: AdminArticleQuery = {},
  signal?: AbortSignal
) => adminRequest<AdminArticlePage>(`/articles${articleQuery(page, size, query)}`, { signal })

export const getAdminHomepageFeatures = (signal?: AbortSignal) =>
  adminRequest<AdminHomepageFeature[]>('/homepage/features', { signal })

export const updateAdminHomepageFeatures = (articleIds: number[]) =>
  adminRequest<AdminHomepageFeature[]>('/homepage/features', {
    method: 'PUT',
    body: { articleIds }
  })

export const getAdminHomepageCopy = (signal?: AbortSignal) =>
  adminRequest<AdminHomepageCopy>('/homepage/copy', { signal })

export const updateAdminHomepageCopy = (copy: AdminHomepageCopyUpdate) =>
  adminRequest<AdminHomepageCopy>('/homepage/copy', {
    method: 'PUT',
    body: copy
  })

export const getAdminArticle = (id: number, signal?: AbortSignal) =>
  adminRequest<AdminArticleDetail>(`/articles/${id}`, { signal })

export const getAdminMedia = (page = 0, size = 12, signal?: AbortSignal) =>
  adminRequest<AdminMediaPage>(`/media?page=${page}&size=${size}`, { signal })

export const uploadAdminMedia = (file: File, altText = '') => {
  const body = new FormData()
  body.append('file', file)
  if (altText.trim()) body.append('altText', altText.trim())
  return adminRequest<AdminMediaAsset>('/media', { method: 'POST', body })
}

export const createAdminArticle = (draft: AdminArticleDraft) =>
  adminRequest<AdminArticleDetail>('/articles', { method: 'POST', body: draft })

export const updateAdminArticle = (id: number, draft: AdminArticleUpdate) =>
  adminRequest<AdminArticleDetail>(`/articles/${id}`, { method: 'PUT', body: draft })

export const previewAdminArticle = (request: AdminArticlePreviewRequest, signal?: AbortSignal) =>
  adminRequest<AdminArticlePreview>('/articles/preview', {
    method: 'POST',
    body: request,
    signal
  })

const articleAction = (id: number, action: string, request: AdminArticleAction) =>
  adminRequest<AdminArticleDetail>(`/articles/${id}/${action}`, {
    method: 'POST',
    body: request
  })

export const publishAdminArticle = (id: number, request: AdminArticleAction) =>
  articleAction(id, 'publish', request)

export const unpublishAdminArticle = (id: number, request: AdminArticleAction) =>
  articleAction(id, 'unpublish', request)

export const archiveAdminArticle = (id: number, request: AdminArticleAction) =>
  articleAction(id, 'archive', request)

export const getAdminMessages = (
  page = 0,
  size = 20,
  status?: AdminMessageStatus,
  signal?: AbortSignal
) => {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (status) params.set('status', status)
  return adminRequest<AdminMessagePage>(`/messages?${params.toString()}`, { signal })
}

export const getAdminMessageCount = (signal?: AbortSignal) =>
  adminRequest<{ unread: number }>('/messages/count', { signal })

const messageAction = (id: number, action: string, version: number) =>
  adminRequest<AdminMessage>(`/messages/${id}/${action}`, { method: 'POST', body: { version } })

export const markAdminMessageRead = (id: number, version: number) =>
  messageAction(id, 'read', version)

export const archiveAdminMessage = (id: number, version: number) =>
  messageAction(id, 'archive', version)
