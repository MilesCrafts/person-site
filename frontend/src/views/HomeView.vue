<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { getArticles, getHome, getTaxonomies } from '../api/journal'
import { getErrorMessage } from '../api/client'
import ArticleCard from '../components/ArticleCard.vue'
import ArticleDeck from '../components/ArticleDeck.vue'
import AsyncState from '../components/AsyncState.vue'
import CategoryTabs from '../components/CategoryTabs.vue'
import HeroStory from '../components/HeroStory.vue'
import type { Home, PageResponse, ArticleSummary, Taxonomy } from '../types/api'
import { setPageMeta } from '../utils/content'

const activeCategory = ref('ALL')
const activeTopic = ref<string | null>(null)
const currentPage = ref(0)
const pageSize = 6
const articleGrid = ref<HTMLElement | null>(null)
const homeHero = ref<HTMLElement | null>(null)
const progressBar = ref<HTMLElement | null>(null)
const featuredWork = ref<HTMLElement | null>(null)
const home = ref<Home | null>(null)
const taxonomies = ref<Taxonomy[]>([])
const articlePage = ref<PageResponse<ArticleSummary> | null>(null)
const homeLoading = ref(true)
const homeError = ref<string | null>(null)
const articlesLoading = ref(true)
const articlesError = ref<string | null>(null)
let homeController: AbortController | undefined
let articlesController: AbortController | undefined
const motionCleanups: Array<() => void> = []
const currentHeroSlide = ref(0)
const heroCarouselPaused = ref(false)
let heroCarouselTimer: number | undefined

const defaultHeroCopy = {
  eyebrow: 'LEKANG · 产品、开发与记录',
  headlinePrimary: '把想法',
  headlineEmphasis: '理清楚，',
  headlineAccent: '再做出来。',
  description: '关注产品体验、前端开发和内容系统，也持续记录阅读、电影与工作之外的日常。',
  paperLabel: '正在搭建',
  paperLineOne: '让文章有地方住，',
  paperLineTwo: '让作品慢慢长，',
  paperLineThree: '也让我持续更新。',
  paperFooter: 'BUILDING IN PUBLIC'
}
const heroCopy = computed(() => ({
  ...defaultHeroCopy,
  ...(home.value?.heroCopy ?? {})
}))

const totalPages = computed(() => articlePage.value?.totalPages ?? 0)
const deckArticles = computed(() => {
  const selected = home.value?.features ?? []
  if (selected.length) return selected.slice(0, 5)
  const featureSlug = home.value?.feature?.slug
  const candidates = [...(home.value?.latestStories ?? []), ...(articlePage.value?.items ?? [])]
  return candidates.filter((item, index) =>
    item.slug !== featureSlug && candidates.findIndex(candidate => candidate.slug === item.slug) === index
  ).slice(0, 5)
})
const contactEmail = computed(() => home.value?.profile.email?.trim() ?? '')
const heroSlides = computed(() => {
  const latest = home.value?.latestStories?.[0] ?? home.value?.feature
  return [
    {
      label: '最近在忙',
      lines: ['一边琢磨，', '一边动手，', '顺便记下来。'],
      footer: "COME SEE WHAT I'M MAKING",
      to: '/works'
    },
    {
      label: heroCopy.value.paperLabel,
      lines: [heroCopy.value.paperLineOne, heroCopy.value.paperLineTwo, heroCopy.value.paperLineThree],
      footer: heroCopy.value.paperFooter,
      to: '/capabilities'
    },
    latest
      ? {
          label: '刚刚写下',
          lines: [latest.title],
          footer: 'OPEN THE LATEST NOTE',
          to: `/article/${latest.slug}`
        }
      : {
          label: '继续记录',
          lines: ['慢一点读，', '也慢一点忘记。'],
          footer: 'READ THE JOURNAL',
          to: '/journal'
        }
  ]
})

function stopHeroCarousel() {
  window.clearInterval(heroCarouselTimer)
  heroCarouselTimer = undefined
}

