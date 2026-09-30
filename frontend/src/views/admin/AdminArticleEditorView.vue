<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  archiveAdminArticle,
  createAdminArticle,
  getAdminArticle,
  getAdminMedia,
  previewAdminArticle,
  publishAdminArticle,
  unpublishAdminArticle,
  uploadAdminMedia,
  updateAdminArticle
} from '../../api/admin'
import { ApiError, getErrorMessage } from '../../api/client'
import { getTaxonomies } from '../../api/journal'
import { createArticleSlug } from '../../utils/slug'
import AdminShell from '../../components/admin/AdminShell.vue'
import AdminSceneMark from '../../components/admin/AdminSceneMark.vue'
import ArticleForm from '../../components/admin/ArticleForm.vue'
import MarkdownEditor from '../../components/admin/MarkdownEditor.vue'
import PublishActions from '../../components/admin/PublishActions.vue'
import { useUnsavedChanges } from '../../composables/useUnsavedChanges'
import type {
  AdminArticleDetail,
  AdminArticleDraft,
  AdminArticleStatus,
  AdminMediaAsset
} from '../../types/admin'
import type { Taxonomy } from '../../types/api'

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.name === 'admin-article-new')
const articleId = computed(() => Number(route.params.id))
const form = reactive<AdminArticleDraft>({
  slug: '',
  title: '',
  excerpt: '',
  categoryCode: '',
  topicCode: '',
  bodyMarkdown: '',
  coverAssetId: null
})
const article = ref<AdminArticleDetail | null>(null)
const status = computed<AdminArticleStatus>(() => article.value?.status ?? 'DRAFT')
const taxonomies = ref<Taxonomy[]>([])
const loading = ref(!isNew.value)
const busy = ref(false)
const previewing = ref(false)
const errorMessage = ref('')
const notice = ref('')
const previewHtml = ref('')
const previewReadMinutes = ref<number | null>(null)
const conflict = ref(false)
const autoSaveState = ref<'idle' | 'saving' | 'saved' | 'error'>('idle')
const autoSavedAt = ref<Date | null>(null)
const baseline = ref('')
const mediaAssets = ref<AdminMediaAsset[]>([])
const mediaLoading = ref(false)
const mediaUploading = ref(false)
const slugManuallyEdited = ref(false)
let controller: AbortController | null = null
let previewController: AbortController | null = null
let previewTimer: number | undefined
let autoSaveTimer: number | undefined

