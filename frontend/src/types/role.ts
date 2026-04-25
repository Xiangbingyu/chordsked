import type { PageResult } from './common'
import type { InternalPermissionTreeQueryResultVO } from './permission'

export type RoleStatus = 0 | 1 | 2

export type RoleQueryRequest = {
  page?: number
  pageSize?: number
  keyword?: string
  status?: RoleStatus
}

export type RoleCreateRequest = {
  code: string
  name: string
  description?: string
  status: RoleStatus
  permissionIds: number[]
}

export type RoleUpdateRequest = {
  name: string
  description?: string
  status: RoleStatus
  permissionIds: number[]
}

export type RoleQueryResultVO = {
  id: number
  code: string
  name: string
  description: string | null
  status: RoleStatus | null
  permissionCount: number | null
  userCount: number | null
  createdAt: number | null
  updatedAt: number | null
}

export type RoleDetailQueryResultVO = {
  id: number
  code: string
  name: string
  description: string | null
  status: RoleStatus | null
  permissionCount: number | null
  userCount: number | null
  permissionIds: number[]
  permissionTree: InternalPermissionTreeQueryResultVO[]
  createdAt: number | null
  updatedAt: number | null
}

export type RolePageResult = PageResult<RoleQueryResultVO>
