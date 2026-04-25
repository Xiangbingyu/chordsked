import { isAxiosError } from 'axios'
import { create } from 'zustand'
import { getPermissionTree as apiGetPermissionTree } from '../services/permissionService'
import type { ApiResponse } from '../types/common'
import type { InternalPermissionTreeQueryResultVO } from '../types/permission'

type PermissionState = {
  loading: boolean
  tree: InternalPermissionTreeQueryResultVO[]
  fetchPermissionTree: () => Promise<InternalPermissionTreeQueryResultVO[]>
  clearPermissionTree: () => void
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

export const usePermissionStore = create<PermissionState>((set) => ({
  loading: false,
  tree: [],
  fetchPermissionTree: async () => {
    set({ loading: true })
    try {
      const result = await apiGetPermissionTree()
      if (result.code === 0) {
        set({ tree: result.data })
        return result.data
      }

      throw new Error(result.message || '查询权限树失败')
    } catch (error) {
      resolveApiError(error)
    } finally {
      set({ loading: false })
    }
  },
  clearPermissionTree: () => set({ tree: [] }),
}))
