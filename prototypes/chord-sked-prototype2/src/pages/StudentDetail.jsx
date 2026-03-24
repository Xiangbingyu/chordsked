import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { students } from '../lib/mockData';
import { ArrowLeft, User, Phone, Calendar, BookOpen, Music } from 'lucide-react';

const StudentDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const student = students.find(s => s.id === parseInt(id));

  if (!student) {
    return <div className="p-8 text-center text-text-secondary">未找到学员信息</div>;
  }

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col h-[calc(100vh-8rem)] animate-in fade-in duration-500">
      <div className="p-6 border-b border-gray-100 flex items-center gap-4">
        <button onClick={() => navigate(-1)} className="p-2 hover:bg-gray-50 rounded-lg text-text-secondary transition-colors">
          <ArrowLeft size={20} />
        </button>
        <h2 className="text-xl font-bold">学员详情</h2>
      </div>

      <div className="flex-1 overflow-auto p-8 custom-scrollbar">
        {/* Basic Info Card */}
        <div className="bg-gradient-to-br from-primary to-blue-600 rounded-2xl p-8 text-white shadow-lg mb-8 relative overflow-hidden">
           <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl -mr-16 -mt-16"></div>
           <div className="relative z-10 flex items-start gap-6">
              <div className="w-20 h-20 bg-white/20 backdrop-blur-md rounded-full flex items-center justify-center text-3xl font-bold border-2 border-white/30">
                  {student.name[0]}
              </div>
              <div className="flex-1">
                  <div className="flex justify-between items-start">
                      <div>
                        <h1 className="text-3xl font-bold mb-2">{student.name}</h1>
                        <div className="flex gap-3 text-white/80 text-sm">
                            <span className="flex items-center gap-1"><User size={14}/> {student.age}岁</span>
                            <span className="flex items-center gap-1"><Music size={14}/> {student.instrument} • {student.level}</span>
                            <span className="flex items-center gap-1"><Calendar size={14}/> {student.joinedDate} 入学</span>
                        </div>
                      </div>
                      <div className="text-right">
                          <div className="text-sm text-white/70 mb-1">剩余课时</div>
                          <div className="text-4xl font-bold">{student.balance}</div>
                      </div>
                  </div>
                  
                  <div className="mt-6 pt-6 border-t border-white/20 flex gap-8">
                      <div>
                          <div className="text-xs text-white/60 mb-1">家长姓名</div>
                          <div className="font-medium">{student.parent}</div>
                      </div>
                      <div>
                          <div className="text-xs text-white/60 mb-1">联系电话</div>
                          <div className="font-medium flex items-center gap-2">
                              <Phone size={14} /> {student.phone}
                          </div>
                      </div>
                  </div>
              </div>
           </div>
        </div>

        {/* Growth Record Timeline */}
        <h3 className="text-lg font-bold mb-6 flex items-center gap-2">
            <BookOpen className="text-primary" size={20} />
            成长记录
        </h3>
        
        <div className="relative border-l-2 border-gray-100 ml-3 space-y-8 pb-8">
            {student.records && student.records.length > 0 ? student.records.map(record => (
                <div key={record.id} className="ml-8 relative">
                    <div className={`absolute -left-[41px] w-5 h-5 rounded-full border-4 border-white shadow-sm ${
                        record.type === '上课' ? 'bg-primary' : 'bg-secondary'
                    }`}></div>
                    
                    <div className="bg-gray-50 rounded-xl p-5 border border-gray-100 hover:shadow-md transition-shadow">
                        <div className="flex justify-between items-start mb-2">
                            <span className={`text-xs font-bold px-2 py-1 rounded ${
                                record.type === '上课' ? 'bg-blue-100 text-blue-700' : 'bg-orange-100 text-orange-700'
                            }`}>{record.type}</span>
                            <span className="text-sm text-text-secondary">{record.date}</span>
                        </div>
                        <p className="font-medium text-text-main mb-2">{record.content}</p>
                        <div className="text-sm text-text-secondary flex gap-4">
                            <span>教师: {record.teacher}</span>
                            {record.feedback && <span className="italic">"{record.feedback}"</span>}
                        </div>
                    </div>
                </div>
            )) : (
                <div className="ml-8 text-text-hint text-sm">暂无记录</div>
            )}
        </div>
      </div>
    </div>
  );
};

export default StudentDetail;
