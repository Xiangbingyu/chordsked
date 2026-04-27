import { Button } from 'antd'

type RolePermissionOverviewProps = {
  visibleCount: number
  total: number
  enabledCount: number
  permissionCount: number
  userCount: number
  onCreate: () => void
}

function RolePermissionOverview({
  visibleCount,
  total,
  enabledCount,
  permissionCount,
  userCount,
  onCreate,
}: RolePermissionOverviewProps) {
  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, alignItems: 'center' }}>
        <div style={{ fontSize: 14, color: '#7d7267', lineHeight: 1.8 }}>
          角色列表直接调用角色接口，并在当前页面展示角色状态、权限绑定规模和关联用户情况。角色权限点的修改入口先预留在表格操作列中。
        </div>
        <Button style={{ whiteSpace: 'nowrap' }} onClick={onCreate}>
          创建角色
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
        <div style={{ minWidth: 160, flex: 1 }}>
          <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>当前页可见角色数</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{visibleCount}</div>
          <div style={{ marginTop: 4, fontSize: 12, color: '#7d7267' }}>总条数：{total}</div>
        </div>
        <div style={{ minWidth: 160, flex: 1 }}>
          <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>当前页启用角色数</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{enabledCount}</div>
          <div style={{ marginTop: 4, fontSize: 12, color: '#7d7267' }}>仅统计当前页返回结果</div>
        </div>
        <div style={{ minWidth: 160, flex: 1 }}>
          <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>当前页绑定权限总数</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{permissionCount}</div>
          <div style={{ marginTop: 4, fontSize: 12, color: '#7d7267' }}>按角色权限数量汇总</div>
        </div>
        <div style={{ minWidth: 160, flex: 1 }}>
          <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>当前页关联用户总数</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{userCount}</div>
          <div style={{ marginTop: 4, fontSize: 12, color: '#7d7267' }}>用于辅助判断角色影响范围</div>
        </div>
      </div>
    </>
  )
}

export default RolePermissionOverview