function startHeroCarousel() {
  stopHeroCarousel()
  if (heroCarouselPaused.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  heroCarouselTimer = window.setInterval(() => {
    currentHeroSlide.value = (currentHeroSlide.value + 1) % heroSlides.value.length
  }, 5600)
}

function goToHeroSlide(index: number) {
  currentHeroSlide.value = (index + heroSlides.value.length) % heroSlides.value.length
  startHeroCarousel()
}

function pauseHeroCarousel() {
  heroCarouselPaused.value = true
  stopHeroCarousel()
}

function resumeHeroCarousel() {
  heroCarouselPaused.value = false
  startHeroCarousel()
}

function handleHeroFocusOut(event: FocusEvent) {
  const next = event.relatedTarget
  if (next instanceof Node && (event.currentTarget as HTMLElement).contains(next)) return
  resumeHeroCarousel()
}
async function setupHomeMotion() {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  try {
    const { gsap } = await import('gsap')
    if (!homeHero.value) return

    const copyItems = homeHero.value.querySelectorAll('.home-intro-copy > *')
    const pieces = homeHero.value.querySelectorAll<HTMLElement>('[data-float]')
    gsap.fromTo(copyItems, { y: 22, opacity: 0 }, {
      y: 0,
      opacity: 1,
      duration: .72,
      stagger: .07,
      ease: 'power3.out'
    })
    gsap.fromTo(pieces, { y: 28, opacity: 0, scale: .94 }, {
      y: 0,
      opacity: 1,
      scale: 1,
      duration: .82,
      stagger: .08,
      ease: 'back.out(1.4)'
    })

    const setters = [...pieces].map((piece, index) => ({
      x: gsap.quickTo(piece, 'x', { duration: .65, ease: 'power3.out' }),
      y: gsap.quickTo(piece, 'y', { duration: .65, ease: 'power3.out' }),
      depth: Number(piece.dataset.float ?? index + 1)
    }))
    const onPointerMove = (event: PointerEvent) => {
      if (!homeHero.value) return
      const bounds = homeHero.value.getBoundingClientRect()
      const x = (event.clientX - bounds.left) / bounds.width - .5
      const y = (event.clientY - bounds.top) / bounds.height - .5
      setters.forEach(setter => {
        setter.x(x * setter.depth * 8)
        setter.y(y * setter.depth * 6)
      })
    }
    const onPointerLeave = () => setters.forEach(setter => { setter.x(0); setter.y(0) })
    homeHero.value.addEventListener('pointermove', onPointerMove)
    homeHero.value.addEventListener('pointerleave', onPointerLeave)
    motionCleanups.push(() => homeHero.value?.removeEventListener('pointermove', onPointerMove))
    motionCleanups.push(() => homeHero.value?.removeEventListener('pointerleave', onPointerLeave))

    if (featuredWork.value && window.matchMedia('(hover: hover) and (pointer: fine)').matches) {
      const magneticX = gsap.quickTo(featuredWork.value, 'x', { duration: .55, ease: 'power3.out' })
      const magneticY = gsap.quickTo(featuredWork.value, 'y', { duration: .55, ease: 'power3.out' })
      const onFeaturedMove = (event: PointerEvent) => {
        if (!featuredWork.value) return
        const bounds = featuredWork.value.getBoundingClientRect()
        const offsetX = (event.clientX - (bounds.left + bounds.width / 2)) * .3
        const offsetY = (event.clientY - (bounds.top + bounds.height / 2)) * .3
        magneticX(Math.max(-18, Math.min(18, offsetX)))
        magneticY(Math.max(-14, Math.min(14, offsetY)))
      }
      const onFeaturedLeave = () => { magneticX(0); magneticY(0) }
      featuredWork.value.addEventListener('pointermove', onFeaturedMove)
      featuredWork.value.addEventListener('pointerleave', onFeaturedLeave)
      motionCleanups.push(() => featuredWork.value?.removeEventListener('pointermove', onFeaturedMove))
      motionCleanups.push(() => featuredWork.value?.removeEventListener('pointerleave', onFeaturedLeave))
      motionCleanups.push(() => gsap.set(featuredWork.value, { clearProps: 'x,y' }))
    }

    const setProgress = gsap.quickSetter(progressBar.value, 'scaleX')
    const onScroll = () => {
      const available = document.documentElement.scrollHeight - window.innerHeight
      setProgress(available > 0 ? Math.min(1, window.scrollY / available) : 0)
    }
    window.addEventListener('scroll', onScroll, { passive: true })
    onScroll()
    motionCleanups.push(() => window.removeEventListener('scroll', onScroll))

    const revealTargets = document.querySelectorAll<HTMLElement>('[data-home-reveal]')
    gsap.set(revealTargets, { y: 28, opacity: 0 })
    if ('IntersectionObserver' in window) {
      const revealObserver = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (!entry.isIntersecting) return
          gsap.to(entry.target, { y: 0, opacity: 1, duration: .68, ease: 'power3.out' })
          revealObserver.unobserve(entry.target)
        })
      }, { threshold: .12 })
      revealTargets.forEach(target => revealObserver.observe(target))
      motionCleanups.push(() => revealObserver.disconnect())
    } else {
      gsap.set(revealTargets, { y: 0, opacity: 1 })
    }
  } catch {
    // The page remains fully usable when animation cannot be loaded.
  }
}

