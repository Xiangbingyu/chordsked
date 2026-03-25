import { useState } from 'react'
import { homeworkPool, studentBookings, studentRemaining, studentTimeline } from '../lib/mockData'

export default function StudentView({ page }) {
  const [activeBookingId, setActiveBookingId] = useState(studentBookings[0].id)
  const [activeHomeworkId, setActiveHomeworkId] = useState(homeworkPool[0].id)
  const [learningTab, setLearningTab] = useState('约课')
  const [homeworkTab, setHomeworkTab] = useState('待完成')
  const [studentTip, setStudentTip] = useState('')
  const [scheduleDate, setScheduleDate] = useState('2026-03-24')
  const [scheduleView, setScheduleView] = useState('周')
  const [bookingDate, setBookingDate] = useState('2026-03-24')
  const [profileDetailTab, setProfileDetailTab] = useState('home')
  const [studentApplyModalOpen, setStudentApplyModalOpen] = useState(false)
  const [studentApplyForm, setStudentApplyForm] = useState({ type: '调课', date: '2026-03-26', reason: '' })
  const [studentApplyRows, setStudentApplyRows] = useState([
    { id: 'SA-001', type: '调课', date: '2026-03-20', reason: '与考试时间冲突', status: '已通过' }
  ])
  const activeBooking = studentBookings.find((item) => item.id === activeBookingId)
  const activeHomework = homeworkPool.find((item) => item.id === activeHomeworkId)
  const homeworkRows = homeworkPool.map((item, idx) => ({
    ...item,
    statusTag: idx === 0 ? '待完成' : idx === 1 ? '已提交' : '已批改',
    review: idx === 2 ? '老师点评：右手节奏稳定，注意换和弦连贯性。' : '暂未点评'
  }))
  const filteredHomeworkRows = homeworkRows.filter((item) => item.statusTag === homeworkTab)
  const calendarRows = [
    { date: '2026-03-24', short: '03-24', classes: ['标准课'], notes: '今日 1 节' },
    { date: '2026-03-25', short: '03-25', classes: [], notes: '无课' },
    { date: '2026-03-26', short: '03-26', classes: ['团课'], notes: '1 节' },
    { date: '2026-03-27', short: '03-27', classes: [], notes: '无课' },
    { date: '2026-03-28', short: '03-28', classes: ['体验课'], notes: '1 节' },
    { date: '2026-03-29', short: '03-29', classes: [], notes: '无课' },
    { date: '2026-03-30', short: '03-30', classes: ['正式课'], notes: '1 节' }
  ]
  const monthCalendarRows = Array.from({ length: 31 }, (_, idx) => {
    const date = `2026-03-${String(idx + 1).padStart(2, '0')}`
    const hasClass = ['2026-03-24', '2026-03-26', '2026-03-28', '2026-03-30'].includes(date)
    return {
      date,
      short: `03-${String(idx + 1).padStart(2, '0')}`,
      notes: hasClass ? '1 节' : '无课'
    }
  })
  const scheduleCalendarRows = scheduleView === '周' ? calendarRows : monthCalendarRows
  const bookingCalendarRows = [
    { date: '2026-03-24', short: '03-24', summary: '可约 2 节', ids: ['B-1', 'B-2'] },
    { date: '2026-03-25', short: '03-25', summary: '可约 1 节', ids: ['B-3'] },
    { date: '2026-03-26', short: '03-26', summary: '可约 2 节', ids: ['B-4', 'B-5'] },
    { date: '2026-03-27', short: '03-27', summary: '暂满', ids: [] },
    { date: '2026-03-28', short: '03-28', summary: '可约 1 节', ids: ['B-6'] },
    { date: '2026-03-29', short: '03-29', summary: '可约 1 节', ids: ['B-7'] },
    { date: '2026-03-30', short: '03-30', summary: '可约 2 节', ids: ['B-8', 'B-9'] }
  ]
  const bookingRowsByDate = bookingCalendarRows.find((item) => item.date === bookingDate)?.ids || []
  const bookingList = bookingRowsByDate.map((id, idx) => ({
    id,
    type: idx % 3 === 0 ? '正式课（可选老师）' : idx % 3 === 1 ? '团课（系统推荐）' : '体验课',
    time: idx % 3 === 0 ? '14:00-15:00' : idx % 3 === 1 ? '16:00-17:00' : '19:00-20:00',
    teacher: idx % 3 === 0 ? '刘老师' : '系统推荐',
    blockReason: ''
  }))
  const scheduleClassRows = [
    { id: 'SC-1', date: '2026-03-24', title: '标准课：和弦转换', time: '18:30-19:30', teacher: '赵老师', room: 'A101' },
    { id: 'SC-2', date: '2026-03-26', title: '团课：节奏练习', time: '19:00-20:00', teacher: '陈老师', room: 'B203' },
    { id: 'SC-3', date: '2026-03-28', title: '体验课：入门试听', time: '15:00-16:00', teacher: '刘老师', room: 'A203' }
  ]
  const scheduleList = scheduleClassRows.filter((item) => item.date === scheduleDate)
  const packageRows = [
    { id: 'PK-1', name: '正式课48课时包', remain: '21', status: '已购买', validUntil: '2026-12-31' },
    { id: 'PK-2', name: '进阶课24课时包', remain: '0', status: '可购买', validUntil: '—' }
  ]
  const remindRows = [
    { id: 'RM-1', scene: '上课提醒', rule: '课前1天 + 6小时', channel: '小程序通知', status: '开启' },
    { id: 'RM-2', scene: '核销未预约提醒', rule: '核销后第1天', channel: '小程序通知', status: '开启' },
    { id: 'RM-3', scene: '作业未提交提醒', rule: '截止前12小时', channel: '小程序通知', status: '开启' },
    { id: 'RM-4', scene: '剩余课时提醒', rule: '低于10课时', channel: '小程序通知', status: '开启' }
  ]
  const activeBookingCard = bookingList.find((item) => item.id === activeBookingId) || bookingList[0] || activeBooking

  const submitStudentApply = () => {
    if (!studentApplyForm.reason) {
      setStudentTip('请填写申请原因后再提交')
      return
    }
    const newApply = {
      id: `SA-${String(studentApplyRows.length + 1).padStart(3, '0')}`,
      type: studentApplyForm.type,
      date: studentApplyForm.date,
      reason: studentApplyForm.reason,
      status: '待审核'
    }
    setStudentApplyRows((prev) => [newApply, ...prev])
    setStudentApplyModalOpen(false)
    setStudentApplyForm({ type: '调课', date: '2026-03-26', reason: '' })
    setStudentTip(`已提交${newApply.type}申请，等待教务审核`)
  }

  const mobileCard = 'mx-auto w-full max-w-[390px] rounded-[24px] border border-[#f0ebe3] bg-[#fffdf9] p-4 shadow-sm'

  if (page === 'schedule') {
    return (
      <section className="space-y-3">
        <article className={mobileCard}>
          <div className="flex items-center justify-between">
            <div className="text-base font-semibold">课表</div>
            <div className="flex gap-1">
              <button onClick={() => setScheduleView('周')} className={`rounded-full px-2.5 py-1 text-xs ${scheduleView === '周' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>周</button>
              <button onClick={() => setScheduleView('月')} className={`rounded-full px-2.5 py-1 text-xs ${scheduleView === '月' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>月</button>
            </div>
          </div>
          <div className="mt-3 rounded-2xl bg-[#fff3e8] p-3 text-sm">
            <div className="font-medium">{studentRemaining.todayClass.title}</div>
            <div className="mt-1 text-xs text-[#8f8376]">{studentRemaining.todayClass.time} · {studentRemaining.todayClass.teacher}</div>
            <div className="mt-1 text-xs text-[#8f8376]">地点：北环国基路校区 A101 ｜ 签到规则：课前10分钟可签到</div>
          </div>
          <div className="mt-3 text-xs text-[#8f8376]">手机端{scheduleView}课表（日历可左右滑动）</div>
          <div className="mt-2 overflow-x-auto pb-1">
            <div className={`grid gap-1 ${scheduleView === '周' ? 'min-w-[560px] grid-cols-7' : 'min-w-[860px] grid-cols-7'}`}>
              {scheduleCalendarRows.map((day) => (
                <button key={day.date} onClick={() => setScheduleDate(day.date)} className={`rounded-xl border px-1 py-2 text-center text-[11px] ${scheduleDate === day.date ? 'border-[#ff9b54] bg-[#fff4ea]' : 'border-[#f0ebe3] bg-[#faf8f4]'}`}>
                  <div>{day.short}</div>
                  <div className="mt-1 text-[#8f8376]">{day.notes}</div>
                </button>
              ))}
            </div>
          </div>
          <div className="mt-3 rounded-xl bg-[#faf8f4] p-3">
            <div className="text-xs text-[#8f8376]">当日课程（{scheduleDate}）</div>
            <div className="mt-2 space-y-2">
              {scheduleList.length === 0 && <div className="text-xs text-[#8f8376]">当日暂无课程</div>}
              {scheduleList.map((item) => (
                <div key={item.id} className="rounded-xl bg-white p-2 text-xs">
                  <div className="font-medium text-[#2e2a25]">{item.title}</div>
                  <div className="mt-1 text-[#8f8376]">{item.time} · {item.teacher} · {item.room}</div>
                </div>
              ))}
            </div>
          </div>
        </article>
      </section>
    )
  }

  if (page === 'learning') {
    return (
      <section className="space-y-3">
        <article className={mobileCard}>
          <div className="text-base font-semibold">学习</div>
          <div className="mt-3 flex gap-2">
            {['约课', '作业', '成长'].map((item) => (
              <button key={item} onClick={() => setLearningTab(item)} className={`rounded-full px-3 py-1.5 text-xs ${learningTab === item ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{item}</button>
            ))}
          </div>
          {learningTab === '约课' && (
            <div className="mt-3 space-y-2">
              <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#8f8376]">日历上展示简要可约信息，点击日期后显示可预约课程。</div>
              <div className="overflow-x-auto pb-1">
                <div className="grid min-w-[560px] grid-cols-7 gap-1">
                  {bookingCalendarRows.map((day) => (
                    <button key={day.date} onClick={() => setBookingDate(day.date)} className={`rounded-xl border px-1 py-2 text-center text-[11px] ${bookingDate === day.date ? 'border-[#ff9b54] bg-[#fff4ea]' : 'border-[#f0ebe3] bg-[#faf8f4]'}`}>
                      <div>{day.short}</div>
                      <div className="mt-1 text-[#8f8376]">{day.summary}</div>
                    </button>
                  ))}
                </div>
              </div>
              <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#8f8376]">已选日期：{bookingDate}</div>
              {bookingList.map((item) => (
                <button
                  key={item.id}
                  onClick={() => setActiveBookingId(item.id)}
                  className={`w-full rounded-2xl border p-3 text-left ${
                    activeBookingId === item.id ? 'border-[#ff9b54] bg-[#fff4ea]' : 'border-[#f0ebe3] bg-[#faf8f4]'
                  }`}
                >
                  <div className="text-sm font-medium">{item.type}</div>
                  <div className="text-xs text-[#8f8376]">{item.time} · {item.teacher}</div>
                </button>
              ))}
              {bookingList.length === 0 && (
                <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#8f8376]">当日暂无可预约课程</div>
              )}
              <button onClick={() => setStudentTip(`已预约：${activeBookingCard?.type || '当日课程'} / ${bookingDate}`)} className="w-full rounded-xl bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认预约</button>
            </div>
          )}
          {learningTab === '作业' && (
            <div className="mt-3 space-y-2">
              <div className="flex gap-2 overflow-x-auto pb-1">
                {['待完成', '已提交', '已批改'].map((item) => (
                  <button key={item} onClick={() => setHomeworkTab(item)} className={`rounded-full px-3 py-1 text-xs whitespace-nowrap ${homeworkTab === item ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{item}</button>
                ))}
              </div>
              {filteredHomeworkRows.map((item) => (
                <button
                  key={item.id}
                  onClick={() => setActiveHomeworkId(item.id)}
                  className={`w-full rounded-xl border p-3 text-left ${
                    activeHomeworkId === item.id ? 'border-[#ff9b54] bg-[#fff4ea]' : 'border-[#f0ebe3] bg-[#faf8f4]'
                  }`}
                >
                  <div className="text-sm font-medium">{item.title}</div>
                  <div className="text-xs text-[#8f8376]">{item.deadline}</div>
                  <div className="mt-1 text-xs text-[#6f665d]">{item.review}</div>
                </button>
              ))}
              <textarea className="h-20 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" placeholder="提交作业说明" />
              <div className="grid grid-cols-3 gap-2 text-xs">
                <button className="rounded-lg border border-[#e9e2d8] px-2 py-2">上传图片</button>
                <button className="rounded-lg border border-[#e9e2d8] px-2 py-2">上传音频</button>
                <button className="rounded-lg border border-[#e9e2d8] px-2 py-2">上传视频</button>
              </div>
              <button onClick={() => setStudentTip(`已提交作业：${activeHomework.title}`)} className="w-full rounded-xl bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">提交作业</button>
            </div>
          )}
          {learningTab === '成长' && (
            <div className="mt-3 space-y-2">
              <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#8f8376]">成长记录：课后反馈时间线 + 阶段测评（一期先展示反馈）</div>
              {studentTimeline.map((item) => (
                <div key={item.id} className="rounded-xl bg-[#faf8f4] p-3">
                  <div className="text-xs text-[#8f8376]">{item.date}</div>
                  <div className="mt-1 text-sm font-medium">{item.title}</div>
                  <div className="mt-1 text-xs text-[#8f8376]">{item.summary}</div>
                </div>
              ))}
            </div>
          )}
          {studentTip && (
            <div className="mt-3 rounded-xl bg-[#fff4ea] p-3 text-xs text-[#8f5d34]">{studentTip}</div>
          )}
        </article>
      </section>
    )
  }

  return (
    <section className="space-y-3">
      <article className={mobileCard}>
        <div className="text-base font-semibold">我的</div>
        <div className="mt-3 rounded-xl bg-[#fff4ea] p-3 text-sm">
          剩余课时：<span className="text-lg font-semibold">{studentRemaining.remainingHours}</span>
        </div>
        {profileDetailTab === 'home' && (
          <div className="mt-3 space-y-2">
            <button onClick={() => setProfileDetailTab('packages')} className="w-full rounded-xl border border-[#f0ebe3] bg-[#faf8f4] p-3 text-left text-sm">我的课包</button>
            <button onClick={() => setProfileDetailTab('alerts')} className="w-full rounded-xl border border-[#f0ebe3] bg-[#faf8f4] p-3 text-left text-sm">我的提醒</button>
            <button onClick={() => setProfileDetailTab('info')} className="w-full rounded-xl border border-[#f0ebe3] bg-[#faf8f4] p-3 text-left text-sm">个人信息</button>
            <button onClick={() => setStudentApplyModalOpen(true)} className="w-full rounded-xl border border-[#f0ebe3] bg-[#faf8f4] p-3 text-left text-sm">主动申请调课/消课</button>
            <div className="rounded-xl bg-[#faf8f4] p-3">
              <div className="mb-2 text-xs text-[#8f8376]">我的申请记录</div>
              <div className="space-y-1 text-xs">
                {studentApplyRows.slice(0, 3).map((item) => (
                  <div key={item.id} className="flex items-center justify-between rounded-lg bg-white px-2 py-1.5">
                    <span>{item.type} {item.date}</span>
                    <span>{item.status}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
        {profileDetailTab === 'packages' && (
          <div className="mt-3">
            <button onClick={() => setProfileDetailTab('home')} className="mb-2 rounded-lg border border-[#e8dfd3] px-2 py-1 text-xs">返回</button>
            <div className="overflow-x-auto">
              <table className="min-w-[520px] text-xs">
                <thead>
                  <tr className="bg-[#faf8f4] text-[#8f8376]">
                    <th className="px-2 py-2 text-left">课包</th>
                    <th className="px-2 py-2 text-left">剩余课时</th>
                    <th className="px-2 py-2 text-left">状态</th>
                    <th className="px-2 py-2 text-left">有效期</th>
                  </tr>
                </thead>
                <tbody>
                  {packageRows.map((item) => (
                    <tr key={item.id} className="border-b border-[#f0ebe3]">
                      <td className="px-2 py-2">{item.name}</td>
                      <td className="px-2 py-2">{item.remain}</td>
                      <td className="px-2 py-2">{item.status}</td>
                      <td className="px-2 py-2">{item.validUntil}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
        {profileDetailTab === 'alerts' && (
          <div className="mt-3">
            <button onClick={() => setProfileDetailTab('home')} className="mb-2 rounded-lg border border-[#e8dfd3] px-2 py-1 text-xs">返回</button>
            <div className="overflow-x-auto">
              <table className="min-w-[520px] text-xs">
                <thead>
                  <tr className="bg-[#faf8f4] text-[#8f8376]">
                    <th className="px-2 py-2 text-left">提醒项</th>
                    <th className="px-2 py-2 text-left">规则</th>
                    <th className="px-2 py-2 text-left">渠道</th>
                    <th className="px-2 py-2 text-left">状态</th>
                  </tr>
                </thead>
                <tbody>
                  {remindRows.map((item) => (
                    <tr key={item.id} className="border-b border-[#f0ebe3]">
                      <td className="px-2 py-2">{item.scene}</td>
                      <td className="px-2 py-2">{item.rule}</td>
                      <td className="px-2 py-2">{item.channel}</td>
                      <td className="px-2 py-2">{item.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
        {profileDetailTab === 'info' && (
          <div className="mt-3">
            <button onClick={() => setProfileDetailTab('home')} className="mb-2 rounded-lg border border-[#e8dfd3] px-2 py-1 text-xs">返回</button>
            <div className="space-y-2 text-sm">
              <div className="rounded-xl bg-[#faf8f4] p-3">姓名：李予安</div>
              <div className="rounded-xl bg-[#faf8f4] p-3">手机号：138****1024</div>
              <div className="rounded-xl bg-[#faf8f4] p-3">我的申请：调课申请 #A203 已通过</div>
            </div>
          </div>
        )}
        {studentApplyModalOpen && (
          <div className="fixed inset-0 z-40 flex items-end justify-center bg-black/30 p-0">
            <button className="absolute inset-0" onClick={() => setStudentApplyModalOpen(false)} />
            <div className="relative w-full max-w-[390px] rounded-t-3xl bg-white p-4 shadow-2xl">
              <div className="mb-2 text-sm font-semibold">提交申请</div>
              <div className="space-y-2">
                <select value={studentApplyForm.type} onChange={(e) => setStudentApplyForm((p) => ({ ...p, type: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>调课</option><option>消课</option></select>
                <input value={studentApplyForm.date} onChange={(e) => setStudentApplyForm((p) => ({ ...p, date: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                <textarea value={studentApplyForm.reason} onChange={(e) => setStudentApplyForm((p) => ({ ...p, reason: e.target.value }))} className="h-20 w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" placeholder="请输入申请原因" />
              </div>
              <button onClick={submitStudentApply} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">提交申请</button>
            </div>
          </div>
        )}
      </article>
    </section>
  )
}
