import { isAxiosError } from 'axios'
import { create } from 'zustand'
import { login as apiLogin, logout as apiLogout } from '../services/authService'
import type { AuthLoginRequest, AuthLoginResultVO } from '../types/auth'
import type { ApiResponse } from '../types/common'

type AuthState = {
  loggedIn: boolean
  user: AuthLoginResultVO | null
  login: (data: AuthLoginRequest) => Promise<void>
  logout: () => Promise<void>
  clearAuth: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  loggedIn: false,
  user: null,
  login: async (data: AuthLoginRequest) => {
    try {
      const result = await apiLogin(data)
      if (result.code === 0) {
        set({ loggedIn: true, user: result.data })
        return
      }

      throw new Error(result.message || '登录失败')
    } catch (error) {
      if (isAxiosError<ApiResponse<null>>(error)) {
        const message = error.response?.data?.message
        if (message) {
          throw new Error(message)
        }
      }
      throw error
    }
  },
  logout: async () => {
    try {
      await apiLogout()
    } finally {
      set({ loggedIn: false, user: null })
    }
  },
  clearAuth: () => set({ loggedIn: false, user: null }),
}))

