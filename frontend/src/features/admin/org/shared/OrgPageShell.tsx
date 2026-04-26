import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { adminNavigation } from '../../../../config/adminNavigation'

type OrgPageShellProps = {
  pageKey: string
  title: string
  description: string
  children?: ReactNode
}

function OrgPageShell({ pageKey, title, description, children }: OrgPageShellProps) {
  const navigate = useNavigate()
  const orgGroup = adminNavigation.groups.find((group) => group.key === 'org') ?? adminNavigation.groups[0]
  const currentItem = orgGroup.children.find((item) => item.key === pageKey) ?? orgGroup.children[0]
  const currentCategory =
    orgGroup.categories.find((category) =>
      category.children.some((item) => item.key === currentItem.key),
    ) ?? orgGroup.categories[0]

  return (
    <div
      style={{
        display: 'flex',
        minHeight: 320,
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: 920,
          borderRadius: 24,
          padding: 32,
          backgroundColor: '#fff8f1',
          boxShadow: 'inset 0 0 0 1px rgba(255, 155, 84, 0.18)',
        }}
      >
        <div style={{ fontSize: 14, fontWeight: 600, color: '#ff9b54', marginBottom: 12 }}>
          {orgGroup.label}
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
          {title}
        </h2>
        <p
          style={{
            margin: '12px 0 0',
            color: '#7f7f88',
            fontSize: 15,
            lineHeight: 1.8,
          }}
        >
          {description}
        </p>

        <div
          style={{
            marginTop: 24,
            borderRadius: 20,
            border: '1px solid #f0ebe3',
            backgroundColor: '#fffaf2',
            padding: 16,
          }}
        >
          <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
            {orgGroup.categories.map((category) => {
              const isActive = category.key === currentCategory.key

              return (
                <button
                  key={category.key}
                  type="button"
                  onClick={() => navigate(category.children[0].href)}
                  style={{
                    border: 'none',
                    borderRadius: 999,
                    padding: '6px 12px',
                    backgroundColor: isActive ? '#ff9b54' : '#ffffff',
                    color: isActive ? '#1f1f1f' : '#8f8376',
                    fontSize: 12,
                    fontWeight: isActive ? 600 : 400,
                    cursor: 'pointer',
                  }}
                >
                  {category.label}
                </button>
              )
            })}
          </div>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {currentCategory.children.map((item) => {
              const isActive = item.key === currentItem.key

              return (
                <button
                  key={item.key}
                  type="button"
                  onClick={() => navigate(item.href)}
                  style={{
                    border: 'none',
                    borderRadius: 999,
                    padding: '6px 12px',
                    backgroundColor: isActive ? '#2a2a2f' : '#ffffff',
                    color: isActive ? '#ffffff' : '#8f8376',
                    fontSize: 12,
                    cursor: 'pointer',
                  }}
                >
                  {item.label}
                </button>
              )
            })}
          </div>
        </div>

        <div
          style={{
            marginTop: 20,
            borderRadius: 20,
            backgroundColor: '#ffffff',
            padding: 20,
            boxShadow: '0 10px 24px rgba(15, 23, 42, 0.04)',
          }}
        >
          {children}
        </div>
      </div>
    </div>
  )
}

export default OrgPageShell