async function loadHome() {
  homeController?.abort()
  const requestController = new AbortController()
  homeController = requestController
  homeLoading.value = true
  homeError.value = null
  try {
    const [homeResponse, taxonomyResponse] = await Promise.all([
      getHome(requestController.signal),
      getTaxonomies(requestController.signal)
    ])
    home.value = homeResponse
    taxonomies.value = taxonomyResponse
    setPageMeta('LEKANG', '乐康的个人网站：产品实践、前端开发、内容系统与日常记录。')
  } catch (caught) {
    if (caught instanceof DOMException && caught.name === 'AbortError') return
    homeError.value = getErrorMessage(caught)
  } finally {
    if (!requestController.signal.aborted) homeLoading.value = false
  }
}

async function loadArticles() {
  articlesController?.abort()
  const requestController = new AbortController()
  articlesController = requestController
  articlesLoading.value = true
  articlesError.value = null
  try {
    articlePage.value = await getArticles({
      page: currentPage.value,
      size: pageSize,
      category: activeCategory.value === 'ALL' ? null : activeCategory.value,
      topic: activeTopic.value,
      signal: requestController.signal
    })
  } catch (caught) {
    if (caught instanceof DOMException && caught.name === 'AbortError') return
    articlesError.value = getErrorMessage(caught)
  } finally {
    if (!requestController.signal.aborted) articlesLoading.value = false
  }
}

const setPage = (page: number) => {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) return
  currentPage.value = page
  articleGrid.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const selectCategory = (value: string) => {
  activeCategory.value = value
  activeTopic.value = null
  currentPage.value = 0
}
const selectTopic = (value: string | null) => {
  activeTopic.value = value
  currentPage.value = 0
}

watch([activeCategory, activeTopic, currentPage], loadArticles, { immediate: true })
onMounted(() => {
  loadHome()
  setupHomeMotion()
  startHeroCarousel()
})
onBeforeUnmount(() => {
  homeController?.abort()
  articlesController?.abort()
  stopHeroCarousel()
  motionCleanups.splice(0).forEach(cleanup => cleanup())
})
</script>

