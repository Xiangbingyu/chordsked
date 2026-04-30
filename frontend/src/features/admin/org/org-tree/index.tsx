import { Alert, Button, Select, Spin, Table, Tree, Typography } from 'antd'
import type { DataNode } from 'antd/es/tree'
import { useCallback, useEffect, useMemo, useState } from 'react'
import OrgPageShell from '../shared/OrgPageShell'
import {
  listOrgAccountOptions,
  listOrgNodeBoundUsers,
  listOrgTree,
  updateOrgNodeBoundUsers,
} from '../../../../services/orgService'
import type { OrgAccountOptionVO, OrgNodeBoundUserVO, OrgTreeNodeVO } from '../../../../types/org'

function flattenNodes(nodes: OrgTreeNodeVO[]): OrgTreeNodeVO[] {
  return nodes.flatMap((node) => [node, ...flattenNodes(node.children || [])])
}

function buildTreeData(nodes: OrgTreeNodeVO[]): DataNode[] {
  return nodes.map((node) => ({
    key: String(node.id),
    title: `${node.name}${node.boundUserCount ? ` (${node.boundUserCount})` : ''}`,
    children: buildTreeData(node.children || []),
  }))
}

function OrgTreePage() {
  const [loading, setLoading] = useState(true)
  const [treeNodes, setTreeNodes] = useState<OrgTreeNodeVO[]>([])
  const [selectedNodeId, setSelectedNodeId] = useState<number | null>(null)
  const [selectedUsers, setSelectedUsers] = useState<OrgNodeBoundUserVO[]>([])
  const [accountOptions, setAccountOptions] = useState<OrgAccountOptionVO[]>([])
  const [selectedUserIds, setSelectedUserIds] = useState<number[]>([])
  const [errorMessage, setErrorMessage] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [actionErrorMessage, setActionErrorMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const loadBaseData = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const [treeResult, accountResult] = await Promise.all([
        listOrgTree(),
        listOrgAccountOptions(),
      ])
      if (treeResult.code !== 0) {
        throw new Error(treeResult.message || '加载组织树失败')
      }
      if (accountResult.code !== 0) {
        throw new Error(accountResult.message || '加载可绑定账号失败')
      }
      setTreeNodes(treeResult.data || [])
      setAccountOptions(accountResult.data || [])
    } catch (error) {
      setTreeNodes([])
      setAccountOptions([])
      setErrorMessage(error instanceof Error ? error.message : '加载组织树失败')
    } finally {
      setLoading(false)
    }
  }, [])

  const loadBoundUsers = useCallback(async (nodeId: number) => {
    try {
      const result = await listOrgNodeBoundUsers(nodeId)
      if (result.code !== 0) {
        throw new Error(result.message || '加载节点绑定账号失败')
      }
      const users = result.data || []
      setSelectedUsers(users)
      setSelectedUserIds(users.map((item) => item.userId))
    } catch (error) {
      setSelectedUsers([])
      setSelectedUserIds([])
      setActionErrorMessage(error instanceof Error ? error.message : '加载节点绑定账号失败')
    }
  }, [])

  useEffect(() => {
    void loadBaseData()
  }, [loadBaseData])

  const flatNodes = useMemo(() => flattenNodes(treeNodes), [treeNodes])
  const treeData = useMemo(() => buildTreeData(treeNodes), [treeNodes])
  const selectedNode = useMemo(
    () => flatNodes.find((node) => node.id === selectedNodeId) ?? null,
    [flatNodes, selectedNodeId],
  )
  const accountSelectOptions = useMemo(
    () =>
      accountOptions.map((option) => ({
        label: `${option.name} (${option.username})`,
        value: option.userId,
      })),
    [accountOptions],
  )

  useEffect(() => {
    if (!selectedNodeId && flatNodes.length > 0) {
      setSelectedNodeId(flatNodes[0].id)
    }
  }, [flatNodes, selectedNodeId])

  useEffect(() => {
    if (selectedNodeId) {
      void loadBoundUsers(selectedNodeId)
    }
  }, [loadBoundUsers, selectedNodeId])

  const handleSubmitBindings = async () => {
    if (!selectedNodeId) {
      return
    }
    setSubmitting(true)
    setActionMessage('')
    setActionErrorMessage('')
    try {
      const result = await updateOrgNodeBoundUsers(selectedNodeId, { userIds: selectedUserIds })
      if (result.code !== 0) {
        throw new Error(result.message || '更新绑定账号失败')
      }
      setActionMessage('节点绑定账号更新成功')
      await Promise.all([loadBaseData(), loadBoundUsers(selectedNodeId)])
    } catch (error) {
      setActionErrorMessage(error instanceof Error ? error.message : '更新绑定账号失败')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <OrgPageShell
      pageKey="org-tree"
      title="组织树管理"
      description="该页面只负责组织节点与教务账号的绑定关系维护。节点本身的创建、编辑、删除统一在组织管理页面处理。"
    >
      <section style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        {errorMessage ? <Alert message={errorMessage} type="error" showIcon /> : null}
        {actionErrorMessage ? <Alert message={actionErrorMessage} type="error" showIcon closable onClose={() => setActionErrorMessage('')} /> : null}
        {actionMessage ? <Alert message={actionMessage} type="success" showIcon closable onClose={() => setActionMessage('')} /> : null}

        <section
          style={{
            display: 'grid',
            gridTemplateColumns: 'minmax(280px, 360px) minmax(0, 1fr)',
            gap: 16,
          }}
        >
          <div style={{ border: '1px solid #f0ebe3', borderRadius: 18, padding: 16, backgroundColor: '#fffdf9' }}>
            <Typography.Title level={5} style={{ marginTop: 0 }}>
              组织树
            </Typography.Title>
            {loading ? (
              <div style={{ display: 'flex', justifyContent: 'center', padding: '48px 0' }}>
                <Spin />
              </div>
            ) : (
              <Tree
                treeData={treeData}
                selectedKeys={selectedNodeId ? [String(selectedNodeId)] : []}
                onSelect={(keys) => {
                  const first = keys[0]
                  if (first) {
                    setSelectedNodeId(Number(first))
                  }
                }}
                defaultExpandAll
              />
            )}
          </div>

          <div style={{ border: '1px solid #f0ebe3', borderRadius: 18, padding: 16, backgroundColor: '#fffdf9', display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div>
              <Typography.Title level={5} style={{ marginTop: 0 }}>
                节点绑定账号
              </Typography.Title>
              <Typography.Text type="secondary">
                当前节点：{selectedNode?.name || '未选择节点'}
              </Typography.Text>
            </div>

            <div>
              <div style={{ marginBottom: 6, fontSize: 12, color: '#8f8376' }}>绑定账号列表</div>
              <Select
                mode="multiple"
                allowClear
                value={selectedUserIds}
                options={accountSelectOptions}
                placeholder="选择要绑定到当前组织节点的教务账号"
                disabled={!selectedNodeId || submitting}
                onChange={(value) => setSelectedUserIds(value)}
                style={{ width: '100%' }}
              />
            </div>

            <div>
              <Button type="primary" disabled={!selectedNodeId || submitting} loading={submitting} onClick={() => void handleSubmitBindings()}>
                保存绑定关系
              </Button>
            </div>

            <Table
              rowKey="userId"
              size="small"
              pagination={false}
              dataSource={selectedUsers}
              columns={[
                { title: '姓名', dataIndex: 'name' },
                { title: '用户名', dataIndex: 'username' },
                {
                  title: '数据范围',
                  dataIndex: 'dataScopeType',
                  render: (value: number | null) => {
                    if (value === 2) {
                      return '指定组织'
                    }
                    if (value === 1) {
                      return '全部'
                    }
                    if (value === 3) {
                      return '本人'
                    }
                    return '-'
                  },
                },
                { title: '所属校区', dataIndex: 'campusId' },
              ]}
              locale={{ emptyText: selectedNodeId ? '当前节点暂无绑定账号' : '请先选择组织节点' }}
            />
          </div>
        </section>
      </section>
    </OrgPageShell>
  )
}

export default OrgTreePage
