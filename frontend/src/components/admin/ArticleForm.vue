<script setup lang="ts">
import { ref } from 'vue'
import type { AdminArticleDraft } from '../../types/admin'
import type { AdminMediaAsset } from '../../types/admin'
import type { Taxonomy } from '../../types/api'

const props = defineProps<{
  modelValue: AdminArticleDraft
  taxonomies: Taxonomy[]
  slugLocked: boolean
  coverImageUrl?: string | null
  saveStatus: string
  saveState: 'idle' | 'dirty' | 'saving' | 'saved' | 'error'
  mediaAssets: AdminMediaAsset[]
  mediaLoading: boolean
  uploading: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: AdminArticleDraft]
  'extract-excerpt': []
  'upload-cover': [file: File]
  'select-cover': [id: number]
  'clear-cover': []
  'slug-edited': []
  'generate-slug': []
}>()

const fileInput = ref<HTMLInputElement | null>(null)

function chooseFile() {
  if (!props.uploading) fileInput.value?.click()
}

function submitFiles(files: FileList | null) {
  const file = files?.[0]
  if (file) emit('upload-cover', file)
}

function handleDrop(event: DragEvent) {
  submitFiles(event.dataTransfer?.files ?? null)
}

function update<K extends keyof AdminArticleDraft>(key: K, value: AdminArticleDraft[K]) {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}

function updateCategory(categoryCode: string) {
  const topicCode = props.taxonomies.find(item => item.code === categoryCode)?.topics[0]?.code ?? ''
  emit('update:modelValue', { ...props.modelValue, categoryCode, topicCode })
}
</script>

<template>
  <div class="article-fields">
    <label class="field title-field">
      <span class="title-label"><span>标题</span><small class="save-state" :data-state="saveState"><i />{{ saveStatus }}</small></span>
      <input
        :value="modelValue.title"
        required
        autocomplete="off"
        placeholder="一篇文章的标题"
        @input="update('title', ($event.target as HTMLInputElement).value)"
      >
    </label>
    <div class="field slug-field">
      <div class="field-label">
        <span>文章网址</span>
        <button v-if="!slugLocked" type="button" @click="emit('generate-slug')">根据标题生成</button>
      </div>
      <input
        :value="modelValue.slug"
        required
        :disabled="slugLocked"
        pattern="[a-z0-9]+(?:-[a-z0-9]+)*"
        autocomplete="off"
        placeholder="输入标题后自动生成"
        aria-describedby="article-slug-help"
        @input="emit('slug-edited'); update('slug', ($event.target as HTMLInputElement).value)"
      >
      <small id="article-slug-help">
        {{ slugLocked ? '文章已发布，公开地址保持锁定' : `公开地址：/article/${modelValue.slug || '自动生成'}` }}
      </small>
    </div>
    <div class="field excerpt-field">
      <div class="field-label"><span>摘要</span><button type="button" @click="emit('extract-excerpt')">从正文提取</button></div>
      <textarea
        :value="modelValue.excerpt"
        required
        rows="3"
        placeholder="用几句话为读者打开故事。"
        @input="update('excerpt', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>
    <label class="field">
      <span>栏目</span>
      <select
        :value="modelValue.categoryCode"
        required
        @change="updateCategory(($event.target as HTMLSelectElement).value)"
      >
        <option value="" disabled>选择栏目</option>
        <option v-for="taxonomy in taxonomies" :key="taxonomy.code" :value="taxonomy.code">
          {{ taxonomy.displayName }}
        </option>
      </select>
    </label>
    <label class="field">
      <span>主题</span>
      <select
        :value="modelValue.topicCode"
        required
        @change="update('topicCode', ($event.target as HTMLSelectElement).value)"
      >
        <option value="" disabled>选择主题</option>
        <option
          v-for="topic in taxonomies.find(item => item.code === modelValue.categoryCode)?.topics ?? []"
          :key="topic.code"
          :value="topic.code"
        >
          {{ topic.displayName }}
        </option>
      </select>
    </label>
    <p class="taxonomy-hint"><i>＋</i> 新建栏目与主题将在分类管理接口完成后开放</p>
    <div class="field cover-field">
      <span>封面资源（可选）</span>
      <div
        class="cover-picker"
        :class="{ 'has-cover': coverImageUrl, busy: uploading }"
        role="button"
        tabindex="0"
        :aria-busy="uploading"
        @click="chooseFile"
        @keydown.enter.prevent="chooseFile"
        @keydown.space.prevent="chooseFile"
        @dragover.prevent
        @drop.prevent="handleDrop"
      >
        <img v-if="coverImageUrl" :src="coverImageUrl" alt="当前文章封面预览">
        <template v-else>
          <span class="upload-mark" aria-hidden="true"><i /></span>
          <strong>{{ uploading ? '正在上传…' : '拖拽图片到这里' }}</strong>
          <p>JPEG / PNG，最大 8MB；也可点击选择</p>
        </template>
        <span v-if="coverImageUrl" class="cover-change">{{ uploading ? '正在上传…' : '点击更换封面' }}</span>
      </div>
      <input ref="fileInput" class="visually-hidden" type="file" accept="image/jpeg,image/png" @change="submitFiles(($event.target as HTMLInputElement).files)">
      <button v-if="modelValue.coverAssetId" class="cover-clear" type="button" @click="emit('clear-cover')">移除封面</button>
      <details class="media-library">
        <summary>从已有素材选择 <span v-if="mediaAssets.length">{{ mediaAssets.length }}</span></summary>
        <p v-if="mediaLoading" class="media-state">正在加载素材…</p>
        <p v-else-if="!mediaAssets.length" class="media-state">还没有可用素材，先上传一张图片。</p>
        <div v-else class="media-grid">
          <button
            v-for="asset in mediaAssets"
            :key="asset.id"
            type="button"
            :class="{ active: modelValue.coverAssetId === asset.id }"
            :title="asset.originalName"
            @click="emit('select-cover', asset.id)"
          >
            <img :src="asset.url" :alt="asset.altText || asset.originalName">
            <span>#{{ asset.id }}</span>
          </button>
        </div>
      </details>
      <details class="cover-advanced">
        <summary>高级：关联已有素材</summary>
        <label><span>资源 ID</span><input :value="modelValue.coverAssetId ?? ''" type="number" min="1" inputmode="numeric" placeholder="输入已有资源 ID" @input="update('coverAssetId', ($event.target as HTMLInputElement).value ? Number(($event.target as HTMLInputElement).value) : null)"></label>
      </details>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;