const snapshot = computed(() => JSON.stringify({
  slug: form.slug,
  title: form.title,
  excerpt: form.excerpt,
  categoryCode: form.categoryCode,
  topicCode: form.topicCode,
  bodyMarkdown: form.bodyMarkdown,
  coverAssetId: form.coverAssetId
}))
const isDirty = computed(() => baseline.value !== '' && snapshot.value !== baseline.value)
const saveState = computed<'idle' | 'dirty' | 'saving' | 'saved' | 'error'>(() => {
  if (busy.value || autoSaveState.value === 'saving') return 'saving'
  if (autoSaveState.value === 'error') return 'error'
  if (isDirty.value) return 'dirty'
  return article.value ? 'saved' : 'idle'
})
const saveStatus = computed(() => {
  if (saveState.value === 'saving') return '保存中…'
  if (saveState.value === 'error') return '自动保存失败'
  if (saveState.value === 'dirty') return status.value === 'DRAFT' && article.value ? '等待自动保存' : '有未保存修改'
  const savedTime = autoSavedAt.value ?? (article.value ? new Date(article.value.updatedAt) : null)
  if (savedTime) return `已保存 · ${savedTime.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  return '新草稿尚未保存'
})
const selectedCoverUrl = computed(() =>
  mediaAssets.value.find(asset => asset.id === form.coverAssetId)?.url
    ?? (article.value?.coverAssetId === form.coverAssetId ? article.value?.coverImageUrl : null)
)
useUnsavedChanges(isDirty)

function applyArticle(next: AdminArticleDetail) {
  article.value = next
  Object.assign(form, {
    slug: next.slug,
    title: next.title,
    excerpt: next.excerpt,
    categoryCode: next.categoryCode,
    topicCode: next.topicCode,
    bodyMarkdown: next.bodyMarkdown,
    coverAssetId: next.coverAssetId
  })
  previewHtml.value = next.bodyHtml
  previewReadMinutes.value = next.readMinutes
  baseline.value = snapshot.value
  conflict.value = false
  slugManuallyEdited.value = true
}

function markSlugEdited() {
  slugManuallyEdited.value = true
}

function generateSlug() {
  const generated = createArticleSlug(form.title)
  if (!generated) {
    errorMessage.value = '先填写文章标题，再生成文章网址。'
    return
  }
  form.slug = generated
  slugManuallyEdited.value = false
  errorMessage.value = ''
  notice.value = '文章网址已根据标题生成，你仍然可以手动修改。'
}

async function loadArticle() {
  if (isNew.value) {
    baseline.value = snapshot.value
    return
  }
  if (!Number.isInteger(articleId.value) || articleId.value <= 0) {
    errorMessage.value = '文章编号无效。'
    loading.value = false
    return
  }
  controller?.abort()
  controller = new AbortController()
  loading.value = true
  errorMessage.value = ''
  try {
    applyArticle(await getAdminArticle(articleId.value, controller.signal))
  } catch (error) {
    if ((error as Error).name !== 'AbortError') errorMessage.value = getErrorMessage(error)
  } finally {
    loading.value = false
  }
}

async function loadMedia() {
  mediaLoading.value = true
  try {
    mediaAssets.value = (await getAdminMedia(0, 18)).items
  } catch (error) {
    errorMessage.value = `素材加载失败：${getErrorMessage(error)}`
  } finally {
    mediaLoading.value = false
  }
}

async function uploadCover(file: File) {
  if (mediaUploading.value) return
  mediaUploading.value = true
  errorMessage.value = ''
  try {
    const uploaded = await uploadAdminMedia(file, form.title.trim())
    mediaAssets.value = [uploaded, ...mediaAssets.value.filter(asset => asset.id !== uploaded.id)]
    form.coverAssetId = uploaded.id
    notice.value = '封面已上传并选中，保存草稿后会与文章关联。'
  } catch (error) {
    handleApiError(error)
  } finally {
    mediaUploading.value = false
  }
}

function validateDraft(): string | null {
  if (!form.title.trim() || !form.slug.trim() || !form.excerpt.trim() || !form.bodyMarkdown.trim()) {
    return '请填写标题、Slug、摘要和正文。'
  }
  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(form.slug)) {
    return 'Slug 只能使用小写字母、数字和连字符。'
  }
  if (!form.categoryCode || !form.topicCode) return '请选择栏目和主题。'
  return null
}

function handleApiError(error: unknown) {
  if (error instanceof ApiError && error.status === 409) {
    if (isNew.value || !article.value) {
      errorMessage.value = '这个 Slug 已被使用，请更换后再创建草稿。'
    } else {
      conflict.value = true
      errorMessage.value = '服务器上的文章已变化。你的未保存内容仍保留在当前页面。'
    }
  } else if (error instanceof ApiError && error.status === 403) {
    errorMessage.value = '当前账号没有执行此操作的权限。'
  } else {
    errorMessage.value = getErrorMessage(error)
  }
}

async function save(mode: 'manual' | 'auto' = 'manual') {
  const validation = validateDraft()
  if (validation) {
    if (mode === 'manual') errorMessage.value = validation
    return null
  }
  if (busy.value) return null
  busy.value = true
  if (mode === 'manual') {
    errorMessage.value = ''
    notice.value = ''
  } else {
    autoSaveState.value = 'saving'
  }
  try {
    if (isNew.value) {
      const created = await createAdminArticle({ ...form })
      applyArticle(created)
      if (mode === 'manual') notice.value = '草稿已创建。'
      await router.replace({ name: 'admin-article-edit', params: { id: created.id } })
      return created
    }
    if (!article.value) return null
    const updated = await updateAdminArticle(article.value.id, {
      ...form,
      version: article.value.version
    })
    applyArticle(updated)
    if (mode === 'manual') notice.value = '修改已保存。'
    else {
      autoSaveState.value = 'saved'
      autoSavedAt.value = new Date()
    }
    return updated
  } catch (error) {
    if (mode === 'auto') autoSaveState.value = 'error'
    handleApiError(error)
    return null
  } finally {
    busy.value = false
  }
}

async function preview(silent = true) {
  if (!form.bodyMarkdown.trim()) {
    previewHtml.value = ''
    previewReadMinutes.value = null
    return
  }
  previewController?.abort()
  previewController = new AbortController()
  previewing.value = true
  try {
    const result = await previewAdminArticle({ bodyMarkdown: form.bodyMarkdown }, previewController.signal)
    previewHtml.value = result.bodyHtml
    previewReadMinutes.value = result.readMinutes
  } catch (error) {
    if ((error as Error).name !== 'AbortError' && !silent) handleApiError(error)
  } finally {
    previewing.value = false
  }
}

function schedulePreview() {
  window.clearTimeout(previewTimer)
  previewTimer = window.setTimeout(() => void preview(), 600)
}

function scheduleAutoSave() {
  window.clearTimeout(autoSaveTimer)
  if (isNew.value || status.value !== 'DRAFT' || !article.value || !isDirty.value || conflict.value) return
  autoSaveState.value = 'idle'
  autoSaveTimer = window.setTimeout(() => void save('auto'), 3000)
}

function extractExcerpt() {
  const plain = form.bodyMarkdown
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
    .replace(/(^|\n)#{1,6}\s+/g, '$1')
    .replace(/[>*_`~-]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  if (!plain) {
    errorMessage.value = '先写一点正文，再提取摘要。'
    return
  }
  form.excerpt = `${plain.slice(0, 140)}${plain.length > 140 ? '…' : ''}`
  notice.value = '已从正文提取摘要，你还可以继续修改。'
}

