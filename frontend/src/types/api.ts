export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  hasNext: boolean
}

export interface ArticleSummary {
  slug: string
  category: string
  topic: string
  title: string
  excerpt: string
  publishedAt: string
  readMinutes: number
  imageUrl: string | null
  imageAlt: string | null
}

export interface ArticleDetail extends ArticleSummary {
  bodyMarkdown: string
  bodyHtml: string
  updatedAt: string
}

export interface ArticleNavigationItem {
  slug: string
  title: string
}

export interface ArticleNavigation {
  previous: ArticleNavigationItem | null
  next: ArticleNavigationItem | null
}

export interface TaxonomyTopic {
  code: string
  displayName: string
  slug: string
}

export interface Taxonomy {
  code: string
  displayName: string
  contentType: 'ARTICLE' | 'ALBUM' | string
  topics: TaxonomyTopic[]
}

export interface ArchiveItem {
  year: number
  month: number
  articleCount: number
}

export interface Profile {
  displayName: string
  bio: string
  manifesto: string
  email: string | null
  nowWatchingTitle: string
  nowWatchingDetail: string
}

export interface HomepageCopy {
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
}

export interface Home {
  heroCopy?: HomepageCopy | null
  feature: ArticleSummary | null
  features: ArticleSummary[]
  latestStories: ArticleSummary[]
  profile: Profile
}

export interface Album {
  slug: string
  title: string
  description: string
  publishedAt: string
}

export interface AlbumPhoto {
  id: number
  topic: string | null
  title: string
  location: string | null
  shotAt: string | null
  caption: string | null
  sortOrder: number
  imageUrl: string
  imageAlt: string
}

export interface ProblemDetails {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  requestId?: string
}

export interface GuestMessageDraft {
  name: string
  contact: string
  message: string
  website: string
}

export interface GuestMessageCreated {
  message: string
}
