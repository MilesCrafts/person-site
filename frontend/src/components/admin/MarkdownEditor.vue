<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'

const props = defineProps<{
  modelValue: string
  previewHtml: string
  readMinutes: number | null
  previewing: boolean
}>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const textarea = ref<HTMLTextAreaElement | null>(null)

const characterCount = computed(() => props.modelValue.replace(/\s/g, '').length)
const wordCount = computed(() => {
  const latinWords = props.modelValue.match(/[A-Za-z0-9]+(?:['’-][A-Za-z0-9]+)*/g)?.length ?? 0
  const chineseCharacters = props.modelValue.match(/[\u3400-\u9fff]/g)?.length ?? 0
  return latinWords + chineseCharacters
})

async function insert(before: string, after = '', placeholder = '文字', linePrefix = false) {
  const element = textarea.value
  if (!element) return
  const start = element.selectionStart
  const end = element.selectionEnd
  const selected = props.modelValue.slice(start, end) || placeholder
  let insertion = `${before}${selected}${after}`
  let insertionStart = start
  if (linePrefix) {
    const lineStart = props.modelValue.lastIndexOf('\n', start - 1) + 1
    insertionStart = lineStart
    const block = props.modelValue.slice(lineStart, end || start)
    insertion = block.split('\n').map(line => `${before}${line || placeholder}`).join('\n')
  }
  const next = props.modelValue.slice(0, insertionStart) + insertion + props.modelValue.slice(end)
  emit('update:modelValue', next)
  await nextTick()
  element.focus()
  const selectionStart = insertionStart + before.length
  element.setSelectionRange(selectionStart, selectionStart + selected.length)
}
</script>

<template>
  <section class="markdown-editor">
    <div class="editor-heading">
      <div><p>BODY / MARKDOWN</p><span>输入后约 0.6 秒自动生成安全预览</span></div>
      <div class="writing-stats"><span>{{ wordCount }} 字词</span><span>{{ characterCount }} 字符</span><span v-if="readMinutes !== null">约 {{ readMinutes }} 分钟</span><span v-if="previewing" class="syncing">预览生成中…</span></div>
    </div>
    <div class="markdown-toolbar" role="toolbar" aria-label="Markdown 快捷工具">
      <div class="tool-group" aria-label="文本样式">
        <button type="button" title="二级标题" @click="insert('## ', '', '小标题', true)">H2</button>
        <button type="button" title="加粗" @click="insert('**', '**', '重点文字')"><strong>B</strong></button>
        <button type="button" title="斜体" @click="insert('*', '*', '强调文字')"><em>I</em></button>
      </div>
      <div class="tool-group" aria-label="段落样式">
        <button type="button" title="引用" @click="insert('> ', '', '引用内容', true)">❝ 引用</button>
        <button type="button" title="无序列表" @click="insert('- ', '', '列表项', true)">• 列表</button>
        <button type="button" title="行内代码" @click="insert('`', '`', 'code')">&lt;/&gt;</button>
      </div>
      <div class="tool-group" aria-label="插入内容">
        <button type="button" title="链接" @click="insert('[', '](https://)', '链接文字')">↗ 链接</button>
        <button type="button" title="插入图片语法" @click="insert('![', '](https://)', '图片说明')">▧ 图片</button>
      </div>
    </div>
    <div class="editor-columns">
      <label><span class="sr-only">Markdown 正文</span><textarea ref="textarea" :value="modelValue" placeholder="从这里开始写作…" spellcheck="true" @input="emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)" /></label>
      <div class="preview-pane" :aria-busy="previewing">
        <div class="preview-meta"><span>LIVE PREVIEW</span><span class="preview-status" :data-state="previewing ? 'syncing' : previewHtml ? 'ready' : 'empty'"><i />{{ previewing ? 'SYNCING…' : previewHtml ? 'READY' : 'WAITING' }}</span></div>
        <article v-if="previewHtml" class="preview-body" v-html="previewHtml" />
        <div v-else class="preview-empty"><span class="empty-sheet" aria-hidden="true"><i /><i /><i /></span><strong>预览还空着</strong><p>开始输入正文，排版会自动出现在这里。</p></div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;
.markdown-editor{margin-top:22px;overflow:hidden;border:1px solid $border;border-radius:15px;background:#fff}.editor-heading{display:flex;align-items:center;justify-content:space-between;gap:30px;padding:18px 20px;border-bottom:1px solid $border}.editor-heading p{margin:0 0 5px;color:$accent;font-size:10px;font-weight:750;letter-spacing:.1em}.editor-heading span{color:$text-secondary;font-size:11px}.writing-stats{display:flex;flex-wrap:wrap;justify-content:flex-end;gap:7px}.writing-stats span{padding:5px 8px;border:1px solid $border;border-radius:999px;background:$surface;font-size:8px;font-weight:700}.writing-stats .syncing{color:$accent;border-color:rgba($accent,.2);background:$accent-subtle}
.markdown-toolbar{display:flex;align-items:center;gap:0;padding:8px 12px;overflow-x:auto;border-bottom:1px solid $border;background:#fcfcfd}.tool-group{display:flex;align-items:center;gap:4px;padding:0 10px}.tool-group:first-child{padding-left:0}.tool-group+ .tool-group{border-left:1px solid $border}.markdown-toolbar button{min-height:32px;padding:0 10px;border:1px solid transparent;border-radius:7px;color:$text-secondary;background:transparent;font-size:10px;font-weight:650;white-space:nowrap;cursor:pointer;transition:border-color .18s ease,color .18s ease,background .18s ease}.markdown-toolbar button:hover,.markdown-toolbar button:focus-visible{border-color:rgba($accent,.16);outline:0;color:$accent;background:$accent-subtle}
.editor-columns{display:grid;grid-template-columns:minmax(0,3fr) minmax(320px,2fr);min-height:620px}.editor-columns>label{display:flex;border-right:1px solid $border}textarea{width:100%;min-height:620px;padding:30px;resize:vertical;border:0;outline:0;color:$text-primary;background:transparent;font:400 14px/1.85 Consolas,'SFMono-Regular',monospace}textarea::placeholder{color:#98a2b3;opacity:1}textarea:focus{background:#fcfcfd;box-shadow:inset 3px 0 0 $accent}.preview-pane{min-width:0;padding:30px 34px 45px;background:#fcfcfd;transition:opacity .2s ease}.preview-pane[aria-busy='true']{opacity:.72}.preview-meta{display:flex;justify-content:space-between;color:$text-secondary;font-size:8px;font-weight:700;letter-spacing:.16em}.preview-status{display:inline-flex;align-items:center;gap:6px}.preview-status i{width:6px;height:6px;border-radius:50%;background:#98a2b3}.preview-status[data-state='ready']{color:$green}.preview-status[data-state='ready'] i{background:$green;box-shadow:0 0 0 3px rgba($green,.1)}.preview-status[data-state='syncing']{color:$accent}.preview-status[data-state='syncing'] i{background:$accent}.preview-body{max-width:680px;margin:45px auto 0}.preview-body :deep(h1),.preview-body :deep(h2),.preview-body :deep(h3){font-family:$sans;font-weight:680;letter-spacing:-.03em}.preview-body :deep(h2){margin:42px 0 18px;font-size:29px}.preview-body :deep(p),.preview-body :deep(li){font-size:16px;line-height:1.85}.preview-body :deep(blockquote){margin:35px 0;padding:16px 20px;border-left:3px solid $accent;border-radius:0 10px 10px 0;color:$accent-strong;background:$accent-subtle;font-size:19px;line-height:1.65}.preview-body :deep(img){max-width:100%}.preview-body :deep(a){color:$accent;border-bottom:1px solid currentColor}.preview-empty{display:flex;align-items:center;flex-direction:column;margin:105px auto 0;color:$text-secondary;text-align:center}.preview-empty strong{margin-top:15px;color:$text-primary;font-size:14px}.preview-empty p{margin:6px 0 0;font-size:11px;line-height:1.6}.empty-sheet{display:flex;width:58px;height:70px;align-items:stretch;justify-content:center;flex-direction:column;gap:7px;padding:14px 11px;border:1px solid rgba($accent,.18);border-radius:8px;background:#fff;box-shadow:5px 5px 0 $accent-subtle;transform:rotate(2deg)}.empty-sheet i{display:block;height:2px;border-radius:999px;background:rgba($accent,.2)}.empty-sheet i:nth-child(2){width:78%}.empty-sheet i:nth-child(3){width:56%}
@media(max-width:900px){.editor-columns{grid-template-columns:1fr}.editor-columns>label{border-right:0;border-bottom:1px solid $border}textarea{min-height:480px}.preview-pane{min-height:480px}.editor-heading{align-items:flex-start;flex-direction:column}.writing-stats{justify-content:flex-start}}
</style>
