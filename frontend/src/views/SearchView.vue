<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { getArticles, searchArticles } from '../api/journal'
import { getErrorMessage } from '../api/client'
import ArticleCard from '../components/ArticleCard.vue'
import AsyncState from '../components/AsyncState.vue'
import { useScrollCardReveal } from '../composables/useScrollCardReveal'
import type { ArticleSummary, PageResponse } from '../types/api'
import { setPageMeta } from '../utils/content'

const query = ref('')
const results = ref<PageResponse<ArticleSummary> | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const page = ref(0)
const resultsRoot = ref<HTMLElement | null>(null)
let timer: ReturnType<typeof setTimeout> | undefined
let controller: AbortController | undefined
const { reveal: revealPage } = useScrollCardReveal()
const { reveal: revealResults } = useScrollCardReveal()

async function loadResults() {
  const term = query.value.trim()
  if (term.length === 1) {
    results.value = null
    loading.value = false
    error.value = null
    return
  }

  controller?.abort()
  const requestController = new AbortController()
  controller = requestController
  loading.value = true
  error.value = null
  try {
    results.value = term
      ? await searchArticles(term, page.value, 12, requestController.signal)
      : await getArticles({ page: page.value, size: 12, signal: requestController.signal })
  } catch (caught) {
    if (caught instanceof DOMException && caught.name === 'AbortError') return
    error.value = getErrorMessage(caught)
  } finally {
    if (!requestController.signal.aborted) {
      loading.value = false
      if (!error.value) void revealResults(resultsRoot.value ?? undefined)
    }
  }
}

function scheduleSearch() {
  if (timer) clearTimeout(timer)
  controller?.abort()
  page.value = 0
  timer = setTimeout(loadResults, 350)
}

function applySuggestion(value: string) {
  query.value = value
}

function setPage(nextPage: number) {
  if (!results.value || nextPage < 0 || nextPage >= results.value.totalPages) return
  page.value = nextPage
  void loadResults()
}

watch(query, scheduleSearch)
onMounted(() => {
  setPageMeta('搜索', '搜索 LEKANG 中已经发布的文章。')
  void revealPage()
  void loadResults()
})
onBeforeUnmount(() => {
  if (timer) clearTimeout(timer)
  controller?.abort()
})
</script>

<template>
  <div class="search-view container">
    <header class="page-intro"><p class="eyebrow">搜索文章</p><h1>想找什么？<br />丢个词进来试试。</h1><p>标题、摘要和正文都可以搜。记不清原话也没关系，先从一个印象开始。</p></header>
    <section class="search-play-card" data-scroll-card>
      <div class="search-card-copy"><p class="eyebrow">SEARCH THE JOURNAL</p><h2>可能是一部电影，<br />一本书，或者某个雨天。</h2><p>这里没有复杂筛选器，只需要一个你还记得的词。</p></div>
      <div class="search-card-form">
        <label class="search-box"><span class="sr-only">搜索文章</span><span class="search-icon" aria-hidden="true"></span><input v-model="query" autofocus type="search" placeholder="输入两个字以上……" /></label>
        <div class="search-suggestions"><span>不知道搜什么？</span><button type="button" @click="applySuggestion('电影')">电影</button><button type="button" @click="applySuggestion('阅读')">阅读</button><button type="button" @click="applySuggestion('网站')">网站</button></div>
      </div>
    </section>
    <div ref="resultsRoot">
      <p v-if="query.trim().length === 1" class="result-count">再输入一个字，就可以开始找了。</p>
      <p v-else-if="results" class="result-count">{{ query ? `找到 ${results.totalItems} 篇结果` : `这里一共有 ${results.totalItems} 篇文章` }}</p>
      <AsyncState
        v-if="loading || error || (results && results.items.length === 0)"
        :loading="loading"
        :error="error"
        :empty="!loading && !error && Boolean(results) && results?.items.length === 0"
        empty-title="这一词暂时没有回声。"
        empty-hint="换个说法再试试，或者从“电影”“阅读”开始。"
        @retry="loadResults"
      />
      <section v-else-if="results?.items.length" class="article-grid"><ArticleCard v-for="(article, index) in results.items" :key="article.slug" :article="article" :number="page * 12 + index + 1" /></section>
    </div>
    <nav v-if="results && results.totalPages > 1" class="site-pagination" aria-label="搜索结果分页">
      <button :disabled="page === 0" @click="setPage(page - 1)"><span>←</span> PREVIOUS</button>
      <div class="page-numbers">
        <button v-for="pageNumber in results.totalPages" :key="pageNumber" :class="{ active: page === pageNumber - 1 }" :aria-current="page === pageNumber - 1 ? 'page' : undefined" @click="setPage(pageNumber - 1)">{{ String(pageNumber).padStart(2, '0') }}</button>
      </div>
      <p>PAGE {{ String(page + 1).padStart(2, '0') }} / {{ String(results.totalPages).padStart(2, '0') }}</p>
      <button :disabled="!results.hasNext" @click="setPage(page + 1)">NEXT <span>→</span></button>
    </nav>
  </div>
</template>
