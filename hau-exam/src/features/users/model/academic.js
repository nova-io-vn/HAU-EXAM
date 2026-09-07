const rankLabels={NONE:'',PGS:'PGS',GS:'GS'}
const degreeLabels={NONE:'',CN:'CN',KS:'KS',THS:'ThS',TS:'TS'}
export function formatAcademicName(user={}){const name=(user.fullName||user.lecturerCode||'Giảng viên').trim();return [rankLabels[user.academicRank]||'',degreeLabels[user.academicDegree]||'',name].filter(Boolean).join(' ')}
export function formatRoleFaculty(user={},facultyId){const role=user.role==='SUBJECT_ADMIN'?'Quản trị viên chuyên môn':user.role==='SYSTEM_ADMIN'?'Quản trị viên hệ thống':'Giảng viên';const faculty=user.facultyName||user.faculty?.name||user.facultyId||facultyId;return `${role}${faculty?` • Khoa ${faculty}`:''}`}
export function initialsFromName(user={}){const source=(user.fullName||user.lecturerCode||'GV').trim();return source.split(/\s+/).filter(Boolean).slice(-2).map(part=>part[0]).join('').toUpperCase()}
