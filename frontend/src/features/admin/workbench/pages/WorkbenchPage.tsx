function WorkbenchPage() {
  return (
    <div
      style={{
        display: 'flex',
        minHeight: 240,
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: 560,
          borderRadius: 24,
          padding: 32,
          backgroundColor: '#fff8f1',
          boxShadow: 'inset 0 0 0 1px rgba(255, 155, 84, 0.18)',
        }}
      >
        <div style={{ fontSize: 14, fontWeight: 600, color: '#ff9b54', marginBottom: 12 }}>
          Workbench
        </div>
        <h2
          style={{
            margin: 0,
            color: '#2a2a2f',
            fontSize: 28,
            fontWeight: 600,
            lineHeight: 1.3,
          }}
        >
          工作台页面
        </h2>
        <p
          style={{
            margin: '12px 0 0',
            color: '#7f7f88',
            fontSize: 15,
            lineHeight: 1.8,
          }}
        >
          工作台已经迁移到 `features/admin/workbench/pages`，后续这里用于承载教务端首页看板、提醒和快捷操作内容。
        </p>
      </div>
    </div>
  )
}

export default WorkbenchPage
