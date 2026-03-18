import React, { useState } from 'react';
import { Search, Filter, MoreHorizontal, Phone, Music, ChevronRight } from 'lucide-react';
import { students as initialStudents } from '../lib/mockData';
import { useNavigate } from 'react-router-dom';

const Students = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [students, setStudents] = useState(initialStudents);
  const navigate = useNavigate();

  const filteredStudents = students.filter(student => 
    student.name.includes(searchTerm) || student.phone.includes(searchTerm)
  );

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col h-[calc(100vh-8rem)] animate-in fade-in duration-500">
        {/* Header */}
        <div className="p-6 border-b border-gray-100 flex justify-between items-center">
            <h2 className="text-xl font-bold">学员管理</h2>
            <div className="flex gap-4">
                <div className="relative group">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-text-hint group-focus-within:text-primary transition-colors" size={18} />
                    <input 
                        type="text" 
                        placeholder="搜索学员姓名/手机号" 
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="pl-10 pr-4 py-2 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-primary/20 w-64 outline-none transition-all placeholder:text-text-hint"
                    />
                </div>
                <button className="flex items-center gap-2 px-4 py-2 border border-gray-200 rounded-xl hover:bg-gray-50 hover:border-gray-300 transition-all text-text-secondary">
                    <Filter size={18} />
                    <span>筛选</span>
                </button>
                <button className="bg-primary text-white px-6 py-2 rounded-xl hover:bg-blue-900 hover:shadow-lg hover:shadow-primary/20 transition-all active:scale-95">
                    新增学员
                </button>
            </div>
        </div>

        {/* Table */}
        <div className="flex-1 overflow-auto custom-scrollbar">
            <table className="w-full text-left">
                <thead className="bg-gray-50/80 backdrop-blur-sm text-text-secondary text-sm sticky top-0 z-10">
                    <tr>
                        <th className="px-6 py-4 font-medium">学员信息</th>
                        <th className="px-6 py-4 font-medium">家长联系方式</th>
                        <th className="px-6 py-4 font-medium">当前级别</th>
                        <th className="px-6 py-4 font-medium">剩余课时</th>
                        <th className="px-6 py-4 font-medium">入读时间</th>
                        <th className="px-6 py-4 font-medium text-right">操作</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                    {filteredStudents.length > 0 ? filteredStudents.map(student => (
                        <tr 
                            key={student.id} 
                            onClick={() => navigate(`/students/${student.id}`)}
                            className="group hover:bg-blue-50/30 transition-colors cursor-pointer"
                        >
                            <td className="px-6 py-4">
                                <div className="flex items-center gap-3">
                                    <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-100 to-indigo-100 text-primary flex items-center justify-center font-bold text-sm shadow-sm group-hover:scale-110 transition-transform">
                                        {student.name.slice(0, 1)}
                                    </div>
                                    <div>
                                        <div className="font-bold text-text-main">{student.name}</div>
                                        <div className="text-xs text-text-hint">{student.age}岁</div>
                                    </div>
                                </div>
                            </td>
                            <td className="px-6 py-4">
                                <div className="flex flex-col">
                                    <span className="text-text-main font-medium">{student.parent}</span>
                                    <div className="flex items-center gap-1 text-xs text-text-secondary mt-1 group-hover:text-primary transition-colors">
                                        <Phone size={12} />
                                        {student.phone}
                                    </div>
                                </div>
                            </td>
                            <td className="px-6 py-4">
                                <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium border ${
                                    student.level === '体验' 
                                        ? 'bg-orange-50 text-orange-600 border-orange-100' 
                                        : 'bg-blue-50 text-blue-600 border-blue-100'
                                }`}>
                                    <Music size={12} />
                                    {student.level}
                                </span>
                            </td>
                            <td className="px-6 py-4">
                                <div className="flex items-center gap-2">
                                    <div className="w-16 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                                        <div 
                                            className={`h-full rounded-full ${student.balance < 5 ? 'bg-error' : 'bg-success'}`} 
                                            style={{ width: `${Math.min(student.balance * 5, 100)}%` }}
                                        ></div>
                                    </div>
                                    <span className={`text-sm font-medium ${student.balance < 5 ? 'text-error' : 'text-text-main'}`}>
                                        {student.balance} 课时
                                    </span>
                                </div>
                            </td>
                            <td className="px-6 py-4 text-text-secondary text-sm">
                                {student.joinedDate}
                            </td>
                            <td className="px-6 py-4 text-right">
                                <div className="flex items-center justify-end gap-2 text-text-secondary">
                                    <span className="text-xs opacity-0 group-hover:opacity-100 transition-opacity">查看详情</span>
                                    <ChevronRight size={16} />
                                </div>
                            </td>
                        </tr>
                    )) : (
                        <tr>
                            <td colSpan="6" className="px-6 py-8 text-center text-text-hint">
                                没有找到匹配的学员
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    </div>
  );
};

export default Students;
