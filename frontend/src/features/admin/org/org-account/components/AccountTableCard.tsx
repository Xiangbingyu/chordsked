import { Button, Pagination, Popconfirm, Spin } from 'antd'
import type { InternalUserQueryResultVO } from '../../../../../types/internalUser'
import {
  getAccountOperationBlockReason,
  getDataScopeLabel,
  getStatusMeta,
} from '../utils/accountPageShared'

type AccountTableCardProps = {
  rows: InternalUserQueryResultVO[]
  total: number
  page: number
  pageSize: number
  loading: boolean
  errorMessage: string
  permissionDenied: boolean
  actionLoadingKey: string | null
  showTableLoadingRow: boolean
  showTableOverlay: boolean
  onPageChange: (nextPage: number) => void
  onUpdateUser: (row: InternalUserQueryResultVO) => Promise<void>
  onToggleUserStatus: (row: InternalUserQueryResultVO) => Promise<void>
  onOpenResetPasswordModal: (row: InternalUserQueryResultVO) => void
}

function AccountTableCard({
  rows,
  total,
  page,
  pageSize,
  loading,
  errorMessage,
  permissionDenied,
  actionLoadingKey,
  showTableLoadingRow,
  showTableOverlay,
  onPageChange,
  onUpdateUser,
  onToggleUserStatus,
  onOpenResetPasswordModal,
}: AccountTableCardProps) {
  return (
    <div
      style={{
        overflow: 'hidden',
        borderRadius: 18,
        border: '1px solid #f0ebe3',
        backgroundColor: '#ffffff',
        position: 'relative',
      }}
    >
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14, lineHeight: 1.6 }}>
        <thead style={{ backgroundColor: '#faf7f1', color: '#6f655b' }}>
          <tr>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>账号</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>姓名</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>联系方式</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>所属校区</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>绑定角色</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>数据范围</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>状态</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>操作</th>
          </tr>
        </thead>
        <tbody>
          {showTableLoadingRow ? (
            <tr>
              <td colSpan={8} style={{ padding: 24, color: '#7d7267', textAlign: 'center' }}>
                正在加载账号列表...
              </td>
            </tr>
          ) : errorMessage ? (
            <tr>
              <td
                colSpan={8}
                style={{ padding: 24, color: permissionDenied ? '#a64545' : '#7d7267', textAlign: 'center' }}
              >
                {errorMessage}
              </td>
            </tr>
          ) : rows.length === 0 ? (
            <tr>
              <td colSpan={8} style={{ padding: 24, color: '#7d7267', textAlign: 'center' }}>
                当前权限范围下暂无可见教务账号
              </td>
            </tr>
          ) : (
            rows.map((row) => {
              const roleNames = row.roleNames || []
              const statusMeta = getStatusMeta(row.status)
              const updateLoading = actionLoadingKey === `${row.id}:update`
              const statusToggleLoading = actionLoadingKey === `${row.id}:status-toggle`
              const resetLoading = actionLoadingKey === `${row.id}:reset-password`
              const operationBlockedReason = getAccountOperationBlockReason(row)
              const operationBlocked = operationBlockedReason.length > 0
              const statusActionLabel = row.status === 0 ? '启用' : '禁用'
              const statusUnavailable = operationBlocked || row.status === 2 || row.status === null

              return (
                <tr
                  key={row.id}
                  style={{
                    borderTop: '1px solid #f1ece4',
                    backgroundColor: '#ffffff',
                  }}
                >
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.username}</td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.name}</td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.phone}</td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    {row.primaryCampusName || '未分配'}
                  </td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    {roleNames.length > 0 ? roleNames.join(' / ') : '未绑定'}
                  </td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    {getDataScopeLabel(row.dataScopeType)}
                  </td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    <span
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        borderRadius: 999,
                        backgroundColor: statusMeta.backgroundColor,
                        color: statusMeta.textColor,
                        padding: '4px 10px',
                        fontSize: 12,
                        fontWeight: 600,
                      }}
                    >
                      {statusMeta.label}
                    </span>
                  </td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      <Button
                        type="link"
                        size="small"
                        onClick={() => {
                          void onUpdateUser(row)
                        }}
                        disabled={loading || actionLoadingKey !== null || operationBlocked}
                        title={operationBlocked ? operationBlockedReason : undefined}
                        style={{ paddingInline: 0 }}
                      >
                        {updateLoading ? '修改中...' : '修改'}
                      </Button>
                      <Popconfirm
                        title={`确认${statusActionLabel}账号`}
                        description={`账号：${row.username}`}
                        okText="确认"
                        cancelText="取消"
                        disabled={loading || actionLoadingKey !== null || statusUnavailable}
                        onConfirm={() => {
                          void onToggleUserStatus(row)
                        }}
                      >
                        <Button
                          type="link"
                          size="small"
                          danger={row.status !== 0}
                          disabled={loading || actionLoadingKey !== null || statusUnavailable}
                          title={operationBlocked ? operationBlockedReason : undefined}
                          style={{ paddingInline: 0 }}
                        >
                          {statusToggleLoading ? `${statusActionLabel}中...` : statusActionLabel}
                        </Button>
                      </Popconfirm>
                      <Button
                        type="link"
                        size="small"
                        onClick={() => {
                          onOpenResetPasswordModal(row)
                        }}
                        disabled={loading || actionLoadingKey !== null || operationBlocked}
                        title={operationBlocked ? operationBlockedReason : undefined}
                        style={{ paddingInline: 0, color: operationBlocked ? undefined : '#7d7267' }}
                      >
                        {resetLoading ? '重置中...' : '重置密码'}
                      </Button>
                    </div>
                  </td>
                </tr>
              )
            })
          )}
        </tbody>
      </table>
      {showTableOverlay ? (
        <div
          style={{
            position: 'absolute',
            inset: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'rgba(255, 255, 255, 0.68)',
            backdropFilter: 'blur(1px)',
            pointerEvents: 'none',
          }}
        >
          <Spin size="small" tip="正在更新账号列表..." />
        </div>
      ) : null}

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'flex-end',
          borderTop: '1px solid #f1ece4',
          backgroundColor: '#ffffff',
          padding: '12px 16px',
        }}
      >
        <Pagination
          current={page}
          total={total}
          pageSize={pageSize}
          size="small"
          showSizeChanger={false}
          disabled={loading}
          onChange={onPageChange}
        />
      </div>
    </div>
  )
}

export default AccountTableCard
