import type { PermissionTreeSummary } from '../utils/permissionTree'

type PermissionTreeOverviewProps = {
  summary: PermissionTreeSummary
}

function SummaryCard({
  label,
  value,
}: {
  label: string
  value: number
}) {
  return (
    <div
      style={{
        borderRadius: 16,
        border: '1px solid #f0ebe3',
        backgroundColor: '#fffdf9',
        padding: 16,
      }}
    >
      <div style={{ fontSize: 12, color: '#8b7e72', marginBottom: 6 }}>{label}</div>
      <div style={{ fontSize: 24, fontWeight: 600, color: '#2a2a2f' }}>{value}</div>
    </div>
  )
}

function PermissionTreeOverview({ summary }: PermissionTreeOverviewProps) {
  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: 12,
      }}
    >
      <SummaryCard label="权限节点总数" value={summary.totalNodes} />
      <SummaryCard label="叶子节点数量" value={summary.leafNodes} />
      <SummaryCard label="最大层级深度" value={summary.maxDepth} />
    </div>
  )
}

export default PermissionTreeOverview
