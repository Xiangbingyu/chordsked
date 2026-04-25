import http from './http'
import type { ApiResponse } from '../types/common'
import type {
  RoleCreateRequest,
  RoleDetailQueryResultVO,
  RolePageResult,
  RoleQueryRequest,
  RoleUpdateRequest,
} from '../types/role'

export async function listRoles(params: RoleQueryRequest) {
  const response = await http.get<ApiResponse<RolePageResult>>('/admin/api/v1/roles', {
    params,
  })
  return response.data
}

export async function createRole(data: RoleCreateRequest) {
  const response = await http.post<ApiResponse<number>>('/admin/api/v1/roles', data)
  return response.data
}

export async function getRoleDetail(roleId: number) {
  const response = await http.get<ApiResponse<RoleDetailQueryResultVO>>(
    `/admin/api/v1/roles/${roleId}`,
  )
  return response.data
}

export async function updateRole(roleId: number, data: RoleUpdateRequest) {
  const response = await http.put<ApiResponse<void>>(`/admin/api/v1/roles/${roleId}`, data)
  return response.data
}

export async function deleteRole(roleId: number) {
  const response = await http.delete<ApiResponse<void>>(`/admin/api/v1/roles/${roleId}`)
  return response.data
}
