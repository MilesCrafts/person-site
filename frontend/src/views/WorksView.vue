<script setup lang="ts">
import { onMounted } from 'vue'
import WorkProcessDial from '../components/WorkProcessDial.vue'
import { useScrollCardReveal } from '../composables/useScrollCardReveal'
import { setPageMeta } from '../utils/content'
import pdfToolboxHome from '../assets/images/pdf-toolbox-home.png'
import pdfToolboxUpload from '../assets/images/pdf-toolbox-upload.png'

const { reveal } = useScrollCardReveal()
const projects = [
  {
    id: 'lekang-journal',
    index: '01',
    kind: '个人内容平台',
    status: '持续更新',
    title: 'LEKANG JOURNAL',
    summary: '我想留一个不赶时间的地方：文章可以认真写，做产品和写代码时那些绕路、犹豫与小发现，也都不必丢掉。',
    features: [
      '想读时不被打扰，旧文章还能沿着分类、搜索和归档找回来',
      '手机、平板和电脑都不必迁就布局，动效只在确实有帮助时出现',
      '灵感先存成草稿，慢慢改，确认好了再让它出现在公开站'
    ],
    route: '/journal',
    action: '浏览文章与归档',
    visualTitle: 'LEKANG',
    visualDomain: 'journal.lekang.site',
    visualLabel: 'ONGOING / 2026'
  },
  {
    id: 'pdf-toolbox',
    index: '02',
    kind: '微信小程序 · 效率工具',
    status: '已完成',
    title: '阿康的一站式文档格式转换',
    summary: '一款微信小程序里的文档格式转换工具，把 PDF 转 Word、加水印、合并、拆分与压缩集中到一个轻量入口。',
    features: [
      'PDF 转 Word、加水印、合并、拆分和压缩等常用工具一站式完成',
      '未登录也能直接使用，转换记录保存在本地，降低使用门槛',
      '清晰的移动端卡片布局，选择文件后即可开始转换'
    ],
    route: '/works/pdf-toolbox',
    action: '查看工具界面',
    visualImage: pdfToolboxHome,
    visualSecondaryImage: pdfToolboxUpload,
    visualTitle: '阿康的一站式文档格式转换',
    visualDomain: '微信小程序 / 阿康的一站式文档格式转换',
    visualLabel: 'SHIPPED / 2026'
  }
]
onMounted(() => {
  setPageMeta('作品', '乐康已经完成并持续维护的个人项目。')
  void reveal()
})
</script>

