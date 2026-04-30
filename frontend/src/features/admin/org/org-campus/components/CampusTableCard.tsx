import { Button, Pagination, Popconfirm, Spin } from 'antd'
import type { CampusQueryResultVO } from '../../../../../types/campus'
import { formatCampusUpdatedAt, getCampusStatusMeta } from '../utils/campusPageShared'

type CampusTableCardProps = {
  rows: CampusQueryResultVO[]
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
  onOpenEditModal: (row: CampusQueryResultVO) => Promise<void>
  onDelete: (row: CampusQueryResultVO) => Promise<void>
}

function CampusTableCard({
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
  onOpenEditModal,
  onDelete,
}: CampusTableCardProps) {
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
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>校区编码</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>校区名称</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>联系方式</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>状态</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>更新时间</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>操作</th>
          </tr>
        </thead>
        <tbody>
          {showTableLoadingRow ? (
            <tr>
              <td colSpan={6} style={{ padding: 24, color: '#7d7267', textAlign: 'center' }}>
                正在加载校区列表...
              </td>
            </tr>
          ) : errorMessage ? (
            <tr>
              <td
                colSpan={6}
                style={{ padding: 24, color: permissionDenied ? '#a64545' : '#7d7267', textAlign: 'center' }}
              >
                {errorMessage}
              </td>
            </tr>
          ) : rows.length === 0 ? (
            <tr>
              <td colSpan={6} style={{ padding: 24, color: '#7d7267', textAlign: 'center' }}>
                当前筛选条件下暂无校区数据
              </td>
            </tr>
          ) : (
            rows.map((row) => {
              const statusMeta = getCampusStatusMeta(row.status)
              const editLoading = actionLoadingKey === `${row.id}:edit`
              const deleteLoading = actionLoadingKey === `${row.id}:delete`
              const actionDisabled = loading || actionLoadingKey !== null

              return (
                <tr
                  key={row.id}
                  style={{
                    borderTop: '1px solid #f1ece4',
                    backgroundColor: '#ffffff',
                  }}
                >
                  <td style={{ padding: '12px 16px', color: '#3a352f', fontWeight: 600 }}>{row.code}</td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.name}</td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.phone || '未填写'}</td>
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
                    {formatCampusUpdatedAt(row.updatedAt)}
                  </td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      <Button
                        type="link"
                        size="small"
                        disabled={actionDisabled}
                        style={{ paddingInline: 0 }}
                        onClick={() => {
                          void onOpenEditModal(row)
                        }}
                      >
                        {editLoading ? '加载中...' : '修改'}
                      </Button>
                      <Popconfirm
                        title="确认删除校区"
                        description={`校区：${row.name}`}
                        okText="确认删除"
                        cancelText="取消"
                        disabled={actionDisabled}
                        onConfirm={() => onDelete(row)}
                      >
                        <Button
                          type="link"
                          size="small"
                          danger
                          disabled={actionDisabled}
                          style={{ paddingInline: 0 }}
                        >
                          {deleteLoading ? '删除中...' : '删除'}
                        </Button>
                      </Popconfirm>
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
          <Spin size="small" tip="正在更新校区列表..." />
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

export default CampusTableCard
