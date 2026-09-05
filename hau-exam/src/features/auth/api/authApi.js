import {apiRequest} from '../../../services/api/client'
const AUTH_PATH='/api/v1/auth'
export const authApi={
  login:credentials=>apiRequest(`${AUTH_PATH}/login`,{method:'POST',body:credentials,skipRefresh:true}),
  register:registration=>apiRequest(`${AUTH_PATH}/register`,{method:'POST',body:registration,skipRefresh:true}),
  forgotPassword:identity=>apiRequest(`${AUTH_PATH}/forgot-password`,{method:'POST',body:identity,skipRefresh:true}),
  verifyOtp:verification=>apiRequest(`${AUTH_PATH}/verify-otp`,{method:'POST',body:verification,skipRefresh:true}),
  resetPassword:reset=>apiRequest(`${AUTH_PATH}/reset-password`,{method:'POST',body:reset,skipRefresh:true}),
  refresh:refreshToken=>apiRequest(`${AUTH_PATH}/refresh`,{method:'POST',body:{refreshToken},skipRefresh:true}),
  logout:refreshToken=>apiRequest(`${AUTH_PATH}/logout`,{method:'POST',body:{refreshToken},skipRefresh:true}),
}
