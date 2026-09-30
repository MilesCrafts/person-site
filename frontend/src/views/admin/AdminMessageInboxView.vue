<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { archiveAdminMessage, getAdminMessages, markAdminMessageRead } from '../../api/admin'
import { getErrorMessage } from '../../api/client'
import AdminShell from '../../components/admin/AdminShell.vue'
import type { AdminMessage, AdminMessagePage, AdminMessageStatus } from '../../types/admin'

const page = ref<AdminMessagePage | null>(null)
const currentPage = ref(0)
const status = ref<AdminMessageStatus | undefined>()
const selected = ref<AdminMessage | null>(null)
const loading = ref(true)
const actionLoading = ref(false)
const errorMessage = ref('')
const inbox = ref<HTMLElement | null>(null)
let controller: AbortController | undefined
let animationContext: { revert: () => void } | null = null

function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
}

async function animateInbox() {
  if (!inbox.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  try {
    const { gsap } = await import('gsap')
    animationContext?.revert()
    animationContext = gsap.context(() => {
      gsap.from('.inbox-card', { y: 20, opacity: 0, rotate: (index) => index % 2 ? .5 : -.5, duration: .42, stagger: .055, ease: 'power3.out' })
    }, inbox.value)
  } catch { /* 动画失败不影响收件箱。 */ }
}

async function loadMessages() {
  controller?.abort()
  controller = new AbortController()
  loading.value = true
  errorMessage.value = ''
  try {
    page.value = await getAdminMessages(currentPage.value, 20, status.value, controller.signal)
    if (selected.value) selected.value = page.value.items.find(item => item.id === selected.value?.id) ?? null
    await nextTick()
    await animateInbox()
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') return
    errorMessage.value = getErrorMessage(error)
  } finally { loading.value = false }
}

async function openMessage(message: AdminMessage) {
  selected.value = message
  if (message.status !== 'UNREAD') return
  try {
    const updated = await markAdminMessageRead(message.id, message.version)
    Object.assign(message, updated)
    selected.value = message
  } catch (error) { errorMessage.value = getErrorMessage(error) }
}

async function archiveMessage(message: AdminMessage) {
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    await archiveAdminMessage(message.id, message.version)
    selected.value = null
    await loadMessages()
  } catch (error) { errorMessage.value = getErrorMessage(error) }
  finally { actionLoading.value = false }
}

watch(status, () => { currentPage.value = 0; void loadMessages() })
onMounted(loadMessages)
onBeforeUnmount(() => { controller?.abort(); animationContext?.revert() })
</script>

<template>
  <AdminShell section="INBOX">
    <div ref="inbox" class="inbox-page">
      <header class="inbox-heading"><div><p><i /> PRIVATE NOTES</p><h1>收件箱</h1></div><p>来自首页的留言只在这里出现。打开即标记为已读，归档后仍可在归档筛选中找到。</p></header>
      <nav class="inbox-filters" aria-label="留言状态筛选">
        <button :class="{ active: !status }" @click="status = undefined">全部</button>
        <button :class="{ active: status === 'UNREAD' }" @click="status = 'UNREAD'">未读</button>
        <button :class="{ active: status === 'READ' }" @click="status = 'READ'">已读</button>
        <button :class="{ active: status === 'ARCHIVED' }" @click="status = 'ARCHIVED'">已归档</button>
      </nav>
      <p v-if="errorMessage" class="inbox-error" role="alert">{{ errorMessage }}</p>
      <div class="inbox-layout">
        <section class="inbox-list" :aria-busy="loading">
          <p v-if="loading" class="inbox-empty">正在整理信件…</p>
          <p v-else-if="!page?.items.length" class="inbox-empty">这里还没有留言。</p>
          <button v-for="message in page?.items ?? []" :key="message.id" class="inbox-card" :class="{ unread: message.status === 'UNREAD', selected: selected?.id === message.id }" @click="openMessage(message)">
            <span class="message-state"><i />{{ message.status === 'UNREAD' ? 'NEW' : message.status === 'READ' ? 'READ' : 'ARCHIVED' }}</span>
            <strong>{{ message.senderName }}</strong><time>{{ formatDate(message.createdAt) }}</time><p>{{ message.message }}</p>
          </button>
          <div v-if="(page?.totalPages ?? 0) > 1" class="inbox-pages"><button :disabled="currentPage === 0" @click="currentPage--; loadMessages()">←</button><span>{{ currentPage + 1 }} / {{ page?.totalPages }}</span><button :disabled="!page?.hasNext" @click="currentPage++; loadMessages()">→</button></div>
        </section>
        <aside class="message-reader" :class="{ open: selected }">
          <div v-if="selected" :key="selected.id" class="reader-paper">
            <div class="reader-top"><span>MESSAGE / {{ String(selected.id).padStart(4, '0') }}</span><button aria-label="关闭留言" @click="selected = null">×</button></div>
            <p class="reader-date">{{ formatDate(selected.createdAt) }}</p><h2>{{ selected.senderName }}</h2>
            <a v-if="selected.contact" :href="selected.contact.includes('@') ? `mailto:${selected.contact}` : undefined">{{ selected.contact }}</a>
            <p class="reader-message">{{ selected.message }}</p>
            <button v-if="selected.status !== 'ARCHIVED'" class="archive-button" :disabled="actionLoading" @click="archiveMessage(selected)">归档这封信</button>
          </div>
          <div v-else class="reader-placeholder"><span>✉</span><p>选择一封留言阅读</p></div>
        </aside>
      </div>
    </div>
  </AdminShell>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;
