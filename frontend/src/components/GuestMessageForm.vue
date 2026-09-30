<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { createGuestMessage } from '../api/journal'
import { getErrorMessage } from '../api/client'

const form = reactive({ name: '', contact: '', message: '', website: '' })
const messageBox = ref<HTMLElement | null>(null)
const formElement = ref<HTMLFormElement | null>(null)
const submitButton = ref<HTMLButtonElement | null>(null)
const paperPlane = ref<HTMLElement | null>(null)
const deliveryPath = ref<SVGPathElement | null>(null)
const sending = ref(false)
const sentMessage = ref('')
const errorMessage = ref('')

async function animateDelivery() {
  if (!messageBox.value || !submitButton.value || !paperPlane.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  try {
    const { gsap } = await import('gsap')
    const path = deliveryPath.value
    if (!path) return
    const pathLength = path.getTotalLength()
    const progress = { value: 0 }
    paperPlane.value.classList.remove('plane-idle')
    await new Promise<void>(resolve => {
      gsap.timeline({ onComplete: resolve })
        .set(deliveryPath.value, { opacity: 1 })
        .set(paperPlane.value, { xPercent: -50, yPercent: -50, scale: 1.2, opacity: 1 })
        .to(progress, {
          value: 1,
          duration: 1.55,
          ease: 'power1.inOut',
          onUpdate: () => {
            const distance = pathLength * progress.value
            const point = path.getPointAtLength(distance)
            const ahead = path.getPointAtLength(Math.min(pathLength, distance + 2))
            const scaleX = messageBox.value!.clientWidth / 1000
            const scaleY = messageBox.value!.clientHeight / 500
            const angle = Math.atan2((ahead.y - point.y) * scaleY, (ahead.x - point.x) * scaleX) * 180 / Math.PI
            gsap.set(paperPlane.value, {
              x: point.x * scaleX,
              y: point.y * scaleY,
              rotate: angle,
              scale: 1.2 - progress.value * 1.12,
              opacity: 1 - progress.value * .94
            })
          }
        })
        .set([paperPlane.value, deliveryPath.value], { opacity: 0 })
        .set(paperPlane.value, { clearProps: 'transform' })
        .add(() => paperPlane.value?.classList.add('plane-idle'))
    })
  } catch {
    // 发送成功不依赖动画。
  }
}

async function submit() {
  if (sending.value) return
  sending.value = true
  errorMessage.value = ''
  sentMessage.value = ''
  const controller = new AbortController()
  const timeout = window.setTimeout(() => controller.abort(), 12_000)
  try {
    const response = await createGuestMessage({ ...form }, controller.signal)
    await animateDelivery()
    sentMessage.value = response.message
    Object.assign(form, { name: '', contact: '', message: '', website: '' })
    await nextTick()
    formElement.value?.querySelector<HTMLInputElement>('input')?.focus()
  } catch (error) {
    errorMessage.value = error instanceof DOMException && error.name === 'AbortError'
      ? '投递等待超时，按钮已恢复。请稍后重试；为避免重复留言，可先到管理员收件箱确认。'
      : getErrorMessage(error)
  } finally {
    window.clearTimeout(timeout)
    sending.value = false
  }
}
</script>

<template>
  <div id="message" ref="messageBox" class="message-box">
    <svg ref="deliveryPath" class="delivery-path" viewBox="0 0 1000 500" aria-hidden="true"><path d="M220 125 C 390 105 560 65 900 20"/></svg>
    <div ref="paperPlane" class="paper-plane plane-idle" aria-hidden="true">
      <span class="plane-trail"></span>
      <svg viewBox="0 0 76 64" role="presentation"><path d="M4 29.5 69 4 48 59 34 39 4 29.5Z"/><path d="m34 39 35-35-44 30 9 5Z"/><path d="m34 39 8 15 6-20-14 5Z"/></svg>
    </div>
    <div class="message-box-intro">
      <p class="eyebrow">PRIVATE INBOX / 01</p>
      <h2>把想说的，<br />留在这里。</h2>
      <p>表单提交后，内容会进入管理员收件箱。</p>
    </div>
    <form ref="formElement" class="message-form" @submit.prevent="submit">
      <div class="message-form-row">
        <label>怎么称呼你 <small>必填</small><input v-model.trim="form.name" maxlength="60" autocomplete="name" required /></label>
        <label>联系方式 <small>选填</small><input v-model.trim="form.contact" maxlength="120" autocomplete="email" placeholder="邮箱或其他方式" /></label>
      </div>
      <label>想说的话 <span>{{ form.message.length }} / 2000</span><textarea v-model.trim="form.message" maxlength="2000" rows="6" required /></label>
      <label class="message-honeypot" aria-hidden="true">网站<input v-model="form.website" tabindex="-1" autocomplete="off" /></label>
      <p v-if="errorMessage" class="message-feedback error" role="alert">{{ errorMessage }}</p>
      <p v-if="sentMessage" class="message-feedback success" role="status">{{ sentMessage }}</p>
      <div class="message-form-actions"><small>留言会经过基础防滥用保护</small><button ref="submitButton" type="submit" :disabled="sending">{{ sending ? '正在投递…' : '投进收件箱' }} <span>↗</span></button></div>
    </form>
  </div>
</template>

<style scoped lang="scss">
@use '../assets/styles/variables' as *;
.message-box { position: relative; display: grid; grid-template-columns: minmax(0,.76fr) minmax(420px,1fr); gap: clamp(40px,7vw,90px); margin: 42px 0 96px; padding: clamp(38px,5vw,68px); overflow: hidden; border: 1px solid rgba($yellow,.7); border-radius: 18px; background: #fffdf5; scroll-margin-top: 92px; }
.message-box::before { position: absolute; top: -140px; left: -110px; width: 330px; height: 330px; border-radius: 50%; background: rgba($yellow,.16); content: ''; }
.delivery-path { position: absolute; z-index: 2; inset: 0; width: 100%; height: 100%; opacity: 0; pointer-events: none; overflow: visible; }.delivery-path path { fill: none; stroke: rgba($accent,.46); stroke-dasharray: 8 10; stroke-linecap: round; stroke-width: 3; }
.paper-plane { position: absolute; z-index: 5; top: 0; left: 0; width: 76px; height: 64px; opacity: 0; pointer-events: none; will-change: transform,opacity; }.paper-plane.plane-idle { top: 24%; left: 38%; opacity: 1; transform: translate(-50%,-50%) rotate(-12deg); animation: plane-float 3.4s ease-in-out infinite; }.paper-plane svg { position: relative; z-index: 1; display: block; width: 100%; height: 100%; overflow: visible; filter: drop-shadow(0 10px 10px rgba(49,87,213,.22)); }.paper-plane svg path:first-child { fill: #fff; stroke: $accent; stroke-width: 2; }.paper-plane svg path:nth-child(2) { fill: rgba($accent,.18); stroke: $accent; stroke-linejoin: round; stroke-width: 1.5; }.paper-plane svg path:last-child { fill: rgba($coral,.2); stroke: $accent; stroke-linejoin: round; stroke-width: 1.5; }.plane-trail { position: absolute; top: 39px; right: 60px; width: 92px; height: 2px; background: linear-gradient(90deg,transparent,rgba($accent,.45)); transform: rotate(15deg); transform-origin: right center; }
@keyframes plane-float { 0%,100% { margin-top: 0; } 50% { margin-top: -10px; } }
.message-box-intro { position: relative; z-index: 1; align-self: center; }.message-box-intro .eyebrow { color: $accent; }.message-box-intro h2 { margin: 14px 0 20px; color: $text-primary; font-size: clamp(40px,4.8vw,62px); line-height: 1.04; letter-spacing: -.06em; }.message-box-intro > p:last-child { max-width: 470px; color: $text-secondary; font-size: 14px; line-height: 1.8; }
.message-envelope { position: relative; display: grid; width: 124px; height: 82px; place-items: center; margin-bottom: 32px; border: 1px solid rgba($accent,.35); border-radius: 8px; background: #fff; box-shadow: 10px 12px 0 rgba($yellow,.5); transform: rotate(-3deg); }.message-envelope::before, .message-envelope::after { position: absolute; inset: 0; content: ''; clip-path: polygon(0 0,50% 58%,100% 0,100% 7%,50% 65%,0 7%); background: rgba($accent,.14); }.message-envelope::after { clip-path: polygon(0 100%,50% 46%,100% 100%); background: rgba($coral,.1); }.message-envelope i { z-index: 1; color: $accent; font-size: 9px; font-style: normal; font-weight: 800; letter-spacing: .13em; }
.message-form { position: relative; z-index: 1; display: grid; gap: 18px; padding: 26px; border: 1px solid $border; border-radius: 15px; background: #fff; box-shadow: 0 24px 55px -48px rgba(16,24,40,.45); }.message-form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }.message-form label { display: grid; grid-template-columns: 1fr auto; gap: 8px; color: $text-primary; font-size: 11px; font-weight: 720; }.message-form label span,.message-form label small { color: $text-secondary; font-size: 9px; font-weight: 600; }.message-form input,.message-form textarea { grid-column: 1 / -1; width: 100%; border: 1px solid $border; border-radius: 9px; color: $text-primary; background: #fcfcfd; font: inherit; font-weight: 500; outline: none; }.message-form input { height: 43px; padding: 0 12px; }.message-form textarea { padding: 12px; line-height: 1.7; resize: vertical; }.message-form input:focus,.message-form textarea:focus { border-color: rgba($accent,.7); box-shadow: 0 0 0 3px rgba($accent,.08); }.message-honeypot { position: absolute; left: -10000px; }.message-feedback { margin: 0; padding: 10px 12px; border-radius: 8px; font-size: 11px; }.message-feedback.error { color: #a43124; background: rgba($coral,.09); }.message-feedback.success { color: #19704f; background: rgba($green,.1); }.message-form-actions { display: flex; justify-content: space-between; align-items: center; gap: 20px; }.message-form-actions > small { color: $text-secondary; font-size: 9px; }.message-form button { min-height: 43px; padding: 0 18px; border: 0; border-radius: 9px; color: #fff; background: $accent; font-size: 11px; font-weight: 750; cursor: pointer; }.message-form button span { margin-left: 12px; }.message-form button:disabled { cursor: wait; opacity: .62; }
@media (max-width: 850px) { .message-box { grid-template-columns: 1fr; }.message-box-intro { display: grid; grid-template-columns: auto 1fr; column-gap: 28px; }.message-envelope { grid-row: 1 / 4; }.message-box-intro > p:last-child { grid-column: 2; } }
@media (max-width: 560px) { .message-box { margin: 24px 0 72px; padding: 28px 20px; }.message-box-intro { display: block; }.message-envelope { width: 100px; height: 66px; }.message-form { padding: 18px; }.message-form-row { grid-template-columns: 1fr; }.message-form-actions { align-items: stretch; flex-direction: column; }.message-form button { width: 100%; } }
.message-box { border-color: rgba($accent,.22); background: #f4f7ff; }
.message-box::before { background: rgba($accent,.09); }
.message-form { gap: 22px; }
.message-form-row { gap: 18px; }
.message-form label { gap: 9px; }
.message-form textarea { min-height: 150px; }
</style>
