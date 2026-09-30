import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAdminSession } from '../composables/useAdminSession'
import { onAdminUnauthorized } from '../api/admin'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: (to, _from, savedPosition) => {
    const behavior = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, top: 92, behavior }
    return { top: 0, behavior }
  },
  routes: [
    { path: '/', component: HomeView },
    { path: '/works', component: () => import('../views/WorksView.vue') },
    { path: '/works/pdf-toolbox', component: () => import('../views/PdfToolboxView.vue') },
    { path: '/capabilities', component: () => import('../views/CapabilitiesView.vue') },
    { path: '/journal', component: () => import('../views/JournalView.vue') },
    { path: '/message', component: () => import('../views/MessageView.vue') },
    { path: '/article/:id', component: () => import('../views/ArticleView.vue') },
    { path: '/about', component: () => import('../views/AboutView.vue') },
    { path: '/archive', component: () => import('../views/ArchiveView.vue') },
    { path: '/search', component: () => import('../views/SearchView.vue') },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../views/admin/AdminLoginView.vue'),
      meta: { admin: true }
    },
    {
      path: '/admin',
      name: 'admin-home',
      component: () => import('../views/admin/AdminHomeView.vue'),
      meta: { admin: true, requiresAdmin: true }
    },
    {
      path: '/admin/articles',
      name: 'admin-articles',
      component: () => import('../views/admin/AdminArticleListView.vue'),
      meta: { admin: true, requiresAdmin: true }
    },
    {
      path: '/admin/homepage',
      name: 'admin-homepage',
      component: () => import('../views/admin/AdminHomepageView.vue'),
      meta: { admin: true, requiresAdmin: true }
    },
    {
      path: '/admin/messages',
      name: 'admin-messages',
      component: () => import('../views/admin/AdminMessageInboxView.vue'),
      meta: { admin: true, requiresAdmin: true }
    },
    {
      path: '/admin/articles/new',
      name: 'admin-article-new',
      component: () => import('../views/admin/AdminArticleEditorView.vue'),
      meta: { admin: true, requiresAdmin: true }
    },
    {
      path: '/admin/articles/:id/edit',
      name: 'admin-article-edit',
      component: () => import('../views/admin/AdminArticleEditorView.vue'),
      meta: { admin: true, requiresAdmin: true }
    },
    { path: '/:pathMatch(.*)*', redirect: '/journal' }
  ]
})

const adminSession = useAdminSession()
onAdminUnauthorized(() => {
  adminSession.clearSession()
  const current = router.currentRoute.value
  if (current.meta.requiresAdmin) {
    void router.replace({
      name: 'admin-login',
      query: { redirect: current.fullPath, reason: 'expired' }
    })
  }
})

router.beforeEach(async (to) => {
  if (!to.meta.admin) return true
  if (to.name === 'admin-login' && adminSession.status.value === 'anonymous') return true

  try {
    const session = await adminSession.ensureSession()
    if (to.name === 'admin-login' && session.authenticated) return { name: 'admin-home' }
    if (to.meta.requiresAdmin && !session.authenticated) {
      return { name: 'admin-login', query: { redirect: to.fullPath } }
    }
  } catch {
    if (to.meta.requiresAdmin) {
      return { name: 'admin-login', query: { redirect: to.fullPath, reason: 'session' } }
    }
  }
  return true
})

export default router
