import { isAxiosError } from 'axios'
import type { ApiResponse } from '../../../../../types/common'
import type {
  InternalUserQueryResultVO,
  InternalUserStatus,
  UserDataScopeType,
} from '../../../../../types/internalUser'

export const PAGE_SIZE = 10
export const PHONE_PATTERN = /^1[3-9]\d{9}$/
export const TABLE_LOADING_OVERLAY_DELAY_MS = 180
export const SYSTEM_ADMIN_ROLE_CODE = 'SYSTEM_ADMIN'
export const DATA_SCOPE_OPTIONS: Array<{ value: UserDataScopeType; label: string }> = [
  { value: 1, label: '全部校区' },
  { value: 3, label: '仅本人' },
  { value: 4, label: '指定校区' },
]

export type AccountEditFormState = {
  userId: number
  username: string
  name: string
  avatar: string
  phone: string
  campusIds: number[]
  primaryCampusId: number | null
  roleIds: number[]
  dataScopeType: UserDataScopeType | null
}

export type ApiErrorContext =
  | 'list'
  | 'detail'
  | 'editLoad'
  | 'permissionTree'
  | 'update'
  | 'status'
  | 'reset'

export function isForbiddenError(error: unknown) {
  return isAxiosError<ApiResponse<null>>(error) && error.response?.status === 403
}

function getForbiddenErrorMessage(context: ApiErrorContext) {
  switch (context) {
    case 'list':
      return '当前账号没有查看教务账号列表的权限'
    case 'detail':
      return '当前账号没有查看该账号详情的权限'
    case 'editLoad':
      return '当前账号没有加载该账号编辑信息的权限'
    case 'permissionTree':
      return '当前账号没有查看权限树的权限'
    case 'update':
      return '当前账号没有修改该账号的权限'
    case 'status':
      return '当前账号没有修改该账号状态的权限'
    case 'reset':
      return '当前账号没有重置该账号密码的权限'
    default:
      return ''
  }
}

export function resolveApiErrorMessage(error: unknown, fallback: string, context?: ApiErrorContext) {
  if (isAxiosError<ApiResponse<null>>(error)) {
    if (error.response?.status === 403) {
      return error.response?.data?.message || (context ? getForbiddenErrorMessage(context) : fallback)
    }

    return error.response?.data?.message || fallback
  }

  return error instanceof Error ? error.message : fallback
}

export function getStatusMeta(status: InternalUserStatus | null) {
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

export function getDataScopeLabel(dataScopeType: UserDataScopeType | null) {
  switch (dataScopeType) {
    case 1:
      return '全部校区'
    case 3:
      return '仅本人'
    case 4:
      return '指定校区'
    default:
      return '未设置'
  }
}

export function resolveRoleNames(roleIds: number[], roleNameMap: Map<number, string>) {
  return roleIds.map((roleId) => roleNameMap.get(roleId) || `角色#${roleId}`)
}

export function getAccountOperationBlockReason(row: InternalUserQueryResultVO) {
  if (row.systemAccount) {
    return '系统管理员账号不可操作'
  }

  if (row.currentUser) {
    return '不能操作当前登录账号'
  }

  return ''
}
