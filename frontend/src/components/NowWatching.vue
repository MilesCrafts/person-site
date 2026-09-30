<script setup lang="ts">
import { editorialImages } from '../data/media'
import type { Profile } from '../types/api'

defineProps<{ profile: Profile }>()
</script>

<template>
  <section class="now-watching">
    <div class="section-heading"><p>SCREEN DIARY · 01</p><h2>最近在看</h2></div>
    <div class="poster-wrap">
      <img :src="editorialImages.poster" alt="最近观看内容的杂志配图" />
      <p>WATCHING</p>
    </div>
    <div class="watch-title-row"><div><p class="eyebrow">NOW WATCHING</p><h3>{{ profile.nowWatchingTitle }}</h3></div></div>
    <blockquote>“{{ profile.nowWatchingDetail }}”</blockquote>
  </section>
</template>
