import { isAxiosError } from 'axios'
import type { ApiResponse } from '../../../../../types/common'
import type { RoleQueryResultVO, RoleStatus } from '../../../../../types/role'

export const PAGE_SIZE = 10

export function resolveApiErrorMessage(error: unknown, fallback: string) {
  if (isAxiosError<ApiResponse<null>>(error)) {
    if (error.response?.status === 403) {
      return error.response?.data?.message || '当前账号没有查看角色列表的权限'
    }

    return error.response?.data?.message || fallback
  }

  return error instanceof Error ? error.message : fallback
}

export function getRoleStatusMeta(status: RoleStatus | null) {
  switch (status) {
    case 0:
      return { label: '禁用', textColor: '#a64545', backgroundColor: '#fff1f1' }
    case 1:
      return { label: '启用', textColor: '#247a4d', backgroundColor: '#eefbf3' }
    case 2:
      return { label: '已删除', textColor: '#7d7267', backgroundColor: '#f3f0eb' }
    default:
      return { label: '未知', textColor: '#7d7267', backgroundColor: '#f3f0eb' }
  }
}

export function formatRoleUpdatedAt(updatedAt: number | null) {
  if (!updatedAt) {
    return '未更新'
  }

  const normalized = updatedAt < 1_000_000_000_000 ? updatedAt * 1000 : updatedAt
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) {
    return '时间异常'
  }

  const pad = (value: number) => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function summarizeRoleStats(rows: RoleQueryResultVO[]) {
  return rows.reduce(
    (summary, row) => ({
      enabledCount: summary.enabledCount + (row.status === 1 ? 1 : 0),
      permissionCount: summary.permissionCount + (row.permissionCount || 0),
      userCount: summary.userCount + (row.userCount || 0),
    }),
    { enabledCount: 0, permissionCount: 0, userCount: 0 },
  )
}
