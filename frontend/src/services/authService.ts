import http from './http'
import type {
  AuthLoginRequest,
  AuthLoginResultVO,
  AuthRefreshResultVO,
} from '../types/auth'
import type { ApiResponse } from '../types/common'

export async function login(data: AuthLoginRequest) {
  const response = await http.post<ApiResponse<AuthLoginResultVO>>('/admin/api/v1/login', data)
  return response.data
}

export async function refreshToken() {
  const response = await http.post<ApiResponse<AuthRefreshResultVO>>('/admin/api/v1/refresh')
  return response.data
}

export async function logout() {
  const response = await http.post<ApiResponse<void>>('/admin/api/v1/logout')
  return response.data
}