<template>
  <div class="home container">
    <div class="home-scroll-progress" aria-hidden="true"><span ref="progressBar"></span></div>
    <header ref="homeHero" class="home-intro">
      <div class="home-intro-copy">
        <p class="eyebrow"><i aria-hidden="true"></i> {{ heroCopy.eyebrow }}</p>
        <h1>{{ heroCopy.headlinePrimary }}<br /><span class="title-mark">{{ heroCopy.headlineEmphasis }}</span><br /><span class="title-blue">{{ heroCopy.headlineAccent }}</span></h1>
        <p>{{ heroCopy.description }}</p>
        <div class="home-actions">
          <RouterLink class="home-primary-action" to="/works">查看作品 <span aria-hidden="true">→</span></RouterLink>
          <RouterLink class="home-secondary-action" to="/capabilities">了解能力 <span aria-hidden="true">↗</span></RouterLink>
        </div>
        <div class="home-stickers" aria-label="网站主要内容">
          <span>WORK</span><span>CAPABILITIES</span><span>ARTICLES</span><span>DAILY</span>
        </div>
      </div>
      <div
        class="home-playground"
        role="region"
        aria-roledescription="轮播图"
        aria-label="网站近况"
        @mouseenter="pauseHeroCarousel"
        @mouseleave="resumeHeroCarousel"
        @focusin="pauseHeroCarousel"
        @focusout="handleHeroFocusOut"
      >
        <div class="play-shape play-shape-blue" data-float="2" aria-hidden="true"></div>
        <div class="play-shape play-shape-coral" data-float="3" aria-hidden="true"></div>
        <div class="play-shape play-shape-yellow" data-float="4" aria-hidden="true"></div>
        <div class="play-paper" data-float="1">
          <div class="play-paper-top"><i></i><i></i><i></i><span>LEKANG / INDEX {{ String(currentHeroSlide + 1).padStart(2, '0') }}</span></div>
          <Transition name="paper-slide" mode="out-in">
            <div :key="currentHeroSlide" class="play-paper-content" :aria-live="heroCarouselPaused ? 'polite' : 'off'">
              <p>{{ heroSlides[currentHeroSlide].label }}</p>
              <strong><span v-for="line in heroSlides[currentHeroSlide].lines" :key="line">{{ line }}</span></strong>
              <div class="play-lines" aria-hidden="true"><span></span><span></span><span></span></div>
            </div>
          </Transition>
          <div class="play-paper-footer">
            <RouterLink :to="heroSlides[currentHeroSlide].to">{{ heroSlides[currentHeroSlide].footer }} <span aria-hidden="true">↗</span></RouterLink>
            <div class="play-carousel-controls">
              <button type="button" aria-label="上一张" @click="goToHeroSlide(currentHeroSlide - 1)">←</button>
              <span class="play-carousel-dots" aria-label="选择轮播内容">
                <button
                  v-for="(_, index) in heroSlides"
                  :key="index"
                  type="button"
                  :class="{ active: currentHeroSlide === index }"
                  :aria-label="`查看第 ${index + 1} 张`"
                  :aria-current="currentHeroSlide === index ? 'true' : undefined"
                  @click="goToHeroSlide(index)"
                />
              </span>
              <button type="button" aria-label="下一张" @click="goToHeroSlide(currentHeroSlide + 1)">→</button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <section id="works" class="portfolio-section" aria-labelledby="works-title">
      <div class="portfolio-heading" data-home-reveal>
        <div><p class="eyebrow">01 / SELECTED WORK</p><h2 id="works-title">作品不是陈列，<br />而是解决问题的过程。</h2></div>
        <p>先展示这个网站中已经完成、可以被验证的工作。后续项目可以沿用同一结构继续补充背景、约束、过程与结果。</p>
      </div>
      <div ref="featuredWork" class="work-showcase" data-home-reveal>
        <RouterLink class="work-visual" to="/journal" aria-label="查看 LEKANG JOURNAL 文章内容">
          <div class="work-window">
            <div class="work-window-bar"><i></i><i></i><i></i><span>journal.lekang.site</span></div>
            <div class="work-window-body"><strong>LEKANG</strong><span></span><span></span><span></span></div>
          </div>
          <span class="work-visual-label">LIVE PROJECT ↗</span>
        </RouterLink>
        <div class="work-copy">
          <p class="work-index">01</p>
          <p class="eyebrow">个人内容平台 · 2026</p>
          <h3>LEKANG JOURNAL</h3>
          <p>从公开阅读体验到管理端文章工作流，把个人表达、内容组织和长期维护放在同一个系统里。</p>
          <ul>
            <li>Vue 3 / TypeScript 响应式前端</li>
            <li>Spring Boot / PostgreSQL 内容接口</li>
            <li>文章编辑、发布、搜索与归档闭环</li>
          </ul>
          <RouterLink class="primary-link" to="/journal">查看项目内容 <span aria-hidden="true">→</span></RouterLink>
        </div>
      </div>
      <div class="work-secondary" data-home-reveal>
        <div><span>02</span><p class="eyebrow">SYSTEM / WORKFLOW</p><h3>内容管理后台</h3></div>
        <p>围绕真实发布流程实现登录、草稿、Markdown 预览、发布、撤回、归档与冲突处理，而不是只停留在静态页面。</p>
        <div class="work-tags"><span>SESSION</span><span>CSRF</span><span>EDITOR</span><span>API</span></div>
      </div>
    </section>

    <AsyncState v-if="homeLoading || homeError" :loading="homeLoading" :error="homeError" @retry="loadHome" />
    <template v-else-if="home">
      <ArticleDeck :articles="deckArticles" :curated="Boolean(home.features?.length)" />
      <HeroStory v-if="home.feature && !home.features?.length" :article="home.feature" />
    </template>
    <div id="articles" class="stories-heading"><div><p class="eyebrow">文章</p><h2>全部更新</h2></div><p>从最近发布开始，按分类找到想读的内容。</p></div>
    <CategoryTabs
      :taxonomies="taxonomies"
      :model-value="activeCategory"
      :topic="activeTopic"
      @update:model-value="selectCategory"
      @update:topic="selectTopic"
    />
    <div ref="articleGrid">
      <AsyncState
        v-if="articlesLoading || articlesError || !articlePage?.items.length"
        :loading="articlesLoading"
        :error="articlesError"
        :empty="!articlesLoading && !articlesError && articlePage?.items.length === 0"
        empty-title="这个栏目还没有文章。"
        empty-hint="可以切换分类或主题继续浏览。"
        @retry="loadArticles"
      />
      <TransitionGroup name="filter" tag="section" class="article-grid">
        <ArticleCard v-for="(article, index) in articlePage?.items ?? []" :key="article.slug" :article="article" :number="currentPage * pageSize + index + 1" />
      </TransitionGroup>
    </div>
    <nav v-if="totalPages > 1" class="site-pagination" aria-label="文章分页">
      <button :disabled="currentPage === 0" @click="setPage(currentPage - 1)"><span>←</span> 上一页</button>
      <div class="page-numbers">
        <button v-for="page in totalPages" :key="page" :class="{ active: currentPage === page - 1 }" :aria-current="currentPage === page - 1 ? 'page' : undefined" @click="setPage(page - 1)">{{ String(page).padStart(2, '0') }}</button>
      </div>
      <p>第 {{ currentPage + 1 }} / {{ totalPages }} 页</p>
      <button :disabled="currentPage >= totalPages - 1" @click="setPage(currentPage + 1)">下一页 <span>→</span></button>
    </nav>
    <section class="home-contact-card" data-home-reveal aria-labelledby="home-contact-title">
      <span class="contact-shape contact-shape-blue" aria-hidden="true"></span>
      <span class="contact-shape contact-shape-coral" aria-hidden="true"></span>
      <div class="home-contact-copy">
        <p class="eyebrow">SAY HELLO</p>
        <h2 id="home-contact-title">有想法想聊聊？<br />或者刚好有个合适的机会。</h2>
        <p>不用写得很正式。告诉我你在做什么、卡在哪里，或者只是来打个招呼都可以。</p>
        <a v-if="contactEmail" class="home-contact-primary" :href="`mailto:${contactEmail}`">{{ contactEmail }} <span aria-hidden="true">↗</span></a>
        <RouterLink v-else class="home-contact-primary" to="/about">先认识一下我 <span aria-hidden="true">↗</span></RouterLink>
      </div>
      <nav class="home-contact-links" aria-label="继续浏览">
        <RouterLink to="/works">作品</RouterLink><RouterLink to="/capabilities">能力</RouterLink><RouterLink to="/journal">文章</RouterLink>
      </nav>
    </section>
  </div>
</template>
