import { Alert, Empty, Form, Input, Modal, Spin, Tree, Typography } from 'antd'
import type { DataNode } from 'antd/es/tree'
import { useEffect, useMemo, useState } from 'react'
import type { InternalPermissionTreeQueryResultVO } from '../../../../../types/permission'

export type RolePermissionEditorFormState = {
  code: string
  name: string
  description: string
  status: 0 | 1 | 2
  permissionIds: number[]
}

type RolePermissionEditorModalProps = {
  open: boolean
  mode: 'create' | 'edit'
  loading: boolean
  submitting: boolean
  errorMessage: string
  form: RolePermissionEditorFormState | null
  tree: InternalPermissionTreeQueryResultVO[]
  onCancel: () => void
  onSubmit: () => Promise<void>
  onCodeChange: (value: string) => void
  onNameChange: (value: string) => void
  onDescriptionChange: (value: string) => void
  onPermissionIdsChange: (values: number[]) => void
}

function collectExpandedKeys(nodes: InternalPermissionTreeQueryResultVO[]): string[] {
  return nodes.flatMap((node) => [
    ...(node.children.length > 0 ? [String(node.id)] : []),
    ...collectExpandedKeys(node.children),
  ])
}

function resolvePermissionTypeLabel(type: number | null) {
  switch (type) {
    case 1:
      return '目录'
    case 2:
      return '菜单'
    case 3:
      return '按钮'
    default:
      return type === null ? '未分类' : `类型 ${type}`
  }
}

function buildPermissionTreeData(nodes: InternalPermissionTreeQueryResultVO[]): DataNode[] {
  return nodes.map((node) => ({
    key: String(node.id),
    title: (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6, padding: '2px 0' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
          <span style={{ fontSize: 13, fontWeight: 600, color: '#2a2a2f' }}>{node.name}</span>
          <span
            style={{
              borderRadius: 999,
              backgroundColor: '#fff3e8',
              padding: '2px 8px',
              fontSize: 10,
              color: '#b35b16',
              lineHeight: 1.5,
            }}
          >
            {resolvePermissionTypeLabel(node.type)}
          </span>
          <span
            style={{
              borderRadius: 999,
              backgroundColor: '#f5f5f5',
              padding: '2px 8px',
              fontSize: 10,
              color: '#7d7267',
              lineHeight: 1.5,
            }}
          >
            {node.code}
          </span>
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, fontSize: 12, color: '#7d7267' }}>
          <span>路径：{node.path || '-'}</span>
          <span>排序：{node.sort ?? '-'}</span>
          <span>ID：{node.id}</span>
        </div>
      </div>
    ),
    children: buildPermissionTreeData(node.children),
  }))
}

