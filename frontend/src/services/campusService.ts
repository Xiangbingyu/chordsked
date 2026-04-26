import http from './http'
import type { ApiResponse } from '../types/common'
import type { InternalUserCampusOptionVO } from '../types/campus'

export async function listInternalUserCampusOptions() {
  const response = await http.get<ApiResponse<InternalUserCampusOptionVO[]>>(
    '/admin/api/v1/internal-users/campus-options',
  )
  return response.data
}
