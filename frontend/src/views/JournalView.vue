<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { getArticles, getTaxonomies, searchArticles } from '../api/journal'
import { getErrorMessage } from '../api/client'
import ArticleCard from '../components/ArticleCard.vue'
import AsyncState from '../components/AsyncState.vue'
import CategoryTabs from '../components/CategoryTabs.vue'
import type { ArticleSummary, PageResponse, Taxonomy } from '../types/api'
import { setPageMeta } from '../utils/content'

const activeCategory = ref('ALL')
const activeTopic = ref<string | null>(null)
const currentPage = ref(0)
const searchQuery = ref('')
const debouncedQuery = ref('')
const sortOrder = ref<'publishedAt,asc' | 'publishedAt,desc'>('publishedAt,desc')
const pageSize = 9
const articleGrid = ref<HTMLElement | null>(null)
const heroTitle = ref<HTMLElement | null>(null)
const heroSubtitle = ref<HTMLElement | null>(null)
const heroLines = ['慢一点读，', '也慢一点忘记。']
const taxonomies = ref<Taxonomy[]>([])
const articlePage = ref<PageResponse<ArticleSummary> | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
let controller: AbortController | undefined
let searchTimer: ReturnType<typeof setTimeout> | undefined
let heroMotionCleanup: (() => void) | undefined
let cardMotionCleanup: (() => void) | undefined
const totalPages = computed(() => articlePage.value?.totalPages ?? 0)
const isSearching = computed(() => debouncedQuery.value.length >= 2)
const activeCategoryLabel = computed(() => ({ ALL: '全部文章', CINEMA: '日常', BOOKS: '阅读', NOTES: '笔记' })[activeCategory.value] ?? activeCategory.value)
const resultsDescription = computed(() => {
  if (isSearching.value) return `找到 ${articlePage.value?.totalItems ?? 0} 篇与“${debouncedQuery.value}”有关的文章 · 按最近发布`
  return `正在查看：${activeCategoryLabel.value} · ${sortOrder.value === 'publishedAt,desc' ? '最近发布' : '最早发布'}`
})

async function setupHeroMotion() {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  await nextTick()
  const characters = heroTitle.value?.querySelectorAll<HTMLElement>('.journal-title-char')
  if (!characters?.length || !heroSubtitle.value) return
  try {
    const { gsap } = await import('gsap')
    const timeline = gsap.timeline()
    timeline.fromTo(characters, { y: 30, autoAlpha: 0 }, {
      y: 0,
      autoAlpha: 1,
      duration: .62,
      stagger: .03,
      ease: 'back.out(1.55)'
    }).fromTo(heroSubtitle.value, { y: 16, autoAlpha: 0 }, {
      y: 0,
      autoAlpha: 1,
      duration: .5,
      ease: 'power2.out'
    }, heroLines.join('').length * .03 * .5)
    heroMotionCleanup = () => {
      timeline.kill()
      gsap.set([...characters, heroSubtitle.value], { clearProps: 'opacity,visibility,transform' })
    }
  } catch {
    characters.forEach(character => character.removeAttribute('style'))
    heroSubtitle.value.removeAttribute('style')
  }
}

async function setupCardMotion() {
  cardMotionCleanup?.()
  cardMotionCleanup = undefined
  await nextTick()
  const cards = articleGrid.value?.querySelectorAll<HTMLElement>('.article-card')
  if (!cards?.length || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  try {
    const [{ gsap }, { ScrollTrigger }] = await Promise.all([
      import('gsap'),
      import('gsap/ScrollTrigger')
    ])
    gsap.registerPlugin(ScrollTrigger)
    const animation = gsap.fromTo(cards, { y: 24, autoAlpha: 0 }, {
      y: 0,
      autoAlpha: 1,
      duration: .62,
      stagger: .08,
      ease: 'power3.out',
      scrollTrigger: {
        trigger: articleGrid.value,
        start: 'top 86%',
        once: true
      }
    })
    ScrollTrigger.refresh()
    cardMotionCleanup = () => {
      animation.scrollTrigger?.kill()
      animation.kill()
      gsap.set(cards, { clearProps: 'opacity,visibility,transform' })
    }
  } catch {
    cards.forEach(card => card.removeAttribute('style'))
  }
}

async function loadArticles() {
  controller?.abort()
  const requestController = new AbortController()
  controller = requestController
  loading.value = true
  error.value = null
  try {
    const [page, taxonomyResponse] = await Promise.all([
      isSearching.value
        ? searchArticles(debouncedQuery.value, currentPage.value, pageSize, requestController.signal)
        : getArticles({ page: currentPage.value, size: pageSize, category: activeCategory.value === 'ALL' ? null : activeCategory.value, topic: null, sort: sortOrder.value, signal: requestController.signal }),
      taxonomies.value.length ? Promise.resolve(taxonomies.value) : getTaxonomies(requestController.signal)
    ])
    articlePage.value = page
    taxonomies.value = taxonomyResponse
  } catch (caught) {
    if (caught instanceof DOMException && caught.name === 'AbortError') return
    error.value = getErrorMessage(caught)
  } finally {
    if (!requestController.signal.aborted) {
      loading.value = false
      if (!error.value) void setupCardMotion()
    }
  }
}

function selectCategory(value: string) { searchQuery.value = ''; debouncedQuery.value = ''; activeCategory.value = value; activeTopic.value = null; currentPage.value = 0 }
function selectTopic(value: string | null) { activeTopic.value = value; currentPage.value = 0 }
function clearSearch() { searchQuery.value = ''; debouncedQuery.value = ''; currentPage.value = 0 }
function setPage(page: number) { if (page < 0 || page >= totalPages.value || page === currentPage.value) return; currentPage.value = page; articleGrid.value?.scrollIntoView({ behavior: 'smooth', block: 'start' }) }

watch(searchQuery, value => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 0
    debouncedQuery.value = value.trim()
  }, 400)
})
watch([activeCategory, currentPage, sortOrder, debouncedQuery], loadArticles, { immediate: true })
onMounted(() => { setPageMeta('文章', '乐康写下的项目思考、阅读、电影与日常。'); void setupHeroMotion() })
onBeforeUnmount(() => { controller?.abort(); if (searchTimer) clearTimeout(searchTimer); heroMotionCleanup?.(); cardMotionCleanup?.() })
</script>

