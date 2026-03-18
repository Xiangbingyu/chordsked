import { Button, Card, Space, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../stores/authStore'

function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore((state) => state.login)

  const handleLogin = () => {
    login()
    navigate('/students')
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 24,
      }}
    >
      <Card
        style={{
          width: '100%',
          maxWidth: 520,
          borderRadius: 20,
          boxShadow: '0 10px 28px rgba(15, 23, 42, 0.08)',
        }}
      >
        <Space direction="vertical" size={24} style={{ width: '100%' }}>
          <div>
            <Typography.Title level={2} style={{ marginBottom: 8 }}>
              ChordSked
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              欢迎进入学员管理演示系统
            </Typography.Paragraph>
          </div>
          <Button type="primary" size="large" block onClick={handleLogin}>
            登录
          </Button>
        </Space>
      </Card>
    </div>
  )
}

export default LoginPage

