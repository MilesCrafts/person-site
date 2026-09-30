<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getErrorMessage } from '../../api/client'
import { useAdminSession } from '../../composables/useAdminSession'
import { getAdminMessageCount } from '../../api/admin'

defineProps<{ section?: string }>()

const router = useRouter()
const { session, logout } = useAdminSession()
const shell = ref<HTMLElement | null>(null)
const unreadMessages = ref(0)
let animationContext: { revert: () => void } | null = null

async function handleLogout() {
  try {
    await logout()
  } catch (error) {
    window.alert(getErrorMessage(error))
  } finally {
    await router.replace('/admin/login')
  }
}

onMounted(async () => {
  try { unreadMessages.value = (await getAdminMessageCount()).unread } catch { /* 导航计数失败不影响管理功能。 */ }
  if (!shell.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  try {
    const { gsap } = await import('gsap')
    if (!shell.value) return
    animationContext = gsap.context(() => {
      const timeline = gsap.timeline({ defaults: { ease: 'power3.out' } })
      timeline
        .from('.admin-color-strip i', { scaleX: 0, transformOrigin: 'left', duration: .45, stagger: .07 })
        .from('.admin-brand, .admin-header nav, .admin-account', { y: -12, opacity: 0, duration: .48, stagger: .08 }, '-=.2')
        .from('.admin-section-label', { y: 12, opacity: 0, duration: .42 }, '-=.18')
        .from('.admin-content', { y: 18, opacity: 0, duration: .55 }, '-=.26')
    }, shell.value)
  } catch {
    // 动画加载失败不影响管理功能。
  }
})

onBeforeUnmount(() => animationContext?.revert())
</script>

<template>
  <div ref="shell" class="admin-shell">
    <header class="admin-header">
      <div class="admin-color-strip" aria-hidden="true"><i /><i /><i /><i /></div>
      <RouterLink class="admin-brand" to="/admin">
        <span>Lekang</span>
        <strong>写作工作台</strong>
      </RouterLink>
      <nav aria-label="管理导航">
        <RouterLink to="/admin">概览</RouterLink>
        <RouterLink to="/admin/articles">文章</RouterLink>
        <RouterLink class="inbox-link" to="/admin/messages">收件箱 <b v-if="unreadMessages">{{ unreadMessages > 99 ? '99+' : unreadMessages }}</b></RouterLink>
        <RouterLink to="/admin/homepage">首页文案</RouterLink>
        <RouterLink to="/admin/articles/new">写一篇</RouterLink>
      </nav>
      <div class="admin-account">
        <RouterLink class="site-link" to="/journal" target="_blank">去前台看看 ↗</RouterLink>
        <span class="account-name"><i />{{ session?.username }}</span>
        <button type="button" @click="handleLogout">退出</button>
      </div>
    </header>

    <main class="admin-main">
      <div v-if="section" class="admin-section-label">
        <span>PERSONAL PUBLISHING DESK</span>
        <span>{{ section }}</span>
      </div>
      <div class="admin-content"><slot /></div>
    </main>
  </div>
</template>

<style scoped lang="scss">
@use '../../assets/styles/variables' as *;

.admin-shell { min-height: 100vh; background: linear-gradient(#fff 0 74%, #fbfbfc 100%); }
.admin-header {
  position: sticky; z-index: 40; top: 0; display: grid;
  grid-template-columns: minmax(220px, 1fr) auto minmax(320px, 1fr);
  min-height: 78px; align-items: center; padding: 3px 4vw 0;
  border-bottom: 1px solid rgba($border,.9); background: rgba(255,255,255,.9); backdrop-filter: blur(18px);
}
.admin-color-strip { position: absolute; top: 0; right: 0; left: 0; display: grid; grid-template-columns: repeat(4,1fr); height: 3px; overflow: hidden; }
.admin-color-strip i:nth-child(1) { background: $accent; }
.admin-color-strip i:nth-child(2) { background: $coral; }
.admin-color-strip i:nth-child(3) { background: $yellow; }
.admin-color-strip i:nth-child(4) { background: $green; }
.admin-brand { display: inline-flex; width: max-content; align-items: center; gap: 16px; }
.admin-brand span { color: transparent; background: linear-gradient(100deg,$accent 4%,#6b7edc 27%,$coral 48%,#d49a12 68%,$green 88%); background-clip: text; font-family: "Segoe Script","Brush Script MT",cursive; font-size: 25px; font-weight: 700; letter-spacing: -.07em; -webkit-background-clip: text; -webkit-text-fill-color: transparent; transform: rotate(-2deg); }
.admin-brand strong { padding-left: 16px; border-left: 1px solid $border; color: $text-secondary; font-size: 11px; font-weight: 650; letter-spacing: .05em; }
nav { display: flex; align-items: center; gap: 5px; padding: 4px; border: 1px solid $border; border-radius: 999px; background: $surface; }
nav a { display: inline-flex; min-height: 36px; align-items: center; padding: 0 14px; border-radius: 999px; color: $text-secondary; font-size: 12px; font-weight: 650; transition: color .2s ease, background .2s ease, transform .2s ease; }
nav a:hover { color: $text-primary; transform: translateY(-1px); }
nav a.router-link-exact-active { color: $accent-strong; background: #fff; box-shadow: 0 3px 12px rgba(16,24,40,.07); }
.inbox-link { gap: 7px; }.inbox-link b { display: inline-grid; min-width: 18px; height: 18px; place-items: center; padding: 0 4px; border-radius: 99px; color: #fff; background: $coral; font-size: 8px; }
.admin-account { display: flex; justify-content: flex-end; align-items: center; gap: 16px; color: $text-secondary; font-size: 11px; }
.site-link { padding-right: 16px; border-right: 1px solid $border; }
.site-link:hover { color: $accent; }
.account-name { display: inline-flex; align-items: center; gap: 7px; }
.account-name i { width: 7px; height: 7px; border-radius: 50%; background: $green; box-shadow: 0 0 0 4px rgba($green,.11); }
.admin-account button { padding: 7px 10px; border: 1px solid $border; border-radius: 8px; background: #fff; color: $text-secondary; cursor: pointer; }
.admin-account button:hover { color: $coral; border-color: rgba($coral,.35); }
.admin-main { width: min(1240px, calc(100% - 8vw)); margin: 0 auto; padding: 34px 0 100px; }
.admin-section-label { display: flex; justify-content: space-between; margin-bottom: 34px; color: #98a2b3; font-size: 9px; font-weight: 750; letter-spacing: .16em; }
.admin-section-label span:last-child { color: $accent; }

@media (max-width: 980px) {
  .admin-header { grid-template-columns: 1fr auto; gap: 12px; padding-block: 15px 12px; }
  nav { grid-column: 1 / -1; grid-row: 2; justify-self: center; }
  .site-link { display: none; }
}
@media (max-width: 600px) {
  .admin-header { padding-inline: 18px; }
  .admin-brand strong, .account-name { display: none; }
  nav { width: 100%; justify-content: space-between; }
  nav a { flex: 1; justify-content: center; padding-inline: 8px; }
  .admin-main { width: calc(100% - 36px); padding-top: 26px; }
  .admin-section-label span:first-child { display: none; }
  .admin-section-label { justify-content: flex-end; margin-bottom: 24px; }
}
</style>
