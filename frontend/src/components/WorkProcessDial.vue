<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'

const steps = [
  {
    label: '01 / WHY',
    title: '先把散落的东西，收回自己的房间。',
    body: '文章不必追着平台节奏跑。它可以慢慢写、反复改，也能在几年后被自己重新找到。',
    note: '问题不是“缺一个页面”，而是缺一个愿意长期维护的地方。'
  },
  {
    label: '02 / BUILD',
    title: '公开阅读和后台写作，是同一件事的两面。',
    body: '读者看到安静的文章页，我在另一边完成草稿、预览、发布、撤回和归档，让更新不靠手工搬运。',
    note: '界面可以轻，但内容流转不能只停在展示稿。'
  },
  {
    label: '03 / CHECK',
    title: '最后让真实浏览器替我挑错。',
    body: '公开页面接真实 API，在桌面、平板和手机逐一检查路由、内容状态与横向溢出，再把发现的问题改回去。',
    note: '目前完成 24 组路由 × 视口验收；真实流量数据仍在积累。'
  }
]

const activeIndex = ref(0)
const marker = ref<HTMLElement | null>(null)
const content = ref<HTMLElement | null>(null)
let gsapApi: typeof import('gsap')['gsap'] | null = null

async function animateTo(index: number, initial = false) {
  await nextTick()
  if (!marker.value || !content.value) return
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    marker.value.style.transform = `translateX(${index * 100}%)`
    return
  }
  try {
    gsapApi ??= (await import('gsap')).gsap
    gsapApi.to(marker.value, { xPercent: index * 100, duration: initial ? 0 : .48, ease: 'power3.out', overwrite: true })
    if (!initial) gsapApi.fromTo(content.value.children, { y: 12, opacity: 0 }, { y: 0, opacity: 1, duration: .42, stagger: .045, ease: 'power2.out', clearProps: 'transform,opacity' })
  } catch {
    marker.value.style.transform = `translateX(${index * 100}%)`
  }
}

watch(activeIndex, index => void animateTo(index))
onMounted(() => void animateTo(0, true))
</script>

<template>
  <section class="work-process-dial" aria-labelledby="work-process-title">
    <div class="dial-heading">
      <div>
        <p class="eyebrow">PROJECT STORY / 点击切换</p>
        <h2 id="work-process-title">它不是一下子长成的。</h2>
      </div>
      <p>从一个很私人的念头，到能写、能读、也经得起检查的完整小产品。</p>
    </div>

    <div class="dial-tabs" role="tablist" aria-label="项目过程">
      <span ref="marker" class="dial-marker" aria-hidden="true" />
      <button
        v-for="(step, index) in steps"
        :id="`work-step-${index}`"
        :key="step.label"
        type="button"
        role="tab"
        :aria-selected="activeIndex === index"
        :aria-controls="'work-process-panel'"
        @click="activeIndex = index"
      >
        {{ step.label }}
      </button>
    </div>

    <div id="work-process-panel" ref="content" class="dial-content" role="tabpanel" :aria-labelledby="`work-step-${activeIndex}`">
      <span>{{ String(activeIndex + 1).padStart(2, '0') }}</span>
      <h3>{{ steps[activeIndex].title }}</h3>
      <p>{{ steps[activeIndex].body }}</p>
      <small>{{ steps[activeIndex].note }}</small>
    </div>
  </section>
</template>

<style scoped lang="scss">
@use '../assets/styles/variables' as *;

.work-process-dial { margin: 76px 0; padding: 42px; overflow: hidden; border: 1px solid $border; border-radius: 18px; background: #fff; }
.dial-heading { display: grid; grid-template-columns: 1.2fr .8fr; gap: 60px; align-items: end; padding-bottom: 34px; }
.dial-heading h2 { max-width: 620px; margin: 0; font-size: clamp(36px, 5vw, 58px); line-height: 1.03; letter-spacing: -.05em; }
.dial-heading > p { max-width: 410px; margin: 0; color: $text-secondary; font-size: 14px; }
.dial-tabs { position: relative; display: grid; grid-template-columns: repeat(3, 1fr); padding: 4px; border: 1px solid $border; border-radius: 12px; background: $surface; }
.dial-marker { position: absolute; top: 4px; bottom: 4px; left: 4px; width: calc((100% - 8px) / 3); border: 1px solid rgba($accent,.16); border-radius: 9px; background: #fff; box-shadow: 0 8px 24px -18px rgba(16,24,40,.6); }
.dial-tabs button { position: relative; z-index: 1; min-height: 46px; border: 0; color: $text-secondary; background: transparent; font-size: 10px; font-weight: 800; letter-spacing: .11em; cursor: pointer; }
.dial-tabs button[aria-selected='true'] { color: $accent; }
.dial-tabs button:focus-visible { outline: 2px solid $accent; outline-offset: -4px; border-radius: 9px; }
.dial-content { display: grid; grid-template-columns: 90px minmax(260px,.9fr) minmax(280px,1.1fr); gap: 34px; align-items: start; min-height: 250px; margin-top: 18px; padding: 40px; border-radius: 14px; color: #fff; background: $text-primary; }
.dial-content > span { color: $yellow; font-size: 42px; font-weight: 680; letter-spacing: -.06em; }
.dial-content h3 { margin: 0; font-size: clamp(27px,3vw,40px); line-height: 1.12; letter-spacing: -.04em; }
.dial-content p { margin: 0; color: rgba(255,255,255,.72); font-size: 15px; line-height: 1.8; }
.dial-content small { grid-column: 3; padding-top: 18px; border-top: 1px solid rgba(255,255,255,.15); color: $yellow; font-size: 10px; line-height: 1.6; }

@media (max-width: 760px) {
  .work-process-dial { margin: 52px 0; padding: 24px; }
  .dial-heading { grid-template-columns: 1fr; gap: 18px; }
  .dial-heading h2 { font-size: 38px; }
  .dial-content { grid-template-columns: 1fr; gap: 20px; min-height: 360px; padding: 28px 24px; }
  .dial-content > span { font-size: 30px; }
  .dial-content small { grid-column: auto; }
}
</style>
