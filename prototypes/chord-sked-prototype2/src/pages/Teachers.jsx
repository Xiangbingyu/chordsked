import React, { useState } from 'react';
import { teachers as initialTeachers } from '../lib/mockData';
import { User, Clock, Users, Edit2, Save, X, Search, Filter } from 'lucide-react';

const Teachers = () => {
  const [teachers, setTeachers] = useState(initialTeachers);
  const [editingId, setEditingId] = useState(null);
  const [editForm, setEditForm] = useState({});
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('all');

  const startEdit = (teacher) => {
    setEditingId(teacher.id);
    setEditForm(teacher);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditForm({});
  };

  const saveEdit = () => {
    setTeachers(teachers.map(t => t.id === editingId ? editForm : t));
    setEditingId(null);
  };

  const filteredTeachers = teachers.filter(t => {
      const matchesSearch = t.name.includes(searchTerm) || t.instrument.includes(searchTerm);
      const matchesType = typeFilter === 'all' || t.type === typeFilter;
      return matchesSearch && matchesType;
  });

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col h-[calc(100vh-8rem)] animate-in fade-in duration-500">
      <div className="p-6 border-b border-gray-100 flex flex-col gap-4">
        <div className="flex justify-between items-center">
             <h2 className="text-xl font-bold">教师管理</h2>
             <div className="flex gap-4">
                 <div className="relative group">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-text-hint group-focus-within:text-primary transition-colors" size={18} />
                    <input 
                        type="text" 
                        placeholder="搜索教师姓名/科目" 
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
                    <option value="正式">正式教师</option>
                    <option value="团课">团课教师</option>
                </select>
                <button className="bg-primary text-white px-4 py-2 rounded-lg hover:bg-blue-900 transition-colors shadow-sm text-sm">
                  新增教师
                </button>
             </div>
        </div>
      </div>

      <div className="flex-1 overflow-auto custom-scrollbar">
        <table className="w-full text-left">
            <thead className="bg-gray-50/80 backdrop-blur-sm text-text-secondary text-sm sticky top-0 z-10">
                <tr>
                    <th className="px-6 py-4 font-medium">教师信息</th>
                    <th className="px-6 py-4 font-medium">授课类型</th>
                    <th className="px-6 py-4 font-medium">最大承载</th>
                    <th className="px-6 py-4 font-medium">工作时间</th>
                    <th className="px-6 py-4 font-medium">简介</th>
                    <th className="px-6 py-4 font-medium text-right">操作</th>
                </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
                {filteredTeachers.map(teacher => (
                    <tr key={teacher.id} className="group hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4">
                            <div className="flex items-center gap-3">
                                <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold ${teacher.color.replace('bg-', 'bg-opacity-20 bg-').replace('border-', 'text-')}`}>
                                    {teacher.name[0]}
                                </div>
                                <div>
                                    <h3 className="font-bold text-text-main">{teacher.name}</h3>
                                    <span className="text-xs text-text-secondary">{teacher.instrument}</span>
                                </div>
                            </div>
                        </td>
                        <td className="px-6 py-4">
                            <span className={`px-2 py-1 rounded text-xs font-medium border ${teacher.type === '正式' ? 'bg-blue-50 text-blue-600 border-blue-100' : 'bg-green-50 text-green-600 border-green-100'}`}>
                                {teacher.type}
                            </span>
                        </td>
                        <td className="px-6 py-4">
                            {editingId === teacher.id ? (
                                <input 
                                    type="number" 
                                    value={editForm.capacity} 
                                    onChange={e => setEditForm({...editForm, capacity: parseInt(e.target.value)})}
                                    className="w-20 p-1 border border-gray-200 rounded focus:ring-2 focus:ring-primary/20 outline-none text-sm"
                                />
                            ) : (
                                <span className="flex items-center gap-1 text-sm text-text-secondary">
                                    <Users size={14} /> {teacher.capacity}人
                                </span>
                            )}
                        </td>
                        <td className="px-6 py-4">
                             {editingId === teacher.id ? (
                                <input 
                                    type="text" 
                                    value={editForm.workingHours} 
                                    onChange={e => setEditForm({...editForm, workingHours: e.target.value})}
                                    className="w-full p-1 border border-gray-200 rounded focus:ring-2 focus:ring-primary/20 outline-none text-sm"
                                />
                            ) : (
                                <div className="flex items-center gap-1 text-sm text-text-secondary max-w-xs truncate" title={teacher.workingHours}>
                                    <Clock size={14} /> {teacher.workingHours}
                                </div>
                            )}
                        </td>
                        <td className="px-6 py-4">
                             <div className="text-sm text-text-secondary max-w-xs truncate" title={teacher.bio}>
                                {teacher.bio}
                            </div>
                        </td>
                        <td className="px-6 py-4 text-right">
                             {editingId === teacher.id ? (
                                <div className="flex justify-end gap-2">
                                    <button onClick={saveEdit} className="p-1 text-green-600 hover:bg-green-50 rounded"><Save size={18}/></button>
                                    <button onClick={cancelEdit} className="p-1 text-red-600 hover:bg-red-50 rounded"><X size={18}/></button>
                                </div>
                            ) : (
                                <button onClick={() => startEdit(teacher)} className="p-2 text-text-secondary hover:text-primary hover:bg-blue-50 rounded-lg transition-colors">
                                    <Edit2 size={18} />
                                </button>
                            )}
                        </td>
                    </tr>
                ))}
                 {filteredTeachers.length === 0 && (
                    <tr>
                        <td colSpan="6" className="px-6 py-12 text-center text-text-hint">
                            暂无符合条件的教师
                        </td>
                    </tr>
                )}
            </tbody>
        </table>
      </div>
    </div>
  );
};

export default Teachers;
