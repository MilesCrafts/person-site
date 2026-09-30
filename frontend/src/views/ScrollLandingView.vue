<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

import archiveImage from '../assets/scroll-world/stills/archive.png'
import footballImage from '../assets/scroll-world/stills/football.png'
import cinemaImage from '../assets/scroll-world/stills/cinema.png'
import libraryImage from '../assets/scroll-world/stills/library.png'
import cityImage from '../assets/scroll-world/stills/city.png'
import albumImage from '../assets/scroll-world/stills/album.png'

type Scene = {
  number: string
  chapter: string
  title: string
  caption: string
  image: string
  alt: string
  final?: boolean
}

const scenes: Scene[] = [
  {
    number: '01',
    chapter: '档案馆',
    title: '这里收藏着，\n我与时间的相遇。',
    caption: '我看过的电影、热爱的足球、读过的书，以及生活留下的痕迹。',
    image: archiveImage,
    alt: '纸艺书本展开成一座私人档案馆'
  },
  {
    number: '02',
    chapter: '足球',
    title: '我的热爱，\n从来不只关乎胜负。',
    caption: '记下那些守候比赛的夜晚，和足球带给我的相信。',
    image: footballImage,
    alt: '纸艺足球场与夜间看台'
  },
  {
    number: '03',
    chapter: '电影',
    title: '有些人生，\n我在银幕上经历。',
    caption: '这里存放打动过我的电影，以及散场后仍未结束的思绪。',
    image: cinemaImage,
    alt: '纸艺老式电影院和放映机'
  },
  {
    number: '04',
    chapter: '阅读',
    title: '我从书页里，\n借来另一种生活。',
    caption: '读过的故事、划下的句子，还有偶然被照亮的时刻。',
    image: libraryImage,
    alt: '纸艺书房与纵深书架'
  },
  {
    number: '05',
    chapter: '随笔',
    title: '日子没有主题，\n但值得被记下来。',
    caption: '上海的街道、平常的夜晚，以及我不想忘记的小事。',
    image: cityImage,
    alt: '纸艺上海夜巷与湿润街道'
  },
  {
    number: '06',
    chapter: 'LEKANG JOURNAL',
    title: '如果你愿意，\n来翻翻我的故事。',
    caption: '这里是 LEKANG JOURNAL，也是我留给时间的一份私人记录。',
    image: albumImage,
    alt: '纸艺个人影集汇集足球、电影、阅读与城市片段',
    final: true
  }
]

const shell = ref<HTMLElement | null>(null)
const worldPosition = ref(0)
const viewportWidth = ref(1440)
let frame = 0

const clamp = (value: number, min = 0, max = 1) => Math.min(max, Math.max(min, value))

const updateProgress = () => {
  cancelAnimationFrame(frame)
  frame = requestAnimationFrame(() => {
    if (!shell.value) return
    const rect = shell.value.getBoundingClientRect()
    const distance = shell.value.offsetHeight - window.innerHeight
    const progress = distance > 0 ? clamp(-rect.top / distance) : 0
    worldPosition.value = progress * (scenes.length - 1)
    viewportWidth.value = window.innerWidth
  })
}

const activeIndex = computed(() => Math.round(worldPosition.value))
const progressPercent = computed(() => `${(worldPosition.value / (scenes.length - 1)) * 100}%`)

const imageStyle = (index: number) => {
  const distance = worldPosition.value - index
  const opacity = clamp(1 - Math.abs(distance))
  const forwardTravel = clamp(distance, 0, 1)
  const incomingTravel = clamp(distance + 1, 0, 1)
  const scale = distance >= 0 ? 1 + forwardTravel * 0.055 : 0.975 + incomingTravel * 0.025

  return {
    opacity,
    transform: `scale(${scale})`,
    zIndex: opacity > 0 ? index + 1 : 0
  }
}

const copyStyle = (index: number) => {
  const opacity = clamp(1 - Math.abs(worldPosition.value - index) * 2.25)
  const offset = (index - worldPosition.value) * 32

  return {
    opacity,
    transform: `translate3d(0, ${offset}px, 0)`,
    pointerEvents: opacity > 0.65 ? 'auto' as const : 'none' as const
  }
}

