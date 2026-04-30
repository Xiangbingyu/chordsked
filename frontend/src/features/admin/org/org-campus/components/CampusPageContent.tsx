import { Alert, Button, Input, Modal, Select, Spin, Tag, Tree, Typography } from 'antd'
import type { DataNode } from 'antd/es/tree'
import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  createOrgNode,
  deleteOrgNode,
  listOrgTree,
  updateOrgNode,
} from '../../../../../services/orgService'
import type {
  OrgNodeCreateRequest,
  OrgNodeUpdateRequest,
  OrgTreeNodeVO,
} from '../../../../../types/org'

type OrgNodeEditorForm = {
  parentId: number
  nodeType: number
  code: string
  name: string
  sort: string
  status: number
  remark: string
}

const NODE_TYPE_OPTIONS = [
  { label: '校区', value: 1 },
  { label: '部门', value: 2 },
  { label: '小组', value: 3 },
]

const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const CODE_PATTERN = /^[A-Z0-9_-]+$/

const typeMetaMap: Record<number, { label: string; color: string }> = {
  1: { label: '校区', color: 'gold' },
  2: { label: '部门', color: 'blue' },
  3: { label: '小组', color: 'purple' },
}

function flattenOrgTree(nodes: OrgTreeNodeVO[]): OrgTreeNodeVO[] {
  return nodes.flatMap((node) => [node, ...flattenOrgTree(node.children || [])])
}

function buildTreeData(nodes: OrgTreeNodeVO[]): DataNode[] {
  return nodes.map((node) => ({
    key: String(node.id),
    title: `${node.name} (${typeMetaMap[node.nodeType ?? 0]?.label || '节点'})`,
    children: buildTreeData(node.children || []),
  }))
}

function buildParentOptions(nodes: OrgTreeNodeVO[]) {
  return flattenOrgTree(nodes).map((node) => ({
    label: `${node.name} / ${typeMetaMap[node.nodeType ?? 0]?.label || '节点'}`,
    value: node.id,
    nodeType: node.nodeType,
  }))
}

function createEmptyForm(parentId: number, nodeType: number): OrgNodeEditorForm {
  return {
    parentId,
    nodeType,
    code: '',
    name: '',
    sort: '',
    status: 1,
    remark: '',
  }
}

function resolveNextNodeType(parent: OrgTreeNodeVO | null) {
  if (!parent) {
    return 1
  }
  if (parent.nodeType === 1) {
    return 2
  }
  return 3
}

function validateForm(form: OrgNodeEditorForm, editingNode: OrgTreeNodeVO | null) {
  const code = form.code.trim().toUpperCase()
  const name = form.name.trim()
  const remark = form.remark.trim()
  const sort = form.sort.trim()

  if (!code) {
    return { error: '组织编码不能为空' }
  }
  if (code.length > 50) {
    return { error: '组织编码长度不能超过 50 个字符' }
  }
  if (!CODE_PATTERN.test(code)) {
    return { error: '组织编码仅支持大写字母、数字、下划线和中划线' }
  }
  if (!name) {
    return { error: '组织名称不能为空' }
  }
  if (name.length > 100) {
    return { error: '组织名称长度不能超过 100 个字符' }
  }
  if (remark.length > 200) {
    return { error: '备注长度不能超过 200 个字符' }
  }
  if (sort && !/^\d+$/.test(sort)) {
    return { error: '排序值必须为非负整数' }
  }
  if (form.nodeType === 1 && form.parentId !== 0) {
    return { error: '校区节点必须创建在根级' }
  }
  if (form.nodeType !== 1 && form.parentId <= 0 && !editingNode) {
    return { error: '非校区节点必须选择父级组织' }
  }

  const payload = {
    code,
    name,
    sort: sort ? Number(sort) : undefined,
    status: form.status,
    remark: remark || undefined,
  }

  return { payload }
}

