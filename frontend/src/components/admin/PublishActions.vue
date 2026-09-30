<script setup lang="ts">
import type { AdminArticleStatus } from '../../types/admin'

defineProps<{
  status: AdminArticleStatus
  busy: boolean
  isNew: boolean
  placement?: 'top' | 'bottom'
}>()

const emit = defineEmits<{
  save: []
  publish: []
  unpublish: []
  archive: []
}>()
</script>

<template>
  <div class="publish-actions" :data-placement="placement ?? 'bottom'">
    <button class="save" type="submit" :disabled="busy">
      {{ busy ? '处理中…' : placement === 'top' ? (isNew ? '保存草稿' : '保存') : (isNew ? '创建草稿' : '保存修改') }}
    </button>
    <button v-if="!isNew && status === 'DRAFT'" type="button" :disabled="busy" @click="emit('publish')">
      发布文章
    </button>
    <button v-if="status === 'PUBLISHED'" type="button" :disabled="busy" @click="emit('unpublish')">
      撤回发布
    </button>
    <button v-if="!isNew && status !== 'ARCHIVED'" class="quiet" type="button" :disabled="busy" @click="emit('archive')">
      归档
    </button>
  </div>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;

.publish-actions { display: flex; align-items: center; justify-content: flex-end; gap: 9px; }
button {
  min-width: 108px; padding: 11px 15px; border: 1px solid $text-primary; border-radius: 9px;
  color: $background;
  background: $text-primary;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .14em;
  cursor: pointer;
}
button:hover:not(:disabled) { border-color: $accent; background: $accent; transform: translateY(-1px); }
button.quiet { border-color: $border; color: $text-secondary; background: #fff; }
button:disabled { opacity: .45; cursor: wait; }
.publish-actions[data-placement='top'] button { min-width:auto; padding:9px 12px; font-size:9px; }.publish-actions[data-placement='top'] button.save { color:$accent; border-color:rgba($accent,.28); background:#fff; }.publish-actions[data-placement='top'] button.save:hover:not(:disabled) { color:#fff; border-color:$accent; background:$accent; }

@media (max-width: 560px) {
  .publish-actions { align-items: stretch; flex-direction: column; gap: 10px; }
  button { width: 100%; min-height: 45px; }
  .publish-actions[data-placement='top'] {
    display: grid;
    width: 100%;
    grid-template-columns: repeat(auto-fit, minmax(72px, 1fr));
    gap: 8px;
  }
  .publish-actions[data-placement='top'] button { width: 100%; min-height: 40px; }
}
</style>
