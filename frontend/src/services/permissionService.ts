import http from './http'
import type { ApiResponse } from '../types/common'
import type { InternalPermissionTreeQueryResultVO } from '../types/permission'

export async function getPermissionTree() {
  const response = await http.get<ApiResponse<InternalPermissionTreeQueryResultVO[]>>(
    '/admin/api/v1/permissions/tree',
  )
  return response.data
}
