<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { getAdminHomepageCopy, updateAdminHomepageCopy } from '../../api/admin'
import { getErrorMessage } from '../../api/client'
import AdminShell from '../../components/admin/AdminShell.vue'
import type { AdminHomepageCopyUpdate } from '../../types/admin'

const form = reactive<AdminHomepageCopyUpdate>({
  eyebrow: '',
  headlinePrimary: '',
  headlineEmphasis: '',
  headlineAccent: '',
  description: '',
  paperLabel: '',
  paperLineOne: '',
  paperLineTwo: '',
  paperLineThree: '',
  paperFooter: '',
  version: 0
})
const loading = ref(true)
const saving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const controller = new AbortController()

async function loadCopy() {
  loading.value = true
  errorMessage.value = ''
  try {
    const copy = await getAdminHomepageCopy(controller.signal)
    Object.assign(form, copy)
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') return
    errorMessage.value = getErrorMessage(error)
  } finally {
    loading.value = false
  }
}

async function saveCopy() {
  if (saving.value) return
  saving.value = true
  errorMessage.value = ''
  successMessage.value = ''
  try {
    const saved = await updateAdminHomepageCopy({
      eyebrow: form.eyebrow,
      headlinePrimary: form.headlinePrimary,
      headlineEmphasis: form.headlineEmphasis,
      headlineAccent: form.headlineAccent,
      description: form.description,
      paperLabel: form.paperLabel,
      paperLineOne: form.paperLineOne,
      paperLineTwo: form.paperLineTwo,
      paperLineThree: form.paperLineThree,
      paperFooter: form.paperFooter,
      version: form.version
    })
    Object.assign(form, saved)
    successMessage.value = '首页文案已保存，刷新首页即可看到新内容。'
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    saving.value = false
  }
}

onMounted(loadCopy)
onBeforeUnmount(() => controller.abort())
</script>

<template>
  <AdminShell section="HOMEPAGE COPY">
    <div class="copy-page">
      <header class="copy-heading">
        <div><p><i /> HOMEPAGE MESSAGE</p><h1>首页想说的话</h1></div>
        <p>这些文字会显示在首页首屏。三段标题分别保留深色、黄色标记和品牌蓝的视觉层级。</p>
      </header>

      <p v-if="errorMessage" class="notice notice-error" role="alert">{{ errorMessage }}</p>
      <p v-if="successMessage" class="notice notice-success" role="status">{{ successMessage }}</p>

      <div v-if="loading" class="loading-card" aria-busy="true">正在读取首页文案…</div>
      <div v-else class="copy-workspace">
        <form class="copy-form" @submit.prevent="saveCopy">
          <label>眉题 <span>{{ form.eyebrow.length }} / 120</span><input v-model.trim="form.eyebrow" maxlength="120" required /></label>
          <div class="title-fields">
            <label>标题第一行 <span>{{ form.headlinePrimary.length }} / 80</span><input v-model.trim="form.headlinePrimary" maxlength="80" required /></label>
            <label>标题第二行 <span>{{ form.headlineEmphasis.length }} / 80</span><input v-model.trim="form.headlineEmphasis" maxlength="80" required /></label>
            <label>标题第三行 <span>{{ form.headlineAccent.length }} / 80</span><input v-model.trim="form.headlineAccent" maxlength="80" required /></label>
          </div>
          <label>说明文字 <span>{{ form.description.length }} / 500</span><textarea v-model.trim="form.description" maxlength="500" rows="5" required /></label>
          <section class="form-section" aria-labelledby="paper-copy-title">
            <div class="form-section-heading"><div><small>INDEX 02</small><h2 id="paper-copy-title">右侧纸片文案</h2></div><p>对应首页轮播中的“正在搭建”这一张。</p></div>
            <label>小标题 <span>{{ form.paperLabel.length }} / 40</span><input v-model.trim="form.paperLabel" maxlength="40" required /></label>
            <div class="title-fields">
              <label>纸片第一行 <span>{{ form.paperLineOne.length }} / 120</span><input v-model.trim="form.paperLineOne" maxlength="120" required /></label>
              <label>纸片第二行 <span>{{ form.paperLineTwo.length }} / 120</span><input v-model.trim="form.paperLineTwo" maxlength="120" required /></label>
              <label>纸片第三行 <span>{{ form.paperLineThree.length }} / 120</span><input v-model.trim="form.paperLineThree" maxlength="120" required /></label>
            </div>
            <label>底部英文 <span>{{ form.paperFooter.length }} / 80</span><input v-model.trim="form.paperFooter" maxlength="80" required /></label>
          </section>
          <div class="form-actions">
            <RouterLink to="/" target="_blank">打开首页预览 ↗</RouterLink>
            <button type="submit" :disabled="saving">{{ saving ? '正在保存…' : '保存首页文案' }}</button>
          </div>
        </form>

        <aside class="copy-preview" aria-label="首页文案预览">
          <div class="preview-bar"><i /><i /><i /><span>LIVE PREVIEW</span></div>
          <div class="preview-content">
            <p class="preview-eyebrow"><i /> {{ form.eyebrow || '眉题' }}</p>
            <h2>{{ form.headlinePrimary || '标题第一行' }}<br /><span>{{ form.headlineEmphasis || '标题第二行' }}</span><br /><strong>{{ form.headlineAccent || '标题第三行' }}</strong></h2>
            <p class="preview-description">{{ form.description || '这里会显示首页说明文字。' }}</p>
          </div>
          <div class="paper-preview">
            <div class="paper-preview-index">LEKANG / INDEX 02</div>
            <p>{{ form.paperLabel || '小标题' }}</p>
            <strong><span>{{ form.paperLineOne || '纸片第一行' }}</span><span>{{ form.paperLineTwo || '纸片第二行' }}</span><span>{{ form.paperLineThree || '纸片第三行' }}</span></strong>
            <div class="paper-preview-lines" aria-hidden="true"><i /><i /><i /></div>
            <small>{{ form.paperFooter || 'BOTTOM COPY' }} ↗</small>
          </div>
          <small class="preview-note">预览只展示文案层级，实际首页会沿用现有响应式布局。</small>
        </aside>
      </div>
    </div>
  </AdminShell>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;
