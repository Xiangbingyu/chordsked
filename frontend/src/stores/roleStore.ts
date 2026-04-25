import { isAxiosError } from 'axios'
import { create } from 'zustand'
import {
  createRole as apiCreateRole,
  deleteRole as apiDeleteRole,
  getRoleDetail as apiGetRoleDetail,
  listRoles as apiListRoles,
  updateRole as apiUpdateRole,
} from '../services/roleService'
import type { ApiResponse } from '../types/common'
import type {
  RoleCreateRequest,
  RoleDetailQueryResultVO,
  RolePageResult,
  RoleQueryRequest,
  RoleUpdateRequest,
} from '../types/role'

type RoleState = {
  loading: boolean
  detailLoading: boolean
  rolePage: RolePageResult | null
  roleDetail: RoleDetailQueryResultVO | null
  fetchRoles: (params: RoleQueryRequest) => Promise<RolePageResult>
  fetchRoleDetail: (roleId: number) => Promise<RoleDetailQueryResultVO>
  createRole: (data: RoleCreateRequest) => Promise<number>
  updateRole: (roleId: number, data: RoleUpdateRequest) => Promise<void>
  deleteRole: (roleId: number) => Promise<void>
  clearRolePage: () => void
  clearRoleDetail: () => void
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

export const useRoleStore = create<RoleState>((set) => ({
  loading: false,
  detailLoading: false,
  rolePage: null,
  roleDetail: null,
  fetchRoles: async (params) => {
    set({ loading: true })
    try {
      const result = await apiListRoles(params)
      if (result.code === 0) {
        set({ rolePage: result.data })
        return result.data
      }

      throw new Error(result.message || '查询角色列表失败')
    } catch (error) {
      resolveApiError(error)
    } finally {
      set({ loading: false })
    }
  },
  fetchRoleDetail: async (roleId) => {
    set({ detailLoading: true })
    try {
      const result = await apiGetRoleDetail(roleId)
      if (result.code === 0) {
        set({ roleDetail: result.data })
        return result.data
      }

      throw new Error(result.message || '查询角色详情失败')
    } catch (error) {
      resolveApiError(error)
    } finally {
      set({ detailLoading: false })
    }
  },
  createRole: async (data) => {
    try {
      const result = await apiCreateRole(data)
      if (result.code === 0) {
        return result.data
      }

      throw new Error(result.message || '创建角色失败')
    } catch (error) {
      resolveApiError(error)
    }
  },
  updateRole: async (roleId, data) => {
    try {
      const result = await apiUpdateRole(roleId, data)
      if (result.code === 0) {
        return
      }

      throw new Error(result.message || '更新角色失败')
    } catch (error) {
      resolveApiError(error)
    }
  },
  deleteRole: async (roleId) => {
    try {
      const result = await apiDeleteRole(roleId)
      if (result.code === 0) {
        return
      }

      throw new Error(result.message || '删除角色失败')
    } catch (error) {
      resolveApiError(error)
    }
  },
  clearRolePage: () => set({ rolePage: null }),
  clearRoleDetail: () => set({ roleDetail: null }),
}))