async function runAction(action: 'publish' | 'unpublish' | 'archive') {
  if (!article.value || busy.value) return
  const prompts = {
    publish: '确认发布这篇文章？发布后会出现在公共页面。',
    unpublish: '确认撤回发布？公共页面将不再显示这篇文章。',
    archive: '确认归档？归档文章不能直接发布。'
  }
  if (!window.confirm(prompts[action])) return

  if (isDirty.value) {
    const saved = await save()
    if (!saved) return
  }

  busy.value = true
  errorMessage.value = ''
  notice.value = ''
  try {
    const request = { version: article.value.version }
    const updated = action === 'publish'
      ? await publishAdminArticle(article.value.id, request)
      : action === 'unpublish'
        ? await unpublishAdminArticle(article.value.id, request)
        : await archiveAdminArticle(article.value.id, request)
    applyArticle(updated)
    notice.value = { publish: '文章已发布。', unpublish: '文章已撤回为草稿。', archive: '文章已归档。' }[action]
  } catch (error) {
    handleApiError(error)
  } finally {
    busy.value = false
  }
}

async function retryWithLatestVersion() {
  if (!article.value) return
  busy.value = true
  errorMessage.value = ''
  const preservedDraft = { ...form }
  try {
    const latest = await getAdminArticle(article.value.id)
    Object.assign(form, preservedDraft)
    article.value = latest
    const updated = await updateAdminArticle(latest.id, { ...preservedDraft, version: latest.version })
    applyArticle(updated)
    notice.value = '已基于服务器最新版本保存当前内容。'
  } catch (error) {
    handleApiError(error)
  } finally {
    busy.value = false
  }
}

async function copyDraft() {
  const text = [
    `# ${form.title}`,
    `Slug: ${form.slug}`,
    `摘要: ${form.excerpt}`,
    '',
    form.bodyMarkdown
  ].join('\n')
  try {
    await navigator.clipboard.writeText(text)
    notice.value = '当前标题、摘要与正文已复制。'
  } catch {
    errorMessage.value = '浏览器未允许复制，请手动复制正文。'
  }
}

async function reloadLatest() {
  if (isDirty.value && !window.confirm('重新加载会丢弃当前未保存内容，确定继续吗？')) return
  await loadArticle()
}

