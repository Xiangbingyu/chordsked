import { Alert, Empty, Spin, Tree, Typography } from 'antd'
import type { PermissionTreeNode } from '../utils/permissionTree'

type PermissionTreePanelProps = {
  loading: boolean
  errorMessage: string
  treeData: PermissionTreeNode[]
  expandedKeys: string[]
  onExpand: (keys: string[]) => void
}

function PermissionTreePanel({
  loading,
  errorMessage,
  treeData,
  expandedKeys,
  onExpand,
}: PermissionTreePanelProps) {
  return (
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
            展示接口返回的完整权限层级，默认展开所有包含子节点的目录。
          </Typography.Paragraph>
        </div>
        <div style={{ fontSize: 12, color: '#8b7e72' }}>数据来源：`/admin/api/v1/permissions/tree`</div>
      </div>

      {errorMessage ? <Alert message={errorMessage} type="error" showIcon /> : null}

      {!errorMessage && !loading && treeData.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前没有可展示的权限树数据" />
      ) : null}

      {!errorMessage && treeData.length > 0 ? (
        <Tree
          blockNode
          showLine
          selectable={false}
          expandedKeys={expandedKeys}
          onExpand={(keys) => onExpand(keys.map((key) => String(key)))}
          treeData={treeData}
        />
      ) : null}

      {loading ? (
        <div
          style={{
            position: 'absolute',
            inset: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderRadius: 18,
            backgroundColor: 'rgba(255, 253, 249, 0.86)',
          }}
        >
          <Spin tip="正在加载权限树..." />
        </div>
      ) : null}
    </div>
  )
}

export default PermissionTreePanel
