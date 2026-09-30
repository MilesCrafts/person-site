<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { archiveAdminArticle, getAdminArticles, getAdminHomepageFeatures, updateAdminHomepageFeatures } from '../../api/admin'
import { getErrorMessage } from '../../api/client'
import AdminShell from '../../components/admin/AdminShell.vue'
import AdminSceneMark from '../../components/admin/AdminSceneMark.vue'
import type { AdminArticleSort, AdminArticleStatus, AdminArticleSummary, AdminHomepageFeature, SortDirection } from '../../types/admin'

const route = useRoute()
const router = useRouter()
const articles = ref<AdminArticleSummary[]>([])
const page = ref(0)
const totalPages = ref(0)
const totalItems = ref(0)
const loading = ref(false)
const mutating = ref(false)
const errorMessage = ref('')
const notice = ref('')
const featured = ref<AdminHomepageFeature[]>([])
const featureSaving = ref(false)
const searchInput = ref(typeof route.query.q === 'string' ? route.query.q : '')
const selectedIds = ref<number[]>([])
const listRoot = ref<HTMLElement | null>(null)
let controller: AbortController | null = null
let searchTimer: number | undefined
let rowAnimation: { revert: () => void } | null = null

const statusOptions: Array<{ value: '' | AdminArticleStatus; label: string }> = [
  { value: '', label: '全部' }, { value: 'DRAFT', label: '草稿' },
  { value: 'PUBLISHED', label: '已发布' }, { value: 'ARCHIVED', label: '归档' }
]
const selectedStatus = computed(() => {
  const value = route.query.status
  return value === 'DRAFT' || value === 'PUBLISHED' || value === 'ARCHIVED' ? value : ''
})
const selectedSort = computed(() => {
  const sort = route.query.sort
  return sort === 'createdAt' || sort === 'title' ? sort : 'updatedAt'
})
const selectedDirection = computed<SortDirection>(() => route.query.direction === 'asc' ? 'asc' : 'desc')
const sortControl = computed(() => `${selectedSort.value}:${selectedDirection.value}`)
const allSelectable = computed(() => articles.value.filter(item => item.status !== 'ARCHIVED'))
const allChecked = computed(() => allSelectable.value.length > 0 && allSelectable.value.every(item => selectedIds.value.includes(item.id)))
const selectedArticles = computed(() => articles.value.filter(item => selectedIds.value.includes(item.id)))

function formatDate(value: string | null) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value))
}
function statusLabel(status: AdminArticleStatus) {
  return { DRAFT: '草稿', PUBLISHED: '已发布', ARCHIVED: '归档' }[status]
}

function replaceQuery(patch: Record<string, string | undefined>) {
  const query = { ...route.query, ...patch }
  Object.keys(query).forEach(key => { if (!query[key]) delete query[key] })
  void router.replace({ query })
}

async function load(nextPage = 0) {
  controller?.abort()
  controller = new AbortController()
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getAdminArticles(nextPage, 20, {
      status: selectedStatus.value || undefined,
      q: typeof route.query.q === 'string' ? route.query.q : '',
      sort: selectedSort.value as AdminArticleSort,
      direction: selectedDirection.value
    }, controller.signal)
    articles.value = result.items
    page.value = result.page
    totalPages.value = result.totalPages
    totalItems.value = result.totalItems
    selectedIds.value = []
  } catch (error) {
    if ((error as Error).name !== 'AbortError') errorMessage.value = getErrorMessage(error)
  } finally { loading.value = false }

  await nextTick()
  if (listRoot.value && articles.value.length && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    try {
      const { gsap } = await import('gsap')
      rowAnimation?.revert()
      rowAnimation = gsap.context(() => {
        gsap.from('.article-table article', { y: 16, opacity: 0, duration: .42, stagger: .045, ease: 'power3.out', clearProps: 'transform,opacity' })
      }, listRoot.value)
    } catch { /* 静态列表仍可使用 */ }
  }
}

