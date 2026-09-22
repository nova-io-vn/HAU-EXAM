import type { UpdateProfileInput } from '../api/profileApi';

export function validateProfile(input: UpdateProfileInput): Partial<Record<keyof UpdateProfileInput, string>> {
  const errors: Partial<Record<keyof UpdateProfileInput, string>> = {};
  if (!input.fullName.trim()) errors.fullName = 'Vui lòng nhập họ và tên.';
  if (!input.email.trim() || !/^\S+@\S+\.\S+$/.test(input.email.trim())) errors.email = 'Email không hợp lệ.';
  if (input.phone && !/^[0-9+() .-]{7,20}$/.test(input.phone)) errors.phone = 'Số điện thoại không hợp lệ.';
  if (input.dateOfBirth && (Number.isNaN(Date.parse(input.dateOfBirth)) || new Date(input.dateOfBirth) >= new Date())) errors.dateOfBirth = 'Ngày sinh phải là ngày trong quá khứ.';
  return errors;
}

export function calculateAge(dateOfBirth: string | null): number | null {
  if (!dateOfBirth) return null;
  const birth = new Date(dateOfBirth);
  if (Number.isNaN(birth.getTime())) return null;
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  if (today.getMonth() < birth.getMonth() || (today.getMonth() === birth.getMonth() && today.getDate() < birth.getDate())) age -= 1;
  return age >= 0 ? age : null;
}
