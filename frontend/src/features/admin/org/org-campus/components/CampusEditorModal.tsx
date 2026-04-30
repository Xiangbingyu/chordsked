import { Alert, Form, Input, InputNumber, Modal, Select, Spin, Typography } from 'antd'
import type { CampusStatus } from '../../../../../types/campus'
import type { CampusEditorFormState } from '../utils/campusPageShared'

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

type CampusEditorModalProps = {
  mode: 'create' | 'edit'
  open: boolean
  loading: boolean
  submitting: boolean
  errorMessage: string
  form: CampusEditorFormState | null
  statusOptions: Array<{ label: string; value: CampusStatus }>
  onCancel: () => void
  onSubmit: () => Promise<void>
  onCodeChange: (value: string) => void
  onNameChange: (value: string) => void
  onAddressChange: (value: string) => void
  onPhoneChange: (value: string) => void
  onSortChange: (value: string) => void
  onStatusChange: (value: CampusStatus) => void
  onRemarkChange: (value: string) => void
}

function CampusEditorModal({
  mode,
  open,
  loading,
  submitting,
  errorMessage,
  form,
  statusOptions,
  onCancel,
  onSubmit,
  onCodeChange,
  onNameChange,
  onAddressChange,
  onPhoneChange,
  onSortChange,
  onStatusChange,
  onRemarkChange,
}: CampusEditorModalProps) {
  const isCreate = mode === 'create'
  const title = isCreate ? '创建校区' : '修改校区'
  const okText = isCreate ? '创建校区' : '保存修改'

  return (
    <Modal
      open={open}
      title={title}
      onCancel={onCancel}
      onOk={() => {
        void onSubmit()
      }}
      okText={okText}
      cancelText="取消"
      width={820}
      destroyOnHidden={false}
      confirmLoading={submitting}
      maskClosable={!loading && !submitting}
      okButtonProps={{ disabled: loading || !form }}
      cancelButtonProps={{ disabled: loading || submitting }}
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
            {isCreate
              ? '维护校区编码、名称、联系方式、排序和状态，创建后即可在组织管理中继续扩展。'
              : '支持修改校区基础信息、联系方式、排序和状态。'}
          </Typography.Text>
        </div>

        {errorMessage ? <Alert message={errorMessage} type="error" showIcon /> : null}

        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '36px 0' }}>
            <Spin tip={isCreate ? '正在准备校区表单...' : '正在加载校区详情...'} />
          </div>
        ) : form ? (
          <Form layout="vertical" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <section style={sectionCardStyle}>
              <div style={sectionTitleStyle}>基础信息</div>
              <div style={sectionDescriptionStyle}>校区编码建议使用大写字母与数字组合，便于后续对接组织树与外部系统。</div>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
                  gap: 16,
                }}
              >
                <Form.Item
                  label="校区编码"
                  extra="支持大写字母、数字、下划线和中划线。"
                  style={{ marginBottom: 0 }}
                >
                  <Input
                    value={form.code}
                    placeholder="例如：BHGJ"
                    onChange={(event) => onCodeChange(event.target.value)}
                  />
                </Form.Item>
                <Form.Item label="校区名称" style={{ marginBottom: 0 }}>
                  <Input
                    value={form.name}
                    placeholder="请输入校区名称"
                    onChange={(event) => onNameChange(event.target.value)}
                  />
                </Form.Item>
                <Form.Item label="联系方式" style={{ marginBottom: 0 }}>
                  <Input
                    value={form.phone}
                    placeholder="可留空"
                    onChange={(event) => onPhoneChange(event.target.value)}
                  />
                </Form.Item>
                <Form.Item label="状态" style={{ marginBottom: 0 }}>
                  <Select
                    value={form.status}
                    options={statusOptions}
                    onChange={onStatusChange}
                  />
                </Form.Item>
              </div>
            </section>

            <section style={sectionCardStyle}>
              <div style={sectionTitleStyle}>补充信息</div>
              <div style={sectionDescriptionStyle}>地址、排序和备注可以帮助运营人员在列表与后续组织挂载时更快识别校区。</div>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
                  gap: 16,
                  alignItems: 'start',
                }}
              >
                <Form.Item label="校区地址" style={{ marginBottom: 0 }}>
                  <Input
                    value={form.address}
                    placeholder="可留空"
                    onChange={(event) => onAddressChange(event.target.value)}
                  />
                </Form.Item>
                <Form.Item
                  label="排序值"
                  extra="数字越小越靠前，可留空。"
                  style={{ marginBottom: 0 }}
                >
                  <InputNumber
                    value={form.sort.length > 0 ? Number(form.sort) : null}
                    style={{ width: '100%' }}
                    min={0}
                    precision={0}
                    placeholder="请输入排序值"
                    onChange={(value) => onSortChange(value === null ? '' : String(value))}
                  />
                </Form.Item>
              </div>
              <div
                style={{
                  marginTop: 16,
                  paddingTop: 16,
                  borderTop: '1px solid #f1ece4',
                }}
              >
                <Form.Item
                  label="备注"
                  extra="最多 200 个字符。"
                  style={{ marginBottom: 0 }}
                >
                  <Input.TextArea
                    value={form.remark}
                    rows={4}
                    maxLength={200}
                    placeholder="可填写校区说明、地理位置或运营备注"
                    onChange={(event) => onRemarkChange(event.target.value)}
                  />
                </Form.Item>
              </div>
            </section>
          </Form>
        ) : (
          <Alert message="当前无法加载校区表单，请关闭后重试。" type="warning" showIcon />
        )}
      </div>
    </Modal>
  )
}

export default CampusEditorModal
