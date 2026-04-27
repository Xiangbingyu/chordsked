import type { ReactNode } from 'react'
import type { InternalPermissionTreeQueryResultVO } from '../../../../../types/permission'

export type PermissionTreeNode = {
  key: string
  title: ReactNode
  children: PermissionTreeNode[]
}

export type PermissionTreeSummary = {
  totalNodes: number
  leafNodes: number
  maxDepth: number
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

export function collectExpandedKeys(nodes: InternalPermissionTreeQueryResultVO[]): string[] {
  return nodes.flatMap((node) => [
    ...(node.children.length > 0 ? [node.code] : []),
    ...collectExpandedKeys(node.children),
  ])
}

export function summarizePermissionTree(
  nodes: InternalPermissionTreeQueryResultVO[],
  depth = 1,
): PermissionTreeSummary {
  return nodes.reduce<PermissionTreeSummary>(
    (summary, node) => {
      const childSummary =
        node.children.length > 0
          ? summarizePermissionTree(node.children, depth + 1)
          : { totalNodes: 0, leafNodes: 0, maxDepth: depth }

      return {
        totalNodes: summary.totalNodes + 1 + childSummary.totalNodes,
        leafNodes:
          summary.leafNodes + (node.children.length === 0 ? 1 : 0) + childSummary.leafNodes,
        maxDepth: Math.max(summary.maxDepth, depth, childSummary.maxDepth),
      }
    },
    { totalNodes: 0, leafNodes: 0, maxDepth: nodes.length > 0 ? depth : 0 },
  )
}

export function buildPermissionTreeData(
  nodes: InternalPermissionTreeQueryResultVO[],
): PermissionTreeNode[] {
  return nodes.map((node) => ({
    key: node.code,
    title: (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 6, padding: '2px 0' }}>
        <div style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
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
