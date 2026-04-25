import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { adminNavigation } from '../config/adminNavigation'

function AdminLayout() {
  const location = useLocation()
  const orgGroup = adminNavigation.groups.find((group) => group.key === 'org') ?? adminNavigation.groups[0]
  const hasActiveOrgChild = orgGroup.children.some((item) => location.pathname === item.href)
  const [isOrgExpanded, setIsOrgExpanded] = useState(hasActiveOrgChild)

  useEffect(() => {
    if (hasActiveOrgChild) {
      setIsOrgExpanded(true)
    }
  }, [hasActiveOrgChild])

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        background:
          'radial-gradient(circle at top, rgba(255, 155, 84, 0.14), transparent 32%), #f6f1ea',
        color: '#2b2b2b',
      }}
    >
      <div
        style={{
          display: 'flex',
          gap: 20,
          minHeight: '100vh',
          width: '100%',
          maxWidth: 1600,
          margin: '0 auto',
          padding: 20,
          boxSizing: 'border-box',
        }}
      >
        <aside
          style={{
            width: 260,
            flexShrink: 0,
            borderRadius: 24,
            backgroundColor: '#23242e',
            color: '#f1f1f2',
            padding: 20,
            boxShadow: '0 24px 48px rgba(0, 0, 0, 0.18)',
          }}
        >
          <div
            style={{
              marginBottom: 24,
              borderRadius: 16,
              backgroundColor: 'rgba(255, 255, 255, 0.1)',
              padding: 16,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <div
                style={{
                  display: 'flex',
                  height: 40,
                  width: 40,
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderRadius: 12,
                  backgroundColor: '#ff9b54',
                  color: '#1f1f1f',
                  fontWeight: 700,
                  fontSize: 16,
                }}
              >
                C
              </div>
              <div>
                <div style={{ fontSize: 16, fontWeight: 600 }}>{adminNavigation.title}</div>
                <div style={{ fontSize: 12, color: 'rgba(255, 255, 255, 0.7)' }}>
                  {adminNavigation.subtitle}
                </div>
              </div>
            </div>
          </div>

          <nav style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {adminNavigation.links.map((item) => {
              const isActive = location.pathname === item.href

              return (
                <div
                  key={item.key}
                  style={{
                    borderRadius: 16,
                    backgroundColor: 'rgba(255, 255, 255, 0.05)',
                    padding: 4,
                  }}
                >
                  <NavLink
                    to={item.href}
                    style={{
                      display: 'flex',
                      width: '100%',
                      alignItems: 'center',
                      gap: 10,
                      borderRadius: 12,
                      backgroundColor: isActive ? '#ff9b54' : 'transparent',
                      color: isActive ? '#1f1f1f' : 'rgba(255, 255, 255, 0.85)',
                      padding: '10px 12px',
                      textDecoration: 'none',
                      fontSize: 14,
                      fontWeight: 600,
                      boxSizing: 'border-box',
                    }}
                  >
                    {item.label}
                  </NavLink>
                </div>
              )
            })}

            {adminNavigation.groups.map((item) => (
              <div
                key={item.key}
                style={{
                  borderRadius: 16,
                  backgroundColor: 'rgba(255, 255, 255, 0.05)',
                  padding: 4,
                }}
              >
                <button
                  type="button"
                  onClick={() => setIsOrgExpanded((value) => !value)}
                  style={{
                    display: 'flex',
                    width: '100%',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    border: 'none',
                    borderRadius: 12,
                    backgroundColor: hasActiveOrgChild ? '#ff9b54' : 'transparent',
                    color: hasActiveOrgChild ? '#1f1f1f' : 'rgba(255, 255, 255, 0.85)',
                    padding: '10px 12px',
                    fontSize: 14,
                    fontWeight: hasActiveOrgChild ? 600 : 500,
                    textAlign: 'left',
                    cursor: 'pointer',
                  }}
                >
                  <span>{item.label}</span>
                  <span
                    aria-hidden="true"
                    style={{
                      transform: isOrgExpanded ? 'rotate(90deg)' : 'rotate(0deg)',
                      transition: 'transform 0.2s ease',
                    }}
                  >
                    {'>'}
                  </span>
                </button>

                {isOrgExpanded ? (
                  <div style={{ marginTop: 4, display: 'flex', flexDirection: 'column', gap: 4 }}>
                    {item.children.map((child) => {
                      const isChildActive = location.pathname === child.href

                      return (
                        <NavLink
                          key={child.key}
                          to={child.href}
                          style={{
                            display: 'block',
                            borderRadius: 10,
                            padding: '8px 12px 8px 32px',
                            textDecoration: 'none',
                            fontSize: 12,
                            fontWeight: 500,
                            backgroundColor: isChildActive ? 'rgba(255, 155, 84, 0.16)' : 'transparent',
                            color: isChildActive ? '#ffb070' : 'rgba(255, 255, 255, 0.8)',
                          }}
                        >
                          {child.label}
                        </NavLink>
                      )
                    })}
                  </div>
                ) : null}
              </div>
            ))}
          </nav>
        </aside>

        <div style={{ flex: 1, minWidth: 0 }}>
          <header
            style={{
              marginBottom: 16,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              borderRadius: 24,
              backgroundColor: '#ffffff',
              padding: '16px 24px',
              boxShadow: '0 10px 28px rgba(15, 23, 42, 0.06)',
            }}
          >
            <div>
              <h1
                style={{
                  margin: 0,
                  color: '#2a2a2f',
                  fontSize: 30,
                  fontWeight: 600,
                  lineHeight: 1.2,
                }}
              >
                {adminNavigation.headerTitle}
              </h1>
              <p
                style={{
                  margin: '6px 0 0',
                  color: '#7f7f88',
                  fontSize: 14,
                }}
              >
                {adminNavigation.headerSubtitle}
              </p>
            </div>
            <button
              type="button"
              style={{
                position: 'relative',
                border: 'none',
                borderRadius: 16,
                backgroundColor: '#fff3e8',
                color: '#8f6746',
                padding: 10,
                minWidth: 44,
                minHeight: 44,
                cursor: 'pointer',
                fontSize: 18,
              }}
            >
              <span aria-hidden="true">🔔</span>
              <span
                style={{
                  position: 'absolute',
                  right: 4,
                  top: 4,
                  display: 'flex',
                  minWidth: 16,
                  height: 16,
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderRadius: 999,
                  backgroundColor: '#ff6f3d',
                  color: '#ffffff',
                  padding: '0 4px',
                  fontSize: 10,
                  lineHeight: 1,
                }}
              >
                2
              </span>
            </button>
          </header>

          <main
            style={{
              minHeight: 'calc(100vh - 96px)',
              borderRadius: 24,
              backgroundColor: '#ffffff',
              padding: 24,
              boxShadow: '0 10px 28px rgba(15, 23, 42, 0.06)',
            }}
          >
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  )
}

export default AdminLayout
