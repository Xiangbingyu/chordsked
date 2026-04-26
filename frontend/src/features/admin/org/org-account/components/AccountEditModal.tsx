import { Alert, Form, Input, Modal, Select, Spin, Typography } from 'antd'
import type { InternalUserCampusOptionVO } from '../../../../../types/campus'
import type { UserDataScopeType } from '../../../../../types/internalUser'
import { type AccountEditFormState } from '../utils/accountPageShared'

type SelectOption<T extends string | number> = {
  label: string
  value: T
}

type AccountEditModalProps = {
  open: boolean
  editLoading: boolean
  editSubmitting: boolean
  editErrorMessage: string
  editTargetUsername: string
  editForm: AccountEditFormState | null
  availablePrimaryCampuses: InternalUserCampusOptionVO[]
  campusSelectOptions: Array<SelectOption<number>>
  roleSelectOptions: Array<SelectOption<number>>
  dataScopeSelectOptions: Array<SelectOption<UserDataScopeType>>
  onCancel: () => void
  onSubmit: () => Promise<void>
  onCampusIdsChange: (values: number[]) => void
  onNameChange: (value: string) => void
  onPhoneChange: (value: string) => void
  onAvatarChange: (value: string) => void
  onPrimaryCampusChange: (value: number | null) => void
  onDataScopeTypeChange: (value: UserDataScopeType | null) => void
  onRoleIdsChange: (values: number[]) => void
}

function AccountEditModal({
  open,
  editLoading,
  editSubmitting,
  editErrorMessage,
  editTargetUsername,
  editForm,
  availablePrimaryCampuses,
  campusSelectOptions,
  roleSelectOptions,
  dataScopeSelectOptions,
  onCancel,
  onSubmit,
  onCampusIdsChange,
  onNameChange,
  onPhoneChange,
  onAvatarChange,
  onPrimaryCampusChange,
  onDataScopeTypeChange,
  onRoleIdsChange,
}: AccountEditModalProps) {
  return (
    <Modal
      open={open}
      title="编辑账号"
      onCancel={onCancel}
      onOk={() => {
        void onSubmit()
      }}
      okText="保存修改"
      cancelText="取消"
      width={860}
      destroyOnClose={false}
      confirmLoading={editSubmitting}
      maskClosable={!editLoading && !editSubmitting}
      okButtonProps={{ disabled: editLoading || !editForm }}
      cancelButtonProps={{ disabled: editLoading || editSubmitting }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <Typography.Text type="secondary">
          更新账号基础信息、可分配校区、主校区、角色和数据范围
          {editTargetUsername ? `：${editTargetUsername}` : ''}
        </Typography.Text>

        {editErrorMessage ? <Alert message={editErrorMessage} type="error" showIcon /> : null}

        {editLoading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '36px 0' }}>
            <Spin tip="正在加载账号编辑信息..." />
          </div>
        ) : editForm ? (
          <Form layout="vertical">
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
                gap: 16,
              }}
            >
              <Form.Item label="账号" style={{ marginBottom: 8 }}>
                <Input value={editForm.username} disabled />
              </Form.Item>
              <Form.Item label="账户姓名" style={{ marginBottom: 8 }}>
                <Input value={editForm.name} onChange={(event) => onNameChange(event.target.value)} />
              </Form.Item>
              <Form.Item label="联系方式" style={{ marginBottom: 8 }}>
                <Input
                  value={editForm.phone}
                  placeholder="请输入 11 位手机号"
                  onChange={(event) => onPhoneChange(event.target.value)}
                />
              </Form.Item>
              <Form.Item label="头像 URL" style={{ marginBottom: 8 }}>
                <Input
                  value={editForm.avatar}
                  placeholder="可留空"
                  onChange={(event) => onAvatarChange(event.target.value)}
                />
              </Form.Item>
            </div>

            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                gap: 16,
              }}
            >
              <Form.Item
                label="可分配校区"
                extra="用于配置当前账号可分配的校区范围，主校区候选项会随之联动。"
                style={{ marginBottom: 8 }}
              >
                <Select
                  mode="multiple"
                  value={editForm.campusIds}
                  placeholder="请选择可分配校区"
                  options={campusSelectOptions}
                  onChange={onCampusIdsChange}
                />
              </Form.Item>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                <Form.Item
                  label="主校区"
                  extra="主校区必须包含在当前已选的可分配校区中。"
                  style={{ marginBottom: 8 }}
                >
                  <Select
                    allowClear
                    value={editForm.primaryCampusId ?? undefined}
                    placeholder="请选择主校区"
                    options={availablePrimaryCampuses.map((option) => ({
                      label: option.name,
                      value: option.id,
                    }))}
                    onChange={(value) => onPrimaryCampusChange(value ?? null)}
                  />
                </Form.Item>
                <Form.Item label="数据范围" style={{ marginBottom: 8 }}>
                  <Select
                    allowClear
                    value={editForm.dataScopeType ?? undefined}
                    placeholder="请选择数据范围"
                    options={dataScopeSelectOptions}
                    onChange={(value) => onDataScopeTypeChange(value ?? null)}
                  />
                </Form.Item>
              </div>
            </div>

            <Form.Item
              label="绑定角色"
              extra={`当前已选 ${editForm.roleIds.length} 个角色。`}
              style={{ marginBottom: 0 }}
            >
              <Select
                mode="multiple"
                value={editForm.roleIds}
                placeholder="请选择角色"
                options={roleSelectOptions}
                onChange={onRoleIdsChange}
              />
            </Form.Item>
          </Form>
        ) : (
          <Alert message="当前无法加载可编辑内容，请关闭后重试。" type="warning" showIcon />
        )}
      </div>
    </Modal>
  )
}

export default AccountEditModal