onMounted(async () => {
  void loadMedia()
  try {
    taxonomies.value = await getTaxonomies()
    if (isNew.value && taxonomies.value.length) {
      form.categoryCode = taxonomies.value[0].code
      form.topicCode = taxonomies.value[0].topics[0]?.code ?? ''
    }
  } catch (error) {
    errorMessage.value = `栏目加载失败：${getErrorMessage(error)}`
  }
  await loadArticle()
})
watch(() => form.bodyMarkdown, schedulePreview)
watch(() => form.title, (title) => {
  if (isNew.value && !slugManuallyEdited.value) form.slug = createArticleSlug(title)
})
watch(snapshot, scheduleAutoSave)
onBeforeUnmount(() => {
  controller?.abort()
  previewController?.abort()
  window.clearTimeout(previewTimer)
  window.clearTimeout(autoSaveTimer)
})
</script>

<template>
  <AdminShell :section="isNew ? 'NEW ARTICLE' : 'EDIT ARTICLE'">
    <div v-if="loading" class="editor-state">正在打开稿件…</div>
    <div v-else-if="errorMessage && !article && !isNew" class="editor-state error">
      <p>{{ errorMessage }}</p>
      <button type="button" @click="loadArticle">重试</button>
    </div>
    <form v-else class="article-editor" @submit.prevent="save('manual')">
      <header class="editor-topbar">
        <div class="topbar-copy">
          <AdminSceneMark variant="editor" />
          <div>
          <RouterLink to="/admin/articles">← 返回文章</RouterLink>
          <p>
            <span :data-status="status">{{ { DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '已归档' }[status] }}</span>
            <span v-if="article">VERSION {{ article.version }}</span>
            <span v-if="isDirty">未保存</span>
            <span v-if="status === 'DRAFT' && article" class="autosave" :data-state="autoSaveState">
              {{ autoSaveState === 'saving' ? '自动保存中' : autoSaveState === 'error' ? '自动保存失败' : autoSavedAt ? `${autoSavedAt.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })} 已自动保存` : '草稿自动保存已开启' }}
            </span>
          </p>
          </div>
        </div>
        <PublishActions
          :status="status"
          :busy="busy"
          :is-new="isNew"
          placement="top"
          @publish="runAction('publish')"
          @unpublish="runAction('unpublish')"
          @archive="runAction('archive')"
        />
      </header>

      <div v-if="errorMessage" class="editor-message error" role="alert">{{ errorMessage }}</div>
      <div v-if="notice" class="editor-message notice" role="status">{{ notice }}</div>
      <aside v-if="conflict" class="conflict-panel">
        <div>
          <strong>检测到编辑冲突</strong>
          <p>当前文字没有被覆盖。可以复制备份、载入服务器版本，或用服务器最新版本号重试保存当前内容。</p>
        </div>
        <div>
          <button type="button" @click="copyDraft">复制当前内容</button>
          <button type="button" @click="reloadLatest">载入服务器版本</button>
          <button type="button" :disabled="busy" @click="retryWithLatestVersion">重试保存当前内容</button>
        </div>
      </aside>

      <ArticleForm
        :model-value="form"
        :taxonomies="taxonomies"
        :slug-locked="status === 'PUBLISHED' || Boolean(article?.publishedAt)"
        :cover-image-url="selectedCoverUrl"
        :media-assets="mediaAssets"
        :media-loading="mediaLoading"
        :uploading="mediaUploading"
        :save-status="saveStatus"
        :save-state="saveState"
        @extract-excerpt="extractExcerpt"
        @upload-cover="uploadCover"
        @select-cover="form.coverAssetId = $event"
        @clear-cover="form.coverAssetId = null"
        @slug-edited="markSlugEdited"
        @generate-slug="generateSlug"
        @update:model-value="Object.assign(form, $event)"
      />
      <MarkdownEditor
        v-model="form.bodyMarkdown"
        :preview-html="previewHtml"
        :read-minutes="previewReadMinutes"
        :previewing="previewing"
      />
      <footer class="editor-footer">
        <p v-if="article">
          最后更新 {{ new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(article.updatedAt)) }} · 当前有版本冲突保护，历史快照恢复尚未接入
        </p>
        <p v-else>先保存为草稿，再进行发布。</p>
        <PublishActions
          :status="status"
          :busy="busy"
          :is-new="isNew"
          placement="bottom"
          @publish="runAction('publish')"
          @unpublish="runAction('unpublish')"
          @archive="runAction('archive')"
        />
      </footer>
    </form>
  </AdminShell>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;
