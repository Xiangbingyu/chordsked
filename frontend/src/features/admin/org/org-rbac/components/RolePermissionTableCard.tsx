import { Button, Pagination } from 'antd'
import type { RoleQueryResultVO } from '../../../../../types/role'
import { formatRoleUpdatedAt, getRoleStatusMeta } from '../utils/rolePermissionShared'

type RolePermissionTableCardProps = {
  rows: RoleQueryResultVO[]
  total: number
  page: number
  pageSize: number
  loading: boolean
  errorMessage: string
  onPageChange: (nextPage: number) => void
  onEdit: (row: RoleQueryResultVO) => void
}

function RolePermissionTableCard({
  rows,
  total,
  page,
  pageSize,
  loading,
  errorMessage,
  onPageChange,
  onEdit,
}: RolePermissionTableCardProps) {
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
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>角色名称</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>角色状态</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>绑定权限数量</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>关联用户数量</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>更新时间</th>
            <th style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600 }}>操作</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan={6} style={{ padding: 24, color: '#7d7267', textAlign: 'center' }}>
                正在加载角色列表...
              </td>
            </tr>
          ) : errorMessage ? (
            <tr>
              <td colSpan={6} style={{ padding: 24, color: '#a64545', textAlign: 'center' }}>
                {errorMessage}
              </td>
            </tr>
          ) : rows.length === 0 ? (
            <tr>
              <td colSpan={6} style={{ padding: 24, color: '#7d7267', textAlign: 'center' }}>
                当前没有可展示的角色数据
              </td>
            </tr>
          ) : (
            rows.map((row) => {
              const statusMeta = getRoleStatusMeta(row.status)

              return (
                <tr
                  key={row.id}
                  style={{
                    borderTop: '1px solid #f1ece4',
                    backgroundColor: '#ffffff',
                  }}
                >
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                      <span>{row.name}</span>
                      <span style={{ fontSize: 12, color: '#8b7e72' }}>{row.code}</span>
                    </div>
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
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.permissionCount ?? 0}</td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>{row.userCount ?? 0}</td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    {formatRoleUpdatedAt(row.updatedAt)}
                  </td>
                  <td style={{ padding: '12px 16px', color: '#3a352f' }}>
                    <Button
                      type="link"
                      size="small"
                      style={{ paddingInline: 0 }}
                      onClick={() => onEdit(row)}
                    >
                      修改
                    </Button>
                  </td>
                </tr>
              )
            })
          )}
        </tbody>
      </table>

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

export default RolePermissionTableCard
