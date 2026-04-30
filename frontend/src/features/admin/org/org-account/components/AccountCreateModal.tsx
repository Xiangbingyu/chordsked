import { Alert, Form, Input, Modal, Select, Spin, Typography } from 'antd'
import type { UserDataScopeType } from '../../../../../types/internalUser'
import type { OrgNodeOptionVO } from '../../../../../types/org'
import type { AccountCreateFormState } from '../utils/accountPageShared'

type SelectOption<T extends string | number> = {
  label: string
  value: T
}

const sectionCardStyle = {
  border: '1px solid #f0ebe3',
  borderRadius: 16,
  backgroundColor: '#fffdf9',
  padding: 16,
}

const sectionTitleStyle = {
  marginBottom: 4,
  fontSize: 15,
  fontWeight: 600,
  color: '#2a2a2f',
}

const sectionDescriptionStyle = {
  marginBottom: 16,
  fontSize: 12,
  lineHeight: 1.6,
  color: '#7d7267',
}

type AccountCreateModalProps = {
  open: boolean
  createLoading: boolean
  createSubmitting: boolean
  createErrorMessage: string
  createForm: AccountCreateFormState | null
  availablePrimaryOrgNodes: OrgNodeOptionVO[]
  orgNodeSelectOptions: Array<SelectOption<number>>
  roleSelectOptions: Array<SelectOption<number>>
  dataScopeSelectOptions: Array<SelectOption<UserDataScopeType>>
  onCancel: () => void
  onSubmit: () => Promise<void>
  onUsernameChange: (value: string) => void
  onOrgScopeNodeIdsChange: (values: number[]) => void
  onNameChange: (value: string) => void
  onPhoneChange: (value: string) => void
  onAvatarChange: (value: string) => void
  onPrimaryOrgNodeChange: (value: number | null) => void
  onDataScopeTypeChange: (value: UserDataScopeType | null) => void
  onRoleIdsChange: (values: number[]) => void
}

