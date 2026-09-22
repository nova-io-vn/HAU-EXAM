import type {TextInputProps} from 'react-native';
import {Input} from './Input';

export function PasswordInput(props:TextInputProps&{label:string;error?:string}){
  return <Input {...props} secureTextEntry/>;
}