<template>
  <div class="journal-view container">
    <header class="page-intro"><p class="eyebrow">文章 / JOURNAL</p><h1>有些事情做完就算了，<br />有些还是想写下来。</h1><p>项目里的想法、读过的书、看过的电影，以及那些暂时不知道应该放在哪里的日常。</p></header>
    <section class="journal-index-card"><div><small>LEKANG / JOURNAL</small><h2 ref="heroTitle" aria-label="慢一点读，也慢一点忘记。"><span v-for="line in heroLines" :key="line" class="journal-title-line" aria-hidden="true"><span v-for="(character, index) in [...line]" :key="`${line}-${index}`" class="journal-title-char">{{ character }}</span></span></h2></div><div class="journal-index-lines"><span></span><span></span><span></span><p ref="heroSubtitle">{{ articlePage?.totalItems ?? '—' }} 篇文章正在这里</p></div></section>
    <div class="stories-heading journal-heading"><div><p class="eyebrow">全部文章</p><h2>找一篇，慢慢读。</h2></div><p>从项目手记到阅读和日常，这里收着我愿意再回来看的东西。</p></div>
    <section class="journal-toolbar" aria-label="文章搜索与排序">
      <label class="journal-search">
        <span class="search-icon" aria-hidden="true"></span>
        <input v-model="searchQuery" type="search" placeholder="搜索标题或文章内容" aria-label="搜索文章" />
        <button v-if="searchQuery" type="button" aria-label="清除搜索" @click="clearSearch">清除</button>
      </label>
      <label class="journal-sort">
        <span>排序</span>
        <select v-model="sortOrder" :disabled="isSearching" :title="isSearching ? '搜索结果固定按最近发布' : undefined">
          <option value="publishedAt,desc">最近发布</option>
          <option value="publishedAt,asc">最早发布</option>
        </select>
      </label>
    </section>
    <CategoryTabs :taxonomies="taxonomies" :model-value="activeCategory" :topic="activeTopic" @update:model-value="selectCategory" @update:topic="selectTopic" />
    <p class="journal-result-status" aria-live="polite">{{ resultsDescription }}</p>
    <div ref="articleGrid" class="journal-results">
      <AsyncState v-if="loading || error || !articlePage?.items.length" :loading="loading" :error="error" :empty="!loading && !error && articlePage?.items.length === 0" :empty-title="isSearching ? '没有找到相关文章。' : '这个抽屉暂时是空的。'" :empty-hint="isSearching ? '换个关键词，或清除搜索继续浏览。' : '换一个分类看看。'" @retry="loadArticles" />
      <section v-else class="article-grid"><ArticleCard v-for="(article, index) in articlePage.items" :key="article.slug" :article="article" :number="currentPage * pageSize + index + 1" /></section>
    </div>
    <nav v-if="totalPages > 1" class="site-pagination" aria-label="文章分页"><button :disabled="currentPage === 0" @click="setPage(currentPage - 1)">← 上一页</button><div class="page-numbers"><button v-for="page in totalPages" :key="page" :class="{ active: currentPage === page - 1 }" @click="setPage(page - 1)">{{ String(page).padStart(2, '0') }}</button></div><p>第 {{ currentPage + 1 }} / {{ totalPages }} 页</p><button :disabled="currentPage >= totalPages - 1" @click="setPage(currentPage + 1)">下一页 →</button></nav>
  </div>
</template>
