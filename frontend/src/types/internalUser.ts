import type { PageResult } from './common'

export type InternalUserStatus = 0 | 1 | 2
export type UserDataScopeType = 1 | 3 | 4
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
  campusIds: number[]
  primaryCampusId: number
  dataScopeType: UserDataScopeType
}

export type InternalUserUpdateRequest = {
  phone: string
  name: string
  avatar?: string
  roleIds: number[]
  campusIds: number[]
  primaryCampusId: number
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
  primaryCampusId: number | null
  primaryCampusName: string | null
  roleIds: number[]
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
  primaryCampusId: number | null
  campusIds: number[]
  roleIds: number[]
  permissionCodes: string[]
  systemAccount: boolean
  currentUser: boolean
  createdAt: number | null
  updatedAt: number | null
}

export type InternalUserPageResult = PageResult<InternalUserQueryResultVO>
