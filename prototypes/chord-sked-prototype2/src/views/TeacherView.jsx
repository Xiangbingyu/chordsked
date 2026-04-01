import { useState } from 'react'
import {
  teacherTodo,
  teacherTodayCourses,
  homeworkPool,
  students,
  studentRemaining,
  notifications
} from '../lib/mockData'
import { PaginatedTable } from '../components/shared/uiBlocks'

export default function TeacherView({ page, onNavigate }) {
  const [teacherTip, setTeacherTip] = useState('')
  const [teacherMessageFilter, setTeacherMessageFilter] = useState('未读')
  const [teacherMessageDrawerOpen, setTeacherMessageDrawerOpen] = useState(false)
  const [activeTeacherMessageId, setActiveTeacherMessageId] = useState(notifications[0].id)
  const [teacherTimetableView, setTeacherTimetableView] = useState('周视图')
  const [teacherTimetableCampus, setTeacherTimetableCampus] = useState('全部校区')
  const [teacherTimetableType, setTeacherTimetableType] = useState('全部类型')
  const [teacherTimetableDate, setTeacherTimetableDate] = useState('2026-03-24')
  const [teacherCourseDrawerOpen, setTeacherCourseDrawerOpen] = useState(false)
  const [activeCourseId, setActiveCourseId] = useState(teacherTodayCourses[0].id)
  const [attendanceTab, setAttendanceTab] = useState('今日')
  const [attendanceRows, setAttendanceRows] = useState(
    teacherTodayCourses.map((course, idx) => {
      const fallbackStudents = students.slice(idx, idx + (course.studentCount || 1)).map((item) => ({
        name: item.name,
        checked: idx !== 0
      }))
      const baseStudents = (course.students || []).length > 0 ? course.students : fallbackStudents
      return {
        id: course.id,
        title: course.title,
        time: course.time,
        room: course.room,
        period: idx === 0 ? '今日' : '本周',
        status: idx === 0 ? '待签到' : idx === 1 ? '已完成扣减' : '部分签到',
        students: baseStudents.map((student) => ({
          name: student.name,
          checked: idx === 0 ? !!student.checked : idx === 1 ? true : student.name !== baseStudents[0]?.name,
          leave: false,
          truancy: false,
          deductHours: idx === 1 ? 1 : idx === 2 ? (student.name !== baseStudents[0]?.name ? 1 : 0) : student.checked ? 1 : 0
        }))
      }
    })
  )
  const [activeAttendanceId, setActiveAttendanceId] = useState(teacherTodayCourses[0].id)
  const [attendanceDrawerOpen, setAttendanceDrawerOpen] = useState(false)
  const [signedCancelDrawerOpen, setSignedCancelDrawerOpen] = useState(false)
  const [signedCancelCourseId, setSignedCancelCourseId] = useState('')
  const [signedCancelStudentNames, setSignedCancelStudentNames] = useState([])
  const [feedbackQueueRows, setFeedbackQueueRows] = useState(
    teacherTodayCourses.map((course, idx) => ({
      id: course.id,
      title: course.title,
      time: course.time,
      status: idx === 0 ? '待反馈' : '已反馈'
    }))
  )
  const [activeFeedbackId, setActiveFeedbackId] = useState(teacherTodayCourses[0].id)
  const [feedbackDrawerOpen, setFeedbackDrawerOpen] = useState(false)
  const [feedbackStep, setFeedbackStep] = useState(1)
  const [feedbackForm, setFeedbackForm] = useState({
    summary: '',
    review: '',
    homeworkTitle: '',
    homeworkType: '视频',
    homeworkDeadline: '2026-03-26 20:00'
  })
  const [feedbackStudentRows, setFeedbackStudentRows] = useState(
    (teacherTodayCourses[0].students || []).length
      ? teacherTodayCourses[0].students.map((item) => ({
          name: item.name,
          performance: '',
          nextAdvice: ''
        }))
      : students.slice(0, 4).map((item) => ({
          name: item.name,
          performance: '',
          nextAdvice: ''
        }))
  )
  const [homeworkRows, setHomeworkRows] = useState(
    homeworkPool.map((item, idx) => ({
      ...item,
      className: idx % 2 === 0 ? '小班课A班' : '体验班',
      course: idx % 2 === 0 ? '小班课' : '体验课',
      reminderSent: false
    }))
  )
  const [activeHomeworkId, setActiveHomeworkId] = useState(homeworkPool[0].id)
  const [homeworkDrawerOpen, setHomeworkDrawerOpen] = useState(false)
  const [homeworkStudentKeyword, setHomeworkStudentKeyword] = useState('')
  const [homeworkAssetView, setHomeworkAssetView] = useState('')
  const [homeworkComment, setHomeworkComment] = useState('')
  const [teacherStudentDrawerOpen, setTeacherStudentDrawerOpen] = useState(false)
  const [activeTeacherStudentId, setActiveTeacherStudentId] = useState(students[0].id)
  const [teacherAvailabilityRows, setTeacherAvailabilityRows] = useState([
    { id: 'AT-001', mode: '周重复', day: '周一', time: '13:00-18:00', campus: '北环国基路校区', status: '启用' },
    { id: 'AT-002', mode: '周重复', day: '周三', time: '14:00-20:00', campus: '北环国基路校区', status: '启用' },
    { id: 'AT-003', mode: '临时调整', day: '2026-03-28', time: '09:00-12:00', campus: '西大剧院校区', status: '启用' }
  ])
  const [availabilityModalOpen, setAvailabilityModalOpen] = useState(false)
  const [availabilityForm, setAvailabilityForm] = useState({
    mode: '周重复',
    day: '周二',
    time: '',
    campus: '北环国基路校区',
    status: '启用'
  })
  const [availabilityPref, setAvailabilityPref] = useState({
    teachingTypes: ['团课'],
    subjects: ['木吉他'],
    capacity: '5'
  })

  const teacherKpiRows = [
    { id: 'TK-1', label: '本周新增学员', value: 6, trend: '较上周 +2' },
    { id: 'TK-2', label: '本周上课节数', value: 26, trend: '完成率 93%' },
    { id: 'TK-3', label: '本周已上课时', value: '38课时', trend: '已获课时费' },
    { id: 'TK-4', label: '待批改作业', value: 5, trend: '待处理' }
  ]
  const teacherKpiNavigationMap = {
    本周新增学员: '/teacher/teacher-students',
    本周上课节数: '/teacher/teacher-timetable',
    本周已上课时: '/teacher/teacher-attendance-signed',
    待批改作业: '/teacher/teacher-homework'
  }
  const teacherMessageFilterOptions = ['未读', '全部', '课程提醒', '作业提醒', '系统公告', '审批结果']
  const teacherMessages = notifications.filter((item) => {
    if (teacherMessageFilter === '全部') return true
    if (teacherMessageFilter === '未读') return !item.read
    return item.category === teacherMessageFilter
  })

  const timetableRows = teacherTodayCourses
    .map((course, idx) => ({
      id: course.id,
      date: idx === 0 ? '2026-03-24' : '2026-03-26',
      time: course.time,
      title: course.title,
      type: idx === 0 ? '小班课' : '一对一',
      campus: course.room.includes('北环') ? '北环国基路校区' : '西大剧院校区',
      room: course.room
    }))
    .filter((item) => teacherTimetableCampus === '全部校区' || item.campus === teacherTimetableCampus)
    .filter((item) => teacherTimetableType === '全部类型' || item.type === teacherTimetableType)

  const activeCourse = teacherTodayCourses.find((item) => item.id === activeCourseId) || teacherTodayCourses[0]
  const filteredAttendanceRows = attendanceRows.filter((item) => item.period === attendanceTab)
  const activeAttendance = attendanceRows.find((item) => item.id === activeAttendanceId) || attendanceRows[0]
  const signedCancelCourse = attendanceRows.find((item) => item.id === signedCancelCourseId) || attendanceRows[0]
  const activeFeedback = feedbackQueueRows.find((item) => item.id === activeFeedbackId) || feedbackQueueRows[0]
  const activeHomework = homeworkRows.find((item) => item.id === activeHomeworkId) || homeworkRows[0]
  const homeworkCourseGroups = homeworkRows.reduce((acc, item) => {
    const found = acc.find((group) => group.course === item.course)
    if (found) {
      found.submissions.push(item)
      return acc
    }
    return [...acc, { id: `HG-${acc.length + 1}`, course: item.course, submissions: [item] }]
  }, [])
  const filteredHomeworkCourseGroups = (() => {
    const keyword = homeworkStudentKeyword.trim()
    if (!keyword) return homeworkCourseGroups
    return homeworkCourseGroups
      .map((group) => ({
        ...group,
        submissions: group.submissions.filter((item) => item.student.includes(keyword))
      }))
      .filter((group) => group.submissions.length > 0)
  })()
  const getHomeworkAssetType = (row) => {
    if ((row.media || '').includes('音频')) return 'audio'
    if ((row.media || '').includes('视频')) return 'video'
    if ((row.media || '').includes('图文') || (row.media || '').includes('图片')) return 'image'
    return 'text'
  }
  const assignedStudents = students.slice(0, 6).map((item, idx) => ({
    ...item,
    level: ['A', 'B', 'A', 'C', 'B', 'A'][idx] || 'B',
    stageReport: idx % 2 === 0 ? '已完成阶段测评' : '待完成阶段测评',
    transferNode: idx % 2 === 0 ? '团购已转正课' : '团购核销后跟进中'
  }))
  const resolveStudentHours = (student) => {
    const total = Number(student?.signupHours ?? 0)
    const remaining = Number(student?.remaining ?? 0)
    const attended = Math.max(total - remaining, 0)
    return { total, attended, remaining }
  }
  const activeTeacherStudent = assignedStudents.find((item) => item.id === activeTeacherStudentId) || assignedStudents[0]
  const activeTeacherStudentHours = resolveStudentHours(activeTeacherStudent)

  const setAttendanceStudentStatus = (name, status) => {
    setAttendanceRows((prev) =>
      prev.map((course) =>
        course.id === activeAttendance.id
          ? {
              ...course,
              students: course.students.map((student) =>
                student.name === name
                  ? status === 'present'
                      ? { ...student, checked: true, leave: false, truancy: false, deductHours: Math.max(Number(student.deductHours || 0), 1) }
                      : status === 'leave'
                          ? { ...student, checked: false, leave: true, truancy: false, deductHours: 0 }
                          : { ...student, checked: false, leave: false, truancy: true, deductHours: 0 }
                  : student
              )
            }
          : course
      )
    )
  }

  const setAttendanceDeduct = (name, value) => {
    setAttendanceRows((prev) =>
      prev.map((course) =>
        course.id === activeAttendance.id
          ? {
              ...course,
              students: course.students.map((student) =>
                student.name === name
                  ? student.leave || student.truancy
                      ? student
                      : { ...student, deductHours: Number(value || 0) }
                  : student
              )
            }
          : course
      )
    )
  }

  const submitAttendance = () => {
    setAttendanceRows((prev) =>
      prev.map((course) =>
        course.id === activeAttendance.id
          ? { ...course, status: '已完成扣减' }
          : course
      )
    )
    setTeacherTip(`已完成签到并生成上课记录：${activeAttendance.title}`)
    setAttendanceDrawerOpen(false)
  }

  const revokeAttendance = (courseId) => {
    setAttendanceRows((prev) =>
      prev.map((course) =>
        course.id === courseId
          ? {
              ...course,
              status: '待签到',
              students: course.students.map((student) => ({ ...student, checked: false, leave: false, truancy: false, deductHours: 0 }))
            }
          : course
      )
    )
    setTeacherTip('已撤销签到状态')
  }

  const openPartialCancel = (courseId) => {
    const target = attendanceRows.find((item) => item.id === courseId)
    if (!target) return
    const checkedNames = target.students.filter((student) => student.checked).map((student) => student.name)
    setSignedCancelCourseId(courseId)
    setSignedCancelStudentNames(checkedNames)
    setSignedCancelDrawerOpen(true)
  }

  const toggleSignedCancelStudent = (studentName) => {
    setSignedCancelStudentNames((prev) =>
      prev.includes(studentName) ? prev.filter((item) => item !== studentName) : [...prev, studentName]
    )
  }

  const submitPartialCancel = () => {
    if (signedCancelStudentNames.length === 0) {
      setTeacherTip('请至少选择一位学员')
      return
    }
    setAttendanceRows((prev) =>
      prev.map((course) => {
        if (course.id !== signedCancelCourseId) return course
        const nextStudents = course.students.map((student) =>
          signedCancelStudentNames.includes(student.name)
            ? { ...student, checked: false, deductHours: 0 }
            : student
        )
        const checkedCount = nextStudents.filter((student) => student.checked).length
        return {
          ...course,
          status: checkedCount === 0 ? '待签到' : checkedCount === nextStudents.length ? '已完成扣减' : '部分签到',
          students: nextStudents
        }
      })
    )
    setSignedCancelDrawerOpen(false)
    setTeacherTip('已完成部分学员取消签到')
  }

  const goFeedbackStep2 = () => {
    if (!feedbackForm.summary || !feedbackForm.review) {
      setTeacherTip('请先完成课程整体反馈（教学反馈与课后整体回顾）')
      return
    }
    setFeedbackStep(2)
  }

  const backFeedbackStep1 = () => setFeedbackStep(1)

  const updateFeedbackStudent = (studentName, patch) => {
    setFeedbackStudentRows((prev) =>
      prev.map((item) => (item.name === studentName ? { ...item, ...patch } : item))
    )
  }

  const submitFeedback = () => {
    if (!feedbackForm.summary || !feedbackForm.review || !feedbackForm.homeworkTitle) {
      setTeacherTip('请先完成课程整体反馈，并填写作业信息')
      return
    }
    const hasStudentNote = feedbackStudentRows.some((item) => item.performance || item.nextAdvice)
    if (!hasStudentNote) {
      setTeacherTip('请至少为一位学员填写课堂表现或下节课建议')
      return
    }
    setFeedbackQueueRows((prev) =>
      prev.map((item) => (item.id === activeFeedback.id ? { ...item, status: '已反馈' } : item))
    )
    setHomeworkRows((prev) => [
      {
        id: `H-${String(prev.length + 1).padStart(2, '0')}`,
        student: '全班',
        title: feedbackForm.homeworkTitle,
        status: '待提交',
        media: `${feedbackForm.homeworkType}作业`,
        deadline: `截止 ${feedbackForm.homeworkDeadline}`,
        className: '课堂发布',
        course: activeFeedback.title,
        reminderSent: false
      },
      ...prev
    ])
    setTeacherTip(`课后反馈已同步成长记录并发布作业：${feedbackForm.homeworkTitle}`)
    setFeedbackDrawerOpen(false)
    setFeedbackStep(1)
  }

  const submitHomeworkComment = () => {
    if (!homeworkComment) {
      setTeacherTip('请先输入批改点评内容')
      return
    }
    setHomeworkRows((prev) =>
      prev.map((item) => (item.id === activeHomework.id ? { ...item, status: '已批改' } : item))
    )
    setHomeworkComment('')
    setTeacherTip(`已完成作业批改：${activeHomework.title}`)
    setHomeworkDrawerOpen(false)
  }

  const saveAvailability = () => {
    if (!availabilityForm.day || !availabilityForm.time) {
      setTeacherTip('请填写可用时间配置')
      return
    }
    const newRow = { id: `AT-${String(teacherAvailabilityRows.length + 1).padStart(3, '0')}`, ...availabilityForm }
    setTeacherAvailabilityRows((prev) => [newRow, ...prev])
    setAvailabilityModalOpen(false)
    setTeacherTip(`已保存可授课时间：${newRow.day} ${newRow.time}`)
  }

  const saveAvailabilityPref = () => {
    setTeacherTip('已保存授课偏好与承载量配置')
  }

  if (page === 'teacher-workbench-overview') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">工作台</h2>
        <div className="grid grid-cols-4 gap-4">
          {teacherKpiRows.map((item) => (
            <button
              key={item.id}
              onClick={() => onNavigate?.(teacherKpiNavigationMap[item.label] || '/teacher/teacher-workbench-overview')}
              className="rounded-2xl border border-[#f0ebe3] bg-[#fffcf8] p-4 text-left transition hover:-translate-y-0.5 hover:shadow-sm"
            >
              <div className="text-xs text-[#8b8177]">{item.label}</div>
              <div className="mt-2 text-2xl font-semibold text-[#2e2a25]">{item.value}</div>
              <div className="mt-2 text-xs text-[#bc7844]">{item.trend}</div>
            </button>
          ))}
        </div>
        <div className="grid grid-cols-[1.3fr_1fr] gap-4">
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">今日课程</div>
            <PaginatedTable
              columns={[
                { key: 'time', title: '上课时间' },
                { key: 'title', title: '课程名称' },
                { key: 'room', title: '教室' },
                { key: 'studentCount', title: '人数' },
                { key: 'action', title: '操作', render: (row) => <button onClick={() => { setActiveCourseId(row.id); setTeacherCourseDrawerOpen(true) }} className="text-[#bc7844]">课程详情</button> }
              ]}
              rows={teacherTodayCourses}
              pageSize={4}
            />
          </article>
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">待办</div>
            <PaginatedTable
              columns={[
                { key: 'todo', title: '待办事项' },
                { key: 'count', title: '数量' },
                { key: 'next', title: '处理动作' }
              ]}
              rows={[
                { id: 'TT-1', todo: '待签到课程', count: '2节', next: '进入上课签到' },
                { id: 'TT-2', todo: '待写课后反馈', count: '1节', next: '进入课后反馈' },
                { id: 'TT-3', todo: '待批改作业', count: '3份', next: '进入作业管理' },
                { id: 'TT-4', todo: '未提交作业跟进', count: '5人', next: '批量跟进' }
              ]}
              pageSize={4}
            />
          </article>
        </div>
        {teacherCourseDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherCourseDrawerOpen(false)} />
            <div className="h-full w-[500px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">课程详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherCourseDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">课程：{activeCourse.title}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">时间：{activeCourse.time}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">教室：{activeCourse.room}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">签到入口：上课签到</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">课后反馈入口：课后反馈 / 反馈编辑</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-messages-list') {
    const activeMessage = notifications.find((item) => item.id === activeTeacherMessageId) || notifications[0]
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">消息中心 / 消息列表</h2>
        <div className="flex flex-wrap gap-2">
          {teacherMessageFilterOptions.map((item) => (
            <button key={item} onClick={() => setTeacherMessageFilter(item)} className={`rounded-full px-3 py-1 text-xs ${teacherMessageFilter === item ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{item}</button>
          ))}
        </div>
        <PaginatedTable
          columns={[
            { key: 'title', title: '消息标题' },
            { key: 'category', title: '类型' },
            { key: 'time', title: '时间' },
            { key: 'status', title: '状态', render: (row) => (row.read ? '已读' : '未读') },
            { key: 'action', title: '操作', render: (row) => <button onClick={() => { setActiveTeacherMessageId(row.id); setTeacherMessageDrawerOpen(true) }} className="text-[#bc7844]">查看详情</button> }
          ]}
          rows={teacherMessages}
          pageSize={6}
        />
        {teacherMessageDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherMessageDrawerOpen(false)} />
            <div className="h-full w-[500px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">{activeMessage.title}</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherMessageDrawerOpen(false)}>关闭</button>
              </div>
              <div className="space-y-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">消息类型：{activeMessage.category}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">发送时间：{activeMessage.time}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">内容：{activeMessage.content}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">关联入口：{activeMessage.category === '课程提醒' ? '我的课表' : '工作台'}</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-timetable') {
    const weekDays = ['2026-03-24', '2026-03-25', '2026-03-26', '2026-03-27', '2026-03-28', '2026-03-29', '2026-03-30']
    const monthDays = Array.from({ length: 31 }, (_, idx) => `2026-03-${String(idx + 1).padStart(2, '0')}`)
    const calendarDays = teacherTimetableView === '日视图'
      ? [teacherTimetableDate]
      : teacherTimetableView === '周视图'
        ? weekDays
        : monthDays
    const dayCourseRows = timetableRows.filter((item) => item.date === teacherTimetableDate)
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">我的课表 / 日周视图</h2>
          <div className="flex items-center gap-2">
            <button onClick={() => setTeacherTimetableView('日视图')} className={`rounded-lg px-2.5 py-1.5 text-xs ${teacherTimetableView === '日视图' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>日视图</button>
            <button onClick={() => setTeacherTimetableView('周视图')} className={`rounded-lg px-2.5 py-1.5 text-xs ${teacherTimetableView === '周视图' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>周视图</button>
            <button onClick={() => setTeacherTimetableView('月视图')} className={`rounded-lg px-2.5 py-1.5 text-xs ${teacherTimetableView === '月视图' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>月视图</button>
            <select value={teacherTimetableCampus} onChange={(e) => setTeacherTimetableCampus(e.target.value)} className="rounded-lg border border-[#e8dfd3] px-2 py-1.5 text-xs outline-none"><option>全部校区</option><option>北环国基路校区</option><option>西大剧院校区</option></select>
            <select value={teacherTimetableType} onChange={(e) => setTeacherTimetableType(e.target.value)} className="rounded-lg border border-[#e8dfd3] px-2 py-1.5 text-xs outline-none"><option>全部类型</option><option>小班课</option><option>一对一</option></select>
          </div>
        </div>
        <div className="grid grid-cols-7 gap-3">
          {calendarDays.map((date) => {
            const items = timetableRows.filter((row) => row.date === date)
            return (
              <button key={date} onClick={() => setTeacherTimetableDate(date)} className={`rounded-2xl border p-3 text-left ${teacherTimetableDate === date ? 'border-[#ff9b54] bg-[#fff8f1]' : 'border-[#f0ebe3] hover:bg-[#faf8f4]'}`}>
                <div className="text-xs text-[#8f8376]">{date}</div>
                <div className="mt-1 text-xs text-[#6f665d]">{items.length} 节课</div>
                <div className="mt-2 space-y-1">
                  {items.slice(0, 2).map((item) => (
                    <div key={item.id} className="rounded bg-white px-2 py-1 text-[11px] text-[#6a6259]">{item.time}</div>
                  ))}
                  {items.length > 2 && <div className="text-[11px] text-[#8f8376]">+{items.length - 2} 节</div>}
                </div>
              </button>
            )
          })}
        </div>
        <article className="rounded-2xl border border-[#f0ebe3] p-4">
          <div className="mb-2 text-sm font-semibold">当日课程（{teacherTimetableDate}）</div>
          <div className="space-y-2">
            {dayCourseRows.length === 0 && (
              <div className="rounded-xl bg-[#faf8f4] p-3 text-sm text-[#8f8376]">当日暂无课程</div>
            )}
            {dayCourseRows.map((row) => (
              <div key={row.id} className="rounded-xl border border-[#f0ebe3] p-3">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-sm font-medium">{row.time} · {row.title}</div>
                    <div className="text-xs text-[#8f8376]">{row.type} / {row.campus} / {row.room}</div>
                  </div>
                  <button onClick={() => { setActiveCourseId(row.id); setTeacherCourseDrawerOpen(true) }} className="text-[#bc7844] text-sm">课程详情</button>
                </div>
              </div>
            ))}
          </div>
        </article>
        {teacherCourseDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherCourseDrawerOpen(false)} />
            <div className="h-full w-[500px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">课程详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherCourseDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">课程：{activeCourse.title}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">时间：{activeCourse.time}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">教室：{activeCourse.room}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">签到入口：上课签到</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">课后反馈入口：课后反馈 / 反馈编辑</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-attendance') {
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">上课签到 / 待签到列表</h2>
          <div className="flex gap-2 text-xs">
            <button onClick={() => setAttendanceTab('今日')} className={`rounded-lg px-2.5 py-1.5 ${attendanceTab === '今日' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>今日</button>
            <button onClick={() => setAttendanceTab('本周')} className={`rounded-lg px-2.5 py-1.5 ${attendanceTab === '本周' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>本周</button>
          </div>
        </div>
        <PaginatedTable
          columns={[
            { key: 'title', title: '课程' },
            { key: 'time', title: '时间' },
            { key: 'room', title: '教室' },
            { key: 'status', title: '状态' },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <button
                  onClick={() => {
                    setActiveAttendanceId(row.id)
                    setAttendanceDrawerOpen(true)
                  }}
                  className="text-[#bc7844]"
                >
                  签到详情
                </button>
              )
            }
          ]}
          rows={filteredAttendanceRows.filter((item) => item.status === '待签到')}
          pageSize={6}
        />
        {attendanceDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setAttendanceDrawerOpen(false)} />
            <div className="h-full w-[520px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">签到详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setAttendanceDrawerOpen(false)}>关闭</button>
              </div>
              <div className="mb-2 rounded-xl bg-[#faf8f4] p-3 text-sm">{activeAttendance.title} · {activeAttendance.time}</div>
              <div className="space-y-2">
                {activeAttendance.students.map((student) => (
                  <div key={student.name} className="rounded-xl border border-[#f0ebe3] p-3">
                    <div className="flex items-center justify-between text-sm">
                      <div>{student.name}</div>
                      <div className="flex gap-2">
                        <button onClick={() => setAttendanceStudentStatus(student.name, 'present')} className={`rounded px-2 py-1 text-xs ${student.checked ? 'bg-[#effaf1] text-[#2f8a47]' : 'bg-[#faf8f4] text-[#7b7064]'}`}>签到</button>
                        <button onClick={() => setAttendanceStudentStatus(student.name, 'leave')} className={`rounded px-2 py-1 text-xs ${student.leave ? 'bg-[#fff4ea] text-[#bc7844]' : 'bg-[#faf8f4] text-[#7b7064]'}`}>请假</button>
                        <button onClick={() => setAttendanceStudentStatus(student.name, 'truancy')} className={`rounded px-2 py-1 text-xs ${student.truancy ? 'bg-[#fff1ef] text-[#c9413a]' : 'bg-[#faf8f4] text-[#7b7064]'}`}>旷课</button>
                      </div>
                    </div>
                    <div className="mt-2 flex items-center justify-between text-xs text-[#7b7064]">
                      <span>特殊扣减课时</span>
                      <input disabled={student.leave || student.truancy} value={student.deductHours} onChange={(e) => setAttendanceDeduct(student.name, e.target.value)} className={`w-16 rounded-md border border-[#e9e2d8] px-2 py-1 text-center outline-none ${student.leave || student.truancy ? 'bg-[#faf8f4] text-[#c4c4c4]' : 'bg-white'}`} />
                    </div>
                  </div>
                ))}
              </div>
              <button onClick={submitAttendance} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">完成签到并生成上课记录</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-attendance-signed') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">上课签到 / 已签到列表</h2>
        <PaginatedTable
          columns={[
            { key: 'title', title: '课程' },
            { key: 'time', title: '时间' },
            { key: 'room', title: '教室' },
            { key: 'status', title: '状态' },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <div className="flex gap-2">
                  <button onClick={() => revokeAttendance(row.id)} className="text-[#a64545]">统一取消签到</button>
                  <button onClick={() => openPartialCancel(row.id)} className="text-[#bc7844]">部分取消签到</button>
                </div>
              )
            }
          ]}
          rows={attendanceRows.filter((item) => item.status !== '待签到')}
          pageSize={6}
        />
        {signedCancelDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setSignedCancelDrawerOpen(false)} />
            <div className="h-full w-[520px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">部分学员取消签到</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setSignedCancelDrawerOpen(false)}>关闭</button>
              </div>
              <div className="mb-3 rounded-xl bg-[#faf8f4] p-3 text-sm">{signedCancelCourse?.title} · {signedCancelCourse?.time}</div>
              <div className="space-y-2">
                {signedCancelCourse?.students?.map((student) => (
                  <label key={student.name} className="flex items-center justify-between rounded-xl border border-[#f0ebe3] p-3 text-sm">
                    <span>{student.name}</span>
                    <input type="checkbox" checked={signedCancelStudentNames.includes(student.name)} onChange={() => toggleSignedCancelStudent(student.name)} />
                  </label>
                ))}
              </div>
              <button onClick={submitPartialCancel} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">确认取消所选学员签到</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-feedback') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">课后反馈 / 待反馈列表</h2>
        <PaginatedTable
          columns={[
            { key: 'title', title: '课程' },
            { key: 'time', title: '时间' },
            { key: 'status', title: '状态' },
            {
              key: 'action',
              title: '操作',
              render: (row) => (
                <button
                  onClick={() => {
                    setActiveFeedbackId(row.id)
                    setFeedbackStep(1)
                    setFeedbackDrawerOpen(true)
                  }}
                  className="text-[#bc7844]"
                >
                  反馈详情
                </button>
              )
            }
          ]}
          rows={feedbackQueueRows}
          pageSize={6}
        />
        {feedbackDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setFeedbackDrawerOpen(false)} />
            <div className="h-full w-[560px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">反馈详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setFeedbackDrawerOpen(false)}>关闭</button>
              </div>
              <div className="mb-2 rounded-xl bg-[#faf8f4] p-3 text-sm">{activeFeedback.title} · {activeFeedback.time}</div>
              <div className="rounded-xl border border-[#f0ebe3] bg-[#fffaf2] p-3 text-xs text-[#6f665d]">
                步骤 {feedbackStep}/2：{feedbackStep === 1 ? '课程整体反馈（教学反馈 + 课后整体回顾）' : '逐学员建议与评语'}
              </div>
              {feedbackStep === 1 && (
                <div className="mt-3 space-y-3">
                  <div className="space-y-1"><div className="text-xs text-[#7b7064]">教学反馈</div><textarea value={feedbackForm.summary} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, summary: e.target.value }))} className="h-28 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" /></div>
                  <div className="space-y-1"><div className="text-xs text-[#7b7064]">课后整体回顾</div><textarea value={feedbackForm.review} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, review: e.target.value }))} className="h-28 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" /></div>
                  <div className="mt-3 flex gap-2">
                    <button onClick={() => setFeedbackDrawerOpen(false)} className="flex-1 rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">稍后处理</button>
                    <button onClick={goFeedbackStep2} className="flex-1 rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">下一步：逐学员填写</button>
                  </div>
                </div>
              )}
              {feedbackStep === 2 && (
                <div className="mt-3 space-y-3">
                  <div className="space-y-2">
                    {feedbackStudentRows.map((student) => (
                      <div key={student.name} className="rounded-xl border border-[#f0ebe3] p-3">
                        <div className="mb-2 text-sm font-semibold">{student.name}</div>
                        <div className="space-y-2">
                          <div className="space-y-1">
                            <div className="text-xs text-[#7b7064]">课堂表现与评语</div>
                            <textarea value={student.performance} onChange={(e) => updateFeedbackStudent(student.name, { performance: e.target.value })} className="h-20 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" />
                          </div>
                          <div className="space-y-1">
                            <div className="text-xs text-[#7b7064]">下节课建议</div>
                            <textarea value={student.nextAdvice} onChange={(e) => updateFeedbackStudent(student.name, { nextAdvice: e.target.value })} className="h-20 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" />
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                  <div className="mt-3 rounded-2xl border border-[#f0ebe3] p-3">
                    <div className="mb-2 text-sm font-semibold">发布作业（同页完成）</div>
                    <div className="grid grid-cols-3 gap-2">
                      <input value={feedbackForm.homeworkTitle} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, homeworkTitle: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" placeholder="作业标题" />
                      <select value={feedbackForm.homeworkType} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, homeworkType: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>文字</option><option>图片</option><option>音频</option><option>视频</option></select>
                      <input value={feedbackForm.homeworkDeadline} onChange={(e) => setFeedbackForm((prev) => ({ ...prev, homeworkDeadline: e.target.value }))} className="rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" />
                    </div>
                  </div>
                  <div className="mt-3 flex gap-2">
                    <button onClick={backFeedbackStep1} className="flex-1 rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上一步</button>
                    <button onClick={submitFeedback} className="flex-1 rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存反馈并同步成长记录、发布作业</button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-homework') {
    const currentAssetType = homeworkAssetView || getHomeworkAssetType(activeHomework)
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">作业管理 / 作业列表（课程组-学员筛选）</h2>
        <div className="rounded-2xl border border-[#f0ebe3] p-3">
          <div className="text-xs text-[#7b7064]">按学员姓名搜索</div>
          <input value={homeworkStudentKeyword} onChange={(e) => setHomeworkStudentKeyword(e.target.value)} className="mt-1 w-full rounded-lg border border-[#e9e2d8] px-3 py-2 text-sm outline-none" placeholder="请输入学员姓名，例如：张小满" />
        </div>
        <div className="space-y-3">
          {filteredHomeworkCourseGroups.map((group) => (
            <article key={group.id} className="rounded-2xl border border-[#f0ebe3] p-4">
              <div className="mb-2 flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold">{group.course}</div>
                  <div className="text-xs text-[#8f8376]">作业提交 {group.submissions.length} 份</div>
                </div>
                <div className="text-xs text-[#8f8376]">
                  未提交 {group.submissions.filter((item) => item.status === '未提交').length} 份
                </div>
              </div>
              <PaginatedTable
                columns={[
                  {
                    key: 'student',
                    title: '学员提交',
                    render: (row) => {
                      const target = students.find((item) => item.name === row.student)
                      const hours = resolveStudentHours(target)
                      return `${row.student}（总${hours.total}/已上${hours.attended}/剩余${hours.remaining}）`
                    }
                  },
                  { key: 'title', title: '作业标题' },
                  { key: 'className', title: '班级' },
                  { key: 'deadline', title: '截止时间' },
                  { key: 'status', title: '状态' },
                  {
                    key: 'action',
                    title: '操作',
                    render: (row) => (
                      <button
                        onClick={() => {
                          setActiveHomeworkId(row.id)
                          setHomeworkDrawerOpen(true)
                          setHomeworkAssetView(getHomeworkAssetType(row))
                        }}
                        className="text-[#bc7844]"
                      >
                        去批改
                      </button>
                    )
                  }
                ]}
                rows={group.submissions}
                pageSize={5}
              />
            </article>
          ))}
        </div>
        {homeworkDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setHomeworkDrawerOpen(false)} />
            <div className="h-full w-[560px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">批改页</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setHomeworkDrawerOpen(false)}>关闭</button>
              </div>
              <div className="rounded-xl bg-[#faf8f4] p-3 text-sm">
                <div className="font-medium">{activeHomework.title}</div>
                <div className="mt-1 text-xs text-[#8f8376]">{activeHomework.student} · {activeHomework.media} · {activeHomework.deadline}</div>
              </div>
              <div className="mt-3 rounded-2xl border border-[#f0ebe3] p-3">
                <div className="mb-2 text-sm font-semibold">学员作业内容</div>
                <div className="mb-2 flex gap-2">
                  <button onClick={() => setHomeworkAssetView('audio')} className={`rounded px-2 py-1 text-xs ${currentAssetType === 'audio' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>打开音频</button>
                  <button onClick={() => setHomeworkAssetView('video')} className={`rounded px-2 py-1 text-xs ${currentAssetType === 'video' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>打开视频</button>
                  <button onClick={() => setHomeworkAssetView('image')} className={`rounded px-2 py-1 text-xs ${currentAssetType === 'image' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>打开图文</button>
                  <button onClick={() => setHomeworkAssetView('text')} className={`rounded px-2 py-1 text-xs ${currentAssetType === 'text' ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'border border-[#e8dfd3]'}`}>打开文字</button>
                </div>
                {currentAssetType === 'audio' && (
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">音频作业已打开（原型预览）：可播放学员练习音频并进行点评。</div>
                )}
                {currentAssetType === 'video' && (
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">视频作业已打开（原型预览）：可查看学员演奏视频并进行点评。</div>
                )}
                {currentAssetType === 'image' && (
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">图文作业已打开（原型预览）：可查看谱例图片与文字说明。</div>
                )}
                {currentAssetType === 'text' && (
                  <div className="rounded-xl bg-[#faf8f4] p-3 text-xs text-[#6f665d]">文字作业已打开（原型预览）：可查看学员练习记录和自评内容。</div>
                )}
              </div>
              <textarea value={homeworkComment} onChange={(e) => setHomeworkComment(e.target.value)} className="mt-3 h-32 w-full rounded-xl border border-[#e9e2d8] px-3 py-2 text-sm outline-none" placeholder="请输入文字/语音/视频点评摘要" />
              <div className="mt-2 grid grid-cols-3 gap-2">
                <button className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上传语音点评</button>
                <button className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上传视频点评</button>
                <button className="rounded-lg border border-[#e8dfd3] px-3 py-2 text-sm">上传图文点评</button>
              </div>
              <button onClick={submitHomeworkComment} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">提交点评</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-students') {
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">学员管理 / 已分配学员</h2>
        <PaginatedTable
          columns={[
            { key: 'name', title: '学员' },
            { key: 'totalHours', title: '总课时', render: (row) => resolveStudentHours(row).total },
            { key: 'attendedHours', title: '已上课时', render: (row) => resolveStudentHours(row).attended },
            { key: 'remainingHours', title: '剩余课时', render: (row) => resolveStudentHours(row).remaining },
            { key: 'level', title: '等级标注' },
            { key: 'stageReport', title: '阶段测评' },
            { key: 'transferNode', title: '团购转化节点' },
            { key: 'action', title: '操作', render: (row) => <button onClick={() => { setActiveTeacherStudentId(row.id); setTeacherStudentDrawerOpen(true) }} className="text-[#bc7844]">学员详情</button> }
          ]}
          rows={assignedStudents}
          pageSize={6}
        />
        {teacherStudentDrawerOpen && (
          <div className="fixed inset-0 z-30 flex justify-end bg-black/20">
            <button className="flex-1" onClick={() => setTeacherStudentDrawerOpen(false)} />
            <div className="h-full w-[520px] overflow-auto bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">学员详情</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setTeacherStudentDrawerOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="rounded-xl bg-[#faf8f4] p-3">学员：{activeTeacherStudent.name}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">总课时：{activeTeacherStudentHours.total}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">已上课时：{activeTeacherStudentHours.attended}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">剩余课时：{activeTeacherStudentHours.remaining}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">等级：{activeTeacherStudent.level}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3">阶段测评：{activeTeacherStudent.stageReport}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">作业详情：{(activeTeacherStudent.homeworkRecords || ['暂无']).join('；')}</div>
                <div className="rounded-xl bg-[#faf8f4] p-3 col-span-2">团购转化节点：{activeTeacherStudent.transferNode}</div>
              </div>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-availability-time') {
    return (
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">可授课时间与承载量 / 可用时间配置</h2>
          <button onClick={() => setAvailabilityModalOpen(true)} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">新增时间配置</button>
        </div>
        <PaginatedTable
          columns={[
            { key: 'mode', title: '类型' },
            { key: 'day', title: '日期/星期' },
            { key: 'time', title: '时间段' },
            { key: 'campus', title: '校区' },
            { key: 'status', title: '状态' }
          ]}
          rows={teacherAvailabilityRows}
          pageSize={6}
        />
        {availabilityModalOpen && (
          <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/30 p-4">
            <button className="absolute inset-0" onClick={() => setAvailabilityModalOpen(false)} />
            <div className="relative w-full max-w-[620px] rounded-2xl bg-white p-5 shadow-2xl">
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-base font-semibold">新增可用时间配置</h3>
                <button className="text-sm text-[#8f8376]" onClick={() => setAvailabilityModalOpen(false)}>关闭</button>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">配置类型</div><select value={availabilityForm.mode} onChange={(e) => setAvailabilityForm((prev) => ({ ...prev, mode: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>周重复</option><option>临时调整</option></select></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">日期/星期</div><input value={availabilityForm.day} onChange={(e) => setAvailabilityForm((prev) => ({ ...prev, day: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">时间段</div><input value={availabilityForm.time} onChange={(e) => setAvailabilityForm((prev) => ({ ...prev, time: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none" /></div>
                <div className="space-y-1"><div className="text-xs text-[#7b7064]">校区</div><select value={availabilityForm.campus} onChange={(e) => setAvailabilityForm((prev) => ({ ...prev, campus: e.target.value }))} className="w-full rounded-lg border border-[#e9e2d8] px-2 py-2 text-xs outline-none"><option>北环国基路校区</option><option>西大剧院校区</option></select></div>
              </div>
              <button onClick={saveAvailability} className="mt-3 w-full rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存配置</button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (page === 'teacher-availability-pref') {
    const toggleType = (type) => {
      setAvailabilityPref((prev) => ({
        ...prev,
        teachingTypes: prev.teachingTypes.includes(type)
          ? prev.teachingTypes.filter((item) => item !== type)
          : [...prev.teachingTypes, type]
      }))
    }
    const toggleSubject = (subject) => {
      setAvailabilityPref((prev) => ({
        ...prev,
        subjects: prev.subjects.includes(subject)
          ? prev.subjects.filter((item) => item !== subject)
          : [...prev.subjects, subject]
      }))
    }
    return (
      <section className="space-y-4">
        <h2 className="text-sm font-semibold">可授课时间与承载量 / 授课偏好</h2>
        <div className="grid grid-cols-2 gap-4">
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">授课类型偏好</div>
            <div className="flex flex-wrap gap-2">
              {['团课', '一对一'].map((type) => (
                <button key={type} onClick={() => toggleType(type)} className={`rounded-full px-3 py-1 text-xs ${availabilityPref.teachingTypes.includes(type) ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{type}</button>
              ))}
            </div>
            <div className="mt-3 text-sm text-[#6f665d]">当前：{availabilityPref.teachingTypes.join(' / ')}</div>
          </article>
          <article className="rounded-2xl border border-[#f0ebe3] p-4">
            <div className="mb-2 text-sm font-semibold">擅长科目</div>
            <div className="flex flex-wrap gap-2">
              {['木吉他', '电吉他', '贝斯'].map((subject) => (
                <button key={subject} onClick={() => toggleSubject(subject)} className={`rounded-full px-3 py-1 text-xs ${availabilityPref.subjects.includes(subject) ? 'bg-[#ff9b54] text-[#1f1f1f]' : 'bg-[#faf8f4] text-[#8f8376]'}`}>{subject}</button>
              ))}
            </div>
            <div className="mt-3 text-sm text-[#6f665d]">当前：{availabilityPref.subjects.join(' / ')}</div>
          </article>
        </div>
        <article className="rounded-2xl border border-[#f0ebe3] p-4">
          <div className="mb-2 text-sm font-semibold">承载量配置（同一时间段）</div>
          <div className="flex items-center gap-3">
            <span className="text-sm text-[#6f665d]">最大承载量</span>
            <input value={availabilityPref.capacity} onChange={(e) => setAvailabilityPref((prev) => ({ ...prev, capacity: e.target.value }))} className="w-20 rounded-lg border border-[#e9e2d8] px-2 py-1 text-sm outline-none" />
            <span className="text-sm text-[#6f665d]">人</span>
            <button onClick={saveAvailabilityPref} className="rounded-lg bg-[#ff9b54] px-3 py-2 text-sm font-medium text-[#1f1f1f]">保存偏好</button>
          </div>
        </article>
      </section>
    )
  }

  return (
    <section className="space-y-4">
      <h2 className="text-sm font-semibold">教师端页面预留</h2>
      <div className="rounded-xl bg-[#faf8f4] p-3 text-sm">当前页面正在补充中</div>
    </section>
  )
}
