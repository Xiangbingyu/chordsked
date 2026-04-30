import http from './http'
import type { ApiResponse } from '../types/common'
import type {
  CampusCreateRequest,
  CampusDetailResultVO,
  CampusPageResult,
  CampusQueryRequest,
  CampusUpdateRequest,
  InternalUserCampusOptionVO,
} from '../types/campus'

export async function listCampuses(params: CampusQueryRequest) {
  const response = await http.get<ApiResponse<CampusPageResult>>('/admin/api/v1/campuses', {
    params,
  })
  return response.data
}

export async function createCampus(data: CampusCreateRequest) {
  const response = await http.post<ApiResponse<number>>('/admin/api/v1/campuses', data)
  return response.data
}

export async function getCampusDetail(campusId: number) {
  const response = await http.get<ApiResponse<CampusDetailResultVO>>(
    `/admin/api/v1/campuses/${campusId}`,
  )
  return response.data
}

export async function updateCampus(campusId: number, data: CampusUpdateRequest) {
  const response = await http.put<ApiResponse<void>>(`/admin/api/v1/campuses/${campusId}`, data)
  return response.data
}

export async function deleteCampus(campusId: number) {
  const response = await http.delete<ApiResponse<void>>(`/admin/api/v1/campuses/${campusId}`)
  return response.data
}

export async function listInternalUserCampusOptions() {
  const response = await http.get<ApiResponse<InternalUserCampusOptionVO[]>>(
    '/admin/api/v1/internal-users/campus-options',
  )
  return response.data
}
