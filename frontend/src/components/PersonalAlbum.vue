<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import { getAlbumPhotos, getAlbums } from '../api/journal'
import { getErrorMessage } from '../api/client'
import { resolveMediaUrl } from '../data/media'
import type { Album, AlbumPhoto } from '../types/api'
import AsyncState from './AsyncState.vue'

const props = defineProps<{ topic: string | null }>()

const album = ref<Album | null>(null)
const photos = ref<AlbumPhoto[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
let controller: AbortController | undefined

const formatShotDate = (value: string | null) => value ? value.slice(0, 7).replace('-', '.') : 'UNDATED'

async function loadAlbum() {
  controller?.abort()
  const requestController = new AbortController()
  controller = requestController
  loading.value = true
  error.value = null

  try {
    const albums = await getAlbums(0, 1, requestController.signal)
    album.value = albums.items[0] ?? null
    if (!album.value) {
      photos.value = []
      return
    }
    const response = await getAlbumPhotos(album.value.slug, props.topic, 0, 50, requestController.signal)
    photos.value = response.items
  } catch (caught) {
    if (caught instanceof DOMException && caught.name === 'AbortError') return
    error.value = getErrorMessage(caught)
  } finally {
    if (!requestController.signal.aborted) loading.value = false
  }
}

watch(() => props.topic, loadAlbum, { immediate: true })
onBeforeUnmount(() => controller?.abort())
</script>

<template>
  <section class="personal-album">
    <header class="album-header">
      <div><p>PERSONAL ALBUM</p><span>VOL. 01 · 2026</span></div>
      <h2>{{ album?.title || '一些没有写成文章的瞬间。' }}</h2>
      <p>{{ album?.description || '照片替我保留那些来不及描述的部分。' }}</p>
    </header>
    <AsyncState
      v-if="loading || error || photos.length === 0"
      :loading="loading"
      :error="error"
      :empty="!loading && !error && photos.length === 0"
      empty-title="影集已经建立，照片仍在整理。"
      empty-hint="这里不会使用假图片替代真实影集内容。"
      @retry="loadAlbum"
    />
    <div v-else class="album-grid">
      <figure v-for="(photo, index) in photos" :key="photo.id" class="album-photo">
        <div>
          <img v-if="resolveMediaUrl(photo.imageUrl)" :src="resolveMediaUrl(photo.imageUrl)!" :alt="photo.imageAlt || photo.title" loading="lazy" />
          <span>{{ String(index + 1).padStart(2, '0') }}</span>
        </div>
        <figcaption><strong>{{ photo.title }}</strong><p>{{ photo.location || 'UNKNOWN' }} · {{ formatShotDate(photo.shotAt) }}</p></figcaption>
      </figure>
    </div>
    <footer v-if="photos.length" class="album-footer"><p>END OF ALBUM · 001</p><span>更多生活片段，仍在发生。</span></footer>
  </section>
</template>
