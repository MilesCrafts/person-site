<script setup lang="ts">
import { onMounted } from 'vue'
import AboutTagBoard from '../components/AboutTagBoard.vue'
import { useScrollCardReveal } from '../composables/useScrollCardReveal'
import { setPageMeta } from '../utils/content'

const { reveal } = useScrollCardReveal()
onMounted(() => {
  setPageMeta('关于我', '关于乐康的产品实践、开发能力与个人记录。')
  void reveal()
})
</script>

<template>
  <div class="about-view container">
    <header class="about-hero">
      <div class="about-hero-copy">
        <p class="eyebrow">关于我 / ABOUT</p>
        <h1 aria-label="你好，我是乐康。喜欢把模糊的想法，慢慢做成看得见的东西。"><span aria-hidden="true"><em>你好，</em><strong>我是乐康。</strong></span><span aria-hidden="true"><b>模糊的想法</b><i>慢慢做</i></span><span aria-hidden="true"><strong>成看得见的东西。</strong></span></h1>
        <p>这里没有一长串漂亮头衔，只有正在做的项目、学到的东西，以及偶尔忍不住写下来的日常。</p>
      </div>
      <AboutTagBoard />
    </header>

    <section class="about-now-strip" aria-label="当前个人状态" data-scroll-card>
      <article><small>NOW BUILDING</small><strong>LEKANG JOURNAL</strong><span>持续维护的个人内容站</span></article>
      <article><small>HOW I WORK</small><strong>先具体，再抽象</strong><span>先解决一个真实问题</span></article>
      <article><small>ALSO HERE</small><strong>文章与日常</strong><span>不只记录完成的结果</span></article>
    </section>

    <section class="about-profile-card" data-scroll-card>
      <div class="about-profile-visual" aria-hidden="true">
        <span class="about-shape about-shape-blue"></span><span class="about-shape about-shape-yellow"></span><span class="about-shape about-shape-coral"></span>
        <div class="about-paper"><small>PERSONAL NOTE / 01</small><strong>先想清楚，<br />再动手。</strong><p>不急着贴标签，<br />先把问题做具体。</p></div>
      </div>
      <div class="about-profile-copy">
        <p class="eyebrow">不太正式的自我介绍</p>
        <h2>比起给自己贴标签，<br />我更愿意先解决一个具体问题。</h2>
        <p>有时从页面开始，有时从一段接口开始。过程中会画结构、写代码、改文案，也会在发现方向不对时老老实实推倒重来。</p>
        <p>LEKANG JOURNAL 是我目前持续维护的个人项目。它既是作品，也是练习场：我在这里试新的交互，也认真对待每一篇文章怎么被读到。</p>
        <RouterLink class="primary-link" to="/works">看看我做的东西 <span aria-hidden="true">→</span></RouterLink>
      </div>
    </section>

    <section class="about-card-grid" aria-label="我在意的事情">
      <article data-scroll-card><span>01</span><h2>做点能用的东西</h2><p>好看当然重要，但我更在意它有没有把事情讲清楚，能不能真的被人用起来。</p><small>PRODUCT / BUILD</small></article>
      <article data-scroll-card><span>02</span><h2>把复杂说成人话</h2><p>无论是页面、接口还是一段说明，我都希望第一次看到的人不用猜太久。</p><small>CLARITY / CONTENT</small></article>
      <article data-scroll-card><span>03</span><h2>也留一点生活</h2><p>电影归进日常，书慢慢读，偶尔写点和工作没有直接关系、但不想忘掉的事。</p><small>DAILY / NOTES</small></article>
    </section>

    <section class="about-tool-card" data-scroll-card>
      <div><p class="eyebrow">现在常用 / TOOLBOX</p><h2>工具不是身份，<br />只是把想法落地的方法。</h2><p>需要什么就学什么，用完以后再把复杂度收回去。</p></div>
      <div class="about-tools"><span>Vue 3</span><span>TypeScript</span><span>GSAP</span><span>Spring Boot</span><span>PostgreSQL</span><span>Markdown</span></div>
    </section>

    <section class="about-next-card" data-scroll-card>
      <div><p class="eyebrow">再认识一点</p><h2>项目里看我怎么做，<br />文章里看我在想什么。</h2></div>
      <nav aria-label="关于页后续入口"><RouterLink to="/works">去看作品 <span>→</span></RouterLink><RouterLink to="/capabilities">能力与方法 <span>→</span></RouterLink><RouterLink to="/journal">读几篇文章 <span>→</span></RouterLink></nav>
    </section>
  </div>
</template>