<template>
  <div class="works-view container">
    <header class="page-intro works-intro">
      <p class="eyebrow">作品集 / SELECTED WORKS</p>
      <h1 class="works-hero-title" aria-label="做好一件，就往这里加一件。">
        <span class="works-title-line works-title-first" aria-hidden="true"><strong>做好</strong><em>一件</em><i>，</i></span>
        <span class="works-title-line works-title-second" aria-hidden="true"><b>就往这里</b><span class="works-title-finish"><strong>加一件</strong><i>。</i></span></span>
      </h1>
      <p class="works-intro-description">这是会继续生长的作品集，不是某一个网站的说明书。现在先放两件已经做出来、可以打开、也愿意继续维护的作品。</p>
      <aside class="works-intro-note" aria-label="作品集当前进度">
        <span class="intro-note-palette" aria-hidden="true"><i /><i /><i /><i /></span>
        <div class="intro-note-top"><span>PORTFOLIO / OPEN FILE</span><i>持续更新</i></div>
        <p class="intro-note-count"><strong>01</strong><span>NOW</span></p>
        <div class="intro-note-current"><i aria-hidden="true" /><div><small>CURRENT PROJECT</small><strong>LEKANG JOURNAL</strong></div></div>
        <p class="intro-note-next"><span>02 / SHIPPED</span><strong>阿康的一站式文档格式转换</strong></p>
      </aside>
    </header>

    <section class="portfolio-index-heading" data-scroll-card>
      <div><p class="eyebrow">PROJECT INDEX / {{ String(projects.length).padStart(2, '0') }} NOW</p><h2>当前作品</h2></div>
      <p>每件作品都有自己的项目卡与案例拆解。以后加入新的产品、工具或实验时，会继续按 02、03 往下排。</p>
    </section>

    <section v-for="project in projects" :id="`project-${project.index}`" :key="project.id" class="work-showcase" data-scroll-card>
      <div class="work-visual" aria-hidden="true">
        <div v-if="project.visualImage" class="work-image-frame"><img :src="project.visualImage" :alt="`${project.title} 界面截图`" /><img v-if="project.visualSecondaryImage" class="work-image-secondary" :src="project.visualSecondaryImage" alt="" /></div>
        <div v-else class="work-window"><div class="work-window-bar"><i></i><i></i><i></i><span>{{ project.visualDomain }}</span></div><div class="work-window-body"><strong>{{ project.visualTitle }}</strong><span></span><span></span><span></span></div></div>
        <span class="work-visual-label">{{ project.visualLabel }}</span>
      </div>
      <div class="work-copy"><p class="work-index">{{ project.index }} / FEATURED PROJECT</p><p class="eyebrow">{{ project.kind }} · {{ project.status }}</p><h3>{{ project.title }}</h3><p>{{ project.summary }}</p><RouterLink class="primary-link" :to="project.route">{{ project.action }} <span aria-hidden="true">→</span></RouterLink></div>
    </section>

    <header class="case-study-heading" data-scroll-card><div><p class="eyebrow">CASE STUDY / PROJECT 01</p><h2>第一件作品，拆开讲讲。</h2></div><p>下面的结果、过程和子系统都属于 LEKANG JOURNAL。第二件作品已经加入上方作品索引。</p></header>

    <section class="work-proof" aria-label="项目验证结果" data-scroll-card>
      <article><strong>24</strong><span>组路由 × 视口<br />真实 API 浏览器验收</span></article>
      <article><strong>5</strong><span>个内容动作<br />草稿到归档完整流转</span></article>
      <article><strong>3</strong><span>档响应式检查<br />桌面 / 平板 / 手机</span></article>
      <p>这些是现在能验证的工程结果。访问量和使用数据还在积累，所以暂时不拿虚构数字装点页面。</p>
    </section>

    <section class="work-case-grid" aria-label="项目拆解">
      <article data-scroll-card><span>WHY</span><h2>为什么自己做一个网站？</h2><p>社交平台适合快速发出去，但不适合慢慢整理。这里更像自己的房间，可以决定内容怎么摆、以后怎么找回来。</p></article>
      <article data-scroll-card><span>HOW</span><h2>好看的背后，也得有人收拾房间</h2><p>公开页面之外，还有 API、数据库、登录、编辑和冲突保护。这样我写完一篇文章，不必再复制粘贴到另一个地方。</p></article>
      <article data-scroll-card><span>NEXT</span><h2>先继续用，再继续改</h2><p>下一步不是硬凑新项目，而是补真实复盘、上线后的使用反馈，以及真正能代表我的下一件作品。</p></article>
    </section>

    <WorkProcessDial data-scroll-card />

    <section class="work-project-grid" aria-label="LEKANG JOURNAL 已落地子系统">
      <article class="work-project-card tone-blue" data-scroll-card>
        <div class="project-card-top"><span>A</span><p>INSIDE PROJECT 01 / 写作后台</p></div>
        <h2>把“写完以后怎么办”，<br />也认真做完。</h2>
        <p>实时预览、自动保存、发布与撤回、归档和版本冲突提醒，都藏在读者看不到的另一面。</p>
        <ul><li>减少来回复制和手工发布</li><li>Session / CSRF / 乐观锁保护</li><li>桌面与手机都能继续编辑</li></ul>
        <div class="work-tags"><span>EDITOR</span><span>SESSION</span><span>API</span></div>
      </article>
      <article class="work-project-card tone-paper" data-scroll-card>
        <div class="project-card-top"><span>B</span><p>INSIDE PROJECT 01 / 交互系统</p></div>
        <h2>动效不是表演，<br />它负责提示“这里能动”。</h2>
        <p>文章卡片可以拖动，导航状态会跟着路由变化，内容进入视口时才轻轻出现；减少动态效果时仍能正常阅读。</p>
        <ul><li>GSAP 按需加载与静态降级</li><li>不自动轮播、不劫持滚动</li><li>键盘、指针与触控均可使用</li></ul>
        <div class="work-tags"><span>GSAP</span><span>A11Y</span><span>VUE</span></div>
      </article>
    </section>

    <section class="portfolio-open-slot" data-scroll-card>
      <div><p class="eyebrow">THE NEXT SLOT</p><h2>下一件作品，<br />做好再放进来。</h2><p>位置已经留好，但不会用概念稿或虚构项目把它填满。下一件真实作品出现后，会继续成为 Project 03。</p></div>
      <ol><li class="is-done"><span>01</span><strong>LEKANG JOURNAL</strong><small>AVAILABLE NOW</small></li><li class="is-done"><span>02</span><strong>阿康的一站式文档格式转换</strong><small>SHIPPED</small></li><li><span>03+</span><strong>继续生长</strong><small>OPEN</small></li></ol>
    </section>

    <section class="works-next" data-scroll-card>
      <div><p class="eyebrow">接着看 / 或者聊聊</p><h2>作品说做了什么，<br />能力页说我是怎么做的。</h2><p>如果你正好也在做一个需要长期维护的小产品，欢迎从关于页找到我。</p></div>
      <nav aria-label="作品页后续入口"><RouterLink to="/capabilities">能力与方法 <span>→</span></RouterLink><RouterLink to="/journal">去读文章 <span>→</span></RouterLink><RouterLink to="/about">认识一下 / 联系我 <span>→</span></RouterLink></nav>
    </section>
  </div>
</template>
