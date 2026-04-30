import type { PageResult } from './common'

export type InternalUserStatus = 0 | 1 | 2
export type UserDataScopeType = 1 | 2 | 3
export type MustChangePasswordFlag = 0 | 1

export type InternalUserQueryRequest = {
  page?: number
  pageSize?: number
  keyword?: string
  status?: InternalUserStatus
  roleId?: number
  campusId?: number
}

export type InternalUserCreateRequest = {
  username: string
  phone: string
  name: string
  avatar?: string
  roleIds: number[]
  primaryOrgNodeId: number
  orgScopeNodeIds: number[]
  dataScopeType: UserDataScopeType
}

export type InternalUserUpdateRequest = {
  phone: string
  name: string
  avatar?: string
  roleIds: number[]
  primaryOrgNodeId: number
  orgScopeNodeIds: number[]
  dataScopeType: UserDataScopeType
}

export type InternalUserStatusUpdateRequest = {
  status: InternalUserStatus
}

export type InternalUserResetPasswordRequest = {
  reason?: string
}

export type InternalUserQueryResultVO = {
  id: number
  username: string
  phone: string
  name: string
  status: InternalUserStatus | null
  dataScopeType: UserDataScopeType | null
  campusId: number | null
  campusName: string | null
  orgNodeId: number | null
  orgNodeName: string | null
  orgNodeType: number | null
  roleIds: number[]
  roleNames: string[]
  systemAccount: boolean
  currentUser: boolean
  createdAt: number | null
  updatedAt: number | null
}

export type InternalUserDetailResultVO = {
  id: number
  username: string
  phone: string
  name: string
  avatar: string | null
  status: InternalUserStatus | null
  mustChangePassword: MustChangePasswordFlag | null
  dataScopeType: UserDataScopeType | null
  campusId: number | null
  campusName: string | null
  orgNodeId: number | null
  orgNodeName: string | null
  orgNodeType: number | null
  orgScopeNodeIds: number[]
  roleIds: number[]
  roleNames: string[]
  permissionCodes: string[]
  systemAccount: boolean
  currentUser: boolean
  createdAt: number | null
  updatedAt: number | null
}

export type InternalUserPageResult = PageResult<InternalUserQueryResultVO>
