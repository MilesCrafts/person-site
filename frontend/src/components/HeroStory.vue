<script setup lang="ts">
import { computed } from 'vue'
import { resolveMediaUrl } from '../data/media'
import type { ArticleSummary } from '../types/api'
import { formatCategory, formatPublishedDate, formatReadTime } from '../utils/content'

const props = defineProps<{ article: ArticleSummary }>()
const image = computed(() => resolveMediaUrl(props.article.imageUrl))
</script>

<template>
  <article class="hero-story">
    <div class="hero-copy">
      <p class="eyebrow">精选文章 · {{ formatCategory(article.category) }}</p>
      <h1>{{ article.title }}</h1>
      <p class="hero-excerpt">{{ article.excerpt }}</p>
      <div class="hero-meta"><span>{{ formatPublishedDate(article.publishedAt) }}</span><span>{{ formatReadTime(article.readMinutes) }}</span></div>
      <RouterLink class="primary-link" :to="`/article/${article.slug}`">阅读全文 <span aria-hidden="true">→</span></RouterLink>
    </div>
    <RouterLink :to="`/article/${article.slug}`" class="hero-media" :aria-label="`阅读：${article.title}`">
      <img v-if="image" :src="image" :alt="article.imageAlt || article.title" />
      <span v-else class="image-placeholder">LEKANG</span>
    </RouterLink>
  </article>
</template>