onMounted(() => {
  viewportWidth.value = window.innerWidth
  window.addEventListener('scroll', updateProgress, { passive: true })
  window.addEventListener('resize', updateProgress, { passive: true })
  updateProgress()
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frame)
  window.removeEventListener('scroll', updateProgress)
  window.removeEventListener('resize', updateProgress)
})
</script>

<template>
  <div v-if="viewportWidth >= 640" ref="shell" class="scroll-world" aria-label="LEKANG JOURNAL 入口">
    <section class="world-stage">
      <div class="world-media" aria-hidden="true">
        <figure
          v-for="(scene, index) in scenes"
          :key="scene.chapter"
          class="world-frame"
          :style="imageStyle(index)"
        >
          <img :src="scene.image" :alt="scene.alt">
        </figure>
        <div class="world-vignette"></div>
        <div class="world-grain"></div>
      </div>

      <header class="world-header">
        <RouterLink to="/journal" class="world-wordmark" aria-label="直接进入 LEKANG JOURNAL">
          <span>lekang</span>
          <strong>JOURNAL</strong>
        </RouterLink>
        <i aria-hidden="true"></i>
        <div class="world-actions">
          <span>桌面预览版</span>
          <RouterLink to="/journal">进入日志 <b>↗</b></RouterLink>
        </div>
      </header>

      <div class="world-copy" aria-live="polite">
        <article
          v-for="(scene, index) in scenes"
          :key="`${scene.chapter}-copy`"
          class="scene-copy"
          :class="{ 'scene-copy--final': scene.final }"
          :style="copyStyle(index)"
        >
          <div class="scene-meta">
            <span>{{ scene.number }} / 06</span>
            <i></i>
            <span>{{ scene.chapter }}</span>
          </div>
          <h1>{{ scene.title }}</h1>
          <p>{{ scene.caption }}</p>
          <RouterLink v-if="scene.final" class="enter-journal" to="/journal">
            进入我的杂志 <span>↗</span>
          </RouterLink>
        </article>
      </div>

      <aside class="world-progress" aria-label="场景进度">
        <div class="progress-line"><span :style="{ height: progressPercent }"></span></div>
        <ol>
          <li
            v-for="(scene, index) in scenes"
            :key="`${scene.number}-progress`"
            :class="{ active: activeIndex === index }"
          >
            <span>{{ scene.number }}</span>
            <em>{{ scene.chapter }}</em>
          </li>
        </ol>
      </aside>

      <div v-if="activeIndex < scenes.length - 1" class="scroll-cue">
        <span>滚动探索</span>
        <i></i>
      </div>
    </section>
  </div>

  <section v-else class="desktop-note">
    <div class="desktop-note__mark"><span>lekang</span><strong>JOURNAL</strong></div>
    <p>这段开场旅程为桌面端设计</p>
    <h1>这里记录着我不想被时间带走的瞬间。</h1>
    <RouterLink to="/journal">进入我的杂志 <span>→</span></RouterLink>
  </section>
</template>

<style scoped lang="scss">
.scroll-world {
  position: relative;
  height: 720vh;
  color: #181818;
  background: #f5f2eb;
}

.world-stage {
  position: sticky;
  top: 0;
  height: 100vh;
  min-height: 680px;
  overflow: hidden;
  isolation: isolate;
  background: #eeeae1;
}

.world-media,
.world-frame,
.world-vignette,
.world-grain {
  position: absolute;
  inset: 0;
}

.world-media { background: #eeeae1; }

.world-frame {
  margin: 0;
  transform-origin: 50% 54%;
  will-change: opacity, transform;
}

.world-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  filter: saturate(.82) contrast(.97) brightness(.91);
}

