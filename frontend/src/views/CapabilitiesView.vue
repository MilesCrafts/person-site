<script setup lang="ts">
import { onMounted } from 'vue'
import { useScrollCardReveal } from '../composables/useScrollCardReveal'
import { setPageMeta } from '../utils/content'

const { reveal } = useScrollCardReveal()
onMounted(() => {
  setPageMeta('能力', '乐康在产品梳理、前端实现与内容系统方面的实践能力。')
  void reveal()
})
</script>

<template>
  <div class="capabilities-view container">
    <header class="page-intro"><p class="eyebrow">能力 / CAPABILITIES</p><h1>会想，也会做。<br />卡住的时候会继续查。</h1><p>我不想用一整面技能 Logo 证明自己。下面这些能力，都能在这个网站和已经完成的功能里找到对应的东西。</p></header>

    <section class="capability-grid capability-page-grid">
      <article data-scroll-card><span class="capability-number">01</span><i class="capability-icon">↳</i><h3>产品与信息结构</h3><p>把模糊需求拆成页面、路径和优先级，知道这次应该先做什么，也知道哪些东西可以晚一点。</p><small>SCOPE · IA · CONTENT FLOW</small></article>
      <article data-scroll-card><span class="capability-number">02</span><i class="capability-icon">⌁</i><h3>前端与互动实现</h3><p>使用 Vue 3、TypeScript 和 GSAP，把视觉稿落成能适配不同屏幕、键盘和减少动态效果设置的页面。</p><small>VUE · TYPESCRIPT · GSAP</small></article>
      <article data-scroll-card><span class="capability-number">03</span><i class="capability-icon">{ }</i><h3>接口与内容系统</h3><p>能围绕 Spring Boot、REST 和 PostgreSQL 协调公开读取、后台编辑、权限与发布流程。</p><small>SPRING BOOT · REST · POSTGRESQL</small></article>
    </section>

    <section class="capability-detail-grid">
      <article class="capability-detail-card tone-yellow" data-scroll-card><p class="eyebrow">我的习惯</p><h2>先把最小闭环跑通。</h2><p>先让一件事情从头到尾真的工作，再决定哪里值得增加复杂度。比起一次规划完所有功能，我更相信可以验证的小步迭代。</p><ol><li>确认问题与边界</li><li>搭出可使用的第一版</li><li>在真实反馈里继续调整</li></ol></article>
      <article class="capability-detail-card tone-blue" data-scroll-card><p class="eyebrow">我会在意</p><h2>那些容易被忽略的小地方。</h2><p>加载失败怎么办？手机会不会横向滚动？键盘能不能操作？用户的修改会不会被覆盖？这些不是收尾工作，而是产品体验的一部分。</p><div class="detail-tags"><span>LOADING</span><span>EMPTY</span><span>ERROR</span><span>A11Y</span><span>RESPONSIVE</span></div></article>
    </section>

    <section class="about-tool-card" data-scroll-card><div><p class="eyebrow">当前工具箱</p><h2>够用、熟悉，也还在继续学</h2></div><div class="about-tools"><span>Vue 3</span><span>TypeScript</span><span>Vite</span><span>GSAP</span><span>Spring Boot</span><span>PostgreSQL</span><span>REST API</span><span>Markdown</span></div></section>
    <section class="page-next-card" data-scroll-card><div><p class="eyebrow">有证据更重要</p><h2>不如直接看看，<br />这些能力最后做成了什么。</h2></div><RouterLink to="/works">返回作品页 <span>→</span></RouterLink></section>
  </div>
</template>
