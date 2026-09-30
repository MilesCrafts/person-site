<script setup lang="ts">
import type { ArticleSummary } from '../types/api'
import { formatReadTime } from '../utils/content'

defineProps<{ articles: ArticleSummary[] }>()
</script>

<template>
  <section class="latest-stories">
    <div class="section-heading"><p>TABLE OF CONTENTS</p><h2>最新文章</h2></div>
    <article v-for="(item, index) in articles" :key="item.slug" class="latest-item">
      <p class="story-index">{{ String(index + 1).padStart(2, '0') }}</p>
      <RouterLink :to="`/article/${item.slug}`"><h3>{{ item.title }}</h3></RouterLink>
      <div class="latest-meta"><p>{{ item.category }} / {{ item.topic }}</p><p>{{ formatReadTime(item.readMinutes) }}</p></div>
    </article>
  </section>
</template>
