<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { getAdminArticles } from '../../api/admin'
import { getErrorMessage } from '../../api/client'
import type { AdminArticleSummary } from '../../types/admin'

type ActivityView = 'daily' | 'weekly' | 'total'
type ActivityRecord = { updates: number; publishes: number }
type DayCell = ActivityRecord & { date: Date; key: string; future: boolean; level: number }
type DeskPrompt = { id: number; index: string; title: string; note: string; action: string }

const MS_PER_DAY = 86_400_000
const WEEK_COUNT = 53
const view = ref<ActivityView>('daily')
const articles = ref<AdminArticleSummary[]>([])
const loading = ref(true)
const errorMessage = ref('')
let controller: AbortController | null = null

function startOfDay(value: Date): Date {
  return new Date(value.getFullYear(), value.getMonth(), value.getDate())
}

function addDays(value: Date, amount: number): Date {
  const next = new Date(value)
  next.setDate(next.getDate() + amount)
  return next
}

function dateKey(value: Date): string {
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function parseDate(value: string): Date | null {
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? null : parsed
}

function levelFor(value: number, scale: 'day' | 'week' | 'month'): number {
  if (value <= 0) return 0
  const thresholds = scale === 'day' ? [1, 2, 3] : scale === 'week' ? [1, 3, 5] : [1, 4, 8]
  if (value <= thresholds[0]) return 1
  if (value <= thresholds[1]) return 2
  if (value <= thresholds[2]) return 3
  return 4
}

const today = startOfDay(new Date())
const calendarEnd = addDays(today, 7 - (today.getDay() || 7))
const calendarStart = addDays(calendarEnd, -(WEEK_COUNT * 7 - 1))

const activityByDay = computed(() => {
  const result = new Map<string, ActivityRecord>()
  const add = (value: string | null, kind: keyof ActivityRecord) => {
    if (!value) return
    const parsed = parseDate(value)
    if (!parsed) return
    const key = dateKey(parsed)
    const current = result.get(key) ?? { updates: 0, publishes: 0 }
    current[kind] += 1
    result.set(key, current)
  }
  articles.value.forEach((article) => {
    add(article.updatedAt, 'updates')
    add(article.publishedAt, 'publishes')
  })
  return result
})

const days = computed<DayCell[]>(() => Array.from({ length: WEEK_COUNT * 7 }, (_, index) => {
  const date = addDays(calendarStart, index)
  const record = activityByDay.value.get(dateKey(date)) ?? { updates: 0, publishes: 0 }
  const total = record.updates + record.publishes
  return { date, key: dateKey(date), future: date > today, ...record, level: levelFor(total, 'day') }
}))

const monthLabels = computed(() => {
  const labels: Array<{ label: string; column: number }> = []
  let previousMonth = -1
  for (let column = 0; column < WEEK_COUNT; column += 1) {
    const date = days.value[column * 7]?.date
    if (date && date.getMonth() !== previousMonth) {
      labels.push({ label: `${date.getMonth() + 1}月`, column: column + 1 })
      previousMonth = date.getMonth()
    }
  }
  return labels
})

const weeks = computed(() => Array.from({ length: WEEK_COUNT }, (_, index) => {
  const weekDays = days.value.slice(index * 7, index * 7 + 7).filter(day => !day.future)
  const updates = weekDays.reduce((sum, day) => sum + day.updates, 0)
  const publishes = weekDays.reduce((sum, day) => sum + day.publishes, 0)
  const start = weekDays[0]?.date ?? addDays(calendarStart, index * 7)
  const end = weekDays.at(-1)?.date ?? addDays(start, 6)
  return { key: dateKey(start), start, end, updates, publishes, level: levelFor(updates + publishes, 'week') }
}))

const maxWeeklyActivity = computed(() => Math.max(1, ...weeks.value.map(week => week.updates + week.publishes)))

const months = computed(() => {
  const result = new Map<string, { date: Date; updates: number; publishes: number }>()
  days.value.filter(day => !day.future).forEach((day) => {
    const key = `${day.date.getFullYear()}-${day.date.getMonth()}`
    const current = result.get(key) ?? { date: new Date(day.date.getFullYear(), day.date.getMonth(), 1), updates: 0, publishes: 0 }
    current.updates += day.updates
    current.publishes += day.publishes
    result.set(key, current)
  })
  return [...result.values()].slice(-12).map(month => ({ ...month, key: `${month.date.getFullYear()}-${month.date.getMonth()}`, level: levelFor(month.updates + month.publishes, 'month') }))
})

const activeDayKeys = computed(() => days.value.filter(day => !day.future && day.level > 0).map(day => day.key))
const summary = computed(() => {
  const publishedLastYear = articles.value.filter((article) => {
    const date = article.publishedAt ? parseDate(article.publishedAt) : null
    return date && date >= calendarStart && date <= today
  }).length
  let longest = 0
  let run = 0
  let previous: Date | null = null
  activeDayKeys.value.forEach((key) => {
    const current = new Date(`${key}T00:00:00`)
    run = previous && current.getTime() - previous.getTime() === MS_PER_DAY ? run + 1 : 1
    longest = Math.max(longest, run)
    previous = current
  })
  const mostActive = months.value.reduce<(typeof months.value)[number] | null>((best, month) => {
    if (!best) return month
    return month.updates + month.publishes > best.updates + best.publishes ? month : best
  }, null)
  const mostActiveValue = mostActive && mostActive.updates + mostActive.publishes > 0 ? `${mostActive.date.getMonth() + 1}月` : '待启程'
  return { publishedLastYear, longest, mostActiveValue }
})

const deskPrompts = computed<DeskPrompt[]>(() => {
  const drafts = articles.value
    .filter(article => article.status === 'DRAFT')
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
  const candidates: Array<{ article: AdminArticleSummary | undefined; note: string; action: string }> = [
    { article: drafts[0], note: drafts[0] ? `上次停在 ${relativeTime(drafts[0].updatedAt)}` : '', action: '接着写' },
    { article: drafts.length > 1 ? drafts.at(-1) : undefined, note: drafts.length > 1 ? `已经安静了 ${daysSince(drafts.at(-1)!.updatedAt)} 天` : '', action: '看一眼' },
    { article: drafts.length > 2 ? drafts[1] : undefined, note: drafts.length > 2 ? `${relativeTime(drafts[1]!.updatedAt)}还动过笔` : '', action: '继续' }
  ]
  const used = new Set<number>()
  return candidates.flatMap(({ article, note, action }) => {
    if (!article || used.has(article.id)) return []
    used.add(article.id)
    return [{ id: article.id, index: String(used.size).padStart(2, '0'), title: article.title, note, action }]
  })
})

function fullDate(value: Date): string {
  return `${value.getFullYear()}年${value.getMonth() + 1}月${value.getDate()}日`
}

function recordText(record: ActivityRecord): string {
  const parts = []
  if (record.updates) parts.push(`更新 ${record.updates} 篇`)
  if (record.publishes) parts.push(`发布 ${record.publishes} 篇`)
  return parts.length ? parts.join(' · ') : '这天没有留下记录'
}

function weeklyBarHeight(record: ActivityRecord): string {
  const total = record.updates + record.publishes
  if (total === 0) return '2px'
  const ratio = total / maxWeeklyActivity.value
  return `${Math.round(18 + ratio * 82)}%`
}

function daysSince(value: string): number {
  const date = parseDate(value)
  return date ? Math.max(0, Math.floor((today.getTime() - startOfDay(date).getTime()) / MS_PER_DAY)) : 0
}

function relativeTime(value: string): string {
  const difference = daysSince(value)
  if (difference === 0) return '今天'
  if (difference === 1) return '昨天'
  if (difference < 30) return `${difference} 天前`
  const date = parseDate(value)
  return date ? `${date.getMonth() + 1}月${date.getDate()}日` : '不久前'
}

async function loadActivity() {
  controller?.abort()
  controller = new AbortController()
  loading.value = true
  errorMessage.value = ''
  try {
    const result: AdminArticleSummary[] = []
    let page = 0
    let hasNext = true
    while (hasNext) {
      const response = await getAdminArticles(page, 50, { sort: 'updatedAt', direction: 'desc' }, controller.signal)
      result.push(...response.items)
      hasNext = response.hasNext
      page += 1
    }
    articles.value = result
  } catch (error) {
    if (!(error instanceof DOMException && error.name === 'AbortError')) errorMessage.value = getErrorMessage(error)
  } finally {
    loading.value = false
  }
}

onMounted(loadActivity)
onBeforeUnmount(() => controller?.abort())
</script>

<template>
  <section class="activity-panel" aria-labelledby="activity-title">
    <header class="activity-header">
      <div>
        <h2 id="activity-title"><i />写作活跃度</h2>
        <p>不是为了追赶数字，只是回头看看一路写下来的痕迹。</p>
      </div>
      <div class="activity-tabs" role="tablist" aria-label="活跃度统计粒度">
        <button v-for="item in ([['daily', '逐日'], ['weekly', '周度'], ['total', '月度']] as const)" :key="item[0]" type="button" role="tab" :aria-selected="view === item[0]" :class="{ active: view === item[0] }" @click="view = item[0]">{{ item[1] }}</button>
      </div>
    </header>

    <div v-if="loading" class="activity-state" aria-live="polite"><i />正在整理这一年的写作足迹…</div>
    <div v-else-if="errorMessage" class="activity-state activity-error" role="alert">
      <span>{{ errorMessage }}</span><button type="button" @click="loadActivity">重新读取</button>
    </div>
    <div v-else class="activity-content">
      <div class="activity-layout">
        <div class="activity-history">
          <div v-if="activeDayKeys.length < 12" class="activity-gentle-start">
            <i>✦</i>
            <p><strong>{{ activeDayKeys.length ? '足迹刚刚开始' : '第一格还在等你' }}</strong><span>{{ activeDayKeys.length ? `这一年有 ${activeDayKeys.length} 天留下记录。空白也没关系，它会随着每一次写作慢慢亮起来。` : '写下第一篇草稿后，这里就会留下属于你的第一点蓝色。' }}</span></p>
          </div>
          <div class="activity-chart-scroll" tabindex="0" aria-label="写作活跃度热力图，可横向滚动">
        <div v-if="view === 'daily'" class="daily-chart">
          <div class="month-row" aria-hidden="true"><span v-for="month in monthLabels" :key="`${month.label}-${month.column}`" :style="{ gridColumn: month.column }">{{ month.label }}</span></div>
          <div class="day-labels" aria-hidden="true"><span>周一</span><span>周二</span><span>周三</span><span>周四</span><span>周五</span><span>周六</span><span>周日</span></div>
          <div class="heat-grid">
            <span v-for="day in days" :key="day.key" class="heat-cell-wrap">
              <i class="heat-cell" :class="[`level-${day.level}`, { future: day.future }]" :tabindex="day.future ? -1 : 0" :aria-label="day.future ? undefined : `${fullDate(day.date)}，${recordText(day)}`" />
              <span v-if="!day.future" class="cell-tooltip">{{ fullDate(day.date) }}<strong>{{ recordText(day) }}</strong></span>
            </span>
          </div>
        </div>

        <div v-else-if="view === 'weekly'" class="aggregate-chart weekly-chart">
          <span v-for="week in weeks" :key="week.key" class="weekly-column" tabindex="0" :aria-label="`${fullDate(week.start)}至${fullDate(week.end)}，${recordText(week)}`">
            <i class="weekly-bar" :class="`level-${week.level}`" :style="{ height: weeklyBarHeight(week) }" />
            <span class="cell-tooltip">{{ fullDate(week.start) }} — {{ fullDate(week.end) }}<strong>{{ recordText(week) }}</strong></span>
          </span>
        </div>

        <div v-else class="aggregate-chart month-chart">
          <span v-for="month in months" :key="month.key" class="month-cell" :class="`level-${month.level}`" tabindex="0" :aria-label="`${month.date.getFullYear()}年${month.date.getMonth() + 1}月，${recordText(month)}`">
            <b>{{ month.date.getMonth() + 1 }}月</b><small>{{ month.updates + month.publishes ? month.updates + month.publishes : '—' }}</small>
            <span class="cell-tooltip">{{ month.date.getFullYear() }}年{{ month.date.getMonth() + 1 }}月<strong>{{ recordText(month) }}</strong></span>
          </span>
        </div>
          </div>

          <div class="activity-legend" aria-label="颜色越深，写作活动越多"><span>少</span><i v-for="level in 5" :key="level" :class="`level-${level - 1}`" /><span>多</span></div>
          <div class="activity-summary">
            <div><strong>{{ summary.publishedLastYear }}</strong><p><b>篇</b> · 这一年送到读者面前</p></div>
            <div><strong>{{ summary.longest }}</strong><p><b>天</b> · 最长的一段连续书写</p></div>
            <div><strong>{{ summary.mostActiveValue }}</strong><p>留下最多痕迹的月份</p></div>
          </div>
        </div>

        <aside class="desk-prompts" aria-labelledby="desk-prompts-title">
          <div class="prompt-heading">
            <p>DESK REMINDERS</p>
            <h3 id="desk-prompts-title">留在桌边</h3>
            <span>未完成的句子，等你有空再回来看看。</span>
          </div>
          <div v-if="deskPrompts.length" class="prompt-list">
            <RouterLink v-for="prompt in deskPrompts" :key="prompt.id" :to="`/admin/articles/${prompt.id}/edit`" class="prompt-item">
              <small>{{ prompt.index }}</small>
              <span><strong>{{ prompt.title }}</strong><em>{{ prompt.note }}</em></span>
              <b>{{ prompt.action }} <i>→</i></b>
            </RouterLink>
          </div>
          <div v-else class="prompt-empty">
            <i>✓</i><p>没有搁置的草稿。<br><span>想写的时候，再开始一篇。</span></p>
          </div>
          <RouterLink class="prompt-new" to="/admin/articles/new">放下一张新稿纸 <span>＋</span></RouterLink>
        </aside>
      </div>
      <p class="activity-source">按文章最近更新与发布时间回顾 · 从今天开始，慢慢积累就好</p>
    </div>
  </section>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;

.activity-panel { margin-top: 52px; padding: 38px 0 0; border-top: 1px solid $border; }
.activity-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 28px; margin-bottom: 29px; }
.activity-header h2 { display: flex; align-items: center; gap: 10px; margin: 0; font-size: 20px; font-weight: 760; letter-spacing: -.02em; }
.activity-header h2 i { width: 9px; height: 9px; border-radius: 50%; background: $accent; box-shadow: 0 0 0 4px rgba($accent,.1); }
.activity-header p { margin: 9px 0 0; color: $text-secondary; font-size: 13px; }
.activity-tabs { display: inline-flex; flex: 0 0 auto; padding: 3px; border-radius: 10px; background: #f2f4f7; }
.activity-tabs button { min-width: 57px; padding: 8px 12px; border: 0; border-radius: 8px; color: $text-secondary; background: transparent; font-size: 12px; font-weight: 680; cursor: pointer; }
.activity-tabs button.active { color: $text-primary; background: #fff; box-shadow: 0 1px 4px rgba(16,24,40,.12); }
.activity-tabs button:focus-visible { outline: 2px solid rgba($accent,.34); outline-offset: 2px; }
.activity-content { padding: 26px 28px 0; border: 1px solid $border; border-radius: 14px; background: #fff; }
.activity-layout { display: grid; grid-template-columns: minmax(0,1fr) 292px; gap: 28px; }
.activity-history { min-width: 0; }
.activity-gentle-start { display: flex; align-items: center; gap: 12px; margin: 0 0 18px; padding: 12px 14px; border: 1px solid rgba($accent,.13); border-radius: 10px; color: $accent-strong; background: rgba($accent,.035); }
.activity-gentle-start > i { display: grid; width: 28px; height: 28px; flex: 0 0 28px; place-items: center; border-radius: 8px; background: $accent-subtle; font-style: normal; }
.activity-gentle-start p { display: grid; gap: 3px; margin: 0; }.activity-gentle-start strong { font-size: 11px; }.activity-gentle-start span { color: $text-secondary; font-size: 10px; line-height: 1.5; }
.activity-state { display: flex; min-height: 220px; align-items: center; justify-content: center; gap: 10px; border: 1px solid $border; border-radius: 14px; color: $text-secondary; background: #fff; font-size: 12px; }
.activity-state > i { width: 9px; height: 9px; border-radius: 50%; background: $accent; animation: activity-pulse 1s ease-in-out infinite alternate; }
.activity-error { flex-direction: column; }.activity-error button { padding: 7px 11px; border: 1px solid $border; border-radius: 8px; color: $accent; background: #fff; cursor: pointer; }
@keyframes activity-pulse { to { opacity: .25; } }
.activity-chart-scroll { overflow-x: auto; padding: 3px 1px 13px; outline: none; scrollbar-color: #d0d5dd transparent; scrollbar-width: thin; }
.activity-chart-scroll:focus-visible { border-radius: 8px; box-shadow: 0 0 0 3px rgba($accent,.1); }
.daily-chart { position: relative; width: 778px; padding: 24px 0 50px 38px; }
.month-row { position: absolute; top: 0; left: 38px; display: grid; width: 739px; grid-template-columns: repeat(53,11px); column-gap: 3px; }
.month-row span { color: #667085; font-size: 10px; font-weight: 600; white-space: nowrap; }
.day-labels { position: absolute; top: 28px; left: 0; display: grid; height: 95px; grid-template-rows: repeat(7,11px); row-gap: 3px; color: #667085; font-size: 9px; font-weight: 600; }
.heat-grid { display: grid; grid-auto-flow: column; grid-template-rows: repeat(7,11px); grid-auto-columns: 11px; gap: 3px; }
.heat-cell-wrap, .weekly-column, .month-cell { position: relative; }
.heat-cell { display: block; width: 11px; height: 11px; border-radius: 3px; background: #edeef2; }
.heat-cell.future { background: transparent; }
.level-1 { background: #cee2f7 !important; }.level-2 { background: #85b7eb !important; }.level-3 { background: #378add !important; }.level-4 { background: #0c447c !important; }
.heat-cell:focus-visible, .weekly-column:focus-visible, .month-cell:focus-visible { outline: 2px solid $accent; outline-offset: 2px; }
.cell-tooltip { position: absolute; z-index: 5; top: calc(100% + 8px); left: 50%; display: none; min-width: 180px; padding: 9px 11px; border: 1px solid rgba(12,68,124,.18); border-radius: 8px; color: #fff; background: #101828; box-shadow: 0 8px 20px rgba(16,24,40,.14); font-size: 10px; line-height: 1.5; pointer-events: none; transform: translateX(-50%); }
.cell-tooltip strong { display: block; color: #cee2f7; font-size: 11px; font-weight: 650; white-space: nowrap; }
.heat-cell-wrap:hover .cell-tooltip, .heat-cell-wrap:focus-within .cell-tooltip, .weekly-column:hover .cell-tooltip, .weekly-column:focus .cell-tooltip, .month-cell:hover .cell-tooltip, .month-cell:focus .cell-tooltip { display: block; }
.aggregate-chart { display: grid; min-width: 739px; min-height: 95px; align-items: stretch; gap: 3px; padding: 23px 0 50px; }
.weekly-chart { height: 145px; grid-template-columns: repeat(53,11px); align-items: end; padding-top: 14px; background: repeating-linear-gradient(to top, transparent 0 35px, rgba(228,231,236,.65) 35px 36px); }
.weekly-column { display: flex; height: 110px; align-items: flex-end; border-bottom: 1px solid #d0d5dd; border-radius: 3px 3px 0 0; }
.weekly-bar { display: block; width: 100%; min-height: 2px; border-radius: 3px 3px 2px 2px; background: #dfe2e8; transition: height .28s ease, background .2s ease; }
.month-chart { min-width: 780px; grid-template-columns: repeat(12,1fr); gap: 8px; }
.month-cell { display: flex; min-height: 82px; flex-direction: column; justify-content: space-between; padding: 9px; border-radius: 8px; color: #475467; background: #edeef2; }
.month-cell b { font-size: 9px; }.month-cell small { font-size: 18px; font-weight: 760; }
.month-cell.level-3, .month-cell.level-4 { color: #fff; }
.activity-legend { display: flex; align-items: center; justify-content: flex-start; gap: 6px; margin-left: 38px; padding: 12px 0 24px; color: #667085; font-size: 10px; }
.activity-legend i { width: 12px; height: 12px; border-radius: 3px; background: #edeef2; }
.activity-summary { display: grid; grid-template-columns: repeat(3,1fr); border-top: 1px solid $border; }
.activity-summary > div { display: flex; min-width: 0; align-items: baseline; gap: 10px; padding: 25px 18px; }
.activity-summary > div + div { border-left: 1px solid $border; }
.activity-summary strong { color: $text-primary; font-size: 35px; font-weight: 730; line-height: 1; letter-spacing: -.04em; white-space: nowrap; }
.activity-summary p { margin: 0; color: #667085; font-size: 11px; line-height: 1.5; }.activity-summary p b { color: #475467; font-weight: 700; }
.desk-prompts { display: flex; min-width: 0; flex-direction: column; margin: -7px 0 18px; padding: 22px 20px 18px; border: 1px solid rgba($accent,.15); border-radius: 13px; background: linear-gradient(180deg, rgba($accent,.035), #fff 34%); }
.prompt-heading { padding-bottom: 17px; border-bottom: 1px solid $border; }
.prompt-heading p { margin: 0 0 10px; color: $accent; font-size: 9px; font-weight: 780; letter-spacing: .13em; }
.prompt-heading h3 { margin: 0; font-size: 18px; font-weight: 750; letter-spacing: -.02em; }
.prompt-heading span { display: block; margin-top: 7px; color: #667085; font-size: 11px; line-height: 1.55; }
.prompt-list { display: grid; }
.prompt-item { position: relative; display: grid; grid-template-columns: 23px minmax(0,1fr) auto; gap: 8px; align-items: center; padding: 16px 0; border-bottom: 1px solid rgba(228,231,236,.76); color: $text-primary; }
.prompt-item::before { position: absolute; top: 18px; bottom: 18px; left: 0; width: 2px; border-radius: 99px; background: rgba($accent,.18); content: ''; transition: background .2s ease; }
.prompt-item:hover::before, .prompt-item:focus-visible::before { background: $accent; }
.prompt-item > small { padding-left: 8px; color: rgba($accent,.58); font-size: 8px; font-weight: 760; }
.prompt-item > span { display: grid; min-width: 0; gap: 5px; }
.prompt-item strong { overflow: hidden; font-size: 12px; font-weight: 710; text-overflow: ellipsis; white-space: nowrap; }
.prompt-item em { overflow: hidden; color: #667085; font-size: 10px; font-style: normal; text-overflow: ellipsis; white-space: nowrap; }
.prompt-item > b { color: $accent; font-size: 10px; font-weight: 720; white-space: nowrap; }.prompt-item > b i { display: inline-block; font-size: 13px; font-style: normal; transition: transform .2s ease; }
.prompt-item:hover > b i { transform: translateX(3px); }.prompt-item:focus-visible { border-radius: 4px; outline: 2px solid rgba($accent,.28); outline-offset: 3px; }
.prompt-empty { display: flex; align-items: center; gap: 10px; padding: 24px 0; color: #475467; }.prompt-empty > i { display: grid; width: 28px; height: 28px; flex: 0 0 28px; place-items: center; border-radius: 9px; color: $accent; background: $accent-subtle; font-style: normal; }.prompt-empty p { margin: 0; font-size: 11px; font-weight: 680; line-height: 1.5; }.prompt-empty span { color: $text-secondary; font-size: 9px; font-weight: 500; }
.prompt-new { display: flex; align-items: center; justify-content: space-between; margin-top: auto; padding: 11px 12px; border: 1px dashed rgba($accent,.27); border-radius: 9px; color: $accent-strong; background: rgba($accent,.025); font-size: 11px; font-weight: 680; }.prompt-new span { font-size: 16px; font-weight: 500; }.prompt-new:hover { border-color: rgba($accent,.5); background: $accent-subtle; }
.activity-source { margin: 0; padding: 14px 0 17px; border-top: 1px solid rgba(228,231,236,.7); color: #667085; font-size: 10px; text-align: center; }

@media (max-width: 1050px) { .activity-layout { grid-template-columns: 1fr; }.desk-prompts { margin-top: 0; }.prompt-item { grid-template-columns: 25px minmax(0,1fr) auto; } }
@media (max-width: 700px) {
  .activity-panel { margin-top: 38px; padding-top: 30px; }
  .activity-header { flex-direction: column; gap: 18px; }.activity-tabs { width: 100%; }.activity-tabs button { flex: 1; }
  .activity-content { padding: 21px 16px 0; }.activity-summary { grid-template-columns: 1fr; }
  .activity-summary > div { padding: 19px 4px; }.activity-summary > div + div { border-top: 1px solid $border; border-left: 0; }
  .activity-summary strong { min-width: 62px; font-size: 31px; }
  .desk-prompts { padding: 20px 17px 16px; }
}
@media (prefers-reduced-motion: reduce) { .activity-state > i { animation: none; } }
</style>
