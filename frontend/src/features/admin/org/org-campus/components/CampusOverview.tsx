import { Button } from 'antd'

type CampusOverviewProps = {
  visibleCount: number
  total: number
  enabledCount: number
  createDisabled: boolean
  onOpenCreateModal: () => void
}

function CampusOverview({
  visibleCount,
  total,
  enabledCount,
  createDisabled,
  onOpenCreateModal,
}: CampusOverviewProps) {
  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, alignItems: 'center' }}>
        <div style={{ fontSize: 14, color: '#7d7267', lineHeight: 1.8 }}>
          校区是组织树顶层节点（CAMPUS），用于维护校区基础信息、联系方式、启停状态和后续组织挂载范围。
        </div>
        <Button
          type="default"
          disabled={createDisabled}
          style={{
            whiteSpace: 'nowrap',
            borderColor: '#ff9b54',
            backgroundColor: '#ff9b54',
            color: '#1f1f1f',
          }}
          onClick={onOpenCreateModal}
        >
          创建校区
        </Button>
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
          <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>当前页可见校区数</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{visibleCount}</div>
          <div style={{ marginTop: 4, fontSize: 12, color: '#7d7267' }}>总条数：{total}</div>
        </div>
        <div style={{ minWidth: 180, flex: 1 }}>
          <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>启用中的校区</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{enabledCount}</div>
          <div style={{ marginTop: 4, fontSize: 12, color: '#7d7267' }}>按当前页结果统计</div>
        </div>
        <div style={{ minWidth: 180, flex: 1 }}>
          <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>当前页禁用校区</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{visibleCount - enabledCount}</div>
          <div style={{ marginTop: 4, fontSize: 12, color: '#7d7267' }}>按当前页结果统计</div>
        </div>
      </div>
    </>
  )
}

export default CampusOverview
