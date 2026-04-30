import { isAxiosError } from 'axios'
import type {
  CampusDetailResultVO,
  CampusQueryResultVO,
  CampusStatus,
} from '../../../../../types/campus'
import type { ApiResponse } from '../../../../../types/common'

export const PAGE_SIZE = 10
export const TABLE_LOADING_OVERLAY_DELAY_MS = 180
export const CAMPUS_CODE_PATTERN = /^[A-Z0-9_-]+$/

export type CampusEditorFormState = {
  code: string
  name: string
  address: string
  phone: string
  sort: string
  status: CampusStatus
  remark: string
}

export function createEmptyCampusEditorForm(): CampusEditorFormState {
  return {
    code: '',
    name: '',
    address: '',
    phone: '',
    sort: '',
    status: 1,
    remark: '',
  }
}

export function createCampusEditorFormFromDetail(detail: CampusDetailResultVO): CampusEditorFormState {
  return {
    code: detail.code,
    name: detail.name,
    address: detail.address || '',
    phone: detail.phone || '',
    sort: detail.sort === null ? '' : String(detail.sort),
    status: detail.status ?? 1,
    remark: detail.remark || '',
  }
}

export function resolveApiErrorMessage(error: unknown, fallback: string) {
  if (isAxiosError<ApiResponse<null>>(error)) {
    if (error.response?.status === 403) {
      return error.response?.data?.message || fallback
    }

    return error.response?.data?.message || fallback
  }

  return error instanceof Error ? error.message : fallback
}

export function getCampusStatusMeta(status: CampusStatus | null) {
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

export function formatCampusUpdatedAt(updatedAt: number | null) {
  if (!updatedAt) {
    return '未记录'
  }

  const normalized = updatedAt < 1_000_000_000_000 ? updatedAt * 1000 : updatedAt
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) {
    return '时间异常'
  }

  const pad = (value: number) => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function summarizeCampusStats(rows: CampusQueryResultVO[]) {
  return rows.reduce(
    (summary, row) => ({
      enabledCount: summary.enabledCount + (row.status === 1 ? 1 : 0),
    }),
    { enabledCount: 0 },
  )
}
