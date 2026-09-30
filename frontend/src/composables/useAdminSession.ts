import { computed, readonly, ref } from 'vue'
import {
  getAdminSession,
  loginAdmin,
  logoutAdmin,
  onAdminUnauthorized,
  setAdminSecurity
} from '../api/admin'
import type { AdminLoginRequest, AdminSession } from '../types/admin'

type SessionStatus = 'idle' | 'loading' | 'authenticated' | 'anonymous'

const status = ref<SessionStatus>('idle')
const session = ref<AdminSession | null>(null)
let pendingSession: Promise<AdminSession> | null = null

function applySession(next: AdminSession): AdminSession {
  session.value = next
  status.value = next.authenticated ? 'authenticated' : 'anonymous'
  setAdminSecurity(next)
  return next
}

function clearSession(): void {
  session.value = null
  status.value = 'anonymous'
  setAdminSecurity(null)
}

onAdminUnauthorized(clearSession)

async function ensureSession(force = false): Promise<AdminSession> {
  if (!force && session.value && status.value !== 'idle') return session.value
  if (pendingSession) return pendingSession

  status.value = 'loading'
  pendingSession = getAdminSession()
    .then(applySession)
    .catch((error) => {
      clearSession()
      throw error
    })
    .finally(() => {
      pendingSession = null
    })
  return pendingSession
}

async function login(credentials: AdminLoginRequest): Promise<AdminSession> {
  status.value = 'loading'
  try {
    return applySession(await loginAdmin(credentials))
  } catch (error) {
    clearSession()
    throw error
  }
}

async function logout(): Promise<void> {
  try {
    if (session.value?.authenticated) await logoutAdmin()
  } finally {
    clearSession()
  }
}

export function useAdminSession() {
  return {
    status: readonly(status),
    session: readonly(session),
    isAuthenticated: computed(() => status.value === 'authenticated'),
    ensureSession,
    login,
    logout,
    clearSession
  }
}
