<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ApiError, getErrorMessage } from '../../api/client'
import { useAdminSession } from '../../composables/useAdminSession'

const route = useRoute()
const router = useRouter()
const { login } = useAdminSession()
const username = ref('')
const password = ref('')
const showPassword = ref(false)
const rememberUsername = ref(false)
const recoveryHelp = ref(false)
const busy = ref(false)
const errorMessage = ref('')
const loginPage = ref<HTMLElement | null>(null)
const passwordFocused = ref(false)
let animationContext: { revert: () => void } | null = null
let removePointerTracking: (() => void) | null = null

async function setPasswordFocus(focused: boolean) {
  passwordFocused.value = focused
  if (!loginPage.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  try {
    const { gsap } = await import('gsap')
    const hands = loginPage.value.querySelectorAll('.companion-hand')
    const pupils = loginPage.value.querySelectorAll('.companion-pupil')
    gsap.to(hands, {
      x: focused ? 0 : (_index) => _index === 0 ? -10 : 75,
      y: focused ? -51 : (_index) => _index === 0 ? -3 : 1,
      rotate: (_index) => focused ? (_index === 0 ? -12 : 12) : (_index === 0 ? -8 : 12),
      duration: .46,
      ease: focused ? 'back.out(1.8)' : 'power3.out'
    })
    gsap.to(pupils, { x: focused ? 0 : 6, y: focused ? 4 : 3, duration: .28, ease: 'power2.out' })
    gsap.to('.companion-mouth', { scaleX: focused ? .6 : 1, y: focused ? 3 : 0, duration: .3, transformOrigin: 'center' })
    gsap.fromTo('.companion-secret', { opacity: 0, y: 5 }, { opacity: focused ? 1 : 0, y: 0, duration: .3 })
  } catch { /* CSS 状态仍会生效 */ }
}

async function submit() {
  if (busy.value) return
  busy.value = true
  errorMessage.value = ''
  try {
    const session = await login({ username: username.value.trim(), password: password.value })
    password.value = ''
    if (!session.authenticated) {
      errorMessage.value = '账号或密码不正确。'
      return
    }
    if (rememberUsername.value) localStorage.setItem('lekang.admin.username', username.value.trim())
    else localStorage.removeItem('lekang.admin.username')
    const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/admin')
      ? route.query.redirect
      : '/admin'
    await router.replace(redirect)
  } catch (error) {
    password.value = ''
    errorMessage.value = error instanceof ApiError && (error.status === 400 || error.status === 401)
      ? '账号或密码不正确。'
      : getErrorMessage(error)
  } finally {
    busy.value = false
  }
}

onMounted(async () => {
  const rememberedUsername = localStorage.getItem('lekang.admin.username')
  if (rememberedUsername) {
    username.value = rememberedUsername
    rememberUsername.value = true
  }
  if (!loginPage.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  try {
    const { gsap } = await import('gsap')
    if (!loginPage.value) return
    animationContext = gsap.context(() => {
      gsap.timeline({ defaults: { ease: 'power3.out' } })
        .from('.login-color-strip i', { scaleX: 0, transformOrigin: 'left', duration: .45, stagger: .07 })
        .from('.login-brand', { y: -12, opacity: 0, duration: .45 }, '-=.15')
        .from('.login-kicker, .login-heading h1, .login-heading > span', { y: 24, opacity: 0, duration: .55, stagger: .09 }, '-=.18')
        .from('.login-card', { y: 28, rotate: 1.5, opacity: 0, duration: .65 }, '-=.45')
        .from('.login-companion', { y: 22, scale: .88, opacity: 0, duration: .62 }, '-=.48')
        .from('.desk-shape', { scale: 0, rotate: -12, duration: .55, stagger: .08 }, '-=.5')
      gsap.to('.desk-shape-blue', { y: -8, rotate: 8, duration: 2.8, repeat: -1, yoyo: true, ease: 'sine.inOut' })
      gsap.to('.desk-shape-yellow', { x: 7, rotate: -7, duration: 3.3, repeat: -1, yoyo: true, ease: 'sine.inOut' })
      gsap.to('.companion-antenna-dot', { y: -4, duration: 1.3, repeat: -1, yoyo: true, ease: 'sine.inOut' })
      gsap.set('.companion-pupil', { x: 6, y: 3 })
      gsap.set('.companion-head', { rotateY: 4, rotateX: -1 })
    }, loginPage.value)

    const movePupils = (event: PointerEvent) => {
      if (!loginPage.value || passwordFocused.value) return
      const face = loginPage.value.querySelector('.companion-face') as HTMLElement | null
      if (!face) return
      const rect = face.getBoundingClientRect()
      const deltaX = event.clientX - (rect.left + rect.width / 2)
      const deltaY = event.clientY - (rect.top + rect.height / 2)
      const distance = Math.max(1, Math.hypot(deltaX, deltaY))
      const strength = Math.min(8, distance / 22)
      gsap.to('.companion-pupil', {
        x: deltaX / distance * strength,
        y: deltaY / distance * strength,
        duration: .28,
        ease: 'power2.out',
        overwrite: 'auto'
      })
      gsap.to('.companion-head', {
        rotateY: Math.max(-7, Math.min(7, deltaX / 95)),
        rotateX: Math.max(-5, Math.min(5, -deltaY / 120)),
        duration: .45,
        ease: 'power2.out',
        overwrite: 'auto'
      })
    }
    window.addEventListener('pointermove', movePupils, { passive: true })
    removePointerTracking = () => window.removeEventListener('pointermove', movePupils)
  } catch { /* 保持静态登录页 */ }
})
onBeforeUnmount(() => { removePointerTracking?.(); animationContext?.revert() })
</script>

<template>
  <main ref="loginPage" class="admin-login">
    <div class="login-color-strip" aria-hidden="true"><i /><i /><i /><i /></div>
    <RouterLink class="login-brand" to="/journal" aria-label="返回 LEKANG JOURNAL">
      <span>Lekang</span>
      <strong>写作工作台</strong>
    </RouterLink>
    <section>
      <div class="login-heading">
        <p class="login-kicker"><i /> PRIVATE EDITORIAL DESK</p>
        <h1>回来啦，<br><em>继续写。</em></h1>
        <span>灵感先放进草稿，慢慢整理，再认真地交给读者。</span>
        <div class="login-features" aria-label="写作工作台功能">
          <span class="login-feature"><i>↻</i>草稿随时保存</span>
          <span class="login-feature"><i>⌁</i>Markdown 双栏预览</span>
          <span class="login-feature"><i>✓</i>发布状态可管理</span>
        </div>
        <div class="login-companion" :class="{ 'is-hiding': passwordFocused }" aria-hidden="true">
          <div class="companion-note">我会替你看着灵感 <i>↘</i></div>
          <div class="companion-coffee"><i /><span /></div>
          <div class="companion-shadow" />
          <div class="companion-body">
            <div class="companion-antenna"><i class="companion-antenna-dot" /></div>
            <div class="companion-head">
              <div class="companion-face">
                <span class="companion-eye"><i class="companion-pupil" /></span>
                <span class="companion-eye"><i class="companion-pupil" /></span>
                <i class="companion-mouth" />
              </div>
            </div>
            <span class="companion-hand companion-hand-left"><i /></span>
            <span class="companion-hand companion-hand-right"><i /></span>
            <i class="companion-accent" />
            <small class="companion-secret">密码我不看 👀</small>
          </div>
          <div class="companion-book"><span>IDEAS</span><i /><i /><i /></div>
        </div>
      </div>
      <div class="login-bridge" aria-hidden="true">
        <i class="login-bridge-glow" />
        <i class="login-bridge-path" />
        <i class="login-bridge-fragment login-bridge-fragment-blue" />
        <i class="login-bridge-fragment login-bridge-fragment-yellow" />
      </div>
      <div class="login-desk" :class="{ 'has-panel': recoveryHelp || errorMessage, 'has-two-panels': recoveryHelp && errorMessage }">
        <i class="desk-shape desk-shape-blue" aria-hidden="true" />
        <i class="desk-shape desk-shape-coral" aria-hidden="true" />
        <i class="desk-shape desk-shape-yellow" aria-hidden="true" />
        <form class="login-card" @submit.prevent="submit">
          <div class="card-top"><span /><span /><span /><small>lekang / private</small></div>
          <p>欢迎回到自己的小编辑部。</p>
        <label :class="{ 'has-error': errorMessage }">
          <span>管理员账号</span>
          <input
            v-model="username"
            required
            autocomplete="username"
            placeholder="请输入管理员账号"
            :aria-invalid="Boolean(errorMessage)"
            :aria-describedby="errorMessage ? 'login-error' : undefined"
            @input="errorMessage = ''"
          >
        </label>
        <label :class="{ 'has-error': errorMessage }">
          <span>密码</span>
          <span class="password-control">
            <input
              v-model="password"
              required
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              placeholder="请输入密码"
              :aria-invalid="Boolean(errorMessage)"
              :aria-describedby="errorMessage ? 'login-error' : undefined"
              @input="errorMessage = ''"
              @focus="setPasswordFocus(true)"
              @blur="setPasswordFocus(false)"
            >
            <button
              class="password-toggle"
              type="button"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              :title="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            ><i :class="{ visible: showPassword }" /></button>
          </span>
        </label>
        <div class="login-utilities">
          <label class="remember-option">
            <input v-model="rememberUsername" type="checkbox">
            <span>记住账号</span>
          </label>
          <button class="forgot-link" type="button" @click="recoveryHelp = !recoveryHelp">忘记密码？</button>
        </div>
        <p v-if="recoveryHelp" class="recovery-help" role="status">
          管理员密码不通过网页找回，请在服务器端安全重置后重新登录。
        </p>
        <p v-if="errorMessage" id="login-error" class="login-error" role="alert"><i>!</i><span><strong>没有登录成功</strong>{{ errorMessage }}</span></p>
          <button type="submit" :disabled="busy" :aria-busy="busy" :class="{ 'is-loading': busy }">
            <span class="button-text">{{ busy ? '正在验证…' : '进入工作台' }}</span>
            <span class="button-arrow" aria-hidden="true">→</span>
          </button>
          <p class="login-security"><i />仅限授权人员使用 · 密码不会在浏览器中保存</p>
        </form>
        <small class="desk-signoff">© LEKANG 写作工作台 · PRIVATE DESK</small>
      </div>
    </section>
    <footer>
      <span>LEKANG / PERSONAL PUBLISHING DESK</span>
      <span>写作是一件慢慢来的事</span>
    </footer>
  </main>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;

.admin-login {
  position: relative;
  display: grid;
  min-height: 100vh;
  min-height: 100dvh;
  grid-template-rows: auto 1fr auto;
  width: min(1220px, calc(100% - 80px));
  margin: auto;
  padding: 40px 0 25px;
}
.login-color-strip { position: fixed; z-index: 5; top: 0; right: 0; left: 0; display: grid; grid-template-columns: repeat(4,1fr); height: 4px; }
.login-color-strip i:nth-child(1) { background: $accent; } .login-color-strip i:nth-child(2) { background: $coral; } .login-color-strip i:nth-child(3) { background: $yellow; } .login-color-strip i:nth-child(4) { background: $green; }
.login-brand { display: inline-flex; width: max-content; align-items: baseline; gap: 15px; }
.login-brand span { color: transparent; background: linear-gradient(100deg,$accent,$coral 48%,#d49a12 68%,$green); background-clip: text; font-family: "Segoe Script","Brush Script MT",cursive; font-size: 29px; font-weight: 700; letter-spacing: -.07em; -webkit-background-clip: text; -webkit-text-fill-color: transparent; transform: rotate(-2deg); }
.login-brand strong { padding-left: 15px; border-left: 1px solid $border; color: $text-secondary; font-size: 10px; letter-spacing: .08em; }
section {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  align-self: start;
  align-items: center;
  gap: 10%;
  width: 100%;
  padding: clamp(34px, 5vh, 58px) 0 32px;
}
.login-heading { position: relative; z-index: 1; }
.login-heading p { display: flex; align-items: center; gap: 9px; margin: 0 0 22px; color: $accent; font-size: 10px; font-weight: 750; letter-spacing: .13em; }
.login-heading p i { width: 8px; height: 8px; border-radius: 50%; background: $green; box-shadow: 0 0 0 4px rgba($green,.12); }
h1 { margin: 0; font-size: clamp(58px,7vw,94px); font-weight: 680; line-height: .94; letter-spacing: -.065em; }
h1 em { position: relative; color: $accent; font-style: normal; }
h1 em::after { position: absolute; z-index: -1; right: 0; bottom: .03em; left: 0; height: .13em; background: $yellow; content: ''; transform: rotate(-1deg); }
.login-heading > span { display: block; max-width: 430px; margin-top: 30px; color: $text-secondary; font-size: 16px; line-height: 1.75; }
.login-features { display: flex; flex-wrap: wrap; gap: 8px; max-width: 455px; margin-top: 20px; }
.login-feature { display: inline-flex; align-items: center; gap: 6px; padding: 7px 9px; border: 1px solid $border; border-radius: 7px; color: #475467; background: rgba(255,255,255,.9); font-size: 10px; font-weight: 650; white-space: nowrap; }
.login-feature i { display: grid; width: 17px; height: 17px; place-items: center; border-radius: 5px; color: $accent-strong; background: $accent-subtle; font-size: 11px; font-style: normal; font-weight: 800; }
.login-feature:nth-child(2) i { color: #a94135; background: rgba($coral,.1); }
.login-feature:nth-child(3) i { color: #3b765b; background: rgba($green,.13); }
.login-companion { position: relative; width: min(450px,100%); height: 190px; margin-top: 17px; perspective: 700px; }
.companion-note { position: absolute; z-index: 3; top: 5px; left: 0; padding: 8px 10px; border: 1px dashed $text-primary; border-radius: 5px; color: $accent-strong; background: #fff; font-size: 10px; font-weight: 750; letter-spacing: .03em; transform: rotate(-2deg); }
.companion-note i { display: inline-block; margin-left: 4px; color: $coral; font-size: 18px; font-style: normal; transform: translate(2px,3px); }
.companion-coffee { position: absolute; z-index: 4; bottom: 24px; left: 49px; width: 39px; height: 33px; border: 2px solid $text-primary; border-radius: 4px 4px 10px 10px; background: #fff; box-shadow: inset 0 -7px 0 rgba($coral,.14), 3px 4px 0 rgba($yellow,.7); transform: rotate(-7deg); }
.companion-coffee::after { position: absolute; top: 6px; right: -13px; width: 13px; height: 15px; border: 2px solid $text-primary; border-left: 0; border-radius: 0 9px 9px 0; content: ''; }
.companion-coffee i, .companion-coffee span { position: absolute; bottom: 39px; width: 1px; height: 13px; border-left: 1px solid rgba(102,112,133,.55); border-radius: 50%; content: ''; transform: rotate(9deg); }
.companion-coffee i { left: 12px; }.companion-coffee span { left: 24px; bottom: 42px; transform: rotate(-8deg); }
.companion-shadow { position: absolute; bottom: 11px; left: 116px; width: 210px; height: 21px; border-radius: 50%; background: rgba(16,24,40,.1); filter: blur(8px); }
.companion-body { position: absolute; z-index: 2; bottom: 20px; left: 115px; width: 158px; height: 132px; border: 1px solid rgba(16,24,40,.15); border-radius: 48px 48px 31px 31px; background: $accent; box-shadow: inset 0 -12px 0 rgba(36,67,168,.18), 0 18px 28px -22px rgba(16,24,40,.55); }
.companion-antenna { position: absolute; top: -29px; left: 50%; width: 2px; height: 31px; background: $text-primary; transform: translateX(-50%) rotate(5deg); transform-origin: bottom; }
.companion-antenna-dot { position: absolute; top: -7px; left: -6px; width: 14px; height: 14px; border: 2px solid #fff; border-radius: 50%; background: $coral; box-shadow: 0 0 0 1px rgba(16,24,40,.13); }
.companion-head { position: absolute; inset: 17px 18px 25px; transform-style: preserve-3d; }
.companion-face { position: relative; display: flex; height: 73px; align-items: center; justify-content: center; gap: 13px; border: 1px solid rgba(16,24,40,.13); border-radius: 34px 34px 25px 25px; background: #fff; box-shadow: inset 0 -6px 0 $accent-subtle; }
.companion-eye { position: relative; width: 31px; height: 36px; overflow: hidden; border: 2px solid $text-primary; border-radius: 50%; background: #fff; }
.companion-pupil { position: absolute; top: 10px; left: 9px; width: 10px; height: 13px; border-radius: 50%; background: $text-primary; }
.companion-pupil::after { position: absolute; top: 2px; left: 2px; width: 3px; height: 3px; border-radius: 50%; background: #fff; content: ''; }
.companion-mouth { position: absolute; bottom: 9px; left: 50%; width: 18px; height: 7px; border-bottom: 2px solid $coral; border-radius: 50%; transform: translateX(-50%); }
.companion-hand { position: absolute; z-index: 5; bottom: -2px; width: 41px; height: 41px; border: 2px solid $text-primary; border-radius: 50% 50% 43% 43%; background: $yellow; box-shadow: inset 0 -5px 0 rgba(212,154,18,.22); }
.companion-hand::before { position: absolute; bottom: -19px; left: 14px; width: 12px; height: 25px; border: 2px solid $text-primary; border-top: 0; border-radius: 0 0 8px 8px; background: $yellow; content: ''; }
.companion-hand i, .companion-hand i::before, .companion-hand i::after { position: absolute; top: -4px; width: 10px; height: 18px; border: 2px solid $text-primary; border-bottom: 0; border-radius: 8px 8px 0 0; background: $yellow; content: ''; }
.companion-hand i { left: 13px; }.companion-hand i::before { top: 1px; left: -10px; }.companion-hand i::after { top: 1px; left: 6px; }
.companion-hand-left { left: 37px; transform: translate(-10px,-3px) rotate(-8deg); }.companion-hand-right { right: 37px; transform: translate(75px,1px) rotate(12deg); }
.companion-accent { position: absolute; right: 18px; bottom: 12px; width: 25px; height: 8px; border: 1px solid rgba(16,24,40,.25); border-radius: 99px; background: $coral; transform: rotate(-7deg); }
.companion-body::before, .companion-body::after { position: absolute; bottom: 13px; height: 3px; border-radius: 99px; background: rgba(255,255,255,.28); content: ''; }
.companion-body::before { left: 20px; width: 35px; transform: rotate(3deg); }.companion-body::after { left: 61px; width: 17px; transform: rotate(-4deg); }
.companion-secret { position: absolute; z-index: 7; top: 3px; right: -134px; padding: 6px 8px; border: 1px solid rgba($coral,.25); border-radius: 7px; color: $coral; background: #fff; box-shadow: 0 7px 16px rgba(16,24,40,.08); font-size: 9px; font-weight: 750; opacity: 0; white-space: nowrap; transform: rotate(2deg); }
.companion-book { position: absolute; z-index: 4; right: 36px; bottom: 21px; display: grid; width: 128px; height: 94px; grid-template-columns: 1fr; gap: 7px; align-content: start; padding: 17px 14px; border: 2px solid $text-primary; border-radius: 4px 11px 11px 4px; background: $yellow; box-shadow: 7px 7px 0 $coral; transform: rotate(4deg); }
.companion-book::before { position: absolute; top: 0; bottom: 0; left: 9px; width: 1px; background: rgba(16,24,40,.25); content: ''; }
.companion-book span { color: $text-primary; font-size: 11px; font-weight: 850; letter-spacing: .12em; }.companion-book i { width: 100%; height: 4px; border-radius: 9px; background: rgba(16,24,40,.18); }.companion-book i:last-child { width: 62%; }
.login-companion.is-hiding .companion-face { background: #fffaf0; }
.login-bridge { position: absolute; z-index: 0; top: 43%; left: 50%; width: clamp(110px,14vw,190px); height: 210px; pointer-events: none; transform: translate(-50%,-50%); }
.login-bridge-glow { position: absolute; inset: -22px -34px; border-radius: 50%; background: radial-gradient(circle at 28% 35%, rgba($accent,.13), transparent 32%), radial-gradient(circle at 72% 61%, rgba($yellow,.15), transparent 30%), radial-gradient(circle at 52% 74%, rgba($coral,.1), transparent 25%); }
.login-bridge-path { position: absolute; top: 85px; right: -10px; left: -10px; height: 1px; border-top: 1px dashed rgba($accent,.28); transform: rotate(12deg); }
.login-bridge-path::before, .login-bridge-path::after { position: absolute; top: -3px; width: 6px; height: 6px; border-radius: 50%; background: #fff; box-shadow: 0 0 0 1px rgba($accent,.32); content: ''; }
.login-bridge-path::before { left: 3px; }.login-bridge-path::after { right: 3px; background: $accent-subtle; }
.login-bridge-fragment { position: absolute; display: block; border: 1px solid rgba(16,24,40,.09); clip-path: polygon(8% 0,100% 12%,91% 93%,0 100%); }
.login-bridge-fragment-blue { top: 119px; left: 20%; width: 25px; height: 20px; background: rgba($accent,.13); transform: rotate(-13deg); }
.login-bridge-fragment-yellow { top: 151px; right: 17%; width: 18px; height: 25px; background: rgba($yellow,.2); transform: rotate(17deg); }
.login-desk { position: relative; z-index: 1; min-height: 555px; }
.login-desk.has-panel { min-height: 620px; }
.login-desk.has-two-panels { min-height: 680px; }
.desk-shape { position: absolute; display: block; border: 1px solid rgba(16,24,40,.12); border-radius: 18px; }
.desk-shape-blue { top: 0; right: 4px; width: 154px; height: 154px; background: $accent; transform: rotate(5deg); }
.desk-shape-coral { right: 12px; bottom: 18px; width: 132px; height: 105px; background: $coral; transform: rotate(-5deg); }
.desk-shape-yellow { bottom: 6px; left: 4px; width: 148px; height: 112px; background: $yellow; transform: rotate(5deg); }
.login-card { position: absolute; z-index: 2; inset: 28px 28px 38px 20px; display: flex; flex-direction: column; gap: 18px; padding: 25px 30px 24px; border: 1px solid $border; border-radius: 18px; background: #fff; box-shadow: 0 30px 64px -38px rgba(16,24,40,.45); transform: rotate(-1.3deg); }
.card-top { display: flex; align-items: center; gap: 6px; padding-bottom: 16px; border-bottom: 1px solid $border; }
.card-top span { width: 7px; height: 7px; border-radius: 50%; background: $border; }
.card-top span:nth-child(1) { background: $coral; } .card-top span:nth-child(2) { background: $yellow; } .card-top span:nth-child(3) { background: $green; }
.card-top small { margin-left: auto; color: $text-secondary; font-size: 9px; font-weight: 700; letter-spacing: .08em; }
.login-card > p { margin: 0; font-size: 14px; font-weight: 650; }
label { display: flex; flex-direction: column; gap: 10px; }
label > span:first-child { color: #344054; font-size: 12px; font-weight: 760; }
input:not([type='checkbox']) { width: 100%; min-height: 48px; padding: 0 13px; border: 1px solid $border; border-radius: 10px; outline: 0; color: $text-primary; background: $surface; font-size: 14px; transition: border-color .2s ease, box-shadow .2s ease, background .2s ease; }
input::placeholder { color: #98a2b3; }
input:not([type='checkbox']):focus { border-color: $accent; background: #fff; box-shadow: 0 0 0 3px rgba($accent,.09); }
.has-error input:not([type='checkbox']) { border-color: rgba($coral,.55); background: rgba($coral,.025); }
.password-control { position: relative; display: block; }
.password-control input { padding-right: 48px; }
.password-toggle { position: absolute; top: 50%; right: 8px; display: grid; width: 36px; height: 36px; place-items: center; margin: 0; padding: 0; border: 0; border-radius: 8px; color: $text-secondary; background: transparent; cursor: pointer; transform: translateY(-50%); }
.password-toggle:hover { color: $accent; background: $accent-subtle; }
.password-toggle i { position: relative; display: block; width: 19px; height: 13px; border: 1.8px solid currentColor; border-radius: 50% / 60%; }
.password-toggle i::before { position: absolute; top: 50%; left: 50%; width: 5px; height: 5px; border-radius: 50%; background: currentColor; content: ''; transform: translate(-50%,-50%); }
.password-toggle i::after { position: absolute; top: 5px; left: -2px; width: 23px; height: 1.8px; background: currentColor; content: ''; transform: rotate(-38deg); transition: opacity .2s ease; }
.password-toggle i.visible::after { opacity: 0; }
.login-utilities { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-top: -2px; }
.remember-option { display: inline-flex; flex-direction: row; align-items: center; gap: 8px; color: $text-secondary; cursor: pointer; }
.remember-option input { width: 15px; height: 15px; margin: 0; accent-color: $accent; }.remember-option span { font-size: 10px; font-weight: 620; }
.forgot-link { margin: 0; padding: 0; border: 0; color: $accent; background: transparent; font-size: 10px; font-weight: 680; cursor: pointer; }
.forgot-link:hover { color: $accent-strong; text-decoration: underline; text-underline-offset: 3px; }
.recovery-help { margin: -6px 0 0 !important; padding: 9px 11px; border: 1px solid rgba($accent,.18); border-radius: 8px; color: $accent-strong; background: $accent-subtle; font-size: 10px !important; font-weight: 550 !important; line-height: 1.55; }
.login-card > button[type='submit'] {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0;
  padding: 14px 16px;
  border: 1px solid $accent-strong;
  border-radius: 7px 7px 6px 8px;
  color: #fff;
  background: linear-gradient(105deg, $accent-strong 0%, $accent 100%);
  box-shadow: none;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .04em;
  cursor: pointer;
  transition: border-color .2s ease, color .2s ease, background .2s ease, transform .2s ease;
}
.button-text { font: inherit !important; }
.button-arrow { color: $yellow; font: 650 21px/1 $sans !important; transition: color .2s ease, transform .2s ease; }
.login-card > button[type='submit']:hover:not(:disabled) { border-color: #1d3587; color: #fff; background: linear-gradient(105deg, #1d3587 0%, $accent-strong 100%); transform: translateY(-1px); }
.login-card > button[type='submit']:hover:not(:disabled) .button-arrow { color: $yellow; transform: translateX(3px); }
.login-card > button[type='submit']:focus-visible { outline: 3px solid rgba($accent,.22); outline-offset: 3px; }
.login-card > button[type='submit']:active:not(:disabled) { transform: translateY(0); }
.login-card > button[type='submit']:disabled { border-color: #d0d5dd; color: #98a2b3; background: #f2f4f7; cursor: wait; }
.login-card > button[type='submit'].is-loading { border-color: rgba($accent,.3); color: $accent-strong; background: $accent-subtle; }
.login-card > button[type='submit'].is-loading .button-arrow { width: 16px; height: 16px; border: 2px solid rgba($accent,.22); border-top-color: $accent; border-radius: 50%; color: transparent; animation: login-spin .7s linear infinite; }
@keyframes login-spin { to { transform: rotate(360deg); } }
.login-error { display: flex; align-items: center; gap: 10px; margin: -5px 0 0 !important; padding: 10px 12px; border: 1px solid rgba($coral,.28); border-radius: 9px; color: #b42318; background: #fff5f3; font-size: 11px !important; font-weight: 550 !important; line-height: 1.45; }
.login-error > i { display: grid; width: 20px; height: 20px; flex: 0 0 20px; place-items: center; border-radius: 50%; color: #fff; background: $coral; font-style: normal; font-weight: 800; }.login-error > span { display: grid; }.login-error strong { font-size: 11px; }.login-error span { color: #b42318; }
.login-security { display: flex; align-items: center; justify-content: center; gap: 7px; margin: 1px 0 0 !important; color: #98a2b3; font-size: 9px !important; font-weight: 600 !important; line-height: 1.4; }.login-security i { width: 6px; height: 6px; border-radius: 50%; background: $green; box-shadow: 0 0 0 3px rgba($green,.1); }
.desk-signoff { position: absolute; z-index: 3; right: 38px; bottom: 5px; color: #98a2b3; font-size: 8px; font-weight: 650; letter-spacing: .08em; }
footer { display: flex; justify-content: space-between; padding-top: 18px; border-top: 1px solid $border; color: $text-secondary; font-size: 9px; letter-spacing: .1em; }

@media (max-width: 980px) and (min-width: 701px) {
  section { gap: 6%; }
  .login-feature { padding-inline: 7px; }
  .login-bridge { width: 90px; opacity: .75; }
}

@media (max-width: 700px) {
  .admin-login { width: calc(100% - 36px); padding-top: 25px; }
  section { grid-template-columns: 1fr; gap: 45px; padding: 52px 0; }
  .login-bridge { display: none; }
  .login-features { gap: 7px; margin-top: 18px; }
  .login-feature { padding: 6px 8px; font-size: 9px; }
  .login-desk { min-height: 555px; }
  h1 { font-size: clamp(54px, 18vw, 75px); }
  .login-companion { height: 180px; margin-top: 32px; transform: scale(.9); transform-origin: left center; }
  .companion-body { left: 90px; }.companion-shadow { left: 90px; }.companion-book { right: 8px; }.companion-note { font-size: 9px; }.companion-coffee { left: 29px; }
  .companion-secret { display: none; }
  footer span:last-child { display: none; }
}
</style>
