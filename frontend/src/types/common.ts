export type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export type PageResult<T> = {
  total: number
  items: T[]
}
