import http from './http'
import type { ApiResponse, PageResult, Student } from '../types/student'
import { normalizeChineseText } from '../utils/text'

export async function fetchStudents(params?: {
  page?: number
  pageSize?: number
  keyword?: string
  level?: string
}) {
  const response = await http.get<ApiResponse<PageResult<Student>>>('/v1/students', {
    params: {
      page: params?.page ?? 1,
      pageSize: params?.pageSize ?? 20,
      keyword: params?.keyword,
      level: params?.level,
    },
  })
  const normalizedItems = response.data.data.items.map((item) => ({
    ...item,
    name: normalizeChineseText(item.name),
    level: normalizeChineseText(item.level),
  }))
  return {
    ...response.data,
    data: {
      ...response.data.data,
      items: normalizedItems,
    },
  }
}