function changeStatus(status: string) { replaceQuery({ status: status || undefined }) }
function changeSort(value: string) {
  const [sort, direction] = value.split(':') as [AdminArticleSort, SortDirection]
  replaceQuery({ sort: sort === 'updatedAt' ? undefined : sort, direction: direction === 'desc' ? undefined : direction })
}
function queueSearch() {
  window.clearTimeout(searchTimer)
  searchTimer = window.setTimeout(() => replaceQuery({ q: searchInput.value.trim() || undefined }), 380)
}
function clearSearch() { searchInput.value = ''; replaceQuery({ q: undefined }) }
function toggleAll() { selectedIds.value = allChecked.value ? [] : allSelectable.value.map(item => item.id) }

async function loadFeatures() {
  try {
    featured.value = await getAdminHomepageFeatures()
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  }
}

async function saveFeatures(articleIds: number[], message: string) {
  featureSaving.value = true
  errorMessage.value = ''
  notice.value = ''
  try {
    featured.value = await updateAdminHomepageFeatures(articleIds)
    notice.value = message
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
    await loadFeatures()
  } finally {
    featureSaving.value = false
  }
}

function toggleFeature(article: AdminArticleSummary) {
  const ids = featured.value.map(item => item.articleId)
  const existing = ids.indexOf(article.id)
  if (existing >= 0) {
    ids.splice(existing, 1)
    void saveFeatures(ids, `已将《${article.title}》移出首页精选。`)
    return
  }
  if (article.status !== 'PUBLISHED') {
    errorMessage.value = '只有已经公开发布的文章可以加入首页精选。'
    return
  }
  if (ids.length >= 5) {
    errorMessage.value = '首页最多保留 5 篇精选，请先移除一篇。'
    return
  }
  void saveFeatures([...ids, article.id], `已将《${article.title}》加入首页精选。`)
}

function moveFeature(index: number, direction: -1 | 1) {
  const target = index + direction
  if (target < 0 || target >= featured.value.length) return
  const ids = featured.value.map(item => item.articleId)
  ;[ids[index], ids[target]] = [ids[target], ids[index]]
  void saveFeatures(ids, '首页精选顺序已更新。')
}

async function copyLink(article: AdminArticleSummary) {
  try {
    await navigator.clipboard.writeText(`${window.location.origin}/article/${article.slug}`)
    notice.value = `已复制《${article.title}》的公开链接。`
  } catch { errorMessage.value = '浏览器未允许复制链接。' }
}

async function archiveArticles(targets: AdminArticleSummary[]) {
  const candidates = targets.filter(item => item.status !== 'ARCHIVED')
  if (!candidates.length || !window.confirm(`确认归档选中的 ${candidates.length} 篇文章？`)) return
  mutating.value = true
  errorMessage.value = ''
  notice.value = ''
  let completed = 0
  try {
    for (const article of candidates) {
      await archiveAdminArticle(article.id, { version: article.version })
      completed += 1
    }
    notice.value = `已归档 ${completed} 篇文章。`
    await load(page.value)
  } catch (error) {
    errorMessage.value = `${completed ? `已完成 ${completed} 篇；` : ''}${getErrorMessage(error)}`
    await load(page.value)
  } finally { mutating.value = false }
}

watch(() => route.fullPath, () => {
  searchInput.value = typeof route.query.q === 'string' ? route.query.q : ''
  void load(0)
}, { immediate: true })
void loadFeatures()
onBeforeUnmount(() => { controller?.abort(); rowAnimation?.revert(); window.clearTimeout(searchTimer) })
</script>

