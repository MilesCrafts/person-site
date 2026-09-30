import type { PageResponse } from './api'

export type AdminArticleStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
export type AdminMessageStatus = 'UNREAD' | 'READ' | 'ARCHIVED'

export interface AdminSession {
  authenticated: boolean
  username: string | null
  csrfToken: string
  csrfHeaderName: string
}

export interface AdminOverview {
  drafts: number
  published: number
  archived: number
  dailyNote: string
}

export type AdminArticleSort = 'updatedAt' | 'createdAt' | 'title'
export type SortDirection = 'asc' | 'desc'

export interface AdminLoginRequest {
  username: string
  password: string
}

export interface AdminArticleSummary {
  id: number
  slug: string
  title: string
  categoryCode: string
  topicCode: string
  status: AdminArticleStatus
  publishedAt: string | null
  updatedAt: string
  version: number
}

export interface AdminHomepageFeature {
  articleId: number
  slug: string
  title: string
  sortOrder: number
}

export interface AdminHomepageCopy {
  eyebrow: string
  headlinePrimary: string
  headlineEmphasis: string
  headlineAccent: string
  description: string
  paperLabel: string
  paperLineOne: string
  paperLineTwo: string
  paperLineThree: string
  paperFooter: string
  version: number
  updatedAt: string
}

export interface AdminHomepageCopyUpdate {
  eyebrow: string
  headlinePrimary: string
  headlineEmphasis: string
  headlineAccent: string
  description: string
  paperLabel: string
  paperLineOne: string
  paperLineTwo: string
  paperLineThree: string
  paperFooter: string
  version: number
}

export interface AdminArticleDetail extends AdminArticleSummary {
  excerpt: string
  bodyMarkdown: string
  bodyHtml: string
  coverAssetId: number | null
  coverImageUrl: string | null
  readMinutes: number
  createdAt: string
  archivedAt: string | null
}

export interface AdminArticleDraft {
  slug: string
  title: string
  excerpt: string
  categoryCode: string
  topicCode: string
  bodyMarkdown: string
  coverAssetId: number | null
}

export interface AdminArticleUpdate extends AdminArticleDraft {
  version: number
}

export interface AdminArticleAction {
  version: number
}

export interface AdminArticlePreviewRequest {
  bodyMarkdown: string
}

export interface AdminArticlePreview {
  bodyHtml: string
  readMinutes: number
}

export interface AdminMediaAsset {
  id: number
  url: string
  originalName: string
  contentType: string
  sizeBytes: number | null
  width: number | null
  height: number | null
  altText: string | null
  createdAt: string
}

export type AdminMediaPage = PageResponse<AdminMediaAsset>

export type AdminArticlePage = PageResponse<AdminArticleSummary>

export interface AdminMessage {
  id: number
  senderName: string
  contact: string | null
  message: string
  status: AdminMessageStatus
  version: number
  createdAt: string
  readAt: string | null
  archivedAt: string | null
}

export type AdminMessagePage = PageResponse<AdminMessage>
