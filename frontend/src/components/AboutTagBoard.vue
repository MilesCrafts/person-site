<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const board = ref<HTMLElement | null>(null)
const scattered = ref(false)
let gsapApi: typeof import('gsap')['gsap'] | null = null

const tags = [
  { label: 'PRODUCT', note: '先问为什么', tone: 'blue', x: 178, y: 158, rotation: -4, stackX: 10, stackY: 7, stackRotation: -2 },
  { label: 'CODE', note: '再把它做出来', tone: 'coral', x: -178, y: 158, rotation: 6, stackX: -9, stackY: 7, stackRotation: 1 },
  { label: 'WORDS', note: '把复杂说清楚', tone: 'yellow', x: 178, y: -158, rotation: 4, stackX: 9, stackY: -8, stackRotation: -1 },
  { label: 'NOTES', note: '也把过程留下', tone: 'ink', x: -178, y: -158, rotation: -7, stackX: -8, stackY: -7, stackRotation: 2 }
]

function prefersReducedMotion() {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

async function loadGsap() {
  gsapApi ??= (await import('gsap')).gsap
  return gsapApi
}

async function toggleTags() {
  scattered.value = !scattered.value
  const root = board.value
  if (!root) return
  const nodes = [...root.querySelectorAll<HTMLElement>('.about-play-tag')]
  if (prefersReducedMotion()) return
  try {
    const gsap = await loadGsap()
    const compact = window.matchMedia('(max-width: 640px)').matches
    gsap.to(root.querySelector('.about-play-card'), { opacity: scattered.value ? .08 : 1, scale: scattered.value ? .86 : 1, duration: .48, ease: 'power2.out', overwrite: true })
    nodes.forEach((node, index) => {
      const tag = tags[index]
      gsap.to(node, {
        x: scattered.value ? tag.x * (compact ? .62 : 1) : tag.stackX,
        y: scattered.value ? tag.y * (compact ? .62 : 1) : tag.stackY,
        rotation: scattered.value ? tag.rotation : tag.stackRotation,
        duration: .62,
        delay: index * .045,
        ease: 'back.out(1.7)',
        overwrite: true
      })
    })
  } catch {
    // Vue state and CSS keep the board usable when the animation runtime is unavailable.
  }
}

async function handlePointerMove(event: PointerEvent) {
  const root = board.value
  if (!root || prefersReducedMotion() || event.pointerType === 'touch') return
  const rect = root.getBoundingClientRect()
  const x = (event.clientX - rect.left) / rect.width - .5
  const y = (event.clientY - rect.top) / rect.height - .5
  try {
    const gsap = await loadGsap()
    gsap.to(root.querySelector('.about-play-card'), { x: x * 8, y: y * 7, rotationY: x * 5, rotationX: y * -5, duration: .45, ease: 'power2.out', overwrite: true })
    gsap.to(root.querySelector('.about-play-cursor'), { x: x * 18, y: y * 14, duration: .55, ease: 'power2.out', overwrite: true })
  } catch {
    // Static fallback is intentional.
  }
}

async function resetPointer() {
  const root = board.value
  if (!root || !gsapApi) return
  gsapApi.to([root.querySelector('.about-play-card'), root.querySelector('.about-play-cursor')], { x: 0, y: 0, rotationX: 0, rotationY: 0, duration: .55, ease: 'power3.out', overwrite: true })
}

onMounted(async () => {
  if (!board.value || prefersReducedMotion()) return
  try {
    const gsap = await loadGsap()
    const nodes = [...board.value.querySelectorAll<HTMLElement>('.about-play-tag')]
    nodes.forEach((node, index) => {
      const tag = tags[index]
      gsap.set(node, { x: tag.stackX, y: tag.stackY, rotation: tag.stackRotation })
    })
    gsap.fromTo(nodes, { scale: .82, opacity: 0 }, { scale: 1, opacity: 1, duration: .58, stagger: .08, ease: 'back.out(1.6)', clearProps: 'opacity' })
  } catch {
    board.value.querySelectorAll<HTMLElement>('.about-play-tag').forEach(node => node.removeAttribute('style'))
  }
})

onBeforeUnmount(() => {
  if (!board.value || !gsapApi) return
  gsapApi.killTweensOf(board.value.querySelectorAll('*'))
})
</script>

<template>
  <section
    ref="board"
    class="about-playboard"
    :class="{ 'is-scattered': scattered }"
    aria-label="乐康的关注方向互动卡"
    @pointermove="handlePointerMove"
    @pointerleave="resetPointer"
  >
    <span class="about-play-cursor" aria-hidden="true" />
    <div class="about-play-card">
      <small>PERSONAL / WORKING BOARD</small>
      <div class="about-play-flow"><strong>想法</strong><i>→</i><strong>作品</strong></div>
      <p>问题 · 结构 · 页面</p>
      <span>把模糊一点点收清楚。</span>
    </div>
    <article v-for="tag in tags" :key="tag.label" class="about-play-tag" :class="`tone-${tag.tone}`">
      <strong>{{ tag.label }}</strong><span>{{ tag.note }}</span>
    </article>
    <button type="button" :aria-pressed="scattered" @click="toggleTags">
      {{ scattered ? '收回来' : '散开看看' }} <span aria-hidden="true">↗</span>
    </button>
  </section>
</template>

<style scoped lang="scss">
@use '../assets/styles/variables' as *;

.about-playboard { position: relative; min-height: 500px; overflow: hidden; border: 1px solid $border; border-radius: 18px; background: $surface; perspective: 900px; isolation: isolate; }
.about-playboard::before { position: absolute; inset: 30px; border: 1px dashed rgba($accent,.14); border-radius: 14px; content: ''; }
.about-play-cursor { position: absolute; z-index: -1; top: 45%; left: 46%; width: 170px; height: 170px; border-radius: 50%; background: rgba($accent,.07); filter: blur(1px); }
.about-play-card { position: absolute; z-index: 2; top: 50%; left: 50%; display: flex; width: 270px; min-height: 220px; justify-content: center; flex-direction: column; margin: -110px 0 0 -135px; padding: 28px; border: 1px solid rgba(16,24,40,.14); border-radius: 16px; background: #fff; box-shadow: 0 30px 56px -38px rgba(16,24,40,.6); transform: rotate(-2deg); transform-style: preserve-3d; }
.about-play-card small { color: $coral; font-size: 8px; font-weight: 800; letter-spacing: .12em; }
.about-play-flow { display: flex; align-items: center; gap: 8px; margin-top: 18px; }
.about-play-flow strong { font-size: 34px; line-height: 1; letter-spacing: -.06em; }.about-play-flow strong:last-child { color: $accent; }.about-play-flow i { color: $coral; font-size: 20px; font-style: normal; }
.about-play-card p { margin: 22px 0 6px; color: $accent; font-size: 12px; font-weight: 750; }
.about-play-card > span { color: $text-secondary; font-size: 10px; }
.about-play-tag { position: absolute; z-index: 1; top: 50%; left: 50%; display: flex; width: 270px; min-height: 220px; justify-content: center; flex-direction: column; margin: -110px 0 0 -135px; padding: 28px; border: 1px solid rgba(16,24,40,.12); border-radius: 16px; box-shadow: 0 22px 42px -28px rgba(16,24,40,.6); }
.about-playboard.is-scattered .about-play-tag { z-index: 3; }
.about-play-tag strong { font-size: 14px; letter-spacing: .08em; }.about-play-tag span { margin-top: 12px; font-size: 12px; opacity: .82; }
.about-play-tag:nth-of-type(1) { transform: rotate(-2deg); }.about-play-tag:nth-of-type(2) { transform: rotate(1deg); }.about-play-tag:nth-of-type(3) { transform: rotate(-1deg); }.about-play-tag:nth-of-type(4) { transform: rotate(2deg); }
.tone-blue { color: #fff; background: $accent; }.tone-coral { color: #fff; background: $coral; }.tone-yellow { background: $yellow; }.tone-ink { color: #fff; background: $text-primary; }
.about-playboard > button { position: absolute; z-index: 5; right: 18px; bottom: 17px; padding: 9px 12px; border: 1px solid $text-primary; border-radius: 8px; color: $text-primary; background: #fff; font-size: 9px; font-weight: 750; cursor: pointer; }
.about-playboard > button:hover, .about-playboard > button:focus-visible { outline: 0; color: #fff; background: $text-primary; }
.about-playboard > button span { display: inline-block; margin-left: 7px; transition: transform .2s ease; }.about-playboard > button:hover span { transform: rotate(45deg); }

@media (max-width: 640px) {
  .about-playboard { min-height: 430px; }
  .about-play-card { width: 190px; min-height: 150px; margin: -75px 0 0 -95px; padding: 18px; }
  .about-play-flow strong { font-size: 29px; }
  .about-play-tag { width: 190px; min-height: 150px; margin: -75px 0 0 -95px; padding: 18px; }
  .about-play-tag strong { font-size: 11px; }.about-play-tag span { margin-top: 8px; font-size: 9px; }
}
</style>
