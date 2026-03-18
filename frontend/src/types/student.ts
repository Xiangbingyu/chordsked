export type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export type PageResult<T> = {
  total: number
  items: T[]
}

export type Student = {
  id: number
  name: string
  age: number
  level: string
}

