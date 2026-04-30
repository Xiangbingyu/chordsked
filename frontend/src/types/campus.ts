import type { PageResult } from './common'

export type CampusStatus = 0 | 1 | 2

export type CampusQueryRequest = {
  page?: number
  pageSize?: number
  keyword?: string
  status?: CampusStatus
}

export type CampusCreateRequest = {
  code: string
  name: string
  address?: string
  phone?: string
  sort?: number
  status: CampusStatus
  remark?: string
}

export type CampusUpdateRequest = CampusCreateRequest

export type CampusQueryResultVO = {
  id: number
  code: string
  name: string
  phone: string | null
  status: CampusStatus | null
  updatedAt: number | null
}

export type CampusDetailResultVO = {
  id: number
  code: string
  name: string
  address: string | null
  phone: string | null
  sort: number | null
  status: CampusStatus | null
  remark: string | null
  createdAt: number | null
  updatedAt: number | null
}

export type CampusPageResult = PageResult<CampusQueryResultVO>

export type InternalUserCampusOptionVO = {
  id: number
  name: string
  status: CampusStatus | null
}
