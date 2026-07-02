import { defineStore } from 'pinia'
import { getSession, setSession, clearSession } from '../api/client'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const session = ref(getSession())

  const isLoggedIn = computed(() => !!session.value?.token)
  const role = computed(() => session.value?.role || '')
  const token = computed(() => session.value?.token || '')

  function login(sessionData) {
    session.value = sessionData
    setSession(sessionData)
  }

  function logout() {
    session.value = null
    clearSession()
  }

  function isShipper() {
    return role.value.startsWith('SHIPPER')
  }

  function isCarrier() {
    return role.value.startsWith('CARRIER')
  }

  function isPlatform() {
    return role.value === 'PLATFORM_ADMIN'
  }

  return { session, isLoggedIn, role, token, login, logout, isShipper, isCarrier, isPlatform }
})
