<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { getAllArticles, getArchives } from '../api/journal'
import { getErrorMessage } from '../api/client'
import AsyncState from '../components/AsyncState.vue'
import { useScrollCardReveal } from '../composables/useScrollCardReveal'
import type { ArchiveItem, ArticleSummary } from '../types/api'
import { formatCategory, formatMonth, formatPublishedDate, getPublishedYearMonth, setPageMeta } from '../utils/content'

const archives = ref<ArchiveItem[]>([])
const articles = ref<ArticleSummary[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
let controller: AbortController | undefined
const { reveal } = useScrollCardReveal()

const grouped = computed(() => {
  const result: Record<string, Record<string, ArticleSummary[]>> = {}
  articles.value.forEach(article => {
    const { year, month } = getPublishedYearMonth(article.publishedAt)
    result[year] ??= {}
    result[year][month] ??= []
    result[year][month].push(article)
  })
  return result
})
const countFor = (year: string | number, month: string | number) =>
  archives.value.find(item => item.year === Number(year) && item.month === Number(month))?.articleCount ?? 0
const archiveStats = computed(() => [
  { value: String(articles.value.length).padStart(2, '0'), label: '篇已经发布的文章', tone: 'blue' },
  { value: String(archives.value.length).padStart(2, '0'), label: '个留下更新的月份', tone: 'coral' },
  { value: String(new Set(articles.value.map(item => formatCategory(item.category))).size).padStart(2, '0'), label: '种仍在生长的主题', tone: 'green' }
])

async function loadArchive() {
  controller?.abort()
  const requestController = new AbortController()
  controller = requestController
  loading.value = true
  error.value = null
  try {
    const [archiveResponse, articleResponse] = await Promise.all([
      getArchives(requestController.signal),
      getAllArticles(requestController.signal)
    ])
    archives.value = archiveResponse
    articles.value = articleResponse
    setPageMeta('归档', '按时间查看 LEKANG 的全部文章。')
  } catch (caught) {
    if (caught instanceof DOMException && caught.name === 'AbortError') return
    error.value = getErrorMessage(caught)
  } finally {
    if (!requestController.signal.aborted) {
      loading.value = false
      if (!error.value) void reveal()
    }
  }
}

onMounted(loadArchive)
onBeforeUnmount(() => controller?.abort())
</script>

<template>
  <div class="archive-view container">
    <header class="page-intro"><p class="eyebrow">文章归档</p><h1>按时间，找到写过的文章。</h1><p>这里收录全部已发布内容，从最近一次更新慢慢往前看。</p></header>
    <AsyncState
      v-if="loading || error || articles.length === 0"
      :loading="loading"
      :error="error"
      :empty="!loading && !error && articles.length === 0"
      empty-title="归档里暂时没有文章。"
      @retry="loadArchive"
    />
    <template v-else>
      <section class="archive-stats" aria-label="归档概览">
        <article v-for="stat in archiveStats" :key="stat.label" :class="`tone-${stat.tone}`" data-scroll-card><strong>{{ stat.value }}</strong><span>{{ stat.label }}</span></article>
        <article class="archive-stats-note" data-scroll-card><small>ARCHIVE NOTE</small><p>有些文章记录答案，更多文章只是把当时的问题留下来。</p></article>
      </section>
      <section v-for="(months, year) in grouped" :key="year" class="archive-year" data-scroll-card>
        <h2>{{ year }}</h2>
        <div><section v-for="(items, month) in months" :key="month" class="archive-month"><h3>{{ formatMonth(Number(month)) }} · {{ countFor(year, month) }} 篇文章</h3><RouterLink v-for="article in items" :key="article.slug" :to="`/article/${article.slug}`"><span>{{ article.title }}</span><small>{{ formatCategory(article.category) }} · {{ formatPublishedDate(article.publishedAt) }}</small></RouterLink></section></div>
      </section>
    </template>
  </div>
</template>
