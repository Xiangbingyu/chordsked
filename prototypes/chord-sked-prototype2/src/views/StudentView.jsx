import { useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Home, CalendarDays, BookOpen, UserSquare2, ChevronRight, Bell, Clock, ChevronLeft, X } from 'lucide-react'
import { QrPreview } from '../components/shared/uiBlocks'

export default function StudentView({ page }) {
  const navigate = useNavigate()
  const activeTab = page || 'home'

  const toastTimerRef = useRef(null)
  const [toast, setToast] = useState('')
  const showToast = (message) => {
    setToast(message)
    if (toastTimerRef.current) window.clearTimeout(toastTimerRef.current)
    toastTimerRef.current = window.setTimeout(() => setToast(''), 1800)
  }

  const student = useMemo(() => ({
    name: '李予安',
    avatar: '李',
    campus: '北环国基路校区',
    phone: '138****1122',
    track: '木吉他'
  }), [])

  const [remainingHours, setRemainingHours] = useState(21)
  const [totalHours, setTotalHours] = useState(24)
  const [currentPackage, setCurrentPackage] = useState({ name: '小班课24节包', validUntil: '2026-12-31' })
  const [unreadNoticeCount, setUnreadNoticeCount] = useState(2)

  const [learningSubTab, setLearningSubTab] = useState('作业')
  const [homeworkStatus, setHomeworkStatus] = useState('未提交')
  const [homeworkSubmitOpen, setHomeworkSubmitOpen] = useState(false)
  const [homeworkSubmitType, setHomeworkSubmitType] = useState('图片')
  const [activeHomeworkId, setActiveHomeworkId] = useState('HW-001')

  const [selectedDate, setSelectedDate] = useState('2026-03-24')
  const [scheduleSheetOpen, setScheduleSheetOpen] = useState(false)
  const [classJoinOpen, setClassJoinOpen] = useState(false)
  const [adjustSheetOpen, setAdjustSheetOpen] = useState(false)
  const [adjustType, setAdjustType] = useState('调课')
  const [adjustReason, setAdjustReason] = useState('')
  const [adjustTargetId, setAdjustTargetId] = useState('')

  const [bookingDate, setBookingDate] = useState('2026-03-25')
  const [bookingType, setBookingType] = useState('全部')
  const [bookingCampus, setBookingCampus] = useState('全部')
  const [bookingTeacher, setBookingTeacher] = useState('不限')
  const [bookingModalOpen, setBookingModalOpen] = useState(false)
  const [bookingDraft, setBookingDraft] = useState(null)

  const [profilePage, setProfilePage] = useState('home')
  const [purchaseTargetPackage, setPurchaseTargetPackage] = useState(null)

  const [myBookings, setMyBookings] = useState([
    {
      id: 'BK-001',
      date: '2026-03-24',
      time: '18:30-19:30',
      title: '小班课（一对一）',
      teacher: '赵老师',
      campus: '北环国基路校区',
      status: '未开始',
      hoursCost: 1
    },
    {
      id: 'BK-002',
      date: '2026-03-26',
      time: '19:00-20:00',
      title: '节奏强化团课',
      teacher: '陈老师',
      campus: '北环国基路校区',
      status: '未开始',
      hoursCost: 1
    }
  ])

  const [applyRows, setApplyRows] = useState([
    {
      id: 'AP-001',
      type: '调课',
      from: '2026-03-20 19:00-20:00',
      to: '2026-03-22 16:00-17:00',
      reason: '与考试时间冲突',
      status: '已通过'
    }
  ])

  const [cancelRows, setCancelRows] = useState([
    {
      id: 'CN-001',
      course: '基础试听体验课',
      datetime: '2026-03-18 15:00-16:00',
      reason: '临时有事',
      status: '已取消'
    }
  ])

  const [homeworkItems, setHomeworkItems] = useState([
    {
      id: 'HW-001',
      title: '和弦转换练习（C-G-Am-F）',
      course: '小班课（一对一）',
      deadline: '明天 23:59',
      status: '未提交',
      review: ''
    },
    {
      id: 'HW-002',
      title: '右手扫弦节奏作业',
      course: '节奏强化团课',
      deadline: '已截止',
      status: '已批改',
      review: '节奏非常稳，继续保持！注意扫弦时的力度均匀，特别是上拨的时候不要太用力。'
    },
    {
      id: 'HW-003',
      title: '节拍器速度阶梯练习',
      course: '吉他进阶课',
      deadline: '下周五 23:59',
      status: '已提交',
      review: ''
    }
  ])

  const packageCatalog = useMemo(() => ([
    { id: 'PK-001', name: '小班课24节包', hours: 24, price: 2980, tag: '热卖' },
    { id: 'PK-002', name: '进阶课12节包', hours: 12, price: 1680, tag: '推荐' },
    { id: 'PK-003', name: '体验课2节包', hours: 2, price: 199, tag: '转化' }
  ]), [])

  const teacherOptions = useMemo(() => (['不限', '刘老师', '陈老师', '赵老师']), [])
  const campusOptions = useMemo(() => (['全部', '北环国基路校区', '西大剧院校区']), [])
  const typeOptions = useMemo(() => (['全部', '体验课', '团课', '一对一']), [])
  const studentLevel = useMemo(() => ({ level: 'Lv.2', title: '初级弹唱达人', progress: '成长值 1280 / 2000' }), [])
  const teacherProfile = useMemo(() => ({
    name: '陈老师',
    tag: '木吉他主教',
    intro: '6年教学经验，擅长启蒙与进阶节奏训练，课堂节奏清晰，重视作业跟进。'
  }), [])
  const coursePackageSummary = useMemo(() => ({ courseType: '小班课 / 团课 / 一对一', packageName: currentPackage.name }), [currentPackage.name])
  const studentBenefits = useMemo(() => ({ beans: 1280, level: '青铜豆友', next: '距升级白银豆友还差 220 豆（待确定，二期）' }), [])
  const studentHourSummary = useMemo(() => ({
    total: totalHours,
    attended: Math.max(totalHours - remainingHours, 0),
    remaining: remainingHours
  }), [remainingHours, totalHours])

  const scheduleDays = useMemo(() => ([
    { date: '2026-03-24', day: '一' },
    { date: '2026-03-25', day: '二' },
    { date: '2026-03-26', day: '三' },
    { date: '2026-03-27', day: '四' },
    { date: '2026-03-28', day: '五' },
    { date: '2026-03-29', day: '六' },
    { date: '2026-03-30', day: '日' }
  ]), [])

  const scheduleByDate = useMemo(() => {
    const map = {}
    myBookings.forEach((bk) => {
      if (!map[bk.date]) map[bk.date] = []
      map[bk.date].push(bk)
    })
    return map
  }, [myBookings])

  const todayClass = useMemo(() => {
    const list = scheduleByDate['2026-03-24'] || []
    return list[0] || null
  }, [scheduleByDate])

  const bookingSlots = useMemo(() => ([
    { id: 'SL-001', date: '2026-03-25', time: '14:00-15:00', type: '一对一', title: '吉他一对一', teacher: '刘老师', campus: '北环国基路校区', remain: 1, available: true, hoursCost: 1 },
    { id: 'SL-002', date: '2026-03-25', time: '16:00-17:00', type: '团课', title: '节奏强化团课', teacher: '系统推荐', campus: '北环国基路校区', remain: 3, available: true, hoursCost: 1 },
    { id: 'SL-003', date: '2026-03-25', time: '19:00-20:00', type: '体验课', title: '基础试听体验课', teacher: '刘老师', campus: '西大剧院校区', remain: 0, available: false, hoursCost: 0 },
    { id: 'SL-004', date: '2026-03-26', time: '15:00-16:00', type: '团课', title: '吉他基础团课', teacher: '陈老师', campus: '北环国基路校区', remain: 5, available: true, hoursCost: 1 },
    { id: 'SL-005', date: '2026-03-26', time: '20:00-21:00', type: '一对一', title: '进阶技巧一对一', teacher: '赵老师', campus: '北环国基路校区', remain: 1, available: true, hoursCost: 1 },
    { id: 'SL-006', date: '2026-03-28', time: '10:00-11:00', type: '体验课', title: '入门体验课', teacher: '刘老师', campus: '西大剧院校区', remain: 6, available: true, hoursCost: 0 }
  ]), [])

  const recommendedSlot = useMemo(() => bookingSlots.find((s) => s.id === 'SL-004'), [bookingSlots])

  const filteredSlots = useMemo(() => {
    return bookingSlots
      .filter((slot) => slot.date === bookingDate)
      .filter((slot) => (bookingType === '全部' ? true : slot.type === bookingType))
      .filter((slot) => (bookingCampus === '全部' ? true : slot.campus === bookingCampus))
      .filter((slot) => {
        if (slot.type !== '一对一') return true
        if (bookingTeacher === '不限') return true
        return slot.teacher === bookingTeacher
      })
  }, [bookingSlots, bookingCampus, bookingDate, bookingTeacher, bookingType])

  const handleTabChange = (key) => {
    navigate(`/student/${key}`)
  }

  const openBookingConfirm = (slot) => {
    setBookingDraft(slot)
    setBookingModalOpen(true)
  }

  const confirmBooking = () => {
    if (!bookingDraft) return
    if (bookingDraft.type === '一对一' && bookingTeacher === '不限') {
      showToast('一对一课程必须选择教师')
      return
    }
    if (bookingDraft.hoursCost > remainingHours) {
      showToast('剩余课时不足，请先购买课包')
      return
    }
    const newBooking = {
      id: `BK-${String(myBookings.length + 1).padStart(3, '0')}`,
      date: bookingDraft.date,
      time: bookingDraft.time,
      title: bookingDraft.type === '一对一' ? `${bookingDraft.title}（一对一）` : bookingDraft.title,
      teacher: bookingDraft.type === '一对一' ? (bookingTeacher === '不限' ? bookingDraft.teacher : bookingTeacher) : bookingDraft.teacher,
      campus: bookingDraft.campus,
      status: '未开始',
      hoursCost: bookingDraft.hoursCost
    }
    setMyBookings((prev) => [newBooking, ...prev])
    setRemainingHours((prev) => prev - bookingDraft.hoursCost)
    setBookingModalOpen(false)
    showToast('预约成功')
  }

  const submitHomework = () => {
    const hw = homeworkItems.find((x) => x.id === activeHomeworkId)
    if (!hw) return
    setHomeworkItems((prev) => prev.map((x) => (x.id === activeHomeworkId ? { ...x, status: '已提交' } : x)))
    setHomeworkSubmitOpen(false)
    setHomeworkStatus('已提交')
    showToast(`已提交作业（${homeworkSubmitType}）`)
  }

  const openAdjust = (type) => {
    setAdjustType(type)
    setAdjustReason('')
    setAdjustTargetId('')
    setAdjustSheetOpen(true)
  }

  const submitAdjust = () => {
    if (!todayClass) return
    if (!adjustReason.trim()) {
      showToast('请填写原因')
      return
    }
    if (adjustType === '取消') {
      const newCancel = {
        id: `CN-${String(cancelRows.length + 1).padStart(3, '0')}`,
        course: todayClass.title,
        datetime: `${todayClass.date} ${todayClass.time}`,
        reason: adjustReason.trim(),
        status: '已取消'
      }
      setCancelRows((prev) => [newCancel, ...prev])
      setMyBookings((prev) => prev.filter((x) => x.id !== todayClass.id))
      setAdjustSheetOpen(false)
      showToast('已提交取消')
      return
    }
    const target = bookingSlots.find((x) => x.id === adjustTargetId)
    const newApply = {
      id: `AP-${String(applyRows.length + 1).padStart(3, '0')}`,
      type: '调课',
      from: `${todayClass.date} ${todayClass.time}`,
      to: target ? `${target.date} ${target.time}` : '待确认',
      reason: adjustReason.trim(),
      status: '待审核'
    }
    setApplyRows((prev) => [newApply, ...prev])
    setAdjustSheetOpen(false)
    showToast('已提交调课申请')
  }

  const openWecomPurchasePage = (pkg) => {
    setPurchaseTargetPackage(pkg)
    setProfilePage('wecom-qr')
  }

  const renderHome = () => (
    <div className="flex-1 overflow-y-auto bg-[#f8f9fa] pb-24">
      <div className="bg-gradient-to-b from-[#ff9b54] to-[#f8f9fa] px-5 pb-8 pt-10">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 rounded-full bg-white/30 p-1">
              <div className="h-full w-full rounded-full bg-white flex items-center justify-center font-bold text-lg text-[#ff9b54]">{student.avatar}</div>
            </div>
            <div>
              <div className="text-lg font-bold text-[#1f1f1f]">{student.name}</div>
              <div className="text-xs text-[#1f1f1f]/70 mt-0.5 font-medium">{student.campus}</div>
            </div>
          </div>
        </div>
        <div className="mt-3 grid grid-cols-3 gap-2 rounded-xl bg-white/55 p-2 text-center text-[11px] text-[#1f1f1f]">
          <div>总课时 {studentHourSummary.total}</div>
          <div>已上课时 {studentHourSummary.attended}</div>
          <div>剩余课时 {studentHourSummary.remaining}</div>
        </div>
      </div>

      <div className="-mt-6 px-4 space-y-4">
        <div className="rounded-2xl bg-white p-3 shadow-sm flex items-center gap-3 border border-[#f0ebe3] cursor-pointer" onClick={() => { setLearningSubTab('作业'); setHomeworkStatus('未提交'); handleTabChange('learning') }}>
          <div className="h-8 w-8 rounded-full bg-[#fff4ea] flex items-center justify-center text-[#ff9b54]">
            <Bell size={16} />
          </div>
          <div className="flex-1 text-sm font-medium text-[#2b2b2b]">
            {homeworkItems.some((x) => x.status === '未提交') ? '您有作业未提交' : '今日没有待办作业'}
          </div>
          <button className="text-xs text-[#ff9b54] bg-[#fff4ea] px-3 py-1 rounded-full font-medium" onClick={(event) => { event.stopPropagation(); setLearningSubTab('作业'); setHomeworkStatus('未提交'); handleTabChange('learning') }}>去处理</button>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div className="rounded-2xl bg-white p-4 shadow-sm border border-[#f0ebe3]">
            <div className="text-xs text-[#7f7f88]">学员等级</div>
            <div className="mt-2 flex items-baseline gap-2">
              <span className="text-xl font-black text-[#2b2b2b]">{studentLevel.level}</span>
              <span className="text-xs font-medium text-[#ff9b54]">{studentLevel.title}</span>
            </div>
            <div className="mt-2 text-xs text-[#7f7f88]">{studentLevel.progress}</div>
          </div>
          <div className="rounded-2xl bg-white p-4 shadow-sm border border-[#f0ebe3]">
            <div className="text-xs text-[#7f7f88]">老师信息</div>
            <div className="mt-2 text-sm font-bold text-[#2b2b2b]">{teacherProfile.name}</div>
            <div className="mt-1 inline-flex rounded-full bg-[#fff4ea] px-2 py-0.5 text-[10px] font-bold text-[#ff9b54]">{teacherProfile.tag}</div>
            <div className="mt-2 line-clamp-2 text-xs text-[#7f7f88]">{teacherProfile.intro}</div>
          </div>
          <div className="rounded-2xl bg-white p-4 shadow-sm border border-[#f0ebe3]">
            <div className="text-xs text-[#7f7f88]">课程类型，课时包</div>
            <div className="mt-2 text-sm font-bold text-[#2b2b2b]">{coursePackageSummary.courseType}</div>
            <div className="mt-1 text-xs text-[#7f7f88]">当前课时包：{coursePackageSummary.packageName}</div>
          </div>
          <div className="rounded-2xl bg-white p-4 shadow-sm border border-[#f0ebe3]">
            <div className="text-xs text-[#7f7f88]">学员权益</div>
            <div className="mt-2 text-sm font-bold text-[#2b2b2b]">{studentBenefits.level}</div>
            <div className="mt-1 text-xs text-[#ff9b54]">权益豆：{studentBenefits.beans}</div>
            <div className="mt-2 text-[11px] text-[#7f7f88]">{studentBenefits.next}</div>
          </div>
        </div>

        <div className="rounded-2xl bg-white p-5 shadow-sm border border-[#f0ebe3]">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-[#2b2b2b]">今日课程</h3>
            <span className="text-xs text-[#ff9b54] bg-[#fff4ea] px-2 py-1 rounded font-medium">即将开始</span>
          </div>
          <div className="mb-5">
            <div className="text-xl font-bold text-[#2b2b2b]">{todayClass ? todayClass.time : '—'}</div>
            <div className="text-sm text-[#7f7f88] mt-1.5 font-medium">{todayClass ? `${todayClass.title} · ${todayClass.teacher}` : '今日暂无课程'}</div>
          </div>
          <div className="flex gap-3">
            <button
              className={`flex-1 py-2.5 rounded-xl text-sm font-medium ${todayClass ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#c4c4c4]'}`}
              onClick={() => {
                if (!todayClass) return
                setClassJoinOpen(true)
              }}
            >
              去上课
            </button>
            <button
              className={`px-5 py-2.5 rounded-xl text-sm font-medium ${todayClass ? 'bg-[#faf8f4] text-[#2b2b2b]' : 'bg-[#faf8f4] text-[#c4c4c4]'}`}
              onClick={() => {
                if (!todayClass) return
                openAdjust('调课')
              }}
            >
              调课/取消
            </button>
          </div>
        </div>

        <div className="rounded-2xl bg-white p-5 shadow-sm border border-[#f0ebe3]">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-[#2b2b2b]">本周课表</h3>
            <button className="text-xs text-[#7f7f88] flex items-center" onClick={() => setScheduleSheetOpen(true)}>查看全部 <ChevronRight size={14}/></button>
          </div>
          <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
            {scheduleDays.map((item) => {
              const dateShort = item.date.slice(8, 10)
              const hasClass = (scheduleByDate[item.date] || []).length > 0
              const active = selectedDate === item.date
              return (
                <div
                  key={item.date}
                  onClick={() => setSelectedDate(item.date)}
                  className={`flex-shrink-0 w-[46px] py-2.5 rounded-xl flex flex-col items-center gap-1.5 cursor-pointer transition ${active ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#2b2b2b]'}`}
                >
                <span className="text-xs opacity-80">{item.day}</span>
                <span className="text-sm font-bold">{dateShort}</span>
                <div className={`w-1 h-1 rounded-full ${hasClass ? (active ? 'bg-[#1f1f1f]' : 'bg-[#ff9b54]') : 'bg-transparent'}`} />
              </div>
              )
            })}
          </div>
          
          <div className="mt-4 p-4 bg-[#faf8f4] rounded-xl border border-[#f0ebe3]">
            {(scheduleByDate[selectedDate] || []).length === 0 && (
              <div className="text-sm text-[#7f7f88] text-center py-2">当日暂无课程</div>
            )}
            {(scheduleByDate[selectedDate] || []).slice(0, 2).map((bk) => (
              <div key={bk.id} className="rounded-xl bg-white p-3 border border-[#f0ebe3] mb-2 last:mb-0">
                <div className="text-sm font-bold text-[#2b2b2b]">{bk.time} {bk.title}</div>
                <div className="text-xs text-[#7f7f88] mt-1">{bk.teacher} · {bk.campus}</div>
                <div className="mt-2 flex gap-2">
                  <button className="flex-1 rounded-lg bg-[#fff4ea] px-3 py-2 text-xs font-bold text-[#ff9b54]" onClick={() => { setClassJoinOpen(true) }}>查看详情</button>
                  <button className="flex-1 rounded-lg bg-[#faf8f4] px-3 py-2 text-xs font-bold text-[#2b2b2b]" onClick={() => { openAdjust('调课') }}>调课/取消</button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-4 gap-3 bg-white rounded-2xl p-4 shadow-sm border border-[#f0ebe3]">
          {[
            { icon: CalendarDays, label: '预约课程', color: 'bg-blue-50 text-blue-500', onClick: () => handleTabChange('booking') },
            { icon: BookOpen, label: '查看作业', color: 'bg-green-50 text-green-500', onClick: () => { setLearningSubTab('作业'); setHomeworkStatus('未提交'); handleTabChange('learning') } },
            { icon: Clock, label: '课后反馈', color: 'bg-purple-50 text-purple-500', onClick: () => { setLearningSubTab('成长记录'); handleTabChange('learning') } },
            { icon: UserSquare2, label: '购买课包', color: 'bg-orange-50 text-orange-500', onClick: () => { setProfilePage('packages'); handleTabChange('profile') } }
          ].map((item, i) => (
            <button key={i} className="flex flex-col items-center gap-2" onClick={item.onClick}>
              <div className={`w-12 h-12 rounded-2xl flex items-center justify-center ${item.color}`}>
                <item.icon size={22} />
              </div>
              <span className="text-xs text-[#2b2b2b] font-medium">{item.label}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  )

  const renderBooking = () => (
    <div className="flex-1 flex flex-col bg-[#f8f9fa] pb-24 h-full">
      <div className="bg-white px-4 pt-10 pb-4 flex items-center justify-center relative border-b border-[#f0ebe3]">
        <h2 className="text-lg font-bold text-[#2b2b2b]">约课</h2>
      </div>
      
      <div className="bg-white px-4 py-3 flex gap-2 overflow-x-auto border-b border-[#f0ebe3] scrollbar-hide">
        {typeOptions.map((t) => (
          <button
            key={t}
            className={`flex-shrink-0 px-4 py-1.5 rounded-full text-sm font-medium border ${bookingType === t ? 'bg-[#fff4ea] text-[#ff9b54] border-[#ff9b54]' : 'bg-[#faf8f4] text-[#7f7f88] border-transparent'}`}
            onClick={() => setBookingType(t)}
          >
            {t === '全部' ? '全部课程' : t}
          </button>
        ))}
      </div>

      <div className="flex-1 overflow-y-auto px-4 py-5 space-y-6">
        <div className="grid grid-cols-2 gap-2">
          <select value={bookingCampus} onChange={(e) => setBookingCampus(e.target.value)} className="w-full rounded-xl border border-[#f0ebe3] bg-white px-3 py-2 text-sm outline-none">
            {campusOptions.map((c) => (
              <option key={c} value={c}>{c === '全部' ? '全部校区' : c}</option>
            ))}
          </select>
          <select value={bookingTeacher} onChange={(e) => setBookingTeacher(e.target.value)} className="w-full rounded-xl border border-[#f0ebe3] bg-white px-3 py-2 text-sm outline-none">
            {teacherOptions.map((t) => (
              <option key={t} value={t}>{t === '不限' ? '教师（可选）' : t}</option>
            ))}
          </select>
        </div>

        <div>
          <h3 className="font-bold text-[#2b2b2b] mb-3">智能推荐</h3>
          <div className="bg-white rounded-2xl p-5 shadow-sm border border-[#fff4ea] relative overflow-hidden">
            <div className="absolute top-0 right-0 bg-[#ff9b54] text-[#1f1f1f] text-[10px] px-3 py-1 rounded-bl-xl font-bold">优先推荐</div>
            <div className="flex gap-2 items-center mb-3 mt-1">
              <span className="text-xs text-[#ff9b54] bg-[#fff4ea] px-2 py-0.5 rounded font-medium">适合你的时间</span>
            </div>
            <div className="text-xl font-bold text-[#2b2b2b]">{recommendedSlot ? `明天 ${recommendedSlot.time}` : '—'}</div>
            <div className="text-sm text-[#7f7f88] mt-1.5 font-medium">{recommendedSlot ? `${recommendedSlot.title} · ${recommendedSlot.teacher}` : '—'}</div>
            <div className="mt-4 pt-4 border-t border-[#f0ebe3] flex items-center justify-between">
              <div className="text-xs text-[#7f7f88] font-medium">消耗 <span className="text-[#ff9b54] font-bold">{recommendedSlot ? recommendedSlot.hoursCost : 1}</span> 课时</div>
              <button
                className="bg-[#ff9b54] text-[#1f1f1f] px-5 py-2 rounded-xl text-sm font-bold"
                onClick={() => {
                  if (!recommendedSlot) return
                  openBookingConfirm(recommendedSlot)
                }}
              >
                一键预约
              </button>
            </div>
          </div>
        </div>

        <div>
          <h3 className="font-bold text-[#2b2b2b] mb-3">按日期选课</h3>
          <div className="flex gap-2 overflow-x-auto pb-3 scrollbar-hide mb-2">
            {[
              { date: '2026-03-24', day: '今日' },
              { date: '2026-03-25', day: '明日' },
              { date: '2026-03-26', day: '周三' },
              { date: '2026-03-27', day: '周四' },
              { date: '2026-03-28', day: '周五' },
            ].map((item, i) => (
              <div key={i} onClick={() => setBookingDate(item.date)} className={`flex-shrink-0 w-[72px] py-2.5 rounded-xl flex flex-col items-center gap-1.5 cursor-pointer transition ${bookingDate === item.date ? 'bg-[#2b2b2b] text-white' : 'bg-white text-[#2b2b2b] border border-[#f0ebe3]'}`}>
                <span className="text-xs opacity-80">{item.day}</span>
                <span className="text-sm font-bold">{item.date.slice(8, 10)}</span>
              </div>
            ))}
          </div>

          <div className="space-y-3">
            {filteredSlots.length === 0 && (
              <div className="rounded-2xl bg-white p-4 text-sm text-[#7f7f88] border border-[#f0ebe3]">暂无可预约课程，可切换筛选条件</div>
            )}
            {filteredSlots.map((slot) => {
              const needTeacher = slot.type === '一对一' && bookingTeacher === '不限'
              const canBook = slot.available && !needTeacher
              return (
                <div key={slot.id} className="bg-white rounded-2xl p-4 shadow-sm border border-[#f0ebe3] flex items-center justify-between">
                  <div>
                    <div className="text-base font-bold text-[#2b2b2b]">{slot.time}</div>
                    <div className="text-sm font-bold text-[#2b2b2b] mt-1.5">
                      {slot.title}
                      <span className="text-xs text-[#7f7f88] font-medium ml-1">· {slot.type === '一对一' ? slot.teacher : slot.teacher}</span>
                    </div>
                    <div className="text-xs text-[#7f7f88] mt-1">校区：{slot.campus} · 剩余名额：{slot.remain}</div>
                    {needTeacher && <div className="text-xs text-[#ff4d4f] mt-1">一对一需先选择教师</div>}
                    {!slot.available && <div className="text-xs text-[#7f7f88] mt-1">暂时满班</div>}
                  </div>
                  <button
                    className={`px-5 py-2 rounded-xl text-sm font-bold ${canBook ? 'bg-[#fff4ea] text-[#ff9b54]' : 'bg-[#faf8f4] text-[#c4c4c4]'}`}
                    onClick={() => {
                      if (!slot.available) {
                        showToast('当前时间段已满')
                        return
                      }
                      if (needTeacher) {
                        showToast('一对一课程必须选择教师')
                        return
                      }
                      openBookingConfirm(slot)
                    }}
                  >
                    {slot.available ? '预约' : '已满'}
                  </button>
                </div>
              )
            })}
          </div>
        </div>
      </div>

      {bookingModalOpen && (
        <div className="absolute inset-0 z-50 flex flex-col justify-end bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-t-3xl p-6 pb-12">
            <div className="flex items-center justify-between mb-4">
              <div className="w-8" />
              <h3 className="text-lg font-bold text-center text-[#2b2b2b]">确认预约</h3>
              <button className="w-8 h-8 flex items-center justify-center rounded-full bg-[#faf8f4]" onClick={() => setBookingModalOpen(false)}>
                <X size={16} className="text-[#7f7f88]" />
              </button>
            </div>
            <div className="bg-[#faf8f4] rounded-2xl p-5 space-y-4 mb-8 border border-[#f0ebe3]">
              <div className="flex justify-between"><span className="text-[#7f7f88] text-sm">课程类型</span><span className="font-bold text-[#2b2b2b] text-sm">{bookingDraft ? bookingDraft.type : '—'}</span></div>
              <div className="flex justify-between"><span className="text-[#7f7f88] text-sm">课程名称</span><span className="font-bold text-[#2b2b2b] text-sm">{bookingDraft ? bookingDraft.title : '—'}</span></div>
              <div className="flex justify-between"><span className="text-[#7f7f88] text-sm">上课教师</span><span className="font-bold text-[#2b2b2b] text-sm">{bookingDraft ? (bookingDraft.type === '一对一' ? (bookingTeacher === '不限' ? bookingDraft.teacher : bookingTeacher) : bookingDraft.teacher) : '—'}</span></div>
              <div className="flex justify-between"><span className="text-[#7f7f88] text-sm">上课校区</span><span className="font-bold text-[#2b2b2b] text-sm">{bookingDraft ? bookingDraft.campus : '—'}</span></div>
              <div className="flex justify-between"><span className="text-[#7f7f88] text-sm">上课时间</span><span className="font-bold text-[#2b2b2b] text-sm">{bookingDraft ? `${bookingDraft.date} ${bookingDraft.time}` : '—'}</span></div>
              <div className="border-t border-[#e9e2d8] my-2 pt-4 flex justify-between items-center"><span className="text-[#7f7f88] text-sm">消耗课时</span><span className="font-bold text-[#ff9b54] text-lg">{bookingDraft ? bookingDraft.hoursCost : 1} 课时</span></div>
            </div>
            <div className="flex gap-4">
              <button className="flex-1 py-3.5 rounded-2xl bg-[#faf8f4] text-[#2b2b2b] font-bold text-sm" onClick={() => setBookingModalOpen(false)}>取消</button>
              <button className="flex-1 py-3.5 rounded-2xl bg-[#ff9b54] text-[#1f1f1f] font-bold text-sm" onClick={confirmBooking}>确认预约</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )

  const renderLearning = () => (
    <div className="flex-1 flex flex-col bg-[#f8f9fa] pb-24 h-full">
      <div className="bg-white px-4 pt-10 pb-0 border-b border-[#f0ebe3]">
        <h2 className="text-lg font-bold text-[#2b2b2b] text-center mb-4">学习</h2>
        <div className="flex justify-around">
          {['作业', '成长记录', '等级(二期)'].map(tab => (
            <button key={tab} className={`pb-3 px-2 text-sm font-bold border-b-2 transition ${learningSubTab === tab ? 'border-[#ff9b54] text-[#ff9b54]' : 'border-transparent text-[#7f7f88]'}`} onClick={() => setLearningSubTab(tab)}>
              {tab}
            </button>
          ))}
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-4">
        {learningSubTab === '作业' && (
          <div className="space-y-5">
            <div className="flex gap-2">
              {['未提交', '已提交', '已批改'].map(status => (
                <button key={status} onClick={() => setHomeworkStatus(status)} className={`px-4 py-1.5 rounded-full text-xs font-medium transition ${homeworkStatus === status ? 'bg-[#2b2b2b] text-white' : 'bg-white text-[#7f7f88] border border-[#f0ebe3]'}`}>{status}</button>
              ))}
            </div>
            
            {homeworkItems.filter((x) => x.status === homeworkStatus).length === 0 && (
              <div className="text-center text-sm text-[#7f7f88] py-10">暂无记录</div>
            )}
            {homeworkItems.filter((x) => x.status === homeworkStatus).map((hw) => (
              <div key={hw.id} className="bg-white rounded-2xl p-5 shadow-sm border border-[#f0ebe3]">
                <div className="flex justify-between items-start mb-3">
                  <div className="font-bold text-base text-[#2b2b2b]">{hw.title}</div>
                  <span className={`text-[10px] px-2 py-1 rounded font-bold ${
                    hw.status === '未提交'
                      ? 'text-[#ff4d4f] bg-red-50'
                      : hw.status === '已批改'
                        ? 'text-green-600 bg-green-50'
                        : 'text-[#7f7f88] bg-[#faf8f4]'
                  }`}>{hw.status}</span>
                </div>
                <div className="text-sm text-[#7f7f88] mb-4 space-y-1">
                  <div>所属课程：{hw.course}</div>
                  <div>截止时间：{hw.deadline}</div>
                </div>
                {hw.status === '未提交' && (
                  <div className="flex gap-3">
                    <button
                      className="flex-1 bg-[#fff4ea] text-[#ff9b54] py-2.5 rounded-xl text-sm font-bold"
                      onClick={() => {
                        setActiveHomeworkId(hw.id)
                        setHomeworkSubmitType('图片')
                        setHomeworkSubmitOpen(true)
                      }}
                    >
                      提交图片
                    </button>
                    <button
                      className="flex-1 bg-[#fff4ea] text-[#ff9b54] py-2.5 rounded-xl text-sm font-bold"
                      onClick={() => {
                        setActiveHomeworkId(hw.id)
                        setHomeworkSubmitType('音视频')
                        setHomeworkSubmitOpen(true)
                      }}
                    >
                      提交音视频
                    </button>
                  </div>
                )}
                {hw.status === '已批改' && (
                  <div className="bg-[#faf8f4] p-4 rounded-xl text-sm text-[#2b2b2b] border border-[#f0ebe3]">
                    <div className="font-bold mb-2">老师评语</div>
                    <div className="text-[#7f7f88] leading-relaxed">{hw.review}</div>
                    <div className="mt-3 flex gap-2">
                      <button className="flex-1 rounded-xl bg-white border border-[#f0ebe3] px-3 py-2 text-xs font-bold text-[#2b2b2b]" onClick={() => showToast('已复制老师点评摘要')}>复制摘要</button>
                      <button className="flex-1 rounded-xl bg-[#fff4ea] px-3 py-2 text-xs font-bold text-[#ff9b54]" onClick={() => showToast('已发送到邮箱：li***@mail.com')}>邮箱接收点评</button>
                    </div>
                  </div>
                )}
                {hw.status === '已提交' && (
                  <button className="w-full rounded-xl bg-[#faf8f4] px-3 py-2 text-sm font-bold text-[#2b2b2b]" onClick={() => showToast('老师批改中，请稍后查看')}>查看提交记录</button>
                )}
              </div>
            ))}
          </div>
        )}

        {learningSubTab === '成长记录' && (
          <div className="py-4 px-2 space-y-0 relative before:absolute before:inset-0 before:ml-6 before:-translate-x-px before:h-full before:w-[2px] before:bg-[#f0ebe3]">
            {[
              { date: '2026-03-20', title: '吉他进阶课', content: '学习了扫弦的进阶节奏型', evaluation: '掌握得很好，反应快', suggest: '每天练习15分钟' },
              { date: '2026-03-15', title: '吉他基础课', content: '复习了基础和弦', evaluation: '和弦转换流畅度提升', suggest: '加强F和弦练习' }
            ].map((item, i) => (
              <div key={i} className="relative flex items-start mb-8 last:mb-0">
                <div className="flex items-center justify-center w-8 h-8 rounded-full border-4 border-[#f8f9fa] bg-[#ff9b54] text-[#1f1f1f] shadow-sm shrink-0 z-10">
                  <Clock size={14} />
                </div>
                <div className="ml-4 bg-white p-4 rounded-2xl shadow-sm border border-[#f0ebe3] flex-1">
                  <div className="flex items-center justify-between mb-3">
                    <h4 className="font-bold text-[#2b2b2b]">{item.title}</h4>
                    <span className="text-xs text-[#7f7f88] font-medium">{item.date}</span>
                  </div>
                  <div className="text-sm space-y-2.5">
                    <div className="flex gap-2"><span className="text-[#7f7f88] shrink-0">教学内容：</span><span className="text-[#2b2b2b]">{item.content}</span></div>
                    <div className="flex gap-2"><span className="text-[#7f7f88] shrink-0">教师评价：</span><span className="text-[#2b2b2b]">{item.evaluation}</span></div>
                    <div className="flex gap-2"><span className="text-[#7f7f88] shrink-0">学习建议：</span><span className="text-[#2b2b2b]">{item.suggest}</span></div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {learningSubTab === '等级' && (
          <div className="bg-white rounded-2xl p-6 shadow-sm border border-[#f0ebe3] text-center mt-2">
            <div className="w-20 h-20 bg-gradient-to-br from-[#ff9b54] to-[#ffb37a] rounded-full mx-auto flex items-center justify-center text-[#1f1f1f] text-2xl font-black mb-4 shadow-md">
              Lv.2
            </div>
            <h3 className="font-black text-xl mb-1 text-[#2b2b2b]">初级弹唱达人</h3>
            <p className="text-xs text-[#7f7f88] font-medium mb-6">已击败 78% 的同阶段学员</p>
            
            <div className="flex flex-wrap justify-center gap-2 mb-6">
              <span className="bg-blue-50 text-blue-600 px-3 py-1 rounded-full text-xs font-bold border border-blue-100">节奏感强</span>
              <span className="bg-purple-50 text-purple-600 px-3 py-1 rounded-full text-xs font-bold border border-purple-100">勤奋练习</span>
              <span className="bg-green-50 text-green-600 px-3 py-1 rounded-full text-xs font-bold border border-green-100">乐感优秀</span>
            </div>

            <div className="bg-[#faf8f4] rounded-xl p-5 text-left border border-[#f0ebe3]">
              <h4 className="font-bold text-sm mb-2 text-[#2b2b2b]">学习轨迹</h4>
              <p className="text-xs text-[#7f7f88] leading-relaxed">
                本阶段共完成 12 节课，提交作业 8 次。主要掌握了 C大调、G大调常用和弦，以及 4/4 拍基础扫弦节奏。能够独立完成《滴答》、《晴天》等曲目的弹唱。
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  )

  const renderProfile = () => (
    <div className="flex-1 overflow-y-auto bg-[#f8f9fa] pb-24 h-full">
      {profilePage !== 'home' && (
        <div className="bg-white px-4 pt-10 pb-3 flex items-center justify-between border-b border-[#f0ebe3]">
          <button className="w-10 h-10 flex items-center justify-center rounded-xl bg-[#faf8f4]" onClick={() => setProfilePage('home')}>
            <ChevronLeft size={18} className="text-[#2b2b2b]" />
          </button>
          <div className="text-base font-bold text-[#2b2b2b]">
            {profilePage === 'packages'
              ? '我的课包'
              : profilePage === 'bookings'
                  ? '我的预约'
                  : profilePage === 'applies'
                      ? '调课申请'
                      : profilePage === 'cancels'
                          ? '取消记录'
                          : profilePage === 'notices'
                              ? '通知中心'
                              : profilePage === 'info'
                                  ? '个人信息'
                                  : profilePage === 'privacy'
                                      ? '隐私设置'
                                      : '添加企微购买课包'}
          </div>
          <div className="w-10" />
        </div>
      )}

      {profilePage === 'home' && (
        <div className="bg-white px-5 pt-12 pb-6 flex items-center gap-4 border-b border-[#f0ebe3]">
          <div className="h-16 w-16 rounded-full bg-[#faf8f4] border-2 border-[#f0ebe3] flex items-center justify-center">
            <span className="text-2xl font-bold text-[#ff9b54]">{student.avatar}</span>
          </div>
          <div>
            <h2 className="text-xl font-bold text-[#2b2b2b]">{student.name}</h2>
            <div className="flex gap-2 mt-2">
              <span className="bg-[#faf8f4] text-[#7f7f88] text-[10px] px-2 py-0.5 rounded font-medium border border-[#f0ebe3]">{student.track}</span>
              <span className="bg-[#faf8f4] text-[#7f7f88] text-[10px] px-2 py-0.5 rounded font-medium border border-[#f0ebe3]">{student.phone}</span>
            </div>
          </div>
        </div>
      )}

      {profilePage === 'home' && (
        <div className="px-4 mt-5 space-y-5">
          <div className="bg-[#2b2b2b] rounded-2xl p-5 text-white shadow-md relative overflow-hidden">
            <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -translate-y-10 translate-x-10"></div>
            <div className="flex justify-between items-start relative z-10">
              <div>
                <div className="text-xs text-white/70 mb-1 font-medium">当前课包：{currentPackage.name}</div>
                <div className="text-4xl font-black mt-2">{remainingHours} <span className="text-sm font-medium text-white/70">课时</span></div>
                <div className="mt-3 text-xs text-white/70">总课时 {studentHourSummary.total} / 已上课时 {studentHourSummary.attended} / 剩余课时 {studentHourSummary.remaining}</div>
              </div>
              <button className="bg-gradient-to-r from-[#ff9b54] to-[#ffb37a] text-[#1f1f1f] px-4 py-2 rounded-xl text-sm font-bold shadow-sm" onClick={() => setProfilePage('packages')}>
                购买课包(二期)
              </button>
            </div>
            <div className="mt-5 text-xs text-white/50 relative z-10 flex justify-between font-medium">
              <span>有效期至：{currentPackage.validUntil}</span>
              <span>支持到店/小程序购买</span>
            </div>
          </div>

          <div className="bg-white rounded-2xl p-5 shadow-sm border border-[#f0ebe3]">
            <h3 className="font-bold text-[#2b2b2b] mb-4 text-sm">我的服务</h3>
            <div className="grid grid-cols-3 gap-4 text-center">
              <button className="flex flex-col items-center gap-2" onClick={() => setProfilePage('bookings')}>
                <div className="w-12 h-12 rounded-full bg-[#faf8f4] flex items-center justify-center text-[#2b2b2b] border border-[#f0ebe3]"><CalendarDays size={20}/></div>
                <span className="text-xs font-medium text-[#2b2b2b]">我的预约</span>
              </button>
              <button className="flex flex-col items-center gap-2" onClick={() => setProfilePage('applies')}>
                <div className="w-12 h-12 rounded-full bg-[#faf8f4] flex items-center justify-center text-[#2b2b2b] border border-[#f0ebe3]"><Clock size={20}/></div>
                <span className="text-xs font-medium text-[#2b2b2b]">调课申请</span>
              </button>
              <button className="flex flex-col items-center gap-2" onClick={() => setProfilePage('cancels')}>
                <div className="w-12 h-12 rounded-full bg-[#faf8f4] flex items-center justify-center text-[#2b2b2b] border border-[#f0ebe3]"><BookOpen size={20}/></div>
                <span className="text-xs font-medium text-[#2b2b2b]">取消记录</span>
              </button>
            </div>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-[#f0ebe3] overflow-hidden">
            {[
              { key: 'notices', icon: Bell, label: '通知中心', extra: unreadNoticeCount > 0 ? `${unreadNoticeCount}条未读` : '' },
              { key: 'info', icon: UserSquare2, label: '个人信息', extra: '' },
              { key: 'privacy', icon: Clock, label: '隐私设置', extra: '' }
            ].map((item, i) => (
              <button key={i} className="w-full flex items-center justify-between p-4 border-b border-[#f0ebe3] last:border-0 hover:bg-gray-50 transition" onClick={() => setProfilePage(item.key)}>
                <div className="flex items-center gap-3">
                  <item.icon size={18} className="text-[#7f7f88]" />
                  <span className="text-sm font-bold text-[#2b2b2b]">{item.label}</span>
                </div>
                <div className="flex items-center gap-2">
                  {item.extra && <span className="text-[10px] font-bold text-white bg-[#ff4d4f] px-2 py-0.5 rounded-full">{item.extra}</span>}
                  <ChevronRight size={16} className="text-[#c4c4c4]" />
                </div>
              </button>
            ))}
          </div>
        </div>
      )}

      {profilePage === 'packages' && (
        <div className="p-4 space-y-4">
          <div className="rounded-2xl bg-white border border-[#f0ebe3] p-4">
            <div className="text-sm font-bold text-[#2b2b2b]">当前课包</div>
            <div className="mt-2 text-sm text-[#7f7f88]">{currentPackage.name} · 有效期至 {currentPackage.validUntil}</div>
            <div className="mt-2 text-sm text-[#7f7f88]">总课时：<span className="font-bold text-[#2b2b2b]">{studentHourSummary.total}</span> · 已上课时：<span className="font-bold text-[#2b2b2b]">{studentHourSummary.attended}</span> · 剩余课时：<span className="font-bold text-[#2b2b2b]">{studentHourSummary.remaining}</span></div>
          </div>
          {packageCatalog.map((pkg) => (
            <div key={pkg.id} className="rounded-2xl bg-white border border-[#f0ebe3] p-4">
              <div className="flex items-start justify-between">
                <div>
                  <div className="text-base font-bold text-[#2b2b2b]">{pkg.name}</div>
                  <div className="mt-1 text-xs text-[#7f7f88]">{pkg.hours} 课时 · ¥{pkg.price}</div>
                </div>
                <span className="text-[10px] font-bold text-[#ff9b54] bg-[#fff4ea] px-2 py-1 rounded-full">{pkg.tag}</span>
              </div>
              <div className="mt-3 flex gap-2">
                <button className="flex-1 rounded-xl bg-[#faf8f4] px-3 py-2 text-sm font-bold text-[#2b2b2b]" onClick={() => showToast('已查看课包权益说明')}>查看权益</button>
                <button className="flex-1 rounded-xl bg-[#ff9b54] px-3 py-2 text-sm font-bold text-[#1f1f1f]" onClick={() => openWecomPurchasePage(pkg)}>立即购买</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {profilePage === 'wecom-qr' && (
        <div className="p-4 space-y-4">
          <div className="rounded-2xl bg-white border border-[#f0ebe3] p-4">
            <div className="text-base font-bold text-[#2b2b2b]">添加企微购买课包</div>
            <div className="mt-2 text-sm text-[#7f7f88]">请扫码添加课程顾问企微，发送“购买课包”即可完成下单。</div>
            {purchaseTargetPackage && (
              <div className="mt-3 rounded-xl bg-[#faf8f4] px-3 py-2 text-xs text-[#2b2b2b]">
                目标课包：{purchaseTargetPackage.name}（{purchaseTargetPackage.hours}课时 / ¥{purchaseTargetPackage.price}）
              </div>
            )}
          </div>
          <div className="rounded-2xl bg-white border border-[#f0ebe3] p-6">
            <div className="mx-auto w-fit">
              <QrPreview value={`student-buy-package-${purchaseTargetPackage?.id || 'default'}`} />
            </div>
            <div className="mt-4 space-y-1 text-center text-xs text-[#7f7f88]">
              <div>扫码后备注：{student.name} / {student.phone}</div>
              <div>客服将引导完成支付并发放课时</div>
            </div>
            <button
              className="mt-4 w-full rounded-xl bg-[#ff9b54] px-3 py-2 text-sm font-bold text-[#1f1f1f]"
              onClick={() => {
                if (!purchaseTargetPackage) {
                  showToast('已通知课程顾问')
                  return
                }
                setCurrentPackage({ name: purchaseTargetPackage.name, validUntil: '2027-03-31' })
                setTotalHours((prev) => prev + purchaseTargetPackage.hours)
                setRemainingHours((prev) => prev + purchaseTargetPackage.hours)
                showToast(`已完成购买：${purchaseTargetPackage.name}`)
              }}
            >
              我已添加企微，购买完成后课时将自动添加
            </button>
          </div>
        </div>
      )}

      {profilePage === 'bookings' && (
        <div className="p-4 space-y-3">
          {myBookings.length === 0 && <div className="rounded-2xl bg-white p-4 text-sm text-[#7f7f88] border border-[#f0ebe3]">暂无预约</div>}
          {myBookings.map((bk) => (
            <div key={bk.id} className="rounded-2xl bg-white border border-[#f0ebe3] p-4">
              <div className="flex items-start justify-between">
                <div>
                  <div className="text-sm text-[#7f7f88]">{bk.date} {bk.time}</div>
                  <div className="mt-1 text-base font-bold text-[#2b2b2b]">{bk.title}</div>
                  <div className="mt-1 text-xs text-[#7f7f88]">{bk.teacher} · {bk.campus}</div>
                </div>
                <span className="text-[10px] font-bold text-[#7f7f88] bg-[#faf8f4] px-2 py-1 rounded-full">{bk.status}</span>
              </div>
              <div className="mt-3 flex gap-2">
                <button className="flex-1 rounded-xl bg-[#faf8f4] px-3 py-2 text-sm font-bold text-[#2b2b2b]" onClick={() => { setSelectedDate(bk.date); showToast('已定位到课表日期') }}>定位到课表</button>
                <button className="flex-1 rounded-xl bg-[#fff4ea] px-3 py-2 text-sm font-bold text-[#ff9b54]" onClick={() => { showToast('已发起调课申请（示例）') }}>申请调课</button>
                <button className="flex-1 rounded-xl bg-red-50 px-3 py-2 text-sm font-bold text-red-600" onClick={() => { setMyBookings((prev) => prev.filter((x) => x.id !== bk.id)); setCancelRows((prev) => [{ id: `CN-${String(prev.length + 1).padStart(3, '0')}`, course: bk.title, datetime: `${bk.date} ${bk.time}`, reason: '临时有事', status: '已取消' }, ...prev]); showToast('已取消预约') }}>取消</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {profilePage === 'applies' && (
        <div className="p-4 space-y-3">
          {applyRows.length === 0 && <div className="rounded-2xl bg-white p-4 text-sm text-[#7f7f88] border border-[#f0ebe3]">暂无申请</div>}
          {applyRows.map((row) => (
            <div key={row.id} className="rounded-2xl bg-white border border-[#f0ebe3] p-4">
              <div className="flex items-start justify-between">
                <div>
                  <div className="text-sm font-bold text-[#2b2b2b]">{row.type}申请</div>
                  <div className="mt-1 text-xs text-[#7f7f88]">原课程：{row.from}</div>
                  <div className="mt-1 text-xs text-[#7f7f88]">目标课程：{row.to}</div>
                  <div className="mt-2 text-xs text-[#7f7f88]">原因：{row.reason}</div>
                </div>
                <span className={`text-[10px] font-bold px-2 py-1 rounded-full ${row.status === '已通过' ? 'bg-green-50 text-green-600' : row.status === '已拒绝' ? 'bg-red-50 text-red-600' : 'bg-[#faf8f4] text-[#7f7f88]'}`}>{row.status}</span>
              </div>
              <button className="mt-3 w-full rounded-xl bg-[#faf8f4] px-3 py-2 text-sm font-bold text-[#2b2b2b]" onClick={() => showToast('已打开申请详情（示例）')}>查看详情</button>
            </div>
          ))}
        </div>
      )}

      {profilePage === 'cancels' && (
        <div className="p-4 space-y-3">
          {cancelRows.length === 0 && <div className="rounded-2xl bg-white p-4 text-sm text-[#7f7f88] border border-[#f0ebe3]">暂无取消记录</div>}
          {cancelRows.map((row) => (
            <div key={row.id} className="rounded-2xl bg-white border border-[#f0ebe3] p-4">
              <div className="text-sm font-bold text-[#2b2b2b]">{row.course}</div>
              <div className="mt-1 text-xs text-[#7f7f88]">{row.datetime}</div>
              <div className="mt-2 text-xs text-[#7f7f88]">原因：{row.reason}</div>
              <div className="mt-3 flex justify-between items-center">
                <span className="text-[10px] font-bold bg-[#faf8f4] text-[#7f7f88] px-2 py-1 rounded-full">{row.status}</span>
                <button className="text-xs font-bold text-[#ff9b54]" onClick={() => { handleTabChange('booking'); showToast('已跳转约课页面') }}>去约新课</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {profilePage === 'notices' && (
        <div className="p-4 space-y-3">
          {[
            { id: 'NT-001', title: '课程提醒', content: '明天 16:00 有一节团课，请提前到校', read: false },
            { id: 'NT-002', title: '作业提醒', content: '和弦转换练习作业即将到期', read: false },
            { id: 'NT-003', title: '系统通知', content: '本周末校区营业时间调整', read: true }
          ].map((nt) => (
            <div key={nt.id} className={`rounded-2xl border p-4 ${nt.read ? 'bg-white border-[#f0ebe3]' : 'bg-[#fff4ea] border-[#ffd8bf]'}`}>
              <div className="flex items-center justify-between">
                <div className="text-sm font-bold text-[#2b2b2b]">{nt.title}</div>
                {!nt.read && <span className="text-[10px] font-bold bg-[#ff4d4f] text-white px-2 py-1 rounded-full">未读</span>}
              </div>
              <div className="mt-2 text-xs text-[#7f7f88] leading-relaxed">{nt.content}</div>
              <button
                className="mt-3 w-full rounded-xl bg-white border border-[#f0ebe3] px-3 py-2 text-sm font-bold text-[#2b2b2b]"
                onClick={() => {
                  if (!nt.read && unreadNoticeCount > 0) setUnreadNoticeCount((x) => Math.max(0, x - 1))
                  showToast('已标记已读（示例）')
                }}
              >
                标记已读
              </button>
            </div>
          ))}
        </div>
      )}

      {profilePage === 'info' && (
        <div className="p-4 space-y-3">
          {[
            { label: '姓名', value: student.name },
            { label: '手机号', value: student.phone },
            { label: '学习方向', value: student.track },
            { label: '当前校区', value: student.campus },
            { label: '总课时', value: `${studentHourSummary.total}` },
            { label: '已上课时', value: `${studentHourSummary.attended}` },
            { label: '剩余课时', value: `${studentHourSummary.remaining}` }
          ].map((row) => (
            <div key={row.label} className="rounded-2xl bg-white border border-[#f0ebe3] p-4 flex items-center justify-between">
              <div className="text-sm text-[#7f7f88]">{row.label}</div>
              <div className="text-sm font-bold text-[#2b2b2b]">{row.value}</div>
            </div>
          ))}
          <button className="w-full rounded-2xl bg-[#faf8f4] px-3 py-3 text-sm font-bold text-[#2b2b2b]" onClick={() => showToast('已发起手机号绑定流程（示例）')}>手机号绑定</button>
          <button className="w-full rounded-2xl bg-[#faf8f4] px-3 py-3 text-sm font-bold text-[#2b2b2b]" onClick={() => showToast('已保存邮箱（示例）')}>设置邮箱（用于接收点评）</button>
        </div>
      )}

      {profilePage === 'privacy' && (
        <div className="p-4 space-y-3">
          {[
            { label: '允许课程提醒', desc: '课前1天 + 6小时提醒', on: true },
            { label: '允许作业提醒', desc: '截止前12小时提醒', on: true },
            { label: '剩余课时提醒', desc: '低于10课时提醒', on: true }
          ].map((row) => (
            <button key={row.label} className="w-full rounded-2xl bg-white border border-[#f0ebe3] p-4 flex items-center justify-between" onClick={() => showToast('已切换设置（示例）')}>
              <div className="text-left">
                <div className="text-sm font-bold text-[#2b2b2b]">{row.label}</div>
                <div className="text-xs text-[#7f7f88] mt-1">{row.desc}</div>
              </div>
              <div className={`h-6 w-11 rounded-full p-1 ${row.on ? 'bg-[#ff9b54]' : 'bg-[#e9e2d8]'}`}>
                <div className={`h-4 w-4 rounded-full bg-white transition ${row.on ? 'translate-x-5' : ''}`} />
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  )

  const tabs = [
    { key: 'home', label: '首页', icon: Home },
    { key: 'booking', label: '约课', icon: CalendarDays },
    { key: 'learning', label: '学习', icon: BookOpen },
    { key: 'profile', label: '我的', icon: UserSquare2 }
  ]

  return (
    <div className="flex items-center justify-center w-full h-full bg-[#e9e2d8] p-4 rounded-2xl">
      <div className="relative w-full max-w-[390px] h-[844px] bg-white rounded-[40px] shadow-2xl overflow-hidden border-[8px] border-[#2b2b2b] flex flex-col">
        <div className="absolute top-0 w-full h-7 bg-transparent z-50 flex justify-between items-center px-6 pointer-events-none">
          <div className="text-[11px] font-bold text-black/80">9:41</div>
          <div className="flex gap-1.5 items-center">
            <div className="w-4 h-3 bg-black/80 rounded-sm"></div>
            <div className="w-3 h-3 bg-black/80 rounded-full"></div>
            <div className="w-5 h-3 border border-black/80 rounded-sm"></div>
          </div>
        </div>

        {activeTab === 'home' && renderHome()}
        {activeTab === 'booking' && renderBooking()}
        {activeTab === 'learning' && renderLearning()}
        {activeTab === 'profile' && renderProfile()}

        <div className="absolute bottom-0 w-full bg-white border-t border-[#f0ebe3] pb-6 pt-2 px-6 flex justify-between items-center z-40">
          {tabs.map((tab) => {
            const isActive = activeTab === tab.key
            return (
              <button 
                key={tab.key} 
                onClick={() => handleTabChange(tab.key)}
                className="flex flex-col items-center gap-1.5 p-2"
              >
                <tab.icon size={22} className={`transition-colors ${isActive ? 'text-[#ff9b54]' : 'text-[#c4c4c4]'}`} />
                <span className={`text-[10px] font-bold transition-colors ${isActive ? 'text-[#2b2b2b]' : 'text-[#c4c4c4]'}`}>{tab.label}</span>
              </button>
            )
          })}
        </div>

        {toast && (
          <div className="absolute top-12 left-1/2 z-50 -translate-x-1/2 rounded-full bg-black/80 px-4 py-2 text-xs font-medium text-white">
            {toast}
          </div>
        )}

        {scheduleSheetOpen && (
          <div className="absolute inset-0 z-50 flex flex-col justify-end bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-t-3xl p-6 pb-10 max-h-[80%] overflow-y-auto">
              <div className="flex items-center justify-between mb-4">
                <div className="w-8" />
                <div className="text-lg font-bold text-[#2b2b2b]">全部课表</div>
                <button className="w-8 h-8 flex items-center justify-center rounded-full bg-[#faf8f4]" onClick={() => setScheduleSheetOpen(false)}>
                  <X size={16} className="text-[#7f7f88]" />
                </button>
              </div>
              <div className="space-y-3">
                {scheduleDays.map((d) => (
                  <div key={d.date} className="rounded-2xl border border-[#f0ebe3] bg-white p-4">
                    <div className="flex items-center justify-between">
                      <div className="text-sm font-bold text-[#2b2b2b]">{d.date}（周{d.day}）</div>
                      <button className="text-xs font-bold text-[#ff9b54]" onClick={() => { setSelectedDate(d.date); setScheduleSheetOpen(false); showToast('已切换日期') }}>查看</button>
                    </div>
                    {(scheduleByDate[d.date] || []).length === 0 && (
                      <div className="mt-2 text-xs text-[#7f7f88]">无课</div>
                    )}
                    {(scheduleByDate[d.date] || []).slice(0, 3).map((bk) => (
                      <div key={bk.id} className="mt-2 rounded-xl bg-[#faf8f4] p-3 border border-[#f0ebe3]">
                        <div className="text-sm font-bold text-[#2b2b2b]">{bk.time} {bk.title}</div>
                        <div className="mt-1 text-xs text-[#7f7f88]">{bk.teacher} · {bk.campus}</div>
                      </div>
                    ))}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {classJoinOpen && (
          <div className="absolute inset-0 z-50 flex flex-col justify-end bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-t-3xl p-6 pb-12">
              <div className="flex items-center justify-between mb-4">
                <div className="w-8" />
                <div className="text-lg font-bold text-[#2b2b2b]">课程详情</div>
                <button className="w-8 h-8 flex items-center justify-center rounded-full bg-[#faf8f4]" onClick={() => setClassJoinOpen(false)}>
                  <X size={16} className="text-[#7f7f88]" />
                </button>
              </div>
              <div className="rounded-2xl bg-[#faf8f4] border border-[#f0ebe3] p-5 space-y-3">
                <div className="flex justify-between"><span className="text-sm text-[#7f7f88]">课程</span><span className="text-sm font-bold text-[#2b2b2b]">{todayClass ? todayClass.title : '—'}</span></div>
                <div className="flex justify-between"><span className="text-sm text-[#7f7f88]">教师</span><span className="text-sm font-bold text-[#2b2b2b]">{todayClass ? todayClass.teacher : '—'}</span></div>
                <div className="flex justify-between"><span className="text-sm text-[#7f7f88]">校区</span><span className="text-sm font-bold text-[#2b2b2b]">{todayClass ? todayClass.campus : '—'}</span></div>
                <div className="flex justify-between"><span className="text-sm text-[#7f7f88]">时间</span><span className="text-sm font-bold text-[#2b2b2b]">{todayClass ? `${todayClass.date} ${todayClass.time}` : '—'}</span></div>
              </div>
              <div className="mt-5 grid grid-cols-2 gap-3">
                <button className="rounded-2xl bg-[#faf8f4] px-3 py-3 text-sm font-bold text-[#2b2b2b]" onClick={() => showToast('已复制上课链接（示例）')}>复制上课链接</button>
                <button className="rounded-2xl bg-[#ff9b54] px-3 py-3 text-sm font-bold text-[#1f1f1f]" onClick={() => { setClassJoinOpen(false); showToast('已进入上课（示例）') }}>去上课</button>
              </div>
              <button className="mt-3 w-full rounded-2xl bg-white border border-[#f0ebe3] px-3 py-3 text-sm font-bold text-[#2b2b2b]" onClick={() => { setClassJoinOpen(false); openAdjust('调课') }}>申请调课/取消</button>
            </div>
          </div>
        )}

        {adjustSheetOpen && (
          <div className="absolute inset-0 z-50 flex flex-col justify-end bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-t-3xl p-6 pb-12">
              <div className="flex items-center justify-between mb-4">
                <div className="w-8" />
                <div className="text-lg font-bold text-[#2b2b2b]">申请{adjustType}</div>
                <button className="w-8 h-8 flex items-center justify-center rounded-full bg-[#faf8f4]" onClick={() => setAdjustSheetOpen(false)}>
                  <X size={16} className="text-[#7f7f88]" />
                </button>
              </div>
              <div className="flex gap-2 mb-4">
                {['调课', '取消'].map((t) => (
                  <button key={t} className={`flex-1 rounded-full px-3 py-2 text-sm font-bold ${adjustType === t ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#2b2b2b]'}`} onClick={() => setAdjustType(t)}>
                    {t}
                  </button>
                ))}
              </div>
              {adjustType === '调课' && (
                <div className="rounded-2xl border border-[#f0ebe3] bg-white p-4 mb-4">
                  <div className="text-sm font-bold text-[#2b2b2b] mb-2">选择目标课程（示例）</div>
                  <select value={adjustTargetId} onChange={(e) => setAdjustTargetId(e.target.value)} className="w-full rounded-xl border border-[#f0ebe3] bg-white px-3 py-2 text-sm outline-none">
                    <option value="">请选择一个可调课课程</option>
                    {bookingSlots.filter((x) => x.available).slice(0, 5).map((x) => (
                      <option key={x.id} value={x.id}>{x.date} {x.time} · {x.title}</option>
                    ))}
                  </select>
                </div>
              )}
              <textarea value={adjustReason} onChange={(e) => setAdjustReason(e.target.value)} className="h-24 w-full rounded-2xl border border-[#f0ebe3] bg-white px-4 py-3 text-sm outline-none" placeholder="填写原因，示例：与考试时间冲突" />
              <div className="mt-4 flex gap-3">
                <button className="flex-1 rounded-2xl bg-[#faf8f4] px-3 py-3 text-sm font-bold text-[#2b2b2b]" onClick={() => setAdjustSheetOpen(false)}>取消</button>
                <button className="flex-1 rounded-2xl bg-[#ff9b54] px-3 py-3 text-sm font-bold text-[#1f1f1f]" onClick={submitAdjust}>提交</button>
              </div>
              <button className="mt-3 w-full rounded-2xl bg-white border border-[#f0ebe3] px-3 py-3 text-sm font-bold text-[#2b2b2b]" onClick={() => { setAdjustSheetOpen(false); setProfilePage('applies'); handleTabChange('profile'); showToast('已打开申请列表') }}>查看我的申请</button>
            </div>
          </div>
        )}

        {homeworkSubmitOpen && (
          <div className="absolute inset-0 z-50 flex flex-col justify-end bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-t-3xl p-6 pb-12">
              <div className="flex items-center justify-between mb-4">
                <div className="w-8" />
                <div className="text-lg font-bold text-[#2b2b2b]">提交作业</div>
                <button className="w-8 h-8 flex items-center justify-center rounded-full bg-[#faf8f4]" onClick={() => setHomeworkSubmitOpen(false)}>
                  <X size={16} className="text-[#7f7f88]" />
                </button>
              </div>
              <div className="rounded-2xl bg-[#faf8f4] border border-[#f0ebe3] p-4">
                <div className="text-sm font-bold text-[#2b2b2b]">类型：{homeworkSubmitType}</div>
                <div className="mt-2 text-xs text-[#7f7f88]">这里用Mock模拟上传流程：选择文件 → 预览 → 提交</div>
              </div>
              <div className="mt-4 space-y-2">
                {[
                  { id: 'F-1', label: `${homeworkSubmitType}文件_01` },
                  { id: 'F-2', label: `${homeworkSubmitType}文件_02` }
                ].map((f) => (
                  <button key={f.id} className="w-full rounded-2xl bg-white border border-[#f0ebe3] px-4 py-3 text-left" onClick={() => showToast(`已选择：${f.label}`)}>
                    <div className="text-sm font-bold text-[#2b2b2b]">{f.label}</div>
                    <div className="mt-1 text-xs text-[#7f7f88]">点击选择（示例）</div>
                  </button>
                ))}
              </div>
              <div className="mt-4 flex gap-3">
                <button className="flex-1 rounded-2xl bg-[#faf8f4] px-3 py-3 text-sm font-bold text-[#2b2b2b]" onClick={() => setHomeworkSubmitOpen(false)}>取消</button>
                <button className="flex-1 rounded-2xl bg-[#ff9b54] px-3 py-3 text-sm font-bold text-[#1f1f1f]" onClick={submitHomework}>确认提交</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
