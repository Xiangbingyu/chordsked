import { isAxiosError } from 'axios'
import { create } from 'zustand'
import {
  createInternalUser as apiCreateInternalUser,
  getInternalUserDetail as apiGetInternalUserDetail,
  listInternalUsers as apiListInternalUsers,
  resetInternalUserPassword as apiResetInternalUserPassword,
  updateInternalUser as apiUpdateInternalUser,
  updateInternalUserStatus as apiUpdateInternalUserStatus,
} from '../services/internalUserService'
import type { ApiResponse } from '../types/common'
import type {
  InternalUserCreateRequest,
  InternalUserDetailResultVO,
  InternalUserPageResult,
  InternalUserQueryRequest,
  InternalUserResetPasswordRequest,
  InternalUserStatusUpdateRequest,
  InternalUserUpdateRequest,
} from '../types/internalUser'

type InternalUserState = {
  loading: boolean
  detailLoading: boolean
  internalUserPage: InternalUserPageResult | null
  internalUserDetail: InternalUserDetailResultVO | null
  fetchInternalUsers: (params: InternalUserQueryRequest) => Promise<InternalUserPageResult>
  fetchInternalUserDetail: (userId: number) => Promise<InternalUserDetailResultVO>
  createInternalUser: (data: InternalUserCreateRequest) => Promise<number>
  updateInternalUser: (userId: number, data: InternalUserUpdateRequest) => Promise<void>
  updateInternalUserStatus: (userId: number, data: InternalUserStatusUpdateRequest) => Promise<void>
  resetInternalUserPassword: (
    userId: number,
    data: InternalUserResetPasswordRequest,
  ) => Promise<void>
  clearInternalUserPage: () => void
  clearInternalUserDetail: () => void
}

function resolveApiError(error: unknown): never {
  if (isAxiosError<ApiResponse<null>>(error)) {
    const message = error.response?.data?.message
    if (message) {
      throw new Error(message)
    }
  }

  throw error
}

export const useInternalUserStore = create<InternalUserState>((set) => ({
  loading: false,
  detailLoading: false,
  internalUserPage: null,
  internalUserDetail: null,
  fetchInternalUsers: async (params) => {
    set({ loading: true })
    try {
      const result = await apiListInternalUsers(params)
      if (result.code === 0) {
        set({ internalUserPage: result.data })
        return result.data
      }

      throw new Error(result.message || '查询教务端账号列表失败')
    } catch (error) {
      resolveApiError(error)
    } finally {
      set({ loading: false })
    }
  },
  fetchInternalUserDetail: async (userId) => {
    set({ detailLoading: true })
    try {
      const result = await apiGetInternalUserDetail(userId)
      if (result.code === 0) {
        set({ internalUserDetail: result.data })
        return result.data
      }

      throw new Error(result.message || '查询教务端账号详情失败')
    } catch (error) {
      resolveApiError(error)
    } finally {
      set({ detailLoading: false })
    }
  },
  createInternalUser: async (data) => {
    try {
      const result = await apiCreateInternalUser(data)
      if (result.code === 0) {
        return result.data
      }

      throw new Error(result.message || '创建教务端账号失败')
    } catch (error) {
      resolveApiError(error)
    }
  },
  updateInternalUser: async (userId, data) => {
    try {
      const result = await apiUpdateInternalUser(userId, data)
      if (result.code === 0) {
        return
      }

      throw new Error(result.message || '更新教务端账号失败')
    } catch (error) {
      resolveApiError(error)
    }
  },
  updateInternalUserStatus: async (userId, data) => {
    try {
      const result = await apiUpdateInternalUserStatus(userId, data)
      if (result.code === 0) {
        return
      }

      throw new Error(result.message || '更新教务端账号状态失败')
    } catch (error) {
      resolveApiError(error)
    }
  },
  resetInternalUserPassword: async (userId, data) => {
    try {
      const result = await apiResetInternalUserPassword(userId, data)
      if (result.code === 0) {
        return
      }

      throw new Error(result.message || '重置教务端账号密码失败')
    } catch (error) {
      resolveApiError(error)
    }
  },
  clearInternalUserPage: () => set({ internalUserPage: null }),
  clearInternalUserDetail: () => set({ internalUserDetail: null }),
}))
