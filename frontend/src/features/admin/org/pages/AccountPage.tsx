import { isAxiosError } from 'axios'
import { useEffect, useMemo, useState } from 'react'
import { getInternalUserDetail, listInternalUsers } from '../../../../services/internalUserService'
import { getPermissionTree } from '../../../../services/permissionService'
import { listRoles } from '../../../../services/roleService'
import type { ApiResponse } from '../../../../types/common'
import type {
  InternalUserDetailResultVO,
  InternalUserQueryResultVO,
  InternalUserStatus,
  UserDataScopeType,
} from '../../../../types/internalUser'
import type { InternalPermissionTreeQueryResultVO } from '../../../../types/permission'
import type { RoleQueryResultVO } from '../../../../types/role'
import OrgPageShell from './OrgPageShell'

const PAGE_SIZE = 10

function resolveApiErrorMessage(error: unknown, fallback: string) {
  if (isAxiosError<ApiResponse<null>>(error)) {
    if (error.response?.status === 403) {
      return '当前账号没有查看教务账号列表的权限'
    }

    return error.response?.data?.message || fallback
  }

  return error instanceof Error ? error.message : fallback
}

function getStatusMeta(status: InternalUserStatus | null) {
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

function getDataScopeLabel(dataScopeType: UserDataScopeType | null) {
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

function resolveRoleNames(roleIds: number[], roleNameMap: Map<number, string>) {
  return roleIds.map((roleId) => roleNameMap.get(roleId) || `角色#${roleId}`)
}

function collectExpandableCodes(nodes: InternalPermissionTreeQueryResultVO[]): string[] {
  return nodes.flatMap((node) => [
    ...(node.children.length > 0 ? [node.code] : []),
    ...collectExpandableCodes(node.children),
  ])
}

function filterPermissionTree(
  nodes: InternalPermissionTreeQueryResultVO[],
  permissionCodes: Set<string>,
): InternalPermissionTreeQueryResultVO[] {
  return nodes
    .map((node) => {
      const children = filterPermissionTree(node.children, permissionCodes)
      const matched = permissionCodes.has(node.code) || children.length > 0
      return matched ? { ...node, children } : null
    })
    .filter((node): node is InternalPermissionTreeQueryResultVO => node !== null)
}

function getPermissionNodePrefix(node: InternalPermissionTreeQueryResultVO) {
  if (node.children.length > 0) {
    return '−'
  }

  return '•'
}

type AccountVisibilityPreviewProps = {
  rows: InternalUserQueryResultVO[]
  roles: RoleQueryResultVO[]
  permissionDenied: boolean
}

function AccountVisibilityPreview({
  rows,
  roles,
  permissionDenied,
}: AccountVisibilityPreviewProps) {
  const [permissionTree, setPermissionTree] = useState<InternalPermissionTreeQueryResultVO[]>([])
  const [permissionTreeErrorMessage, setPermissionTreeErrorMessage] = useState('')
  const [expandedPermissionCodes, setExpandedPermissionCodes] = useState<string[]>([])
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null)
  const [selectedUserDetail, setSelectedUserDetail] = useState<InternalUserDetailResultVO | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailErrorMessage, setDetailErrorMessage] = useState('')

  useEffect(() => {
    setSelectedUserId((currentId) => (rows.some((item) => item.id === currentId) ? currentId : rows[0]?.id ?? null))
  }, [rows])

  useEffect(() => {
    let isMounted = true

    const loadInternalUserDetail = async () => {
      if (!selectedUserId) {
        setSelectedUserDetail(null)
        setDetailErrorMessage('')
        setDetailLoading(false)
        return
      }

      setDetailLoading(true)
      setDetailErrorMessage('')

      try {
        const result = await getInternalUserDetail(selectedUserId)
        if (!isMounted) {
          return
        }

        if (result.code !== 0) {
          throw new Error(result.message || '加载账号详情失败')
        }

        setSelectedUserDetail(result.data)
      } catch (error) {
        if (!isMounted) {
          return
        }

        setSelectedUserDetail(null)
        setDetailErrorMessage(resolveApiErrorMessage(error, '加载账号详情失败'))
      } finally {
        if (isMounted) {
          setDetailLoading(false)
        }
      }
    }

    void loadInternalUserDetail()

    return () => {
      isMounted = false
    }
  }, [selectedUserId])

  useEffect(() => {
    let isMounted = true

    const loadPermissionTree = async () => {
      try {
        const result = await getPermissionTree()
        if (!isMounted) {
          return
        }

        if (result.code !== 0) {
          throw new Error(result.message || '加载权限树失败')
        }

        setPermissionTree(result.data)
        setExpandedPermissionCodes(collectExpandableCodes(result.data))
        setPermissionTreeErrorMessage('')
      } catch (error) {
        if (!isMounted) {
          return
        }

        setPermissionTree([])
        setExpandedPermissionCodes([])
        setPermissionTreeErrorMessage(resolveApiErrorMessage(error, '加载权限树失败'))
      }
    }

    void loadPermissionTree()

    return () => {
      isMounted = false
    }
  }, [])

  const roleNameMap = useMemo(
    () => new Map(roles.map((role) => [role.id, role.name])),
    [roles],
  )
  const selectedUser = rows.find((item) => item.id === selectedUserId) ?? rows[0] ?? null
  const activeSelectedUserDetail = selectedUserDetail?.id === selectedUserId ? selectedUserDetail : null
  const selectedRoleIds = activeSelectedUserDetail?.roleIds ?? selectedUser?.roleIds ?? []
  const selectedUserRoleNames = resolveRoleNames(selectedRoleIds, roleNameMap)
  const selectedUserStatusMeta = getStatusMeta(activeSelectedUserDetail?.status ?? selectedUser?.status ?? null)
  const selectedUserDataScopeLabel = getDataScopeLabel(
    activeSelectedUserDetail?.dataScopeType ?? selectedUser?.dataScopeType ?? null,
  )
  const displayPermissionCodes = useMemo(() => {
    if (activeSelectedUserDetail) {
      return activeSelectedUserDetail.permissionCodes
    }

    return selectedUserDetail?.permissionCodes || []
  }, [activeSelectedUserDetail, selectedUserDetail])
  const selectedPermissionCodes = useMemo(
    () => new Set(displayPermissionCodes),
    [displayPermissionCodes],
  )
  const filteredPermissionTree = useMemo(
    () => filterPermissionTree(permissionTree, selectedPermissionCodes),
    [permissionTree, selectedPermissionCodes],
  )

  const togglePermissionNode = (code: string) => {
    setExpandedPermissionCodes((current) =>
      current.includes(code) ? current.filter((item) => item !== code) : [...current, code],
    )
  }

  const renderPermissionTree = (nodes: InternalPermissionTreeQueryResultVO[], level = 0): JSX.Element[] =>
    nodes.map((node) => {
      const expanded = expandedPermissionCodes.includes(node.code)
      const hasChildren = node.children.length > 0

      return (
        <div key={node.code} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <div
            style={{
              marginLeft: level * 16,
              borderRadius: 10,
              border: '1px solid #ece7df',
              backgroundColor: '#ffffff',
              padding: 10,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <button
                type="button"
                onClick={() => {
                  if (hasChildren) {
                    togglePermissionNode(node.code)
                  }
                }}
                style={{
                  height: 20,
                  width: 20,
                  borderRadius: 6,
                  border: '1px solid #e8dfd3',
                  backgroundColor: '#ffffff',
                  fontSize: 12,
                  color: '#5f564d',
                  cursor: hasChildren ? 'pointer' : 'default',
                }}
              >
                {hasChildren ? (expanded ? '−' : '+') : getPermissionNodePrefix(node)}
              </button>
              <span style={{ fontSize: 12, fontWeight: 500, color: '#2a2a2f' }}>{node.name}</span>
              <span
                style={{
                  borderRadius: 999,
                  backgroundColor: '#faf8f4',
                  padding: '2px 8px',
                  fontSize: 10,
                  color: '#7d7267',
                }}
              >
                {node.code}
              </span>
            </div>
          </div>
          {hasChildren && expanded ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {renderPermissionTree(node.children, level + 1)}
            </div>
          ) : null}
        </div>
      )
    })

  return (
    <div
      style={{
        borderRadius: 14,
        border: '1px solid #f0ebe3',
        padding: 14,
      }}
    >
      <div style={{ marginBottom: 10, fontSize: 14, fontWeight: 600, color: '#2a2a2f' }}>
        账号可见性预览
      </div>
      {selectedUser ? (
        <>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, marginBottom: 10, alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ fontSize: 12, color: '#7b7064' }}>预览账号</span>
              <select
                value={selectedUserId ?? ''}
                onChange={(event) => setSelectedUserId(Number(event.target.value))}
                style={{
                  borderRadius: 10,
                  border: '1px solid #e9e2d8',
                  backgroundColor: '#ffffff',
                  padding: '6px 10px',
                  fontSize: 12,
                  color: '#3a352f',
                }}
              >
                {rows.map((row) => (
                  <option key={row.id} value={row.id}>
                    {row.name}（{row.username}）
                  </option>
                ))}
              </select>
            </div>
            <div style={{ fontSize: 12, color: '#7b7064' }}>
              当前页可见账号：{rows.length} 个
            </div>
          </div>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
              gap: 8,
              fontSize: 12,
            }}
          >
            <div style={{ borderRadius: 10, backgroundColor: '#faf8f4', padding: 10 }}>
              绑定角色：{selectedUserRoleNames.length > 0 ? selectedUserRoleNames.join(' / ') : '未绑定'}
            </div>
            <div style={{ borderRadius: 10, backgroundColor: '#faf8f4', padding: 10 }}>
              当前状态：{selectedUserStatusMeta.label}
            </div>
            <div style={{ borderRadius: 10, backgroundColor: '#faf8f4', padding: 10 }}>
              数据范围：{selectedUserDataScopeLabel}
            </div>
            <div style={{ borderRadius: 10, backgroundColor: '#faf8f4', padding: 10 }}>
              所属校区：{selectedUser.primaryCampusName || '未分配'}
            </div>
          </div>
          <div
            style={{
              borderRadius: 12,
              border: '1px solid #f0ebe3',
              backgroundColor: '#fffdf9',
              padding: 12,
              marginTop: 10,
              minHeight: 220,
              position: 'relative',
            }}
          >
            <div style={{ marginBottom: 8, fontSize: 13, fontWeight: 600, color: '#2a2a2f' }}>
              当前账号拥有的权限
            </div>
            {detailErrorMessage ? (
              <div style={{ fontSize: 12, color: '#a64545' }}>{detailErrorMessage}</div>
            ) : permissionTreeErrorMessage ? (
              <div style={{ fontSize: 12, color: '#a64545' }}>{permissionTreeErrorMessage}</div>
            ) : displayPermissionCodes.length === 0 ? (
              <div style={{ fontSize: 12, color: '#7d7267' }}>当前账号暂无权限代码</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {filteredPermissionTree.length > 0 ? (
                  renderPermissionTree(filteredPermissionTree)
                ) : (
                  <div style={{ fontSize: 12, color: '#7d7267' }}>当前账号暂无可映射的权限树节点</div>
                )}
              </div>
            )}
            {detailLoading ? (
              <div
                style={{
                  position: 'absolute',
                  inset: 0,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderRadius: 12,
                  backgroundColor: 'rgba(255, 253, 249, 0.82)',
                  backdropFilter: 'blur(1px)',
                  color: '#7d7267',
                  fontSize: 12,
                }}
              >
                正在切换预览账号...
              </div>
            ) : null}
          </div>
        </>
      ) : (
        <div style={{ fontSize: 13, color: '#7d7267', lineHeight: 1.8 }}>
          {permissionDenied ? '当前账号没有查看该模块数据的权限。' : '当前没有可预览的教务账号。'}
        </div>
      )}
    </div>
  )
}

