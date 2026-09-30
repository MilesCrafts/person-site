<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const open = ref(false)
const route = useRoute()
const headerRoot = ref<HTMLElement | null>(null)
const headerInner = ref<HTMLElement | null>(null)
const navRoot = ref<HTMLElement | null>(null)
const navIndicator = ref<HTMLElement | null>(null)
let gsapApi: typeof import('gsap')['gsap'] | null = null

const positionIndicator = (immediate = false) => {
  if (!gsapApi || !navRoot.value || !navIndicator.value) return
  const active = navRoot.value.querySelector<HTMLElement>('.router-link-active')
  if (!active || window.innerWidth <= 640) {
    gsapApi.to(navIndicator.value, { opacity: 0, duration: immediate ? 0 : .2 })
    return
  }
  gsapApi.to(navIndicator.value, {
    x: active.offsetLeft,
    width: active.offsetWidth,
    opacity: 1,
    duration: immediate ? 0 : .42,
    ease: 'power3.out'
  })
}

const updateHeader = () => {
  if (!gsapApi || !headerInner.value) return
  const compact = window.scrollY > 18 && window.innerWidth > 640
  headerRoot.value?.classList.toggle('is-compact', compact)
  gsapApi.to(headerInner.value, {
    borderRadius: compact ? 14 : 0,
    boxShadow: compact ? '0 12px 32px rgba(16,24,40,.10)' : '0 0 0 rgba(16,24,40,0)',
    scale: compact ? .992 : 1,
    duration: .34,
    ease: 'power2.out',
    overwrite: true
  })
}
const handleResize = () => { positionIndicator(); updateHeader() }

watch(() => route.fullPath, async () => {
  open.value = false
  await nextTick()
  positionIndicator()
})

onMounted(async () => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  try {
    const { gsap } = await import('gsap')
    gsapApi = gsap
    await nextTick()
    const entrance = [headerInner.value?.querySelector('.wordmark'), navRoot.value].filter(Boolean)
    if (entrance.length) gsap.from(entrance, { y: -10, opacity: 0, duration: .5, stagger: .08, ease: 'power3.out', clearProps: 'transform,opacity' })
    positionIndicator(true)
    updateHeader()
    window.addEventListener('scroll', updateHeader, { passive: true })
    window.addEventListener('resize', handleResize)
  } catch {
    // Static navigation remains available when animation cannot load.
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateHeader)
  window.removeEventListener('resize', handleResize)
  gsapApi?.killTweensOf([headerInner.value, navRoot.value, navIndicator.value])
})
</script>

<template>
  <header ref="headerRoot" class="site-header">
    <div class="header-color-strip" aria-hidden="true"><i></i><i></i><i></i><i></i></div>
    <div ref="headerInner" class="site-header-inner container">
      <RouterLink class="wordmark" to="/" aria-label="LEKANG 首页">
        <strong class="wordmark-signature" aria-hidden="true">Lekang</strong>
        <span>产品、开发与持续记录</span>
      </RouterLink>
      <button class="menu-button" :aria-expanded="open" aria-label="切换导航" @click="open = !open">
        <span></span><span></span>
      </button>
      <nav ref="navRoot" :class="{ open }" aria-label="主导航">
        <span ref="navIndicator" class="nav-indicator" aria-hidden="true"></span>
        <RouterLink to="/works">作品</RouterLink>
        <RouterLink to="/capabilities">能力</RouterLink>
        <RouterLink to="/journal">文章</RouterLink>
        <RouterLink to="/message">留言</RouterLink>
        <RouterLink to="/about">关于</RouterLink>
        <RouterLink class="nav-search" to="/search" aria-label="搜索文章"><span aria-hidden="true"></span>搜索</RouterLink>
      </nav>
    </div>
  </header>
</template>
