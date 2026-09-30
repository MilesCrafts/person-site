<script setup lang="ts">
import { computed } from 'vue'
import type { Taxonomy } from '../types/api'

const props = defineProps<{
  taxonomies: Taxonomy[]
  modelValue: string
  topic: string | null
}>()
const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:topic': [value: string | null]
}>()

const categories = computed(() => [
  { code: 'ALL', displayName: '全部' },
  ...props.taxonomies
    .filter(item => item.contentType === 'ARTICLE' && item.code !== 'FOOTBALL')
    .map(item => ({
      code: item.code,
      displayName: {
        CINEMA: '日常',
        BOOKS: '阅读',
        NOTES: '笔记'
      }[item.code] ?? item.displayName
    }))
])
const selectCategory = (category: string) => {
  emit('update:modelValue', category)
  emit('update:topic', null)
}
</script>

<template>
  <section class="category-explore">
    <p>按分类浏览</p>
    <nav class="category-tabs" aria-label="文章分类">
      <button v-for="category in categories" :key="category.code" :class="{ active: modelValue === category.code }" @click="selectCategory(category.code)">
        {{ category.displayName }}
      </button>
    </nav>
  </section>
</template>
