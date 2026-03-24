import React, { useState } from 'react';
import { format, addDays, startOfWeek, isSameDay } from 'date-fns';
import { zhCN } from 'date-fns/locale';
import { schedule as initialSchedule, teachers, students } from '../lib/mockData';
import { ChevronLeft, ChevronRight, Plus, Calendar, X, CheckCircle } from 'lucide-react';

const Schedule = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [scheduleData, setScheduleData] = useState(initialSchedule);
  const [selectedClass, setSelectedClass] = useState(null);

  const startDate = startOfWeek(currentDate, { weekStartsOn: 1 });
  const weekDays = Array.from({ length: 7 }).map((_, i) => addDays(startDate, i));
  const timeSlots = Array.from({ length: 13 }).map((_, i) => i + 9); // 9:00 - 21:00

  // Filter classes for the week
  const getClassesForDayAndTime = (day, hour) => {
    const dateStr = format(day, 'yyyy-MM-dd');
    // Simple matching for hour
    return scheduleData.filter(s => s.date === dateStr && parseInt(s.time.split(':')[0]) === hour);
  };

  const handleCheckIn = (clsId) => {
    if (confirm('确认签到？将扣除学员1课时。')) {
        setScheduleData(scheduleData.map(s => 
            s.id === clsId ? { ...s, status: '已签到' } : s
        ));
        setSelectedClass(null);
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 h-[calc(100vh-8rem)] flex flex-col animate-in fade-in duration-500 relative">
      <div className="p-6 border-b border-gray-100 flex justify-between items-center">
        <h2 className="text-xl font-bold flex items-center gap-2">
            <Calendar size={24} className="text-primary"/>
            排课日历
        </h2>
        <div className="flex items-center gap-4">
             <div className="flex bg-gray-100 rounded-lg p-1">
                <button className="px-3 py-1 bg-white shadow-sm rounded-md text-sm font-medium transition-all">周视图</button>
                <button className="px-3 py-1 text-text-secondary text-sm font-medium hover:text-text-main transition-colors">日视图</button>
             </div>
             <div className="flex items-center gap-2 bg-gray-50 rounded-lg p-1">
                <button onClick={() => setCurrentDate(addDays(currentDate, -7))} className="p-2 hover:bg-white hover:shadow-sm rounded-md transition-all"><ChevronLeft size={18}/></button>
                <span className="font-medium text-sm w-32 text-center select-none">{format(currentDate, 'yyyy年M月', {locale: zhCN})}</span>
                <button onClick={() => setCurrentDate(addDays(currentDate, 7))} className="p-2 hover:bg-white hover:shadow-sm rounded-md transition-all"><ChevronRight size={18}/></button>
             </div>
             <button className="bg-primary text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-900 transition-colors shadow-lg shadow-primary/20">
                <Plus size={20} />
                <span>排课</span>
             </button>
        </div>
      </div>

      <div className="flex-1 overflow-auto custom-scrollbar relative">
        <div className="min-w-[1000px]">
            {/* Header */}
            <div className="grid grid-cols-8 border-b border-gray-100 sticky top-0 bg-white z-20 shadow-sm">
                <div className="p-4 text-center text-text-hint text-sm border-r border-gray-100 bg-gray-50/50">时间</div>
                {weekDays.map(day => (
                    <div key={day.toString()} className={`p-4 text-center border-r border-gray-100 ${isSameDay(day, new Date()) ? 'bg-blue-50/50' : ''}`}>
                        <div className="text-xs text-text-secondary mb-1">{format(day, 'EEE', {locale: zhCN})}</div>
                        <div className={`text-lg font-bold ${isSameDay(day, new Date()) ? 'text-primary' : ''}`}>{format(day, 'd')}</div>
                    </div>
                ))}
            </div>

            {/* Body */}
            {timeSlots.map(hour => (
                <div key={hour} className="grid grid-cols-8 border-b border-gray-100 h-28">
                    <div className="p-2 text-center text-xs text-text-hint border-r border-gray-100 relative bg-gray-50/30">
                        <span className="-top-3 relative bg-white px-1">{hour}:00</span>
                    </div>
                    {weekDays.map(day => {
                        const classes = getClassesForDayAndTime(day, hour);
                        return (
                            <div key={day.toString()} className="border-r border-gray-100 relative group hover:bg-gray-50 transition-colors">
                                {classes.map(cls => {
                                    const teacher = teachers.find(t => t.id === cls.teacherId);
                                    return (
                                        <div 
                                            key={cls.id}
                                            className={`absolute inset-x-1 top-1 bottom-1 rounded-lg p-2 text-xs border cursor-pointer hover:brightness-95 hover:-translate-y-0.5 transition-all shadow-sm z-10 ${teacher?.color || 'bg-gray-100'} ${cls.status === '已签到' ? 'opacity-60 grayscale' : ''}`}
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                setSelectedClass(cls);
                                            }}
                                        >
                                            <div className="font-bold truncate text-gray-800">{cls.type}</div>
                                            <div className="truncate opacity-75 mt-1 flex items-center gap-1">
                                                <div className="w-1.5 h-1.5 rounded-full bg-current"></div>
                                                {teacher?.name}
                                            </div>
                                            {cls.status === '已签到' && (
                                                <div className="absolute top-1 right-1 text-green-600"><CheckCircle size={12} /></div>
                                            )}
                                        </div>
                                    )
                                })}
                                {/* Hover Add Button - Only show if empty */}
                                {classes.length === 0 && (
                                    <button 
                                        className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity z-0"
                                        onClick={() => alert(`在 ${format(day, 'MM-dd')} ${hour}:00 添加课程`)}
                                    >
                                        <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center text-primary hover:bg-primary hover:text-white transition-colors">
                                            <Plus size={20} />
                                        </div>
                                    </button>
                                )}
                            </div>
                        )
                    })}
                </div>
            ))}
        </div>
      </div>

      {/* Class Detail Modal */}
      {selectedClass && (
        <div className="absolute inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4" onClick={() => setSelectedClass(null)}>
            <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-200" onClick={e => e.stopPropagation()}>
                <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50">
                    <h3 className="text-lg font-bold">课程详情</h3>
                    <button onClick={() => setSelectedClass(null)} className="text-gray-400 hover:text-gray-600"><X size={20}/></button>
                </div>
                <div className="p-6 space-y-4">
                    <div className="flex items-center justify-between">
                        <span className="text-text-secondary">时间</span>
                        <span className="font-medium text-lg">{selectedClass.date} {selectedClass.time}</span>
                    </div>
                    <div className="flex items-center justify-between">
                        <span className="text-text-secondary">类型</span>
                        <span className="px-2 py-1 bg-blue-50 text-blue-700 rounded text-sm font-bold">{selectedClass.type}</span>
                    </div>
                    <div className="flex items-center justify-between">
                        <span className="text-text-secondary">教师</span>
                        <span className="font-medium">{teachers.find(t => t.id === selectedClass.teacherId)?.name}</span>
                    </div>
                    <div className="flex items-center justify-between">
                        <span className="text-text-secondary">学员</span>
                        <div className="text-right">
                            {selectedClass.studentId ? (
                                <span className="font-bold text-primary">{students.find(s => s.id === selectedClass.studentId)?.name}</span>
                            ) : (
                                <div className="flex flex-col items-end">
                                    <span className="font-bold text-secondary">团课 ({selectedClass.students?.length}人)</span>
                                    <span className="text-xs text-text-hint">{selectedClass.students?.map(id => students.find(s => s.id === id)?.name).join(', ')}</span>
                                </div>
                            )}
                        </div>
                    </div>
                    <div className="flex items-center justify-between">
                        <span className="text-text-secondary">状态</span>
                        <span className={`font-medium ${selectedClass.status === '已签到' ? 'text-green-600' : 'text-orange-500'}`}>{selectedClass.status}</span>
                    </div>
                </div>
                <div className="p-6 border-t border-gray-100 bg-gray-50 flex gap-3">
                    {selectedClass.status !== '已签到' ? (
                        <button 
                            onClick={() => handleCheckIn(selectedClass.id)}
                            className="flex-1 bg-primary text-white py-3 rounded-xl font-bold hover:bg-blue-900 transition-colors shadow-lg shadow-primary/20 active:scale-95 flex items-center justify-center gap-2"
                        >
                            <CheckCircle size={18} />
                            签到 (扣课时)
                        </button>
                    ) : (
                        <button className="flex-1 bg-gray-100 text-gray-400 py-3 rounded-xl font-bold cursor-not-allowed flex items-center justify-center gap-2">
                            <CheckCircle size={18} />
                            已完成签到
                        </button>
                    )}
                </div>
            </div>
        </div>
      )}
    </div>
  );
};

export default Schedule;
