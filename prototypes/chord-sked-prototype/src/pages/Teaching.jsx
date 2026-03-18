import React, { useState } from 'react';
import { PlayCircle, Mic, FileText, Search, Filter, CheckCircle, Clock } from 'lucide-react';
import { homework as initialHomework, students, teachers } from '../lib/mockData';

const Teaching = () => {
  const [homeworkList, setHomeworkList] = useState(initialHomework);
  const [statusFilter, setStatusFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('all');

  const handleGrade = (id) => {
    const feedback = prompt('请输入评语：', '练习得不错，继续加油！');
    if (feedback) {
      setHomeworkList(homeworkList.map(h => 
        h.id === id ? { ...h, status: '已批改', feedback } : h
      ));
    }
  };

  const filteredHomework = homeworkList.filter(h => {
    const matchesStatus = statusFilter === 'all' || 
      (statusFilter === 'pending' && h.status === '待批改') ||
      (statusFilter === 'graded' && h.status === '已批改');
    
    const matchesType = typeFilter === 'all' || h.type === typeFilter;
    
    const studentName = students.find(s => s.id === h.studentId)?.name || '';
    const matchesSearch = studentName.includes(searchTerm) || h.title.includes(searchTerm);

    return matchesStatus && matchesType && matchesSearch;
  });

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col h-[calc(100vh-8rem)] animate-in fade-in duration-500">
        <div className="p-6 border-b border-gray-100 flex flex-col gap-4">
            <div className="flex justify-between items-center">
                <h2 className="text-xl font-bold">作业管理</h2>
                <div className="flex gap-4">
                     <div className="relative group">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-text-hint group-focus-within:text-primary transition-colors" size={18} />
                        <input 
                            type="text" 
                            placeholder="搜索学员/作业标题" 
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10 pr-4 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary/20 w-64 outline-none transition-all placeholder:text-text-hint"
                        />
                    </div>
                    <select 
                        value={typeFilter}
                        onChange={(e) => setTypeFilter(e.target.value)}
                        className="px-4 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary/20 outline-none text-sm"
                    >
                        <option value="all">所有类型</option>
                        <option value="视频">视频作业</option>
                        <option value="音频">音频作业</option>
                        <option value="文字">文字作业</option>
                    </select>
                    <select 
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="px-4 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary/20 outline-none text-sm"
                    >
                        <option value="all">所有状态</option>
                        <option value="pending">待批改</option>
                        <option value="graded">已批改</option>
                    </select>
                </div>
            </div>
        </div>

        <div className="flex-1 overflow-auto custom-scrollbar">
            <table className="w-full text-left">
                <thead className="bg-gray-50/80 backdrop-blur-sm text-text-secondary text-sm sticky top-0 z-10">
                    <tr>
                        <th className="px-6 py-4 font-medium">学员</th>
                        <th className="px-6 py-4 font-medium">作业内容</th>
                        <th className="px-6 py-4 font-medium">类型</th>
                        <th className="px-6 py-4 font-medium">指导教师</th>
                        <th className="px-6 py-4 font-medium">提交时间</th>
                        <th className="px-6 py-4 font-medium">状态</th>
                        <th className="px-6 py-4 font-medium">评语</th>
                        <th className="px-6 py-4 font-medium text-right">操作</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                    {filteredHomework.map(hw => {
                        const student = students.find(s => s.id === hw.studentId);
                        const teacher = teachers.find(t => t.id === hw.teacherId);
                        
                        return (
                            <tr key={hw.id} className="group hover:bg-gray-50 transition-colors">
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-3">
                                        <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center font-bold text-xs text-gray-600">
                                            {student?.name[0]}
                                        </div>
                                        <span className="font-medium text-text-main">{student?.name}</span>
                                    </div>
                                </td>
                                <td className="px-6 py-4 font-medium text-text-main">{hw.title}</td>
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-2 text-sm text-text-secondary">
                                        {hw.type === '视频' && <PlayCircle size={16} className="text-secondary" />}
                                        {hw.type === '音频' && <Mic size={16} className="text-secondary" />}
                                        {hw.type === '文字' && <FileText size={16} className="text-secondary" />}
                                        <span>{hw.type}</span>
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-text-secondary text-sm">{teacher?.name}</td>
                                <td className="px-6 py-4 text-text-secondary text-sm">{hw.submittedAt || '-'}</td>
                                <td className="px-6 py-4">
                                    {hw.status === '待批改' ? (
                                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-50 text-orange-600">
                                            <Clock size={12} /> 待批改
                                        </span>
                                    ) : hw.status === '已批改' ? (
                                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-50 text-green-600">
                                            <CheckCircle size={12} /> 已批改
                                        </span>
                                    ) : (
                                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-500">
                                            未提交
                                        </span>
                                    )}
                                </td>
                                <td className="px-6 py-4 text-sm text-text-secondary max-w-xs truncate">
                                    {hw.feedback || '-'}
                                </td>
                                <td className="px-6 py-4 text-right">
                                    {hw.status === '待批改' && (
                                        <button 
                                            onClick={() => handleGrade(hw.id)}
                                            className="text-primary hover:text-blue-700 font-medium text-sm hover:underline"
                                        >
                                            批改
                                        </button>
                                    )}
                                    {hw.status === '已批改' && (
                                         <button className="text-text-secondary hover:text-text-main font-medium text-sm hover:underline">查看</button>
                                    )}
                                </td>
                            </tr>
                        );
                    })}
                    {filteredHomework.length === 0 && (
                        <tr>
                            <td colSpan="8" className="px-6 py-12 text-center text-text-hint">
                                暂无符合条件的作业记录
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    </div>
  );
};

export default Teaching;
