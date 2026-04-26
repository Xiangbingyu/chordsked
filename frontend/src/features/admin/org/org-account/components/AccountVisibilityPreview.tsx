import { Alert, Empty, Select, Tree } from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { getInternalUserDetail } from '../../../../../services/internalUserService'
import { getPermissionTree } from '../../../../../services/permissionService'
import type {
  InternalUserDetailResultVO,
  InternalUserQueryResultVO,
} from '../../../../../types/internalUser'
import type { InternalPermissionTreeQueryResultVO } from '../../../../../types/permission'
import type { RoleQueryResultVO } from '../../../../../types/role'
import {
  getDataScopeLabel,
  getStatusMeta,
  resolveApiErrorMessage,
  resolveRoleNames,
} from '../utils/accountPageShared'

type PermissionTreeNode = {
  key: string
  title: JSX.Element
  children: PermissionTreeNode[]
}

type AccountVisibilityPreviewProps = {
  rows: InternalUserQueryResultVO[]
  roles: RoleQueryResultVO[]
  permissionDenied: boolean
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

function buildPermissionTreeData(nodes: InternalPermissionTreeQueryResultVO[]): PermissionTreeNode[] {
  return nodes.map((node) => ({
    key: node.code,
    title: (
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
        <span style={{ fontSize: 12, fontWeight: 500, color: '#2a2a2f' }}>{node.name}</span>
        <span
          style={{
            borderRadius: 999,
            backgroundColor: '#faf8f4',
            padding: '2px 8px',
            fontSize: 10,
            color: '#7d7267',
            lineHeight: 1.4,
          }}
        >
          {node.code}
        </span>
      </div>
    ),
    children: buildPermissionTreeData(node.children),
  }))
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
        setDetailErrorMessage(
          resolveApiErrorMessage(error, '加载账号详情失败', 'detail'),
        )
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
        setPermissionTreeErrorMessage(
          resolveApiErrorMessage(error, '加载权限树失败', 'permissionTree'),
        )
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
  const permissionTreeData = useMemo(
    () => buildPermissionTreeData(filteredPermissionTree),
    [filteredPermissionTree],
  )
  const previewUserOptions = useMemo(
    () =>
      rows.map((row) => ({
        label: `${row.name}（${row.username}）`,
        value: row.id,
      })),
    [rows],
  )

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
              <Select
                size="small"
                value={selectedUserId ?? undefined}
                onChange={(value) => setSelectedUserId(value)}
                options={previewUserOptions}
                style={{ minWidth: 220 }}
                placeholder="请选择预览账号"
              />
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
              <Alert message={detailErrorMessage} type="error" showIcon />
            ) : permissionTreeErrorMessage ? (
              <Alert message={permissionTreeErrorMessage} type="error" showIcon />
            ) : displayPermissionCodes.length === 0 ? (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前账号暂无权限代码" />
            ) : (
              <Tree
                blockNode
                selectable={false}
                showLine
                expandedKeys={expandedPermissionCodes}
                onExpand={(keys) => setExpandedPermissionCodes(keys.map((key) => String(key)))}
                treeData={permissionTreeData}
              />
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
            {!detailErrorMessage &&
            !permissionTreeErrorMessage &&
            displayPermissionCodes.length > 0 &&
            permissionTreeData.length === 0 ? (
              <div style={{ marginTop: 12 }}>
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前账号暂无可映射的权限树节点" />
              </div>
            ) : null}
          </div>
        </>
      ) : (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={permissionDenied ? '当前账号没有查看该模块数据的权限。' : '当前没有可预览的教务账号。'}
        />
      )}
    </div>
  )
}

export default AccountVisibilityPreview