function CampusPageContent() {
  const [loading, setLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')
  const [actionMessage, setActionMessage] = useState('')
  const [actionErrorMessage, setActionErrorMessage] = useState('')
  const [treeNodes, setTreeNodes] = useState<OrgTreeNodeVO[]>([])
  const [selectedNodeId, setSelectedNodeId] = useState<number | null>(null)
  const [editorOpen, setEditorOpen] = useState(false)
  const [editorMode, setEditorMode] = useState<'create' | 'edit'>('create')
  const [editorSubmitting, setEditorSubmitting] = useState(false)
  const [editorErrorMessage, setEditorErrorMessage] = useState('')
  const [editorForm, setEditorForm] = useState<OrgNodeEditorForm | null>(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  const loadTree = useCallback(async () => {
    setLoading(true)
    setErrorMessage('')
    try {
      const result = await listOrgTree()
      if (result.code !== 0) {
        throw new Error(result.message || '加载组织树失败')
      }
      setTreeNodes(result.data || [])
    } catch (error) {
      setTreeNodes([])
      setErrorMessage(error instanceof Error ? error.message : '加载组织树失败')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadTree()
  }, [loadTree])

  const flatNodes = useMemo(() => flattenOrgTree(treeNodes), [treeNodes])
  const selectedNode = useMemo(
    () => flatNodes.find((node) => node.id === selectedNodeId) ?? null,
    [flatNodes, selectedNodeId],
  )
  const treeData = useMemo(() => buildTreeData(treeNodes), [treeNodes])
  const parentOptions = useMemo(() => buildParentOptions(treeNodes), [treeNodes])

  useEffect(() => {
    if (!selectedNodeId && flatNodes.length > 0) {
      setSelectedNodeId(flatNodes[0].id)
    }
  }, [flatNodes, selectedNodeId])

  const openCreateModal = (parent: OrgTreeNodeVO | null) => {
    setEditorMode('create')
    setEditorErrorMessage('')
    setEditorForm(createEmptyForm(parent?.id ?? 0, resolveNextNodeType(parent)))
    setEditorOpen(true)
  }

  const openEditModal = (node: OrgTreeNodeVO) => {
    setEditorMode('edit')
    setEditorErrorMessage('')
    setEditorForm({
      parentId: node.parentId ?? 0,
      nodeType: node.nodeType ?? 1,
      code: node.code,
      name: node.name,
      sort: node.sort === null || node.sort === undefined ? '' : String(node.sort),
      status: node.status ?? 1,
      remark: node.remark || '',
    })
    setEditorOpen(true)
  }

  const closeEditor = () => {
    if (editorSubmitting) {
      return
    }
    setEditorOpen(false)
    setEditorErrorMessage('')
    setEditorForm(null)
  }

  const handleSubmitEditor = async () => {
    if (!editorForm) {
      return
    }
    const validation = validateForm(editorForm, editorMode === 'edit' ? selectedNode : null)
    if ('error' in validation) {
      setEditorErrorMessage(validation.error || '组织表单校验失败')
      return
    }

    setEditorSubmitting(true)
    setEditorErrorMessage('')
    setActionMessage('')
    setActionErrorMessage('')
    try {
      if (editorMode === 'create') {
        const payload: OrgNodeCreateRequest = {
          parentId: editorForm.parentId,
          nodeType: editorForm.nodeType,
          ...validation.payload,
        }
        const result = await createOrgNode(payload)
        if (result.code !== 0) {
          throw new Error(result.message || '创建组织节点失败')
        }
        setActionMessage(`组织节点 ${payload.name} 创建成功`)
      } else if (selectedNode) {
        const payload: OrgNodeUpdateRequest = validation.payload
        const result = await updateOrgNode(selectedNode.id, payload)
        if (result.code !== 0) {
          throw new Error(result.message || '更新组织节点失败')
        }
        setActionMessage(`组织节点 ${payload.name} 修改成功`)
      }

      closeEditor()
      await loadTree()
    } catch (error) {
      setEditorErrorMessage(error instanceof Error ? error.message : '提交组织节点失败')
    } finally {
      setEditorSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!selectedNode) {
      return
    }
    Modal.confirm({
      title: `确认删除 ${selectedNode.name} 吗？`,
      content: '仅允许删除无子节点且未绑定账号的组织节点。校区节点还要求未绑定教务账号和教师。',
      okText: '确认删除',
      cancelText: '取消',
      onOk: async () => {
        setDeleteLoading(true)
        setActionMessage('')
        setActionErrorMessage('')
        try {
          const result = await deleteOrgNode(selectedNode.id)
          if (result.code !== 0) {
            throw new Error(result.message || '删除组织节点失败')
          }
          setSelectedNodeId(null)
          setActionMessage(`组织节点 ${selectedNode.name} 删除成功`)
          await loadTree()
        } catch (error) {
          setActionErrorMessage(error instanceof Error ? error.message : '删除组织节点失败')
        } finally {
          setDeleteLoading(false)
        }
      },
    })
  }

  return (
    <section style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <section
        style={{
          borderRadius: 18,
          border: '1px solid #f0ebe3',
          backgroundColor: '#fffaf2',
          padding: 16,
          display: 'flex',
          flexDirection: 'column',
          gap: 12,
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
          <div>
            <Typography.Title level={5} style={{ margin: 0 }}>
              组织节点总览
            </Typography.Title>
            <Typography.Text type="secondary">
              统一维护校区、部门、小组。校区为根节点，部门和小组必须挂在父级组织下。
            </Typography.Text>
          </div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <Button type="primary" onClick={() => openCreateModal(null)}>
              新建校区
            </Button>
            <Button disabled={!selectedNode} onClick={() => openCreateModal(selectedNode)}>
              新建下级组织
            </Button>
            <Button disabled={!selectedNode} onClick={() => selectedNode && openEditModal(selectedNode)}>
              编辑当前节点
            </Button>
            <Button danger disabled={!selectedNode || deleteLoading} onClick={() => void handleDelete()}>
              删除当前节点
            </Button>
          </div>
        </div>
      </section>

      {errorMessage ? <Alert message={errorMessage} type="error" showIcon /> : null}
      {actionErrorMessage ? <Alert message={actionErrorMessage} type="error" showIcon closable onClose={() => setActionErrorMessage('')} /> : null}
      {actionMessage ? <Alert message={actionMessage} type="success" showIcon closable onClose={() => setActionMessage('')} /> : null}

      <section
        style={{
          border: '1px solid #f0ebe3',
          borderRadius: 18,
          padding: 16,
          backgroundColor: '#fffdf9',
        }}
      >
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
            titleRender={(node) => {
              const record = flatNodes.find((item) => String(item.id) === String(node.key))
              if (!record) {
                return String(node.title ?? '')
              }
              const meta = typeMetaMap[record.nodeType ?? 0]
              return (
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                  <span>{record.name}</span>
                  <Tag color={meta?.color}>{meta?.label || '未知'}</Tag>
                  <span style={{ fontSize: 12, color: '#8f8376' }}>{record.code}</span>
                  <span style={{ fontSize: 12, color: '#8f8376' }}>
                    绑定 {record.boundUserCount ?? 0}
                  </span>
                </div>
              )
            }}
            onSelect={(keys) => {
              const first = keys[0]
              if (first) {
                setSelectedNodeId(Number(first))
              }
            }}
            defaultExpandAll
          />
        )}

        {selectedNode ? (
          <div style={{ marginTop: 16, borderTop: '1px solid #f1ece4', paddingTop: 16 }}>
            <Typography.Title level={5}>当前选中</Typography.Title>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 12 }}>
              <div>组织名称：{selectedNode.name}</div>
              <div>组织编码：{selectedNode.code}</div>
              <div>节点类型：{typeMetaMap[selectedNode.nodeType ?? 0]?.label || '未知'}</div>
              <div>父级节点：{selectedNode.parentId && selectedNode.parentId > 0 ? selectedNode.parentId : '根节点'}</div>
              <div>所属校区：{selectedNode.campusId ?? '-'}</div>
              <div>绑定账号：{selectedNode.boundUserCount ?? 0}</div>
              <div>状态：{selectedNode.status === 1 ? '启用' : '禁用'}</div>
              <div>备注：{selectedNode.remark || '-'}</div>
            </div>
          </div>
        ) : null}
      </section>

      <Modal
        open={editorOpen}
        title={editorMode === 'create' ? '新建组织节点' : '编辑组织节点'}
        onCancel={closeEditor}
        onOk={() => {
          void handleSubmitEditor()
        }}
        okText={editorMode === 'create' ? '创建' : '保存'}
        cancelText="取消"
        confirmLoading={editorSubmitting}
        destroyOnHidden={false}
      >
        {editorErrorMessage ? <Alert message={editorErrorMessage} type="error" showIcon style={{ marginBottom: 16 }} /> : null}
        {editorForm ? (
          <div style={{ display: 'grid', gap: 12 }}>
            <div>
              <div style={{ marginBottom: 6, fontSize: 12, color: '#8f8376' }}>节点类型</div>
              <Select
                value={editorForm.nodeType}
                disabled={editorMode === 'edit'}
                options={NODE_TYPE_OPTIONS}
                onChange={(value) => setEditorForm((current) => (current ? { ...current, nodeType: value } : current))}
              />
            </div>
            <div>
              <div style={{ marginBottom: 6, fontSize: 12, color: '#8f8376' }}>父级组织</div>
              <Select
                value={editorForm.parentId}
                disabled={editorMode === 'edit' || editorForm.nodeType === 1}
                options={[{ label: '根节点', value: 0 }, ...parentOptions]}
                onChange={(value) => setEditorForm((current) => (current ? { ...current, parentId: value } : current))}
              />
            </div>
            <div>
              <div style={{ marginBottom: 6, fontSize: 12, color: '#8f8376' }}>组织编码</div>
              <Input
                value={editorForm.code}
                onChange={(event) => setEditorForm((current) => (current ? { ...current, code: event.target.value.toUpperCase() } : current))}
              />
            </div>
            <div>
              <div style={{ marginBottom: 6, fontSize: 12, color: '#8f8376' }}>组织名称</div>
              <Input
                value={editorForm.name}
                onChange={(event) => setEditorForm((current) => (current ? { ...current, name: event.target.value } : current))}
              />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
              <div>
                <div style={{ marginBottom: 6, fontSize: 12, color: '#8f8376' }}>排序值</div>
                <Input
                  value={editorForm.sort}
                  onChange={(event) => setEditorForm((current) => (current ? { ...current, sort: event.target.value } : current))}
                />
              </div>
              <div>
                <div style={{ marginBottom: 6, fontSize: 12, color: '#8f8376' }}>状态</div>
                <Select
                  value={editorForm.status}
                  options={STATUS_OPTIONS}
                  onChange={(value) => setEditorForm((current) => (current ? { ...current, status: value } : current))}
                />
              </div>
            </div>
            <div>
              <div style={{ marginBottom: 6, fontSize: 12, color: '#8f8376' }}>备注</div>
              <Input.TextArea
                rows={4}
                maxLength={200}
                value={editorForm.remark}
                onChange={(event) => setEditorForm((current) => (current ? { ...current, remark: event.target.value } : current))}
              />
            </div>
          </div>
        ) : null}
      </Modal>
    </section>
  )
}

export default CampusPageContent
