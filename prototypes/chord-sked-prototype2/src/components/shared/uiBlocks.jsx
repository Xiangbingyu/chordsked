import { useState } from 'react'

export function QrPreview({ value }) {
  const dots = Array.from({ length: 21 * 21 }, (_, idx) => {
    const row = Math.floor(idx / 21)
    const col = idx % 21
    const seed = value.split('').reduce((sum, ch) => sum + ch.charCodeAt(0), 0)
    const finder = (row < 7 && col < 7) || (row < 7 && col > 13) || (row > 13 && col < 7)
    if (finder) {
      const inFrame = row === 0 || row === 6 || col === 0 || col === 6
      const inCore = row >= 2 && row <= 4 && col >= 2 && col <= 4
      return inFrame || inCore
    }
    return ((row * 17 + col * 13 + seed) % 3) === 0
  })

  return (
    <div className="rounded-2xl border border-[#e8dfd3] bg-white p-3 inline-flex flex-col items-center gap-2">
      <div
        className="grid gap-[2px] bg-white p-1"
        style={{ gridTemplateColumns: 'repeat(21, minmax(0, 1fr))' }}
      >
        {dots.map((dark, idx) => (
          <div key={idx} className={`h-[6px] w-[6px] ${dark ? 'bg-black' : 'bg-white'}`} />
        ))}
      </div>
      <div className="text-[11px] text-[#8f8376]">扫码预约体验课</div>
    </div>
  )
}

export function PaginatedTable({ columns, rows, rowKey = 'id', pageSize = 5, emptyText = '暂无数据' }) {
  const [page, setPage] = useState(1)
  const totalPages = Math.max(1, Math.ceil(rows.length / pageSize))
  const safePage = Math.min(page, totalPages)
  const start = (safePage - 1) * pageSize
  const pageRows = rows.slice(start, start + pageSize)

  return (
    <div className="rounded-2xl border border-[#f0ebe3] overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-[#faf7f1] text-[#6f655b]">
          <tr>
            {columns.map((col) => (
              <th key={col.key} className="px-3 py-2 text-left font-medium">{col.title}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {pageRows.map((row) => (
            <tr key={row[rowKey]} className="border-t border-[#f1ece4]">
              {columns.map((col) => (
                <td key={col.key} className="px-3 py-2 text-[#3a352f]">
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
          {pageRows.length === 0 && (
            <tr>
              <td colSpan={columns.length} className="px-3 py-8 text-center text-[#9b9187]">{emptyText}</td>
            </tr>
          )}
        </tbody>
      </table>
      <div className="flex items-center justify-end gap-2 border-t border-[#f1ece4] bg-white px-3 py-2 text-xs">
        <button onClick={() => setPage((p) => Math.max(1, p - 1))} className="rounded border border-[#e8dfd3] px-2 py-1">上一页</button>
        <span>{safePage}/{totalPages}</span>
        <button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} className="rounded border border-[#e8dfd3] px-2 py-1">下一页</button>
      </div>
    </div>
  )
}
