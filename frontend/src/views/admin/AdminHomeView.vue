<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { getAdminOverview } from '../../api/admin'
import { getErrorMessage } from '../../api/client'
import AdminShell from '../../components/admin/AdminShell.vue'
import AdminSceneMark from '../../components/admin/AdminSceneMark.vue'
import WritingActivity from '../../components/admin/WritingActivity.vue'
import type { AdminArticleStatus } from '../../types/admin'

const counts = ref<Record<AdminArticleStatus, number>>({ DRAFT: 0, PUBLISHED: 0, ARCHIVED: 0 })
const loading = ref(true)
const errorMessage = ref('')
const dailyNote = ref('先写下来，好文章不必一次完成。')
const noteSegments = computed(() => {
  const text = dailyNote.value.trim()
  const punctuationIndex = Math.max(text.indexOf('，'), text.indexOf(','))
  if (punctuationIndex > 0) {
    const first = text.slice(0, punctuationIndex + 1)
    const remainder = text.slice(punctuationIndex + 1)
    const accentLength = Math.min(Math.max(3, Math.round(remainder.length * .35)), remainder.length)
    return [first, remainder.slice(0, accentLength), remainder.slice(accentLength)].filter(Boolean)
  }
  const chunk = Math.max(2, Math.ceil(text.length / 3))
  return [text.slice(0, chunk), text.slice(chunk, chunk * 2), text.slice(chunk * 2)].filter(Boolean)
})
const desk = ref<HTMLElement | null>(null)
const displayCounts = ref<Record<AdminArticleStatus, number>>({ DRAFT: 0, PUBLISHED: 0, ARCHIVED: 0 })
let animationContext: { revert: () => void } | null = null

async function animateDesk() {
  if (!desk.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    displayCounts.value = { ...counts.value }
    return
  }
  try {
    const { gsap } = await import('gsap')
    if (!desk.value) return
    animationContext?.revert()
    animationContext = gsap.context(() => {
      gsap.from('.desk-copy > *, .desk-note', { y: 24, opacity: 0, duration: .58, stagger: .08, ease: 'power3.out' })
      gsap.from('.stat-card', { y: 28, rotate: (index) => index === 1 ? 1.5 : -1.5, opacity: 0, duration: .62, stagger: .1, ease: 'power3.out' })
      const proxy = { DRAFT: 0, PUBLISHED: 0, ARCHIVED: 0 }
      gsap.to(proxy, {
        ...counts.value, duration: .9, delay: .18, ease: 'power2.out',
        onUpdate: () => { displayCounts.value = { DRAFT: Math.round(proxy.DRAFT), PUBLISHED: Math.round(proxy.PUBLISHED), ARCHIVED: Math.round(proxy.ARCHIVED) } }
      })
    }, desk.value)
  } catch { displayCounts.value = { ...counts.value } }
}

onMounted(async () => {
  try {
    const overview = await getAdminOverview()
    counts.value = {
      DRAFT: overview.drafts,
      PUBLISHED: overview.published,
      ARCHIVED: overview.archived
    }
    dailyNote.value = overview.dailyNote
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    loading.value = false
    await nextTick()
    await animateDesk()
  }
})
onBeforeUnmount(() => animationContext?.revert())
</script>

<template>
  <AdminShell section="OVERVIEW">
    <div ref="desk" class="desk-overview">
    <header class="desk-heading">
      <div class="desk-atmosphere" aria-hidden="true">
        <i class="atmosphere-blue" /><i class="atmosphere-yellow" /><i class="atmosphere-red" />
      </div>
      <div class="desk-copy">
        <p><i /> WELCOME BACK</p>
        <h1>今天，<span>写点什么？</span></h1>
        <p class="desk-intro">这里不追赶更新频率。记下真正想说的，再把它慢慢打磨清楚。</p>
        <RouterLink to="/admin/articles/new">开始一篇新草稿 <span>→</span></RouterLink>
      </div>
      <div class="desk-note-stage">
        <aside class="desk-note">
          <div class="note-toolbar"><span class="window-dots"><i /><i /><i /></span><small>TODAY'S NOTE</small></div>
          <AdminSceneMark class="note-robot" variant="overview" />
          <p class="note-message"><span v-for="(segment, index) in noteSegments" :key="`${index}-${segment}`">{{ segment }}</span></p>
          <small>LEKANG / EDITORIAL DESK</small>
        </aside>
      </div>
    </header>
    <p v-if="errorMessage" class="desk-error" role="alert">{{ errorMessage }}</p>
    <section class="desk-stats" :aria-busy="loading">
      <RouterLink class="stat-card stat-draft" to="/admin/articles?status=DRAFT">
        <span>01 / STILL THINKING</span><strong>{{ loading ? '—' : displayCounts.DRAFT }}</strong><div><p>草稿</p><em>{{ !loading && counts.DRAFT === 0 ? '还没有草稿，现在开始？' : '继续写 →' }}</em></div>
      </RouterLink>
      <RouterLink class="stat-card stat-published" to="/admin/articles?status=PUBLISHED">
        <span>02 / OUT IN THE WORLD</span><strong>{{ loading ? '—' : displayCounts.PUBLISHED }}</strong><div><p>已发布</p><em>{{ !loading && counts.PUBLISHED === 0 ? '第一篇正在路上' : '去看看 →' }}</em></div>
      </RouterLink>
      <RouterLink class="stat-card stat-archived" to="/admin/articles?status=ARCHIVED">
        <span>03 / KEPT SAFE</span><strong>{{ loading ? '—' : displayCounts.ARCHIVED }}</strong><div><p>归档</p><em>{{ !loading && counts.ARCHIVED === 0 ? '这里暂时很清爽' : '翻一翻 →' }}</em></div>
      </RouterLink>
    </section>
    <WritingActivity />
    </div>
  </AdminShell>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;
