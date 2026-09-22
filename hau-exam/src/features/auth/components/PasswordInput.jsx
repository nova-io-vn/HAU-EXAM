import { useState } from "react";
import { Input } from "../../../components/ui/Input";
import { Icon } from "../../../components/ui/Icon";
export function PasswordInput(props) {
  const [visible,setVisible]=useState(false);
  return <div className="password-field"><Input {...props} type={visible?"text":"password"} /><button type="button" className="password-visibility" aria-label={visible?"Ẩn mật khẩu":"Hiện mật khẩu"} onClick={()=>setVisible(value=>!value)}><Icon name={visible?"eyeOff":"eye"} size={18}/></button></div>;
}
