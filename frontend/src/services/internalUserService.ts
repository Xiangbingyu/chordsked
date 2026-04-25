import http from './http'
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

export async function listInternalUsers(params: InternalUserQueryRequest) {
  const response = await http.get<ApiResponse<InternalUserPageResult>>('/admin/api/v1/internal-users', {
    params,
  })
  return response.data
}

export async function createInternalUser(data: InternalUserCreateRequest) {
  const response = await http.post<ApiResponse<number>>('/admin/api/v1/internal-users', data)
  return response.data
}

export async function getInternalUserDetail(userId: number) {
  const response = await http.get<ApiResponse<InternalUserDetailResultVO>>(
    `/admin/api/v1/internal-users/${userId}`,
  )
  return response.data
}

export async function updateInternalUser(userId: number, data: InternalUserUpdateRequest) {
  const response = await http.put<ApiResponse<void>>(`/admin/api/v1/internal-users/${userId}`, data)
  return response.data
}

export async function updateInternalUserStatus(
  userId: number,
  data: InternalUserStatusUpdateRequest,
) {
  const response = await http.put<ApiResponse<void>>(
    `/admin/api/v1/internal-users/${userId}/status`,
    data,
  )
  return response.data
}

export async function resetInternalUserPassword(
  userId: number,
  data: InternalUserResetPasswordRequest,
) {
  const response = await http.put<ApiResponse<void>>(
    `/admin/api/v1/internal-users/${userId}/reset-password`,
    data,
  )
  return response.data
}