.inbox-heading { display: grid; grid-template-columns: 1fr minmax(280px,.55fr); gap: 50px; align-items: end; padding: 18px 0 38px; border-bottom: 1px solid $border; }.inbox-heading > div > p { display: flex; align-items: center; gap: 9px; margin: 0 0 12px; color: $accent; font-size: 9px; font-weight: 750; letter-spacing: .13em; }.inbox-heading i { width: 8px; height: 8px; border-radius: 50%; background: $green; box-shadow: 0 0 0 4px rgba($green,.12); }.inbox-heading h1 { margin: 0; font-size: clamp(48px,6vw,76px); line-height: 1; letter-spacing: -.06em; }.inbox-heading > p { margin: 0; color: $text-secondary; font-size: 13px; line-height: 1.8; }
.inbox-filters { display: flex; gap: 6px; margin: 24px 0; }.inbox-filters button { padding: 9px 14px; border: 1px solid $border; border-radius: 99px; color: $text-secondary; background: #fff; font-size: 10px; cursor: pointer; }.inbox-filters button.active { color: #fff; border-color: $accent; background: $accent; }.inbox-error { padding: 11px 13px; border-radius: 8px; color: #a43124; background: rgba($coral,.09); font-size: 11px; }
.inbox-layout { display: grid; grid-template-columns: minmax(360px,.72fr) minmax(420px,1fr); gap: 24px; align-items: start; }.inbox-list { display: grid; gap: 9px; }.inbox-card { display: grid; grid-template-columns: 1fr auto; gap: 7px 14px; padding: 18px; border: 1px solid $border; border-left: 4px solid transparent; border-radius: 11px; color: $text-primary; background: #fff; text-align: left; cursor: pointer; }.inbox-card:hover,.inbox-card.selected { border-color: rgba($accent,.35); transform: translateX(3px); }.inbox-card.unread { border-left-color: $coral; background: #fffdfc; }.message-state { grid-column: 1 / -1; display: flex; align-items: center; gap: 6px; color: $text-secondary; font-size: 7px; font-weight: 800; letter-spacing: .12em; }.message-state i { width: 6px; height: 6px; border-radius: 50%; background: $border; }.unread .message-state { color: $coral; }.unread .message-state i { background: $coral; box-shadow: 0 0 0 4px rgba($coral,.1); }.inbox-card strong { font-size: 13px; }.inbox-card time { color: $text-secondary; font-size: 9px; }.inbox-card p { grid-column: 1 / -1; margin: 0; overflow: hidden; color: $text-secondary; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }.inbox-empty { padding: 60px 20px; border: 1px dashed $border; border-radius: 12px; color: $text-secondary; text-align: center; }.inbox-pages { display: flex; justify-content: center; align-items: center; gap: 14px; margin-top: 10px; }.inbox-pages button { width: 34px; height: 34px; border: 1px solid $border; border-radius: 8px; background: #fff; }.inbox-pages span { color: $text-secondary; font-size: 9px; }
.message-reader { position: sticky; top: 108px; min-height: 500px; border-radius: 16px; background: #f5f7fb; }.reader-paper { position: relative; min-height: 500px; padding: 30px; overflow: hidden; border: 1px solid rgba($accent,.16); border-radius: 16px; background: #fff; box-shadow: 0 24px 55px -45px rgba(16,24,40,.5); animation: reader-open .35s ease both; }.reader-paper::before { position: absolute; top: -25px; right: 42px; width: 62px; height: 40px; background: rgba($yellow,.78); content: ''; transform: rotate(4deg); }.reader-top { display: flex; justify-content: space-between; padding-bottom: 17px; border-bottom: 1px solid $border; color: $text-secondary; font-size: 8px; font-weight: 750; letter-spacing: .12em; }.reader-top button { border: 0; background: none; font-size: 20px; cursor: pointer; }.reader-date { margin: 38px 0 8px; color: $coral; font-size: 9px; font-weight: 750; }.reader-paper h2 { margin: 0; font-size: 34px; letter-spacing: -.05em; }.reader-paper > a { display: inline-block; margin-top: 8px; color: $accent; font-size: 11px; }.reader-message { min-height: 190px; margin: 34px 0; color: $text-primary; font-size: 14px; line-height: 1.9; white-space: pre-wrap; }.archive-button { padding: 10px 14px; border: 1px solid $border; border-radius: 8px; color: $text-secondary; background: #fff; font-size: 10px; cursor: pointer; }.reader-placeholder { display: grid; min-height: 500px; place-content: center; color: $text-secondary; text-align: center; }.reader-placeholder span { font-size: 42px; opacity: .28; }.reader-placeholder p { font-size: 11px; }@keyframes reader-open { from { opacity: 0; transform: translateY(12px) rotate(.5deg); } }
@media (max-width: 880px) { .inbox-heading,.inbox-layout { grid-template-columns: 1fr; }.message-reader { position: static; min-height: 360px; }.reader-paper,.reader-placeholder { min-height: 360px; } }
@media (max-width: 560px) { .inbox-filters { overflow-x: auto; }.inbox-layout { gap: 16px; }.reader-paper { padding: 22px; }.inbox-heading { gap: 18px; }.inbox-card p { max-width: calc(100vw - 80px); } }
@media (prefers-reduced-motion: reduce) { .reader-paper { animation: none; } }
</style>