.article-fields { display: grid; grid-template-columns: repeat(3,1fr); gap: 22px 18px; padding: 28px; border: 1px solid $border; border-radius: 15px; background: #fff; }
.field { display: flex; min-width: 0; flex-direction: column; gap: 10px; }
.field > span { color: $text-secondary; font-size: 10px; font-weight: 700; }
.title-label { display:flex; align-items:center; justify-content:space-between; gap:12px; }.title-label > span { color:$text-secondary; font-size:10px; font-weight:700; }.save-state { display:inline-flex; align-items:center; gap:6px; color:$text-secondary!important; font-size:9px!important; font-weight:600!important; }.save-state i { width:6px; height:6px; border-radius:50%; background:#98a2b3; }.save-state[data-state='dirty'] i { background:$yellow; }.save-state[data-state='saving'] { color:$accent!important; }.save-state[data-state='saving'] i { background:$accent; }.save-state[data-state='saved'] { color:$green!important; }.save-state[data-state='saved'] i { background:$green; }.save-state[data-state='error'] { color:$coral!important; }.save-state[data-state='error'] i { background:$coral; }
.field-label { display: flex; align-items: center; justify-content: space-between; }.field-label > span { color: $text-secondary; font-size: 10px; font-weight: 700; }.field-label button { padding: 5px 8px; border: 1px solid rgba($accent,.2); border-radius: 7px; color: $accent; background: $accent-subtle; font-size: 9px; font-weight: 700; cursor: pointer; }.field-label button:hover { border-color: $accent; background: #fff; }
input, textarea, select {
  width: 100%; padding-inline: 13px; border: 1px solid $border; border-radius: 10px;
  outline: 0;
  color: $text-primary;
  background: $surface;
}
input::placeholder, textarea::placeholder { color:#98a2b3; opacity:1; }
input, select { min-height: 48px; }
textarea { padding-block: 13px; resize: vertical; font: 400 15px/1.65 $sans; }
input:focus, textarea:focus, select:focus { border-color: $accent; background: #fff; box-shadow: 0 0 0 3px rgba($accent,.08); }
input:disabled { color: $text-secondary; cursor: not-allowed; }
.title-field { grid-column: span 2; }
.title-field input { min-height: 64px; font-size: clamp(23px,3vw,34px); font-weight: 680; letter-spacing: -.035em; }.title-field input:focus { border-color:$accent; box-shadow:0 0 0 4px rgba($accent,.12), inset 3px 0 0 $accent; }
.slug-field input { font-family: Consolas, monospace; font-size: 13px; }
.slug-field small { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.field small { color: $accent; font-size: 9px; line-height: 1.5; }
.excerpt-field { grid-column: span 3; }
.taxonomy-hint { grid-column:1 / span 2; margin:-13px 0 0; color:$accent; font-size:9px; }.taxonomy-hint i { font-style:normal; font-weight:800; }
.cover-field { grid-column:3; grid-row:3 / span 2; }.cover-picker { position:relative; display:flex; min-height:126px; align-items:center; justify-content:center; flex-direction:column; gap:6px; overflow:hidden; padding:14px; border:1px dashed rgba($accent,.38); border-radius:11px; color:$text-secondary; background:$surface; text-align:center; cursor:pointer; transition:border-color .2s ease, background .2s ease; }.cover-picker:hover, .cover-picker:focus-visible { border-color:$accent; background:#f7f9ff; }.cover-picker.busy { opacity:.68; cursor:wait; pointer-events:none; }.cover-picker img { width:100%; height:126px; border-radius:8px; object-fit:cover; }.cover-picker strong { color:$text-primary; font-size:11px; }.cover-picker p { max-width:240px; margin:0; font-size:8px; line-height:1.5; }.cover-change { position:absolute; right:20px; bottom:20px; padding:6px 8px; border-radius:7px; color:#fff!important; background:rgba(16,24,40,.78); font-size:8px!important; }.upload-mark { position:relative; display:grid; width:32px; height:32px; place-items:center; border:1px solid rgba($accent,.2); border-radius:9px; color:$accent; background:#fff; }.upload-mark::before { width:12px; height:9px; border:1.5px solid currentColor; border-top:0; border-radius:0 0 3px 3px; content:''; }.upload-mark::after { position:absolute; top:7px; width:7px; height:7px; border-top:1.5px solid currentColor; border-left:1.5px solid currentColor; content:''; transform:rotate(45deg); }.upload-mark i { position:absolute; top:7px; width:1.5px; height:11px; background:currentColor; }.visually-hidden { position:absolute; width:1px; height:1px; overflow:hidden; clip:rect(0,0,0,0); white-space:nowrap; }.cover-clear { width:max-content; padding:4px 0; border:0; color:$coral; background:transparent; font-size:9px; font-weight:700; cursor:pointer; }.media-library summary, .cover-advanced summary { width:max-content; margin-top:2px; color:$accent; font-size:9px; font-weight:650; cursor:pointer; }.media-library summary span { display:inline-grid; min-width:17px; height:17px; margin-left:4px; place-items:center; border-radius:99px; color:#fff; background:$accent; font-size:8px; }.media-state { margin:10px 0 0; color:$text-secondary; font-size:9px; }.media-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:7px; margin-top:10px; }.media-grid button { position:relative; padding:2px; overflow:hidden; border:1px solid $border; border-radius:7px; background:#fff; cursor:pointer; }.media-grid button.active { border-color:$accent; box-shadow:0 0 0 2px rgba($accent,.12); }.media-grid img { width:100%; aspect-ratio:4/3; border-radius:5px; object-fit:cover; }.media-grid button span { position:absolute; right:4px; bottom:4px; padding:2px 4px; border-radius:4px; color:#fff; background:rgba(16,24,40,.72); font-size:7px; }.cover-advanced label { display:grid; grid-template-columns:auto 1fr; align-items:center; gap:10px; margin-top:9px; color:$text-secondary; font-size:9px; }.cover-advanced input { min-height:38px; background:#fff; }

@media (max-width: 760px) {
  .article-fields { grid-template-columns: 1fr; padding: 20px; }
  .title-field, .excerpt-field, .taxonomy-hint, .cover-field { grid-column: auto; grid-row:auto; }
}
</style>
