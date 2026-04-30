export type OrgNodeOptionVO = {
  id: number
  parentId: number | null
  nodeType: number | null
  code?: string | null
  name: string
  status: number | null
}

export type OrgTreeNodeVO = {
  id: number
  parentId: number | null
  nodeType: number | null
  code: string
  name: string
  campusId: number | null
  level: number | null
  sort: number | null
  status: number | null
  remark?: string | null
  boundUserCount: number | null
  hasChildren: boolean | null
  children: OrgTreeNodeVO[]
}

export type OrgNodeCreateRequest = {
  parentId: number
  nodeType: number
  code: string
  name: string
  sort?: number
  status: number
  remark?: string
}

export type OrgNodeUpdateRequest = {
  code: string
  name: string
  sort?: number
  status: number
  remark?: string
}

export type OrgNodeUserBindUpdateRequest = {
  userIds: number[]
}

export type OrgAccountOptionVO = {
  userId: number
  username: string
  name: string
  dataScopeType: number | null
  campusId: number | null
  orgNodeId: number | null
}

export type OrgNodeBoundUserVO = {
  userId: number
  username: string
  name: string
  dataScopeType: number | null
  campusId: number | null
  orgNodeId: number | null
}
