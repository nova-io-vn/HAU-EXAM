import {getErrorMessage} from './errorMessages'

export class ApiError extends Error{
  constructor({status=0,code='NETWORK_ERROR',message,correlationId,errors}){
    super(getErrorMessage({status,code},message&&code==='NETWORK_ERROR'?message:undefined))
    this.name='ApiError'
    this.status=status
    this.code=code
    this.backendMessage=message
    this.correlationId=correlationId
    this.errors=errors
  }
}
