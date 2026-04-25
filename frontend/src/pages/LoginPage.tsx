import { Button, Card, Form, Input, Space, Typography, message } from 'antd'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../stores/authStore'
import type { AuthLoginRequest } from '../types/auth'

type LoginFormValues = {
  username: string
  password: string
}

function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore((state) => state.login)
  const [loading, setLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [messageApi, contextHolder] = message.useMessage()

  useEffect(() => {
    if (!errorMessage) {
      return
    }

    void messageApi.error(errorMessage)
    setErrorMessage('')
  }, [errorMessage, messageApi])

  const onFinish = async (values: LoginFormValues) => {
    setLoading(true)
    try {
      const loginData: AuthLoginRequest = {
        username: values.username,
        password: values.password,
        loginMethod: 'USERNAME_PASSWORD',
      }
      await login(loginData)
      navigate('/admin/workbench')
    } catch (error) {
      const loginError = error instanceof Error ? error.message : '登录失败，请检查账号密码'
      setErrorMessage(loginError)
    } finally {
      setLoading(false)
    }
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
      {contextHolder}
      <Card
        style={{
          width: '100%',
          maxWidth: 400,
          borderRadius: 20,
          boxShadow: '0 10px 28px rgba(15, 23, 42, 0.08)',
        }}
      >
        <Space direction="vertical" size={24} style={{ width: '100%' }}>
          <div style={{ textAlign: 'center', marginBottom: 8 }}>
            <Typography.Title level={2} style={{ marginBottom: 8 }}>
              ChordSked
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              欢迎进入教务管理系统
            </Typography.Paragraph>
          </div>

          <Form
            name="login"
            layout="vertical"
            onFinish={onFinish}
            autoComplete="off"
            size="large"
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: '请输入账号' }]}
            >
              <Input placeholder="账号" />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password placeholder="密码" />
            </Form.Item>

            <Form.Item style={{ marginBottom: 0, marginTop: 24 }}>
              <Button type="primary" htmlType="submit" block loading={loading}>
                登录
              </Button>
            </Form.Item>
          </Form>
        </Space>
      </Card>
    </div>
  )
}

export default LoginPage