function AccountCreateModal({
  open,
  createLoading,
  createSubmitting,
  createErrorMessage,
  createForm,
  availablePrimaryOrgNodes,
  orgNodeSelectOptions,
  roleSelectOptions,
  dataScopeSelectOptions,
  onCancel,
  onSubmit,
  onUsernameChange,
  onOrgScopeNodeIdsChange,
  onNameChange,
  onPhoneChange,
  onAvatarChange,
  onPrimaryOrgNodeChange,
  onDataScopeTypeChange,
  onRoleIdsChange,
}: AccountCreateModalProps) {
  return (
    <Modal
      open={open}
      title="创建账号"
      onCancel={onCancel}
      onOk={() => {
        void onSubmit()
      }}
      okText="创建账号"
      cancelText="取消"
      width={920}
      destroyOnHidden={false}
      confirmLoading={createSubmitting}
      maskClosable={!createLoading && !createSubmitting}
      okButtonProps={{ disabled: createLoading || !createForm }}
      cancelButtonProps={{ disabled: createLoading || createSubmitting }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
        <div
          style={{
            borderRadius: 14,
            backgroundColor: '#faf7f1',
            padding: '12px 14px',
          }}
        >
          <Typography.Text type="secondary">
            维护新账号的基础信息、主归属组织、角色和数据范围。仅“按分配组织”需要额外配置组织授权节点。
          </Typography.Text>
        </div>

        {createErrorMessage ? <Alert message={createErrorMessage} type="error" showIcon /> : null}

        {createLoading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '36px 0' }}>
            <Spin tip="正在加载账号创建信息..." />
          </div>
        ) : createForm ? (
          <Form layout="vertical" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <section style={sectionCardStyle}>
              <div style={sectionTitleStyle}>基础信息</div>
              <div style={sectionDescriptionStyle}>维护账号名称、联系方式和头像地址。</div>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
                  gap: 16,
                }}
              >
                <Form.Item
                  label="账号"
                  extra="仅支持字母、数字和下划线。"
                  style={{ marginBottom: 0 }}
                >
                  <Input
                    value={createForm.username}
                    placeholder="请输入账号"
                    onChange={(event) => onUsernameChange(event.target.value)}
                  />
                </Form.Item>
                <Form.Item label="账户姓名" style={{ marginBottom: 0 }}>
                  <Input value={createForm.name} onChange={(event) => onNameChange(event.target.value)} />
                </Form.Item>
                <Form.Item label="联系方式" style={{ marginBottom: 0 }}>
                  <Input
                    value={createForm.phone}
                    placeholder="请输入 11 位手机号"
                    onChange={(event) => onPhoneChange(event.target.value)}
                  />
                </Form.Item>
                <Form.Item label="头像 URL" style={{ marginBottom: 0 }}>
                  <Input
                    value={createForm.avatar}
                    placeholder="可留空"
                    onChange={(event) => onAvatarChange(event.target.value)}
                  />
                </Form.Item>
              </div>
            </section>

            <section style={sectionCardStyle}>
              <div style={sectionTitleStyle}>权限与组织配置</div>
              <div style={sectionDescriptionStyle}>主归属组织用于维护账号所属位置；当数据范围选择“按分配组织”时，组织授权节点只决定账号可见范围，不会改变主归属组织。</div>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                  gap: 16,
                  alignItems: 'start',
                }}
              >
                <Form.Item
                  label="组织授权节点"
                    extra="仅当数据范围选择“按分配组织”时必填。"
                  style={{ marginBottom: 0 }}
                >
                  <Select
                    mode="multiple"
                    value={createForm.orgScopeNodeIds}
                    placeholder="请选择组织授权节点（按分配组织时必填）"
                    options={orgNodeSelectOptions}
                    maxTagCount="responsive"
                    onChange={onOrgScopeNodeIdsChange}
                  />
                </Form.Item>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                  <Form.Item
                    label="主归属组织"
                    extra="账号始终需要一个主归属组织；该值仅能在账号页面中修改，不会随组织授权节点变化而自动调整。"
                    style={{ marginBottom: 0 }}
                  >
                    <Select
                      allowClear
                      value={createForm.primaryOrgNodeId ?? undefined}
                      placeholder="请选择主归属组织"
                      options={availablePrimaryOrgNodes.map((option) => ({
                        label: option.name,
                        value: option.id,
                      }))}
                      onChange={(value) => onPrimaryOrgNodeChange(value ?? null)}
                    />
                  </Form.Item>
                  <Form.Item
                    label="数据范围"
                    extra="建议与角色能力和校区分配保持一致，避免出现越权或空权限。"
                    style={{ marginBottom: 0 }}
                  >
                    <Select
                      allowClear
                      value={createForm.dataScopeType ?? undefined}
                      placeholder="请选择数据范围"
                      options={dataScopeSelectOptions}
                      onChange={(value) => onDataScopeTypeChange(value ?? null)}
                    />
                  </Form.Item>
                </div>
              </div>

              <div
                style={{
                  marginTop: 16,
                  paddingTop: 16,
                  borderTop: '1px solid #f1ece4',
                }}
              >
                <Form.Item
                  label="绑定角色"
                  extra={`当前已选 ${createForm.roleIds.length} 个角色。`}
                  style={{ marginBottom: 0 }}
                >
                  <Select
                    mode="multiple"
                    value={createForm.roleIds}
                    placeholder="请选择角色"
                    options={roleSelectOptions}
                    maxTagCount="responsive"
                    onChange={onRoleIdsChange}
                  />
                </Form.Item>
              </div>
            </section>
          </Form>
        ) : (
          <Alert message="当前无法加载可创建内容，请关闭后重试。" type="warning" showIcon />
        )}
      </div>
    </Modal>
  )
}

export default AccountCreateModal