.copy-page { padding-bottom: 40px; }
.copy-heading { display: grid; grid-template-columns: minmax(0,1fr) minmax(280px,.55fr); gap: 50px; align-items: end; padding: 20px 0 42px; border-bottom: 1px solid $border; }
.copy-heading > div > p { display: flex; align-items: center; gap: 9px; margin: 0 0 14px; color: $accent; font-size: 10px; font-weight: 750; letter-spacing: .12em; }
.copy-heading > div > p i, .preview-eyebrow i { width: 8px; height: 8px; border-radius: 50%; background: $green; box-shadow: 0 0 0 4px rgba($green,.12); }
.copy-heading h1 { margin: 0; font-size: clamp(42px,6vw,72px); line-height: 1; letter-spacing: -.055em; }
.copy-heading > p { margin: 0; color: $text-secondary; font-size: 14px; line-height: 1.8; }
.notice { margin: 20px 0 0; padding: 12px 15px; border-radius: 10px; font-size: 12px; }
.notice-error { color: #a43124; background: rgba($coral,.09); }.notice-success { color: #19704f; background: rgba($green,.1); }
.loading-card { margin-top: 28px; padding: 60px; border: 1px solid $border; border-radius: 16px; color: $text-secondary; text-align: center; }
.copy-workspace { display: grid; grid-template-columns: minmax(0,1fr) minmax(360px,.82fr); gap: 28px; align-items: start; margin-top: 28px; }
.copy-form, .copy-preview { border: 1px solid $border; border-radius: 16px; background: #fff; }
.copy-form { display: grid; gap: 20px; padding: 26px; }
.copy-form label { display: grid; grid-template-columns: 1fr auto; gap: 8px; color: $text-primary; font-size: 12px; font-weight: 700; }
.copy-form label > span { color: $text-secondary; font-size: 9px; font-weight: 600; }
.copy-form input, .copy-form textarea { grid-column: 1 / -1; width: 100%; border: 1px solid $border; border-radius: 9px; color: $text-primary; background: #fcfcfd; font: inherit; font-weight: 500; line-height: 1.6; outline: none; transition: border-color .2s, box-shadow .2s; }
.copy-form input { height: 44px; padding: 0 13px; }.copy-form textarea { padding: 11px 13px; resize: vertical; }
.copy-form input:focus, .copy-form textarea:focus { border-color: rgba($accent,.65); box-shadow: 0 0 0 3px rgba($accent,.08); }
.title-fields { display: grid; gap: 14px; padding: 18px; border-radius: 12px; background: $surface; }
.form-section { display: grid; gap: 18px; margin-top: 5px; padding-top: 25px; border-top: 1px solid $border; }
.form-section-heading { display: flex; justify-content: space-between; gap: 20px; align-items: end; }
.form-section-heading small { color: $coral; font-size: 8px; font-weight: 750; letter-spacing: .12em; }
.form-section-heading h2 { margin: 4px 0 0; font-size: 22px; letter-spacing: -.035em; }
.form-section-heading p { max-width: 200px; margin: 0; color: $text-secondary; font-size: 10px; text-align: right; }
.form-actions { display: flex; justify-content: space-between; align-items: center; padding-top: 5px; }
.form-actions a { color: $text-secondary; font-size: 11px; font-weight: 650; }.form-actions a:hover { color: $accent; }
.form-actions button { min-width: 150px; min-height: 42px; border: 0; border-radius: 9px; color: #fff; background: $accent; font-size: 12px; font-weight: 750; cursor: pointer; }
.form-actions button:disabled { cursor: wait; opacity: .6; }
.copy-preview { position: sticky; top: 106px; padding: 18px; overflow: hidden; box-shadow: 0 22px 48px -38px rgba(16,24,40,.35); }
.preview-bar { display: flex; gap: 5px; align-items: center; padding-bottom: 14px; border-bottom: 1px solid $border; }
.preview-bar i { width: 7px; height: 7px; border-radius: 50%; }.preview-bar i:nth-child(1) { background: $coral; }.preview-bar i:nth-child(2) { background: $yellow; }.preview-bar i:nth-child(3) { background: $green; }
.preview-bar span { margin-left: auto; color: $text-secondary; font-size: 8px; font-weight: 750; letter-spacing: .12em; }
.preview-content { padding: 42px 12px 34px; }
.preview-eyebrow { display: flex; align-items: center; gap: 9px; color: $accent; font-size: 9px; font-weight: 750; letter-spacing: .08em; }
.preview-content h2 { margin: 22px 0; font-size: clamp(38px,4.2vw,58px); line-height: .96; letter-spacing: -.065em; }
.preview-content h2 span { position: relative; z-index: 0; }.preview-content h2 span::after { position: absolute; z-index: -1; right: 0; bottom: .04em; left: 0; height: .15em; background: $yellow; content: ''; }
.preview-content h2 strong { color: $accent; font-weight: inherit; }
.preview-description { color: $text-secondary; font-size: 13px; line-height: 1.75; }
.paper-preview { position: relative; margin: 0 2px 18px; padding: 38px 24px 24px; overflow: hidden; border: 1px solid rgba($accent,.16); border-radius: 13px; background: #fff; box-shadow: 0 18px 35px -30px rgba(16,24,40,.35); }
.paper-preview::before { position: absolute; top: -24px; right: -16px; width: 82px; height: 64px; border-radius: 13px; background: $accent; content: ''; transform: rotate(8deg); }
.paper-preview-index { position: absolute; top: 14px; right: 15px; color: $text-secondary; font-size: 7px; font-weight: 750; letter-spacing: .08em; }
.paper-preview > p { margin: 18px 0 10px; color: $coral; font-size: 10px; font-weight: 750; }
.paper-preview > strong { display: grid; font-size: clamp(24px,2.5vw,35px); line-height: 1.08; letter-spacing: -.05em; }
.paper-preview-lines { display: grid; gap: 6px; margin: 20px 0 24px; }.paper-preview-lines i { height: 6px; border-radius: 99px; background: #eceef2; }.paper-preview-lines i:nth-child(2) { width: 76%; background: rgba($accent,.18); }.paper-preview-lines i:nth-child(3) { width: 46%; background: rgba($coral,.2); }
.paper-preview > small { color: $text-secondary; font-size: 8px; font-weight: 750; letter-spacing: .07em; }
.preview-note { display: block; padding: 12px; border-top: 1px solid $border; color: $text-secondary; font-size: 9px; }
@media (max-width: 900px) { .copy-heading, .copy-workspace { grid-template-columns: 1fr; }.copy-preview { position: static; }.copy-heading { gap: 20px; } }
@media (max-width: 560px) { .copy-heading { padding-top: 4px; }.copy-form { padding: 18px; }.copy-workspace { gap: 18px; }.form-section-heading { align-items: start; flex-direction: column; gap: 7px; }.form-section-heading p { text-align: left; }.form-actions { align-items: stretch; flex-direction: column-reverse; gap: 14px; }.form-actions button { width: 100%; }.preview-content { padding-inline: 5px; } }
</style>