<template>
  <AdminShell section="ARTICLES">
    <div ref="listRoot" class="article-library">
      <header class="list-heading">
        <div>
          <p><i /> CONTENT LIBRARY / {{ totalItems }} ITEMS</p>
          <h1>你的文章，<span>都在这里。</span></h1>
          <small>搜索、整理、预览，给每一篇文字留一个清楚的位置。</small>
        </div>
        <div class="heading-action"><AdminSceneMark variant="library" /><RouterLink to="/admin/articles/new">写一篇 <span>＋</span></RouterLink></div>
      </header>

      <section class="library-tools" aria-label="文章筛选和排序">
        <label class="search-box">
          <span class="search-icon" aria-hidden="true" />
          <input v-model="searchInput" type="search" placeholder="搜索标题或 slug" @input="queueSearch">
          <button v-if="searchInput" type="button" aria-label="清空搜索" @click="clearSearch">×</button>
        </label>
        <div class="filter-tools">
          <nav class="status-filter" aria-label="文章状态">
            <button v-for="option in statusOptions" :key="option.value" type="button" :class="{ active: selectedStatus === option.value }" @click="changeStatus(option.value)">{{ option.label }}</button>
          </nav>
          <label class="mobile-status-box"><span>状态</span><select :value="selectedStatus" @change="changeStatus(($event.target as HTMLSelectElement).value)"><option v-for="option in statusOptions" :key="option.value" :value="option.value">{{ option.label }}</option></select></label>
          <label class="sort-box"><span>排序</span><select :value="sortControl" @change="changeSort(($event.target as HTMLSelectElement).value)"><option value="updatedAt:desc">最近更新</option><option value="createdAt:desc">最近创建</option><option value="title:asc">标题 A–Z</option><option value="title:desc">标题 Z–A</option></select></label>
        </div>
      </section>

      <section class="featured-editor" aria-labelledby="featured-editor-title">
        <div class="featured-editor-copy">
          <p>HOMEPAGE PICKS / {{ featured.length }} OF 5</p>
          <h2 id="featured-editor-title">首页精选</h2>
          <small>这里的顺序，就是读者在首页翻到稿纸的顺序。</small>
        </div>
        <div v-if="featured.length" class="featured-strip">
          <article v-for="(item, index) in featured" :key="item.articleId">
            <span>{{ String(index + 1).padStart(2, '0') }}</span>
            <strong :title="item.title">{{ item.title }}</strong>
            <div>
              <button type="button" :disabled="featureSaving || index === 0" :aria-label="`将${item.title}前移`" @click="moveFeature(index, -1)">←</button>
              <button type="button" :disabled="featureSaving || index === featured.length - 1" :aria-label="`将${item.title}后移`" @click="moveFeature(index, 1)">→</button>
              <button type="button" :disabled="featureSaving" :aria-label="`移除${item.title}`" @click="saveFeatures(featured.filter(feature => feature.articleId !== item.articleId).map(feature => feature.articleId), `已将《${item.title}》移出首页精选。`)">×</button>
            </div>
          </article>
        </div>
        <p v-else class="featured-empty">还没有精选文章。可在下方已发布文章旁点击“☆ 精选”。</p>
      </section>

      <div v-if="selectedIds.length" class="bulk-bar">
        <span>已选择 <strong>{{ selectedIds.length }}</strong> 篇</span>
        <small>删除采用安全归档，不会物理移除文章</small>
        <button type="button" :disabled="mutating" @click="archiveArticles(selectedArticles)">{{ mutating ? '正在归档…' : '批量归档' }}</button>
        <button type="button" @click="selectedIds = []">取消选择</button>
      </div>
      <p v-if="notice" class="list-notice" role="status">{{ notice }}</p>
      <div v-if="loading" class="list-state" aria-live="polite">正在整理文章档案…</div>
      <div v-else-if="errorMessage" class="list-state error" role="alert"><p>{{ errorMessage }}</p><button type="button" @click="load(page)">重试</button></div>
      <div v-else-if="!articles.length" class="list-state">
        <strong>{{ route.query.q ? '没有找到匹配的文章' : selectedStatus ? `这里还没有${statusLabel(selectedStatus)}` : '文章库还是空的' }}</strong>
        <p>{{ route.query.q ? '换个标题关键词或 slug 试试。' : '从一篇不必完美的草稿开始。' }}</p>
        <RouterLink v-if="!route.query.q" to="/admin/articles/new">写第一篇 →</RouterLink>
        <button v-else type="button" @click="clearSearch">清空搜索</button>
      </div>
      <div v-else class="article-table">
        <div class="table-head"><label><input type="checkbox" :checked="allChecked" aria-label="全选当前页可归档文章" @change="toggleAll"></label><span>文章</span><span>栏目 / 主题</span><span>状态</span><span>更新时间</span><span>操作</span></div>
        <article v-for="(article, index) in articles" :key="article.id">
          <label class="row-check"><input type="checkbox" :disabled="article.status === 'ARCHIVED'" :checked="selectedIds.includes(article.id)" :aria-label="`选择${article.title}`" @change="selectedIds = selectedIds.includes(article.id) ? selectedIds.filter(id => id !== article.id) : [...selectedIds, article.id]"></label>
          <span class="article-number">{{ String(page * 20 + index + 1).padStart(2, '0') }}</span>
          <div class="article-title"><h2 :title="article.title">{{ article.title }}</h2><p :title="`/article/${article.slug}`">/article/{{ article.slug }}</p></div>
          <div class="taxonomy"><span>{{ article.categoryCode }}</span><span>{{ article.topicCode }}</span></div>
          <p class="status" :data-status="article.status"><i />{{ statusLabel(article.status) }}</p>
          <time :datetime="article.updatedAt">{{ formatDate(article.updatedAt) }}</time>
          <div class="row-actions">
            <button
              type="button"
              class="feature-action"
              :class="{ active: featured.some(item => item.articleId === article.id) }"
              :disabled="featureSaving || (article.status !== 'PUBLISHED' && !featured.some(item => item.articleId === article.id))"
              :title="article.status === 'PUBLISHED' ? '设置首页精选' : '发布后才可设置精选'"
              @click="toggleFeature(article)"
            >{{ featured.some(item => item.articleId === article.id) ? '★ 已精选' : '☆ 精选' }}</button>
            <RouterLink class="edit-action" :to="`/admin/articles/${article.id}/edit`">编辑 <span>→</span></RouterLink>
            <details class="more-menu">
              <summary aria-label="更多操作" title="更多操作">•••</summary>
              <div>
                <RouterLink v-if="article.status === 'PUBLISHED'" :to="`/article/${article.slug}`" target="_blank">预览文章 <span>↗</span></RouterLink>
                <button type="button" @click="copyLink(article)">复制公开链接</button>
                <button v-if="article.status !== 'ARCHIVED'" type="button" class="danger" :disabled="mutating" @click="archiveArticles([article])">归档文章</button>
              </div>
            </details>
          </div>
        </article>
      </div>
      <div v-if="totalPages > 1" class="list-pagination"><button type="button" :disabled="page === 0" @click="load(page - 1)">← 上一页</button><span>{{ page + 1 }} / {{ totalPages }}</span><button type="button" :disabled="page + 1 >= totalPages" @click="load(page + 1)">下一页 →</button></div>
    </div>
  </AdminShell>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;