.article-editor { padding: 24px; border: 1px solid $border; border-radius: 18px; background: $surface; }
.editor-topbar { position: sticky; z-index: 20; top: 82px; display: flex; align-items: center; justify-content: space-between; gap: 30px; margin: -10px -10px 22px; padding: 12px 14px 16px; border-bottom: 1px solid $border; border-radius: 12px 12px 0 0; background: rgba($surface,.94); backdrop-filter: blur(14px); }
.editor-topbar a { color: $text-secondary; font-size: 11px; font-weight: 700; }
.topbar-copy { display: flex; align-items: center; gap: 10px; }
.editor-topbar a:hover { color: $accent; }
.editor-topbar p { display: flex; gap: 15px; margin: 13px 0 0; }
.editor-topbar p span { padding: 5px 8px; border: 1px solid $border; border-radius: 999px; color: $text-secondary; background: #fff; font-size: 8px; font-weight: 700; letter-spacing: .08em; }
.editor-topbar p span[data-status='DRAFT'] { color: #8a6500; border-color: rgba($yellow,.38); background: rgba($yellow,.12); }
.editor-topbar p span[data-status='PUBLISHED'] { color: $green; border-color: rgba($green,.2); background: rgba($green,.06); }
.editor-topbar p span[data-status='ARCHIVED'] { color: $coral; border-color: rgba($coral,.22); background: rgba($coral,.06); }
.editor-topbar p span.autosave[data-state='saving'] { color: $accent; border-color: rgba($accent,.2); }.editor-topbar p span.autosave[data-state='error'] { color: $coral; border-color: rgba($coral,.25); }
.editor-message { margin: 0 0 18px; padding: 13px 16px; border: 1px solid $border; border-radius: 10px; background: #fff; font-size: 12px; }
.editor-message.error { border-color: rgba($coral,.35); color: $coral; background: rgba($coral,.04); }
.editor-message.notice { border-color: rgba($green,.28); color: $green; background: rgba($green,.05); }
.conflict-panel { display: flex; align-items: center; justify-content: space-between; gap: 30px; margin: 0 0 20px; padding: 20px; border: 1px solid rgba($coral,.35); border-radius: 12px; background: rgba($coral,.04); }
.conflict-panel strong { color: $coral; font-size: 17px; }
.conflict-panel p { max-width: 620px; margin: 7px 0 0; color: $text-secondary; font-size: 12px; }
.conflict-panel > div:last-child { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 12px; }
.conflict-panel button, .editor-state button { padding: 8px 10px; border: 1px solid rgba($accent,.2); border-radius: 8px; color: $accent; background: #fff; font-size: 9px; font-weight: 700; cursor: pointer; }
.editor-footer { display: flex; align-items: center; justify-content: space-between; gap: 30px; margin-top: 22px; padding: 20px 4px 0; border-top: 1px solid $border; }
.editor-footer > p { color: $text-secondary; font-size: 10px; }
.editor-state { min-height: 450px; padding: 150px 0; border: 1px dashed $border; border-radius: 15px; color: $text-secondary; font-size: 18px; text-align: center; }
.editor-state.error { color: $coral; }
@media (max-width: 760px) {
  .editor-topbar, .editor-footer, .conflict-panel { align-items: stretch; flex-direction: column; }
  .editor-topbar { gap: 14px; }
  .editor-topbar p { flex-wrap: wrap; gap: 6px; margin-top: 9px; }
  .editor-topbar p span { flex: none; white-space: nowrap; }
  .conflict-panel > div:last-child { justify-content: flex-start; }
  .article-editor { padding: 14px; }
}
</style>
