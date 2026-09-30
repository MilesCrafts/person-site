<script setup lang="ts">
import { setPageMeta } from '../utils/content'
import overviewImage from '../assets/images/pdf-toolbox-overview.png'
import feedbackImage from '../assets/images/pdf-toolbox-feedback.png'
import watermarkImage from '../assets/images/pdf-toolbox-watermark.png'

setPageMeta('阿康的一站式文档格式转换', '微信小程序项目详情。')
</script>

<template>
  <div class="container project-detail-view">
    <header class="project-detail-hero">
      <p class="eyebrow">PROJECT 02 / 微信小程序</p>
      <h1>阿康的一站式文档格式转换</h1>
      <p>把常用的 PDF 文档处理集中到一个轻量入口，选择文件后即可完成转换、加水印、合并、拆分与压缩。</p>
    </header>
    <section class="toolbox-overview" aria-label="项目概览">
      <div class="toolbox-overview-heading"><p class="eyebrow">PROJECT NOTES / 02</p><h2>把重复的文档处理，<br />收进一个小程序。</h2></div>
      <div class="toolbox-metrics"><article><strong>8</strong><span>常用转换与 PDF<br />处理入口</span></article><article><strong>0</strong><span>无需登录也能先<br />开始使用</span></article><article><strong>1</strong><span>个入口完成上传、<br />处理与反馈</span></article><p>从首页工具选择到具体参数配置，尽量让每一步都只回答一个问题：这份文件接下来要怎么处理。</p></div>
    </section>
    <section class="toolbox-notes" aria-label="项目设计说明">
      <article class="toolbox-note tone-blue"><span>WHY</span><h3>把常用工具放在一处。</h3><p>PDF 转换、合并、拆分和水印不该散落在多个入口里。</p></article>
      <article class="toolbox-note tone-yellow"><span>HOW</span><h3>先选功能，再补参数。</h3><p>用清晰的工具卡片和局部配置，减少第一次使用时的判断成本。</p></article>
      <article class="toolbox-note tone-mint"><span>NEXT</span><h3>继续补全真实场景。</h3><p>根据实际使用反馈，逐步完善预览、下载与历史记录体验。</p></article>
    </section>
    <section class="project-steps" aria-label="产品使用步骤">
      <article class="project-step">
        <div class="project-step-copy"><p class="eyebrow">STEP 01</p><h2>上传 PDF 文件</h2><p>从首页上传区域选择需要处理的 PDF，文件进入工具箱后即可继续下一步。</p></div>
        <figure class="project-step-shot shot-upload"><img :src="overviewImage" alt="上传 PDF 文件区域" /></figure>
      </article>
      <article class="project-step">
        <figure class="project-step-shot shot-tools"><img :src="overviewImage" alt="PDF 工具列表" /></figure>
        <div class="project-step-copy"><p class="eyebrow">STEP 02</p><h2>选择处理功能</h2><p>从工具列表中选择转 Word、加水印、合并、拆分或压缩等功能，快速进入对应流程。</p></div>
      </article>
      <article class="project-step">
        <div class="project-step-copy"><p class="eyebrow">STEP 03</p><h2>配置处理选项</h2><p>以加水印为例，输入自定义文字并展开高级选项，调整水印内容和位置。</p></div>
        <figure class="project-step-shot shot-options"><img :src="watermarkImage" alt="PDF 加水印配置选项" /></figure>
      </article>
      <article class="project-step">
        <figure class="project-step-shot shot-result"><img :src="feedbackImage" alt="处理完成后的结果与反馈入口" /></figure>
        <div class="project-step-copy"><p class="eyebrow">STEP 04</p><h2>完成处理并使用结果</h2><p>转换完成后预览、下载或分享结果；未登录时记录会暂存在本地，随时可以回来查看。</p><RouterLink class="primary-link" to="/works">返回作品集 <span aria-hidden="true">←</span></RouterLink></div>
      </article>
    </section>
  </div>
</template>
