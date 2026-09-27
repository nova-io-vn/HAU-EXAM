const examples={
  "Mã môn học":"VD: IT101","Tên môn học":"VD: Lập trình Java","Tên chương":"VD: Chương 1 - Tổng quan Java","Tên chủ đề":"VD: Lập trình hướng đối tượng",
  "Mã giảng viên":"VD: A130124","Mã giảng viên / Cán bộ":"VD: A130124","Email":"VD: giangvien@hau.edu.vn","Số điện thoại":"VD: 0912 345 678","Điện thoại":"VD: 0912 345 678",
  "Tên đề thi":"VD: Kiểm tra giữa kỳ - Lập trình Java","Tên bộ đề":"VD: Kiểm tra giữa kỳ - Lập trình Java","Thời lượng":"VD: 60 phút","Thời lượng (phút)":"VD: 60",
  "Tên chương / chủ đề":"VD: Lập trình hướng đối tượng","Họ và tên":"VD: Nguyễn Văn A","Chủ đề":"VD: Cần hỗ trợ tạo đề thi",
  "Tìm kiếm":"Tìm theo tên, mã hoặc từ khóa..."
};
export function Input({label,error,helper,id,...props}){const inputId=id||props.name;const placeholder=props.placeholder??examples[label];return <label className="field" htmlFor={inputId}><span>{label}</span><input id={inputId} aria-invalid={Boolean(error)} aria-describedby={error||helper?`${inputId}-help`:undefined} placeholder={placeholder} {...props}/>{(error||helper)&&<small id={`${inputId}-help`} className={error?'field-error':''}>{error||helper}</small>}</label>}