function RolePermissionEditorModal({
  open,
  mode,
  loading,
  submitting,
  errorMessage,
  form,
  tree,
  onCancel,
  onSubmit,
  onCodeChange,
  onNameChange,
  onDescriptionChange,
  onPermissionIdsChange,
}: RolePermissionEditorModalProps) {
  const treeData = buildPermissionTreeData(tree)
  const initialExpandedKeys = useMemo(() => collectExpandedKeys(tree), [tree])
  const [expandedKeys, setExpandedKeys] = useState<string[]>([])

  useEffect(() => {
    if (!open) {
      return
    }
    setExpandedKeys(initialExpandedKeys)
  }, [initialExpandedKeys, open])

  return (
    <Modal
      open={open}
      title={mode === 'create' ? '创建角色' : '修改角色'}
      onCancel={onCancel}
      onOk={() => {
        void onSubmit()
      }}
      okText={mode === 'create' ? '创建角色' : '保存角色'}
      cancelText="取消"
      width={960}
      destroyOnHidden={false}
      confirmLoading={submitting}
      maskClosable={!loading && !submitting}
      okButtonProps={{ disabled: loading || !form }}
      cancelButtonProps={{ disabled: loading || submitting }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
        <div
          style={{
            borderRadius: 14,
            backgroundColor: '#faf7f1',
            padding: '12px 14px',
          }}
        >
          <Typography.Text type="secondary">
            {mode === 'create'
              ? '角色编码自动生成且不可修改，请维护角色名称、描述和权限树绑定。'
              : '可调整角色编码、角色名称、描述和权限树绑定。'}
          </Typography.Text>
        </div>

        {errorMessage ? <Alert message={errorMessage} type="error" showIcon /> : null}

        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '36px 0' }}>
            <Spin tip={mode === 'create' ? '正在加载角色创建信息...' : '正在加载角色详情...'} />
          </div>
        ) : form ? (
          <Form layout="vertical" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <section
              style={{
                border: '1px solid #f0ebe3',
                borderRadius: 16,
                backgroundColor: '#fffdf9',
                padding: 16,
              }}
            >
              <div style={{ marginBottom: 4, fontSize: 15, fontWeight: 600, color: '#2a2a2f' }}>
                基础信息
              </div>
              <div style={{ marginBottom: 16, fontSize: 12, lineHeight: 1.6, color: '#7d7267' }}>
                角色编码由系统自动维护；角色名称与描述用于页面展示和识别。
              </div>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
                  gap: 16,
                }}
              >
                <Form.Item
                  label="角色编码"
                  extra={mode === 'create' ? '创建时自动生成，当前先保持只读。' : '最多 50 个字符。'}
                  style={{ marginBottom: 0 }}
                >
                  <Input
                    value={form.code}
                    maxLength={50}
                    placeholder="请输入角色编码"
                    disabled={mode === 'create'}
                    onChange={(event) => onCodeChange(event.target.value)}
                  />
                </Form.Item>
                <Form.Item label="角色名称" style={{ marginBottom: 0 }}>
                  <Input
                    value={form.name}
                    maxLength={50}
                    placeholder="请输入角色名称"
                    onChange={(event) => onNameChange(event.target.value)}
                  />
                </Form.Item>
              </div>
              <div style={{ marginTop: 16 }}>
                <Form.Item
                  label="角色描述"
                  extra="最多 200 个字符，可留空。"
                  style={{ marginBottom: 0 }}
                >
                  <Input.TextArea
                    value={form.description}
                    maxLength={200}
                    rows={4}
                    placeholder="请输入角色描述"
                    onChange={(event) => onDescriptionChange(event.target.value)}
                  />
                </Form.Item>
              </div>
            </section>

            <section
              style={{
                border: '1px solid #f0ebe3',
                borderRadius: 16,
                backgroundColor: '#fffdf9',
                padding: 16,
              }}
            >
              <div style={{ marginBottom: 4, fontSize: 15, fontWeight: 600, color: '#2a2a2f' }}>
                权限树
              </div>
              <div style={{ marginBottom: 16, fontSize: 12, lineHeight: 1.6, color: '#7d7267' }}>
                当前已选 {form.permissionIds.length} 个权限节点。勾选父节点时会联动子节点。
              </div>
              <div
                style={{
                  borderRadius: 18,
                  border: '1px solid #f0ebe3',
                  backgroundColor: '#fffdf9',
                  padding: 18,
                  minHeight: 320,
                  position: 'relative',
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    gap: 12,
                    alignItems: 'flex-start',
                    flexWrap: 'wrap',
                    marginBottom: 16,
                  }}
                >
                  <div>
                    <Typography.Title level={5} style={{ margin: 0, color: '#2a2a2f' }}>
                      权限树结构
                    </Typography.Title>
                    <Typography.Paragraph style={{ margin: '6px 0 0', color: '#7d7267' }}>
                      展示接口返回的完整权限层级，默认展开所有包含子节点的目录，并支持多选。
                    </Typography.Paragraph>
                  </div>
                  <div style={{ fontSize: 12, color: '#8b7e72' }}>
                    数据来源：`/admin/api/v1/permissions/tree`
                  </div>
                </div>

                {treeData.length === 0 ? (
                  <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前没有可配置的权限树数据" />
                ) : (
                  <div style={{ maxHeight: 420, overflow: 'auto' }}>
                    <Tree
                      blockNode
                      checkable
                      showLine
                      selectable={false}
                      expandedKeys={expandedKeys}
                      checkedKeys={form.permissionIds.map((id) => String(id))}
                      onExpand={(keys) => setExpandedKeys(keys.map((key) => String(key)))}
                      onCheck={(checkedKeys) => {
                        const nextValues = Array.isArray(checkedKeys)
                          ? checkedKeys
                              .map((key) => Number(key))
                              .filter((value) => Number.isFinite(value))
                          : checkedKeys.checked
                              .map((key) => Number(key))
                              .filter((value) => Number.isFinite(value))
                        onPermissionIdsChange(nextValues)
                      }}
                      treeData={treeData}
                    />
                  </div>
                )}
              </div>
            </section>
          </Form>
        ) : (
          <Alert message="当前无法加载角色编辑内容，请关闭后重试。" type="warning" showIcon />
        )}
      </div>
    </Modal>
  )
}

export default RolePermissionEditorModal