.world-vignette {
  z-index: 20;
  background:
    linear-gradient(90deg, rgba(245,242,235,.94) 0%, rgba(245,242,235,.68) 19%, transparent 48%),
    linear-gradient(180deg, rgba(245,242,235,.38), transparent 20%, transparent 78%, rgba(24,24,24,.08));
  pointer-events: none;
}

.world-grain {
  z-index: 21;
  opacity: .13;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 180 180' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='.24'/%3E%3C/svg%3E");
  mix-blend-mode: multiply;
  pointer-events: none;
}

.world-header {
  position: absolute;
  z-index: 30;
  top: 0;
  right: 42px;
  left: 42px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  min-height: 106px;
  border-bottom: 1px solid rgba(24,24,24,.3);
  font: 700 9px/1 Arial, Helvetica, sans-serif;
  letter-spacing: .2em;
}

.world-header > p { margin: 0; }
.world-actions {
  display: flex;
  align-items: center;
  justify-self: end;
  gap: 22px;
}

.world-actions > span { color: #77736c; }

.world-actions a {
  position: relative;
  padding: 10px 0;
  color: #181818;
  white-space: nowrap;
}

.world-actions a::after {
  position: absolute;
  right: 0;
  bottom: 3px;
  left: 0;
  height: 1px;
  background: #a32824;
  content: '';
  transform: scaleX(0);
  transform-origin: right;
  transition: transform .25s ease;
}

.world-actions a:hover::after {
  transform: scaleX(1);
  transform-origin: left;
}

.world-actions b { margin-left: 6px; color: #a32824; font-size: 13px; }

.world-wordmark {
  display: inline-flex;
  align-items: baseline;
  justify-self: start;
  gap: 14px;
  width: max-content;
}

.world-wordmark span {
  font: italic 400 38px/.8 'Segoe Script', 'Brush Script MT', cursive;
  letter-spacing: -.08em;
  transform: rotate(-2deg);
}

.world-wordmark strong {
  font: 400 13px/1 Georgia, 'Times New Roman', serif;
  letter-spacing: .24em;
}

.world-copy {
  position: absolute;
  z-index: 30;
  top: 106px;
  bottom: 0;
  left: clamp(42px, 7vw, 118px);
  width: min(560px, 42vw);
}

.scene-copy {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  will-change: opacity, transform;
}

.scene-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
  color: #a32824;
  font: 700 9px/1 Arial, Helvetica, sans-serif;
  letter-spacing: .22em;
}

.scene-meta i { width: 42px; height: 1px; background: currentColor; }

.scene-copy h1 {
  max-width: 600px;
  margin: 0;
  white-space: pre-line;
  font: 500 clamp(52px, 5.2vw, 84px)/1.08 'Songti SC', 'STSong', SimSun, Georgia, serif;
  letter-spacing: -.045em;
}

.scene-copy p {
  max-width: 400px;
  margin: 28px 0 0;
  color: #5f5b55;
  font: 400 13px/1.8 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  letter-spacing: .1em;
}

.scene-copy--final {
  top: 34%;
  width: min(680px, 58vw);
}

.scene-copy--final h1 {
  font-size: clamp(48px, 4.6vw, 72px);
  line-height: 1.06;
}

.scene-copy--final p { margin-top: 22px; }

.scene-copy--final .enter-journal { margin-top: 25px; }

.enter-journal {
  display: inline-flex;
  align-items: center;
  gap: 28px;
  margin-top: 34px;
  padding: 17px 21px 15px;
  border: 1px solid #181818;
  background: rgba(245,242,235,.76);
  font: 700 10px/1 Arial, Helvetica, sans-serif;
  letter-spacing: .2em;
  backdrop-filter: blur(8px);
  transition: color .25s ease, background .25s ease;
}

.enter-journal span { color: #a32824; font-size: 16px; }
.enter-journal:hover { color: #f5f2eb; background: #181818; }

.world-progress {
  position: absolute;
  z-index: 30;
  top: 50%;
  right: 42px;
  display: flex;
  gap: 18px;
  transform: translateY(-42%);
}

.progress-line { width: 1px; height: 252px; background: rgba(24,24,24,.2); }
.progress-line span { display: block; width: 100%; background: #a32824; transition: height .08s linear; }

.world-progress ol {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 252px;
  margin: -5px 0 0;
  padding: 0;
  list-style: none;
}

.world-progress li {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(24,24,24,.46);
  font: 700 8px/1 Arial, Helvetica, sans-serif;
  letter-spacing: .17em;
  transition: color .25s ease;
}

.world-progress li em {
  max-width: 0;
  overflow: hidden;
  font-style: normal;
  white-space: nowrap;
  opacity: 0;
  transition: max-width .35s ease, opacity .25s ease;
}

.world-progress li.active { color: #a32824; }
.world-progress li.active em { max-width: 130px; opacity: 1; }

.scroll-cue {
  position: absolute;
  z-index: 30;
  bottom: 33px;
  left: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: rgba(24,24,24,.62);
  font: 700 7px/1 Arial, Helvetica, sans-serif;
  letter-spacing: .23em;
  transform: translateX(-50%);
}

.scroll-cue i {
  position: relative;
  width: 1px;
  height: 34px;
  overflow: hidden;
  background: rgba(24,24,24,.25);
}

.scroll-cue i::after {
  position: absolute;
  top: -100%;
  left: 0;
  width: 100%;
  height: 100%;
  background: #a32824;
  content: '';
  animation: scroll-line 1.8s ease-in-out infinite;
}

.desktop-note {
  display: grid;
  place-content: center;
  min-height: 100vh;
  padding: 36px;
  color: #181818;
  background: #f5f2eb;
  text-align: center;
}

.desktop-note__mark { display: flex; align-items: baseline; justify-content: center; gap: 12px; }
.desktop-note__mark span { font: italic 400 48px/.8 'Segoe Script', cursive; }
.desktop-note__mark strong { font: 400 13px/1 Georgia, serif; letter-spacing: .2em; }
.desktop-note > p { margin: 58px 0 18px; color: #a32824; font: 700 8px/1 Arial, sans-serif; letter-spacing: .2em; }
.desktop-note h1 { max-width: 540px; margin: 0; font: 500 clamp(44px, 12vw, 70px)/1.12 'Songti SC', 'STSong', SimSun, Georgia, serif; letter-spacing: -.04em; }
.desktop-note > a { width: max-content; margin: 42px auto 0; padding-bottom: 7px; border-bottom: 1px solid #a32824; font: 700 9px/1 Arial, sans-serif; letter-spacing: .18em; }
.desktop-note > a span { margin-left: 10px; color: #a32824; }

@keyframes scroll-line {
  0% { transform: translateY(0); }
  70%, 100% { transform: translateY(200%); }
}

@media (min-width: 640px) and (max-width: 899px) {
  .world-stage { min-height: 560px; }

  .world-header {
    right: 24px;
    left: 24px;
    grid-template-columns: 1fr 1px auto;
    min-height: 92px;
  }

  .world-wordmark { gap: 10px; }
  .world-wordmark span { font-size: 31px; }
  .world-wordmark strong { font-size: 10px; letter-spacing: .18em; }
  .world-actions { gap: 0; }
  .world-actions > span { display: none; }

  .world-copy {
    top: 92px;
    left: 28px;
    width: 62vw;
  }

  .scene-copy h1 {
    font-size: clamp(40px, 6.8vw, 57px);
    line-height: 1.1;
  }

  .scene-copy--final {
    top: 31%;
    width: min(540px, 68vw);
  }

  .scene-copy--final h1 { font-size: clamp(38px, 6vw, 50px); }

  .scene-copy p { max-width: 350px; font-size: 11px; }
  .world-vignette { background: linear-gradient(90deg, rgba(245,242,235,.96), rgba(245,242,235,.72) 28%, transparent 72%); }
  .world-progress { right: 18px; }
  .world-progress li em { display: none; }
  .scroll-cue { bottom: 22px; }
}

@media (prefers-reduced-motion: reduce) {
  .scroll-cue i::after { animation: none; }
}
</style>