.list-heading { display:flex; align-items:flex-end; justify-content:space-between; gap:35px; padding:28px 0 38px; }
.list-heading p { display:flex; align-items:center; gap:9px; margin:0 0 14px; color:$accent; font-size:10px; font-weight:750; letter-spacing:.11em; }.list-heading p i { width:8px; height:8px; border-radius:50%; background:$coral; box-shadow:0 0 0 4px rgba($coral,.1); }
h1 { margin:0; font-size:clamp(48px,6vw,76px); font-weight:680; line-height:.98; letter-spacing:-.06em; }h1 span { color:$accent; }.list-heading small { display:block; margin-top:18px; color:$text-secondary; font-size:13px; }
.list-heading a { display:inline-flex; align-items:center; padding:13px 17px; border:1px solid $text-primary; border-radius:10px; color:#fff; background:$text-primary; font-size:11px; font-weight:700; white-space:nowrap; }.list-heading a:hover { border-color:$accent; background:$accent; transform:translateY(-2px); }.list-heading a span { margin-left:18px; font-size:17px; }
.heading-action{display:flex;align-items:center;gap:14px}
.library-tools { display:grid; grid-template-columns:minmax(390px,1.45fr) auto; align-items:center; gap:24px; margin-bottom:18px; }
.featured-editor { display:grid; grid-template-columns:minmax(190px,.55fr) 1.45fr; gap:24px; margin:0 0 18px; padding:18px; border:1px solid rgba($accent,.16); border-radius:13px; background:linear-gradient(105deg,rgba($accent,.045),#fff 42%); }
.featured-editor-copy p { margin:0 0 7px; color:$accent; font-size:8px; font-weight:750; letter-spacing:.12em; }
.featured-editor-copy h2 { margin:0; font-size:23px; letter-spacing:-.035em; }
.featured-editor-copy small { display:block; margin-top:7px; color:$text-secondary; font-size:9px; }
.featured-strip { display:flex; min-width:0; gap:8px; align-items:stretch; }
.featured-strip article { display:grid; min-width:0; flex:1; grid-template-columns:auto minmax(0,1fr); gap:5px 8px; padding:10px; border:1px solid $border; border-radius:10px; background:#fff; box-shadow:0 6px 16px rgba(16,24,40,.035); }
.featured-strip article>span { color:$accent; font-size:8px; font-weight:800; }
.featured-strip strong { overflow:hidden; font-size:10px; line-height:1.35; text-overflow:ellipsis; white-space:nowrap; }
.featured-strip article>div { display:flex; grid-column:1/-1; gap:4px; }
.featured-strip button { width:24px; height:22px; padding:0; border:1px solid $border; border-radius:6px; color:$text-secondary; background:$surface; cursor:pointer; }
.featured-strip button:last-child { margin-left:auto; }
.featured-strip button:disabled { opacity:.35; cursor:default; }
.featured-empty { align-self:center; margin:0; color:$text-secondary; font-size:10px; }
.search-box { display:flex; min-height:48px; align-items:center; gap:11px; padding:0 15px; border:1px solid transparent; border-radius:11px; background:$surface; transition:border-color .2s ease,background .2s ease,box-shadow .2s ease; }.search-box:hover { border-color:rgba($accent,.16); background:#fff; }.search-box:focus-within { border-color:rgba($accent,.72); background:#fff; box-shadow:0 0 0 4px rgba($accent,.1); }.search-icon { position:relative; width:13px; height:13px; flex:0 0 13px; border:1.7px solid $accent; border-radius:50%; }.search-icon::after { position:absolute; right:-4px; bottom:-2px; width:5px; height:1.7px; border-radius:999px; background:$accent; content:''; transform:rotate(45deg); transform-origin:left center; }.search-box input { width:100%; height:20px; padding:0; border:0; outline:0; color:$text-primary; background:transparent; font:400 12px/20px $sans; }.search-box input::placeholder { color:#8f99aa; }.search-box button { display:grid; width:24px; height:24px; flex:0 0 24px; place-items:center; padding:0; border:0; color:$text-secondary; background:transparent; font-size:18px; line-height:1; cursor:pointer; }
.status-filter { display:flex; gap:5px; padding:4px; border:1px solid $border; border-radius:999px; background:$surface; }.status-filter button { min-height:36px; padding:0 14px; border:0; border-radius:999px; color:$text-secondary; background:transparent; font-size:11px; font-weight:700; cursor:pointer; }.status-filter button.active { color:$accent-strong; background:#fff; box-shadow:0 3px 12px rgba(16,24,40,.07); }
.filter-tools { display:flex; min-width:0; align-items:center; justify-content:flex-end; gap:10px; }
.sort-box { display:flex; min-height:46px; align-items:center; gap:10px; padding:0 12px; border:1px solid $border; border-radius:11px; background:#fff; }.sort-box span { color:$text-secondary; font-size:9px; font-weight:700; }.sort-box select { border:0; outline:0; color:$text-primary; background:#fff; font-size:11px; font-weight:650; }
.mobile-status-box { display:none; min-height:46px; align-items:center; gap:10px; padding:0 12px; border:1px solid $border; border-radius:11px; background:#fff; }.mobile-status-box span { color:$text-secondary; font-size:9px; font-weight:700; }.mobile-status-box select { min-width:0; flex:1; border:0; outline:0; color:$text-primary; background:#fff; font-size:11px; font-weight:650; }
.bulk-bar { position:sticky; z-index:18; top:86px; display:flex; align-items:center; gap:12px; margin:0 0 12px; padding:11px 14px; border:1px solid rgba($accent,.2); border-radius:10px; color:$accent-strong; background:rgba(242,245,255,.96); box-shadow:0 10px 26px rgba(16,24,40,.08); backdrop-filter:blur(12px); font-size:11px; font-weight:700; }.bulk-bar strong { font-size:15px; }.bulk-bar small { color:$text-secondary; font-size:9px; font-weight:500; }.bulk-bar button { padding:7px 10px; border:1px solid rgba($accent,.22); border-radius:7px; color:$accent; background:#fff; font-size:9px; font-weight:700; cursor:pointer; }.bulk-bar button:first-of-type { margin-left:auto; color:#fff; border-color:$accent; background:$accent; }
.list-notice { margin:0 0 12px; padding:11px 14px; border:1px solid rgba($green,.22); border-radius:9px; color:$green; background:rgba($green,.05); font-size:11px; }
.table-head { display:grid; grid-template-columns:24px minmax(300px,2fr) 1fr .65fr .72fr minmax(150px,.7fr); gap:20px; padding:12px 18px; color:#98a2b3; font-size:8px; font-weight:700; letter-spacing:.1em; }.table-head input, .row-check input { accent-color:$accent; }
.article-table { display:grid; gap:10px; }.article-table article { position:relative; display:grid; grid-template-columns:24px 30px minmax(260px,2fr) 1fr .65fr .72fr minmax(150px,.7fr); align-items:center; gap:16px; min-height:110px; padding:14px 18px; border:1px solid $border; border-radius:13px; background:#fff; transition:border-color .2s ease,box-shadow .2s ease,transform .2s ease; }.article-table article:hover { border-color:rgba($accent,.28); box-shadow:0 12px 28px rgba(16,24,40,.06); transform:translateY(-2px)!important; }.article-table article:focus-within { z-index:6; }
.row-check { display:flex; }.article-number { color:$accent; font-size:11px; font-weight:750; }.article-title { min-width:0; max-width:100%; }.article-title h2 { max-width:100%; margin:0 0 8px; overflow:hidden; font-size:clamp(17px,2vw,22px); font-weight:680; line-height:1.25; letter-spacing:-.025em; text-overflow:ellipsis; white-space:nowrap; }.article-title p,time { margin:0; color:$text-secondary; font-size:8px; letter-spacing:.08em; }.article-title p { max-width:100%; overflow:hidden; font-family:Consolas,monospace; text-overflow:ellipsis; white-space:nowrap; }
.taxonomy { display:flex; flex-wrap:wrap; gap:6px; }.taxonomy span { padding:5px 8px; border:1px solid $border; border-radius:999px; color:$text-secondary; background:$surface; font-size:8px; font-weight:700; letter-spacing:.06em; }
.status { display:inline-flex; align-items:center; gap:7px; margin:0; color:$text-secondary; font-size:9px; font-weight:700; }.status i { width:7px; height:7px; border-radius:50%; background:#98a2b3; }.status[data-status='PUBLISHED'] { color:$green; }.status[data-status='PUBLISHED'] i { background:$green; box-shadow:0 0 0 4px rgba($green,.1); }.status[data-status='DRAFT'] { color:#667085; }.status[data-status='DRAFT'] i { background:#98a2b3; box-shadow:0 0 0 4px rgba(152,162,179,.1); }.status[data-status='ARCHIVED'] { color:#b54708; }.status[data-status='ARCHIVED'] i { background:#f79009; box-shadow:0 0 0 4px rgba(247,144,9,.1); }
.row-actions { position:relative; display:flex; align-items:center; justify-content:flex-end; gap:7px; }.edit-action { display:inline-flex; min-height:31px; align-items:center; gap:12px; padding:0 10px; border:1px solid rgba($accent,.25); border-radius:8px; color:$accent; background:$accent-subtle; font-size:9px; font-weight:750; }.edit-action:hover { color:#fff; border-color:$accent; background:$accent; }.edit-action span { font-size:13px; }.more-menu { position:relative; }.more-menu summary { display:grid; width:32px; height:31px; place-items:center; border:1px solid $border; border-radius:8px; color:$text-secondary; background:#fff; font-size:10px; font-weight:800; letter-spacing:1px; list-style:none; cursor:pointer; }.more-menu summary::-webkit-details-marker { display:none; }.more-menu[open] summary,.more-menu summary:hover { color:$accent; border-color:rgba($accent,.3); }.more-menu>div { position:absolute; z-index:30; top:38px; right:0; display:grid; width:156px; padding:6px; border:1px solid $border; border-radius:10px; background:#fff; box-shadow:0 16px 34px rgba(16,24,40,.14); }.more-menu>div a,.more-menu>div button { display:flex; min-height:34px; align-items:center; justify-content:space-between; padding:0 9px; border:0; border-radius:7px; color:$text-secondary; background:transparent; font-size:9px; font-weight:650; text-align:left; cursor:pointer; }.more-menu>div a:hover,.more-menu>div button:hover { color:$accent; background:$accent-subtle; }.more-menu>div .danger:hover { color:$coral; background:rgba($coral,.06); }
.list-state { min-height:300px; padding:100px 20px; border:1px dashed $border; border-radius:14px; color:$text-secondary; text-align:center; }.list-state strong { display:block; color:$text-primary; font-size:20px; }.list-state p { margin:9px 0 16px; font-size:12px; }.list-state a,.list-state button { border:0; border-bottom:1px solid $accent; color:$accent; background:transparent; font-size:11px; cursor:pointer; }.list-state.error { color:$coral; }
.list-pagination { display:flex; align-items:center; justify-content:space-between; padding:32px 0; }.list-pagination button { border:0; background:transparent; font-size:9px; font-weight:700; letter-spacing:.14em; cursor:pointer; }.list-pagination button:disabled { color:$border; cursor:default; }.list-pagination span { color:$text-secondary; font:400 14px $serif; }
@media(max-width:1050px){.library-tools{grid-template-columns:1fr;gap:12px}.filter-tools{justify-content:flex-start}.table-head{display:none}.article-table article{grid-template-columns:24px 28px minmax(0,1fr) auto}.article-title{grid-column:3}.taxonomy,.status,time{grid-column:3}.row-actions{grid-column:4;grid-row:1/span 4}.article-number{align-self:start;padding-top:4px}}
@media(max-width:620px){.list-heading{align-items:flex-start;flex-direction:column}.filter-tools{display:grid;grid-template-columns:1fr 1fr;align-items:stretch}.status-filter{display:none}.mobile-status-box{display:flex}.sort-box,.mobile-status-box{width:100%}.article-table article{grid-template-columns:20px minmax(0,1fr)}.article-number{display:none}.article-title,.taxonomy,.status,time,.row-actions{grid-column:2}.row-actions{grid-row:auto;justify-content:flex-start}.bulk-bar{top:128px;flex-wrap:wrap}.bulk-bar small{width:100%;order:2}.bulk-bar button:first-of-type{margin-left:0}.more-menu>div{right:auto;left:0}}
.feature-action { min-height:31px; padding:0 9px; border:1px solid $border; border-radius:8px; color:$text-secondary; background:#fff; font-size:9px; font-weight:750; cursor:pointer; white-space:nowrap; }
.feature-action:hover:not(:disabled),.feature-action.active { color:$accent-strong; border-color:rgba($accent,.3); background:$accent-subtle; }
.feature-action:disabled { opacity:.38; cursor:not-allowed; }
@media(max-width:1050px){.featured-editor{grid-template-columns:1fr}.featured-strip{flex-wrap:wrap}.featured-strip article{flex:1 1 180px}}
</style>
