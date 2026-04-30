import http from './http'
import type { ApiResponse } from '../types/common'
import type {
  OrgAccountOptionVO,
  OrgNodeBoundUserVO,
  OrgNodeCreateRequest,
  OrgNodeOptionVO,
  OrgNodeUpdateRequest,
  OrgNodeUserBindUpdateRequest,
  OrgTreeNodeVO,
} from '../types/org'

export async function listOrgTree() {
  const response = await http.get<ApiResponse<OrgTreeNodeVO[]>>('/admin/api/v1/org-tree')
  return response.data
}

export async function listOrgNodeOptions() {
  const response = await http.get<ApiResponse<OrgNodeOptionVO[]>>('/admin/api/v1/org-nodes/options')
  return response.data
}

export async function createOrgNode(data: OrgNodeCreateRequest) {
  const response = await http.post<ApiResponse<number>>('/admin/api/v1/org-nodes', data)
  return response.data
}

export async function updateOrgNode(nodeId: number, data: OrgNodeUpdateRequest) {
  const response = await http.put<ApiResponse<void>>(`/admin/api/v1/org-nodes/${nodeId}`, data)
  return response.data
}

export async function deleteOrgNode(nodeId: number) {
  const response = await http.delete<ApiResponse<void>>(`/admin/api/v1/org-nodes/${nodeId}`)
  return response.data
}

export async function listOrgAccountOptions() {
  const response = await http.get<ApiResponse<OrgAccountOptionVO[]>>('/admin/api/v1/org-accounts/options')
  return response.data
}

export async function listOrgNodeBoundUsers(nodeId: number) {
  const response = await http.get<ApiResponse<OrgNodeBoundUserVO[]>>(`/admin/api/v1/org-nodes/${nodeId}/users`)
  return response.data
}

export async function updateOrgNodeBoundUsers(nodeId: number, data: OrgNodeUserBindUpdateRequest) {
  const response = await http.put<ApiResponse<void>>(`/admin/api/v1/org-nodes/${nodeId}/users`, data)
  return response.data
}
