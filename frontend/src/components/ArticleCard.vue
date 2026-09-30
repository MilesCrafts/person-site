<script setup lang="ts">
import { computed } from 'vue'
import { resolveMediaUrl } from '../data/media'
import type { ArticleSummary } from '../types/api'
import { formatCategory, formatPublishedDate, formatReadTime } from '../utils/content'

const props = defineProps<{ article: ArticleSummary; number?: number }>()
const image = computed(() => resolveMediaUrl(props.article.imageUrl))
</script>

<template>
  <article class="article-card" data-scroll-card>
    <RouterLink :to="`/article/${article.slug}`" class="card-image">
      <img v-if="image" :src="image" :alt="article.imageAlt || article.title" loading="lazy" />
      <span v-else class="image-placeholder">LEKANG</span>
    </RouterLink>
    <div class="card-kicker"><p class="eyebrow">{{ formatCategory(article.category) }}</p></div>
    <RouterLink :to="`/article/${article.slug}`"><h2>{{ article.title }}</h2></RouterLink>
    <p class="card-excerpt">{{ article.excerpt }}</p>
    <div class="card-meta"><p>{{ formatPublishedDate(article.publishedAt) }}</p><p>{{ formatReadTime(article.readMinutes) }}</p></div>
  </article>
</template>