.desk-heading { position: relative; isolation: isolate; display: grid; grid-template-columns: minmax(0,1.2fr) minmax(300px,.58fr); gap: 70px; align-items: center; padding: 32px 0 68px; }
.desk-copy, .desk-note-stage { z-index: 1; }
.desk-atmosphere { position: absolute; z-index: 0; inset: 5% 0 22%; overflow: hidden; pointer-events: none; mask-image: linear-gradient(to bottom, #000 0 68%, transparent 100%); -webkit-mask-image: linear-gradient(to bottom, #000 0 68%, transparent 100%); }
.desk-atmosphere i { position: absolute; border-radius: 50%; filter: blur(28px); will-change: transform; }
.atmosphere-blue { top: 14%; left: 49%; width: 175px; height: 135px; background: radial-gradient(ellipse, rgba(49,87,213,.11) 0, rgba(49,87,213,.055) 44%, transparent 74%); animation: atmosphere-drift-a 10s ease-in-out infinite alternate; }
.atmosphere-yellow { top: 38%; left: 40%; width: 185px; height: 120px; background: radial-gradient(ellipse, rgba(244,197,66,.12) 0, rgba(244,197,66,.055) 43%, transparent 74%); animation: atmosphere-drift-b 9s ease-in-out infinite alternate; }
.atmosphere-red { top: 32%; left: 58%; width: 155px; height: 112px; background: radial-gradient(ellipse, rgba(240,90,60,.09) 0, rgba(240,90,60,.04) 42%, transparent 74%); animation: atmosphere-drift-c 11s ease-in-out infinite alternate; }
.desk-copy > p:first-child { display: flex; align-items: center; gap: 9px; margin: 0 0 18px; color: $accent; font-size: 10px; font-weight: 750; letter-spacing: .12em; }
.desk-copy > p:first-child i { width: 8px; height: 8px; border-radius: 50%; background: $green; box-shadow: 0 0 0 4px rgba($green,.12); }
h1 { max-width: 800px; margin: 0; font-size: clamp(54px,7vw,88px); font-weight: 680; line-height: .96; letter-spacing: -.065em; }
h1 span { position: relative; color: $accent; white-space: nowrap; }
h1 span::after { position: absolute; z-index: -1; right: 0; bottom: .03em; left: 0; height: .14em; background: $yellow; content: ''; transform: rotate(-1deg); }
.desk-intro { max-width: 560px; margin: 25px 0 0; color: $text-secondary; font-size: 15px; }
.desk-copy a { display: inline-flex; align-items: center; gap: 14px; margin-top: 27px; padding: 12px 17px; border: 1px solid $text-primary; border-radius: 10px; color: #fff; background: $text-primary; font-size: 12px; font-weight: 700; }
.desk-copy a:hover { border-color: $accent; background: $accent; transform: translateY(-2px); }
.desk-copy a span { font-size: 18px; }
.desk-note-stage { position: relative; isolation: isolate; padding: 20px 18px 22px 8px; }
.desk-note { position: relative; display: flex; min-height: 275px; flex-direction: column; padding: 22px 23px; overflow: hidden; border: 1px solid rgba(49,87,213,.16); border-radius: 17px; color: $text-primary; background: #fff; box-shadow: 0 24px 50px -31px rgba(12,68,124,.42); transform: rotate(1.8deg); }
.desk-note::before { position: absolute; top: -3px; right: 24px; width: 42px; height: 10px; border-radius: 2px; background: rgba(244,197,66,.92); content: ''; transform: rotate(-4deg); }
.desk-note::after { position: absolute; right: -3px; bottom: 32px; width: 8px; height: 42px; border-radius: 2px 0 0 2px; background: rgba(240,90,60,.88); content: ''; transform: rotate(2deg); }
.note-toolbar { display: flex; align-items: center; padding-bottom: 14px; border-bottom: 1px solid rgba(49,87,213,.1); }
.window-dots { display: flex; gap: 5px; }
.note-toolbar i { width: 7px; height: 7px; border-radius: 50%; }
.note-toolbar i:nth-child(1) { background: $coral; } .note-toolbar i:nth-child(2) { background: $yellow; } .note-toolbar i:nth-child(3) { background: $green; }
.note-toolbar small { margin-left: auto; color: rgba(49,87,213,.58); font-size: 8px; font-weight: 750; letter-spacing: .12em; }
.note-message { max-width: 84%; margin: auto 0 25px; line-height: 1.18; letter-spacing: -.04em; }
.note-message span { display: inline; font-weight: 730; }
.note-message span:nth-child(1) { color: $text-primary; font-size: clamp(25px,2.6vw,32px); }
.note-message span:nth-child(2) { position: relative; color: $accent; font-size: clamp(31px,3.15vw,39px); font-weight: 760; }
.note-message span:nth-child(2)::after { position: absolute; z-index: -1; right: -.04em; bottom: .03em; left: -.04em; height: .18em; border-radius: 2px; background: rgba($yellow,.72); content: ''; transform: rotate(-1deg); }
.note-message span:nth-child(3) { color: $coral; font-size: clamp(23px,2.4vw,29px); font-weight: 690; }
.note-robot { position: absolute; top: 47px; right: 18px; opacity: .9; transform: rotate(-3deg) scale(.86); transform-origin: top right; }
.desk-note > small { color: rgba(102,112,133,.72); font-size: 8px; font-weight: 750; letter-spacing: .12em; }
@keyframes atmosphere-drift-a { to { transform: translate(-8px,5px) scale(1.03); } }
@keyframes atmosphere-drift-b { to { transform: translate(9px,-4px) scale(.98); } }
@keyframes atmosphere-drift-c { to { transform: translate(-6px,-5px) scale(1.025); } }
.desk-stats { display: grid; grid-template-columns: repeat(3,1fr); gap: 16px; padding-top: 28px; border-top: 1px solid $border; }
.stat-card { position: relative; display: flex; min-height: 245px; flex-direction: column; padding: 24px; overflow: hidden; border: 1px solid $border; border-top: 4px solid $accent; border-radius: 15px; background: #fff; transition: box-shadow .22s ease, transform .22s ease; }
.stat-card::after { position: absolute; right: -32px; bottom: -45px; width: 118px; height: 118px; border: 1px solid rgba(16,24,40,.09); border-radius: 30px; background: rgba($accent,.08); content: ''; transform: rotate(18deg); }
.stat-card:hover, .stat-card:focus-visible { border-color: rgba($accent,.32); box-shadow: 0 18px 36px rgba(16,24,40,.08); outline: none; transform: translateY(-5px) !important; }
.stat-card > span { color: $text-secondary; font-size: 9px; font-weight: 750; letter-spacing: .1em; }
.stat-card strong { margin-top: 25px; font-size: 76px; font-weight: 680; line-height: 1; letter-spacing: -.06em; }
.stat-card > div { display: flex; align-items: center; justify-content: space-between; margin-top: auto; }
.stat-card p { margin: 0; font-size: 14px; font-weight: 700; }.stat-card em { max-width: 150px; color: $text-secondary; font-size: 10px; font-style: normal; font-weight: 700; text-align: right; }
.stat-published { border-top-color: $green; }.stat-published::after { background: rgba($green,.1); }
.stat-archived { border-top-color: $coral; }.stat-archived::after { background: rgba($coral,.1); }
.desk-error { color: $accent; }
@media (max-width: 820px) { .desk-heading { grid-template-columns: 1fr; gap: 42px; }.desk-atmosphere { inset: 4% 0 42%; }.atmosphere-blue { top: 34%; left: 48%; }.atmosphere-yellow { top: 53%; left: 23%; }.atmosphere-red { top: 49%; left: 64%; }.desk-note-stage { max-width: 485px; }.desk-note { max-width: 460px; }.desk-stats { grid-template-columns: 1fr 1fr; }.stat-card:last-child { grid-column: 1 / -1; } }
@media (max-width: 560px) {
  .desk-heading { padding-top: 12px; }.desk-atmosphere { inset: 4% 0 53%; opacity: .58; }.atmosphere-blue { top: 38%; left: 45%; width: 125px; height: 100px; }.atmosphere-yellow { top: 55%; left: 14%; width: 135px; height: 88px; }.atmosphere-red { top: 51%; left: 67%; width: 105px; height: 82px; }.desk-note-stage { padding: 7px 2px 10px; }.desk-note { min-height: 240px; transform: rotate(1deg); }.desk-stats { grid-template-columns: 1fr; }.stat-card:last-child { grid-column: auto; }.stat-card { min-height: 205px; }
}
@media (prefers-reduced-motion: reduce) { .desk-atmosphere i { animation: none; } }
</style>
