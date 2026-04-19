import { Card, Space, Typography } from 'antd'

function DashboardPage() {
  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 24,
        backgroundColor: '#f0f2f5',
      }}
    >
      <Card
        style={{
          width: '100%',
          maxWidth: 480,
          borderRadius: 20,
          textAlign: 'center',
          boxShadow: '0 10px 28px rgba(15, 23, 42, 0.08)',
        }}
      >
        <Space direction="vertical" size={16} style={{ width: '100%', padding: '24px 0' }}>
          <Typography.Title level={2} style={{ margin: 0, color: '#1677ff' }}>
            ChordSked Demo
          </Typography.Title>
          <Typography.Title level={3} type="secondary" style={{ margin: 0 }}>
            教务端
          </Typography.Title>
          <Typography.Paragraph style={{ fontSize: 20, margin: 0, marginTop: 16 }}>
            欢迎您！
          </Typography.Paragraph>
        </Space>
      </Card>
    </div>
  )
}

export default DashboardPage
