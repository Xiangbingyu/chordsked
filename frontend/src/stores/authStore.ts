import { create } from 'zustand'

type AuthState = {
  loggedIn: boolean
  login: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  loggedIn: false,
  login: () => set({ loggedIn: true }),
}))

