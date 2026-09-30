<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { resolveMediaUrl } from '../data/media'
import type { ArticleSummary } from '../types/api'
import { formatCategory, formatPublishedDate, formatReadTime } from '../utils/content'
import ArticleCard from './ArticleCard.vue'

const props = withDefaults(defineProps<{ articles: ArticleSummary[]; curated?: boolean }>(), {
  curated: false
})
const router = useRouter()

const activeIndex = ref(0)
const sectionRoot = ref<HTMLElement | null>(null)
const cards = ref<HTMLElement[]>([])
const reducedMotion = ref(false)
const ready = ref(false)
const animating = ref(false)
const visibleArticles = computed(() => props.articles.slice(0, 5))
let gsapApi: typeof import('gsap')['gsap'] | null = null
let mediaQuery: MediaQueryList | null = null
let revealObserver: IntersectionObserver | null = null
let revealed = false
let dragStartX: number | null = null
let dragCurrentX = 0
let suppressCardClick = false

const setCardRef = (element: unknown, index: number) => {
  if (element instanceof HTMLElement) cards.value[index] = element
}

const getOrder = (index: number) => {
  const length = visibleArticles.value.length
  return length ? (index - activeIndex.value + length) % length : 0
}

const positionCards = (immediate = false) => {
  if (!gsapApi || reducedMotion.value) return
  cards.value.forEach((card, index) => {
    const order = getOrder(index)
    const visible = order < 3
    gsapApi?.to(card, {
      x: visible ? order * 18 : 42,
      y: visible ? order * 15 : 26,
      scale: visible ? 1 - order * 0.045 : 0.88,
      rotate: visible ? order * 1.1 : 2.5,
      opacity: visible ? 1 : 0,
      duration: immediate ? 0 : 0.55,
      ease: 'power3.out',
      overwrite: true
    })
    card.style.zIndex = String(visibleArticles.value.length - order)
    card.style.pointerEvents = order === 0 ? 'auto' : 'none'
  })
}

const prepareReveal = () => {
  if (!gsapApi || reducedMotion.value || !cards.value.length) return
  gsapApi.set(cards.value, { x: 72, y: 54, scale: 0.93, rotate: 4, opacity: 0 })
  cards.value.forEach((card, index) => {
    card.style.zIndex = String(visibleArticles.value.length - getOrder(index))
    card.style.pointerEvents = getOrder(index) === 0 ? 'auto' : 'none'
  })
}

const revealDeck = () => {
  if (revealed || !gsapApi || reducedMotion.value) return
  revealed = true
  const heading = sectionRoot.value?.querySelector('.section-intro')
  if (heading) {
    gsapApi.fromTo(heading, { y: 18, opacity: 0 }, { y: 0, opacity: 1, duration: 0.55, ease: 'power2.out' })
  }
  positionCards()
}

const move = (direction: 1 | -1) => {
  const length = visibleArticles.value.length
  if (length < 2 || animating.value || reducedMotion.value || !gsapApi) {
    if (length > 1) activeIndex.value = (activeIndex.value + direction + length) % length
    return
  }

  const current = cards.value[activeIndex.value]
  if (!current) return
  animating.value = true
  const nextIndex = (activeIndex.value + direction + length) % length
  gsapApi.to(current, {
    x: direction * -140,
    y: -8,
    rotate: direction * -5,
    opacity: 0,
    duration: 0.32,
    ease: 'power2.in',
    onComplete: () => {
      activeIndex.value = nextIndex
      nextTick(() => {
        positionCards()
        const next = cards.value[nextIndex]
        const copy = next?.querySelectorAll('.deck-card-copy > *')
        const media = next?.querySelector('.deck-card-media')
        if (copy?.length) {
          gsapApi?.fromTo(copy, { y: 18, opacity: 0 }, {
            y: 0, opacity: 1, duration: .48, stagger: .045, ease: 'power3.out', overwrite: true
          })
        }
        if (media) {
          gsapApi?.fromTo(media, { clipPath: 'inset(0 32% 0 0 round 9px)' }, {
            clipPath: 'inset(0 0% 0 0 round 9px)', duration: .62, ease: 'power3.inOut', clearProps: 'clipPath'
          })
        }
        animating.value = false
      })
    }
  })
}

