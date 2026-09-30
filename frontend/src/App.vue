<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'

const route = useRoute()
const immersive = computed(() => Boolean(route.meta.immersive))
const admin = computed(() => Boolean(route.meta.admin))
const showTop = ref(false)
const updateScroll = () => { showTop.value = window.scrollY > 650 }
const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

onMounted(() => window.addEventListener('scroll', updateScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', updateScroll))
</script>

<template>
  <SiteHeader v-if="!immersive && !admin" />
  <main>
    <RouterView v-slot="{ Component }">
      <Transition name="page" mode="out-in"><component :is="Component" /></Transition>
    </RouterView>
  </main>
  <SiteFooter v-if="!immersive && !admin" />
  <Transition name="fade">
    <button v-if="showTop && !immersive && !admin" class="back-top" aria-label="返回顶部" @click="scrollToTop">↑</button>
  </Transition>
</template>
