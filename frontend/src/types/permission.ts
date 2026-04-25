export type InternalPermissionTreeQueryResultVO = {
  id: number
  code: string
  name: string
  type: number | null
  path: string | null
  sort: number | null
  children: InternalPermissionTreeQueryResultVO[]
}
