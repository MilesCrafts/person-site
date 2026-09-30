<script setup lang="ts">
withDefaults(defineProps<{
  loading?: boolean
  error?: string | null
  empty?: boolean
  loadingLabel?: string
  emptyTitle?: string
  emptyHint?: string
}>(), {
  loading: false,
  error: null,
  empty: false,
  loadingLabel: '正在整理内容…',
  emptyTitle: '这里暂时还没有内容。',
  emptyHint: '新的记录会在准备好后出现在这里。'
})

defineEmits<{ retry: [] }>()
</script>

<template>
  <div v-if="loading" class="async-state" role="status" aria-live="polite">
    <span class="loading-mark" aria-hidden="true"></span>
    <p>{{ loadingLabel }}</p>
  </div>
  <div v-else-if="error" class="async-state async-state-error" role="alert">
    <p>{{ error }}</p>
    <button type="button" @click="$emit('retry')">重新加载</button>
  </div>
  <div v-else-if="empty" class="async-state">
    <p>{{ emptyTitle }}</p>
    <small>{{ emptyHint }}</small>
  </div>
</template>
