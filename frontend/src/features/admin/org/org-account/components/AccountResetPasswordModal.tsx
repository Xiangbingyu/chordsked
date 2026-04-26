import { Alert, Form, Input, Modal, Typography } from 'antd'
import type { InternalUserQueryResultVO } from '../../../../../types/internalUser'

type AccountResetPasswordModalProps = {
  open: boolean
  resetTarget: InternalUserQueryResultVO | null
  resetPasswordReason: string
  resetErrorMessage: string
  resetting: boolean
  onCancel: () => void
  onSubmit: () => Promise<void>
  onReasonChange: (value: string) => void
}

function AccountResetPasswordModal({
  open,
  resetTarget,
  resetPasswordReason,
  resetErrorMessage,
  resetting,
  onCancel,
  onSubmit,
  onReasonChange,
}: AccountResetPasswordModalProps) {
  return (
    <Modal
      open={open}
      title="重置密码"
      onCancel={onCancel}
      onOk={() => {
        void onSubmit()
      }}
      okText="确认重置"
      cancelText="取消"
      confirmLoading={resetting}
      okButtonProps={{ disabled: !resetTarget }}
      cancelButtonProps={{ disabled: resetting }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <Typography.Text type="secondary">
          {resetTarget ? `将为账号 ${resetTarget.username} 重置密码。` : '请选择需要重置密码的账号。'}
        </Typography.Text>
        {resetErrorMessage ? <Alert message={resetErrorMessage} type="error" showIcon /> : null}
        <Form layout="vertical">
          <Form.Item label="重置原因" extra="可选，留空时后端按未填写原因处理。" style={{ marginBottom: 0 }}>
            <Input.TextArea
              value={resetPasswordReason}
              rows={3}
              placeholder="请输入重置密码原因"
              onChange={(event) => onReasonChange(event.target.value)}
            />
          </Form.Item>
        </Form>
      </div>
    </Modal>
  )
}

export default AccountResetPasswordModal
