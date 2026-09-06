import {getErrorMessage} from '../../../services/api/errorMessages'

export function getAuthErrorMessage(error,fallback='Không thể hoàn tất yêu cầu. Vui lòng thử lại.'){
  return getErrorMessage(error,fallback)
}