function AccountPage() {
  const [loading, setLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(0)
  const [rows, setRows] = useState<InternalUserQueryResultVO[]>([])
  const [roles, setRoles] = useState<RoleQueryResultVO[]>([])

  useEffect(() => {
    let isMounted = true

    const loadAccounts = async () => {
      setLoading(true)
      setErrorMessage('')
      try {
        const [internalUserResult, roleResult] = await Promise.allSettled([
          listInternalUsers({ page, pageSize: PAGE_SIZE }),
          listRoles({ page: 1, pageSize: 100 }),
        ])

        if (!isMounted) {
          return
        }

        if (internalUserResult.status !== 'fulfilled') {
          throw internalUserResult.reason
        }

        if (internalUserResult.value.code !== 0) {
          throw new Error(internalUserResult.value.message || '加载教务账号列表失败')
        }

        const items = internalUserResult.value.data.items || []
        setRows(items)
        setTotal(internalUserResult.value.data.total || 0)

        if (roleResult.status === 'fulfilled' && roleResult.value.code === 0) {
          setRoles(roleResult.value.data.items || [])
        } else {
          setRoles([])
        }
      } catch (error) {
        if (!isMounted) {
          return
        }

        setRows([])
        setTotal(0)
        setRoles([])
        setErrorMessage(resolveApiErrorMessage(error, '加载教务账号列表失败'))
      } finally {
        if (isMounted) {
          setLoading(false)
        }
      }
    }

    void loadAccounts()

    return () => {
      isMounted = false
    }
  }, [page])

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE))
  const roleNameMap = useMemo(
    () => new Map(roles.map((role) => [role.id, role.name])),
    [roles],
  )
  const permissionDenied = errorMessage.includes('没有查看教务账号列表的权限')

  return (
    <OrgPageShell
      pageKey="org-account"
      title="账号管理"
      description="账号管理属于账号与授权部分。当前列表直接调用教务端账号接口，最终展示结果以当前登录账号的数据权限范围和后端鉴权结果为准。"
    >
      <section style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, alignItems: 'center' }}>
          <div style={{ fontSize: 14, color: '#7d7267', lineHeight: 1.8 }}>
            当前列表会自动按登录账号的权限范围过滤教务账号。权限不足时接口不会返回可见账号，或直接返回无权限错误。
          </div>
          <button
            type="button"
            disabled
            style={{
              border: 'none',
              borderRadius: 10,
              backgroundColor: '#ffe2cc',
              color: '#8f6746',
              padding: '10px 14px',
              fontSize: 14,
              fontWeight: 600,
              cursor: 'not-allowed',
              opacity: 0.75,
              whiteSpace: 'nowrap',
            }}
          >
            创建账号
          </button>
        </div>

        <div
          style={{
            borderRadius: 18,
            border: '1px solid #f0ebe3',
            backgroundColor: '#fffaf2',
            padding: 14,
            display: 'flex',
            flexWrap: 'wrap',
            gap: 12,
          }}
        >
          <div style={{ minWidth: 180, flex: 1 }}>
            <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>当前页可见账号数</div>
            <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{rows.length}</div>
            <div style={{ marginTop: 4, fontSize: 12, color: '#7d7267' }}>
              总条数：{total}
            </div>
          </div>
          <div style={{ minWidth: 180, flex: 1 }}>
            <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>数据来源说明</div>
            <div style={{ fontSize: 13, color: '#6f6256', lineHeight: 1.8 }}>
              列表调用 `listInternalUsers`，预览详情调用 `getInternalUserDetail`，不在前端额外放宽权限范围。
            </div>
          </div>
        </div>

        <div
          style={{
            overflow: 'hidden',
            borderRadius: 18,
            border: '1px solid #f0ebe3',
            backgroundColor: '#ffffff',
          }}
        >
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14, lineHeight: 1.6 }}>
            <thead style={{ backgroundColor: '#faf7f1', color: '#6f655b' }}>
              <tr>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>账号</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>姓名</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>联系方式</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>所属校区</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>绑定角色</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>数据范围</th>
                <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>状态</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={7} style={{ padding: 24, color: '#7d7267', textAlign: 'center' }}>
                    正在加载账号列表...
                  </td>
                </tr>
              ) : errorMessage ? (
                <tr>
                  <td
                    colSpan={7}
                    style={{ padding: 24, color: permissionDenied ? '#a64545' : '#7d7267', textAlign: 'center' }}
                  >
                    {errorMessage}
                  </td>
                </tr>
              ) : rows.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ padding: 24, color: '#7d7267', textAlign: 'center' }}>
                    当前权限范围下暂无可见教务账号
                  </td>
                </tr>
              ) : (
                rows.map((row) => {
                  const roleNames = resolveRoleNames(row.roleIds, roleNameMap)
                  const statusMeta = getStatusMeta(row.status)

                  return (
                    <tr
                      key={row.id}
                      style={{
                        borderTop: '1px solid #f1ece4',
                        backgroundColor: '#ffffff',
                      }}
                    >
                      <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.username}</td>
                      <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.name}</td>
                      <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.phone}</td>
                      <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                        {row.primaryCampusName || '未分配'}
                      </td>
                      <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                        {roleNames.length > 0 ? roleNames.join(' / ') : '未绑定'}
                      </td>
                      <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                        {getDataScopeLabel(row.dataScopeType)}
                      </td>
                      <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                        <span
                          style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            borderRadius: 999,
                            backgroundColor: statusMeta.backgroundColor,
                            color: statusMeta.textColor,
                            padding: '4px 10px',
                            fontSize: 12,
                            fontWeight: 600,
                          }}
                        >
                          {statusMeta.label}
                        </span>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>

          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'flex-end',
              gap: 10,
              borderTop: '1px solid #f1ece4',
              backgroundColor: '#ffffff',
              padding: '12px 16px',
              fontSize: 13,
            }}
          >
            <button
              type="button"
              onClick={() => setPage((current) => Math.max(1, current - 1))}
              disabled={page <= 1 || loading}
              style={{
                borderRadius: 10,
                border: '1px solid #e8dfd3',
                backgroundColor: page <= 1 || loading ? '#f7f3ee' : '#ffffff',
                color: '#5f564d',
                padding: '6px 12px',
                cursor: page <= 1 || loading ? 'not-allowed' : 'pointer',
              }}
            >
              上一页
            </button>
            <span style={{ color: '#6f655b' }}>
              {page}/{totalPages}
            </span>
            <button
              type="button"
              onClick={() => setPage((current) => Math.min(totalPages, current + 1))}
              disabled={page >= totalPages || loading}
              style={{
                borderRadius: 10,
                border: '1px solid #e8dfd3',
                backgroundColor: page >= totalPages || loading ? '#f7f3ee' : '#ffffff',
                color: '#5f564d',
                padding: '6px 12px',
                cursor: page >= totalPages || loading ? 'not-allowed' : 'pointer',
              }}
            >
              下一页
            </button>
          </div>
        </div>

        <AccountVisibilityPreview rows={rows} roles={roles} permissionDenied={permissionDenied} />
      </section>
    </OrgPageShell>
  )
}

export default AccountPage
