import { Button } from 'antd'

type AccountOverviewProps = {
  visibleCount: number
  total: number
  createDisabled: boolean
  createLoading: boolean
  onOpenCreateModal: () => void
}

function AccountOverview({
  visibleCount,
  total,
  createDisabled,
  createLoading,
  onOpenCreateModal,
}: AccountOverviewProps) {
  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, alignItems: 'center' }}>
        <div style={{ fontSize: 14, color: '#7d7267', lineHeight: 1.8 }}>
          当前列表会自动按登录账号的权限范围过滤教务账号。权限不足时接口不会返回可见账号，或直接返回无权限错误。
        </div>
        <Button
          type="default"
          disabled={createDisabled}
          loading={createLoading}
          style={{ whiteSpace: 'nowrap' }}
          onClick={onOpenCreateModal}
        >
          创建账号
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
          <div style={{ fontSize: 12, color: '#8f8376', marginBottom: 6 }}>当前页可见账号数</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: '#2a2a2f' }}>{visibleCount}</div>
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
    </>
  )
}

export default AccountOverview
