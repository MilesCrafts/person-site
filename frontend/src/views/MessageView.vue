<script setup lang="ts">
import GuestMessageForm from '../components/GuestMessageForm.vue'
import { setPageMeta } from '../utils/content'

setPageMeta('留言', '给 LEKANG 留下一封私人留言。留言只会进入管理员收件箱，不会公开展示。')
</script>

<template>
  <main class="message-page container">
    <header class="message-page-heading">
      <p class="eyebrow">MESSAGE / PRIVATE INBOX</p>
      <h1>写点什么，<br /><span>让我收到。</span></h1>
      <p>关于作品、文章、合作，或者只是路过打个招呼。写下你的想法，我会认真读完。</p>
    </header>
    <GuestMessageForm />
  </main>
</template>

<style scoped lang="scss">
@use '../assets/styles/variables' as *;

.message-page { padding-top: clamp(58px, 8vw, 108px); padding-bottom: 24px; }
.message-page-heading { display: grid; grid-template-columns: minmax(0, 1fr) minmax(280px, .62fr); gap: 28px 72px; align-items: end; padding-bottom: clamp(38px, 6vw, 72px); border-bottom: 1px solid $border; }
.message-page-heading .eyebrow { grid-column: 1 / -1; margin: 0; color: $accent; }
.message-page-heading h1 { margin: 0; color: $text-primary; font-size: clamp(54px, 7.4vw, 104px); line-height: .88; letter-spacing: -.075em; }
.message-page-heading h1 span { color: $accent; }
.message-page-heading > p:last-child { max-width: 470px; margin: 0 0 7px; color: $text-secondary; font-size: 15px; line-height: 1.85; }
.message-page :deep(.message-box) { margin-top: clamp(42px, 6vw, 76px); }

@media (max-width: 760px) {
  .message-page { padding-top: 48px; }
  .message-page-heading { grid-template-columns: 1fr; gap: 24px; }
  .message-page-heading .eyebrow { grid-column: auto; }
  .message-page-heading h1 { font-size: clamp(52px, 17vw, 76px); }
}
</style>
