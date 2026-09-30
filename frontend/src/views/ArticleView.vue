<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle, getArticleNavigation } from '../api/journal'
import { getErrorMessage } from '../api/client'
import AsyncState from '../components/AsyncState.vue'
import { useScrollCardReveal } from '../composables/useScrollCardReveal'
import { resolveMediaUrl } from '../data/media'
import type { ArticleDetail, ArticleNavigation } from '../types/api'
import { formatCategory, formatPublishedDate, formatReadTime, sanitizeArticleHtml, setPageMeta } from '../utils/content'
import { getCoverFrameVariant } from '../utils/coverFrame'

const route = useRoute()
const article = ref<ArticleDetail | null>(null)
const navigation = ref<ArticleNavigation | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const cover = computed(() => resolveMediaUrl(article.value?.imageUrl ?? null))
const coverFrame = computed(() => getCoverFrameVariant(article.value?.slug ?? ''))
const safeBody = computed(() => sanitizeArticleHtml(article.value?.bodyHtml ?? ''))
let controller: AbortController | undefined
const { reveal } = useScrollCardReveal()

async function loadArticle() {
  const slug = String(route.params.id ?? '')
  controller?.abort()
  const requestController = new AbortController()
  controller = requestController
  loading.value = true
  error.value = null
  article.value = null
  navigation.value = null

  try {
    const [detail, articleNavigation] = await Promise.all([
      getArticle(slug, requestController.signal),
      getArticleNavigation(slug, requestController.signal)
    ])
    article.value = detail
    navigation.value = articleNavigation
    setPageMeta(detail.title, detail.excerpt)
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

watch(() => route.params.id, loadArticle, { immediate: true })
onBeforeUnmount(() => controller?.abort())
</script>

<template>
  <div v-if="loading || error" class="article-view container">
    <AsyncState :loading="loading" :error="error" loading-label="正在打开文章…" @retry="loadArticle" />
  </div>
  <article v-else-if="article" class="article-view container">
    <header class="article-header" data-scroll-card>
      <p class="eyebrow">{{ formatCategory(article.category) }} / {{ article.topic }}</p><h1>{{ article.title }}</h1><p class="lead">{{ article.excerpt }}</p><p class="meta">{{ formatPublishedDate(article.publishedAt) }} <span></span> {{ formatReadTime(article.readMinutes) }}</p>
    </header>
    <figure v-if="cover" class="article-cover-card" :data-frame="coverFrame" data-scroll-card>
      <img class="article-cover" :src="cover" :alt="article.imageAlt || article.title" />
      <figcaption><span>LEKANG / JOURNAL</span><strong>{{ formatCategory(article.category) }}</strong></figcaption>
    </figure>
    <section class="article-facts" aria-label="文章信息" data-scroll-card>
      <div><small>发布于</small><strong>{{ formatPublishedDate(article.publishedAt) }}</strong></div>
      <div><small>阅读时间</small><strong>{{ formatReadTime(article.readMinutes) }}</strong></div>
      <div><small>放进抽屉</small><strong>{{ formatCategory(article.category) }} / {{ article.topic }}</strong></div>
    </section>
    <div class="article-body" v-html="safeBody"></div>
    <nav class="article-pagination" data-scroll-card>
      <RouterLink v-if="navigation?.previous" :to="`/article/${navigation.previous.slug}`"><small>上一篇</small><strong>← {{ navigation.previous.title }}</strong></RouterLink><span v-else></span>
      <RouterLink v-if="navigation?.next" :to="`/article/${navigation.next.slug}`"><small>下一篇</small><strong>{{ navigation.next.title }} →</strong></RouterLink>
    </nav>
  </article>
</template>