const onPointerDown = (event: PointerEvent) => {
  if (reducedMotion.value || animating.value) return
  if ((event.target as HTMLElement).closest('a, button')) return
  dragStartX = event.clientX
  dragCurrentX = 0
  suppressCardClick = false
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

const onPointerMove = (event: PointerEvent) => {
  if (dragStartX === null || !gsapApi) return
  dragCurrentX = event.clientX - dragStartX
  if (Math.abs(dragCurrentX) > 6) suppressCardClick = true
  const current = cards.value[activeIndex.value]
  if (current) gsapApi.set(current, { x: dragCurrentX, rotate: dragCurrentX * 0.025 })
}

const onPointerUp = () => {
  if (dragStartX === null) return
  const distance = dragCurrentX
  dragStartX = null
  dragCurrentX = 0
  if (Math.abs(distance) > 60) move(distance < 0 ? 1 : -1)
  else positionCards()
  window.setTimeout(() => { suppressCardClick = false }, 0)
}

const openArticle = (article: ArticleSummary, index: number, event: MouseEvent) => {
  if (index !== activeIndex.value || suppressCardClick) return
  if ((event.target as HTMLElement).closest('a, button')) return
  void router.push(`/article/${article.slug}`)
}

const onMotionChange = (event: MediaQueryListEvent) => {
  reducedMotion.value = event.matches
  if (!event.matches) nextTick(() => positionCards(true))
}

watch(() => props.articles, () => {
  activeIndex.value = 0
  cards.value = []
  nextTick(() => {
    if (revealed) positionCards(true)
    else prepareReveal()
  })
})

onMounted(async () => {
  mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  reducedMotion.value = mediaQuery.matches
  mediaQuery.addEventListener('change', onMotionChange)
  if (!reducedMotion.value) {
    try {
      const module = await import('gsap')
      gsapApi = module.gsap
      ready.value = true
      await nextTick()
      prepareReveal()
      if ('IntersectionObserver' in window && sectionRoot.value) {
        revealObserver = new IntersectionObserver(entries => {
          if (entries.some(entry => entry.isIntersecting)) {
            revealDeck()
            revealObserver?.disconnect()
          }
        }, { threshold: 0.18 })
        revealObserver.observe(sectionRoot.value)
      } else {
        revealDeck()
      }
    } catch {
      reducedMotion.value = true
    }
  }
  ready.value = true
})

onBeforeUnmount(() => {
  mediaQuery?.removeEventListener('change', onMotionChange)
  revealObserver?.disconnect()
  gsapApi?.killTweensOf(cards.value)
})
</script>

<template>
  <section v-if="visibleArticles.length" ref="sectionRoot" class="article-deck-section" aria-labelledby="article-deck-title">
    <div class="section-intro">
      <div><p class="eyebrow">{{ curated ? '编辑精选' : '随手翻一篇' }}</p><h2 id="article-deck-title">{{ curated ? '值得留在桌面的文章' : '最近写下的文章' }}</h2></div>
      <p>{{ curated ? '像翻动桌上的稿纸一样，慢慢读过这些特意留下的篇章。' : '拖动卡片，或者使用按钮，在最近更新里继续浏览。' }}</p>
    </div>

    <div v-if="reducedMotion || !ready" class="deck-static">
      <ArticleCard v-for="article in visibleArticles" :key="article.slug" :article="article" />
    </div>

    <div v-else class="article-deck" tabindex="0" aria-label="最近文章卡片叠，使用左右方向键切换" @keydown.left.prevent="move(-1)" @keydown.right.prevent="move(1)">
      <div class="deck-stage">
        <article
          v-for="(article, index) in visibleArticles"
          :key="article.slug"
          :ref="element => setCardRef(element, index)"
          class="deck-card"
          :class="{ active: index === activeIndex }"
          :aria-hidden="index !== activeIndex"
          @pointerdown="onPointerDown"
          @pointermove="onPointerMove"
          @pointerup="onPointerUp"
          @pointercancel="onPointerUp"
          @click="openArticle(article, index, $event)"
        >
          <span v-if="curated" class="deck-paper-tab" aria-hidden="true">精选 {{ String(index + 1).padStart(2, '0') }}</span>
          <div class="deck-card-copy">
            <p class="eyebrow">{{ formatCategory(article.category) }} / {{ article.topic }}</p>
            <h3>{{ article.title }}</h3>
            <p>{{ article.excerpt }}</p>
            <div class="deck-card-meta"><span>{{ formatPublishedDate(article.publishedAt) }}</span><span>{{ formatReadTime(article.readMinutes) }}</span></div>
            <RouterLink :tabindex="index === activeIndex ? 0 : -1" :to="`/article/${article.slug}`">阅读全文 <span aria-hidden="true">→</span></RouterLink>
          </div>
          <div class="deck-card-media">
            <img v-if="resolveMediaUrl(article.imageUrl)" :src="resolveMediaUrl(article.imageUrl)!" :alt="article.imageAlt || article.title" draggable="false" />
            <span v-else class="image-placeholder">LEKANG</span>
          </div>
        </article>
      </div>
      <div class="deck-controls">
        <div class="deck-status"><p><strong>{{ String(activeIndex + 1).padStart(2, '0') }}</strong> / {{ String(visibleArticles.length).padStart(2, '0') }}</p><span><i aria-hidden="true">↔</i> 拖动卡片</span></div>
        <div><button type="button" aria-label="上一篇" @click="move(-1)">←</button><button type="button" aria-label="下一篇" @click="move(1)">→</button></div>
      </div>
    </div>
  </section>
</template>
