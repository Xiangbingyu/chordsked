import { Card, Input, Space, Table, Tag, Typography } from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { fetchStudents } from '../services/studentService'
import type { Student } from '../types/student'

function StudentsPage() {
  const [loading, setLoading] = useState(false)
  const [students, setStudents] = useState<Student[]>([])
  const [total, setTotal] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const response = await fetchStudents({ page, pageSize, keyword })
        if (response.code === 0) {
          setStudents(response.data.items)
          setTotal(response.data.total)
        }
      } finally {
        setLoading(false)
      }
    }
    void load()
  }, [keyword, page, pageSize])

  const columns = useMemo(
    () => [
      { title: 'ID', dataIndex: 'id', width: 100 },
      { title: '姓名', dataIndex: 'name' },
      { title: '年龄', dataIndex: 'age', width: 120 },
      {
        title: '等级',
        dataIndex: 'level',
        width: 140,
        render: (value: string) => <Tag color="blue">{value}</Tag>,
      },
    ],
    [],
  )

  return (
    <div style={{ minHeight: '100vh', padding: 24 }}>
      <Card
        style={{
          maxWidth: 1200,
          margin: '0 auto',
          borderRadius: 20,
          boxShadow: '0 10px 28px rgba(15, 23, 42, 0.08)',
        }}
      >
        <Space
          direction="vertical"
          size={16}
          style={{ width: '100%', marginBottom: 12 }}
        >
          <Typography.Title level={3} style={{ margin: 0 }}>
            学员列表
          </Typography.Title>
          <Input.Search
            placeholder="按姓名搜索"
            allowClear
            enterButton="搜索"
            onSearch={(value) => {
              setPage(1)
              setKeyword(value.trim())
            }}
          />
        </Space>
        <Table<Student>
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={students}
          pagination={{
            current: page,
            pageSize,
            total,
            showSizeChanger: true,
            onChange: (nextPage, nextPageSize) => {
              setPage(nextPage)
              setPageSize(nextPageSize)
            },
          }}
        />
      </Card>
    </div>
  )
}

export default StudentsPage

