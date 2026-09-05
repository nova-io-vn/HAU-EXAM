export const DEFAULT_MAX_DOCUMENT_BYTES=10485760
export const isActiveJob=job=>['PENDING','PROCESSING'].includes(job?.status)
export const jobTypeLabels={QUESTION_GENERATION:'Tạo câu hỏi',ANALYSIS:'Phân tích',CHAT:'Chatbot'}
export function validateDocument(file,maxBytes=DEFAULT_MAX_DOCUMENT_BYTES) {
  if(!file)return 'Vui lòng chọn tài liệu.'
  if(file.type!=='text/plain')return 'Chỉ hỗ trợ tài liệu text/plain (.txt) UTF-8.'
  if(file.size<=0)return 'Tài liệu không được rỗng.'
  if(file.size>maxBytes)return `Tài liệu vượt giới hạn ${(maxBytes/1048576).toFixed(1)} MiB.`
  return null
}
export function generationPayload({documentId,count,difficulty,topicId}) {
  if(!documentId)throw new Error('Vui lòng chọn tài liệu.')
  const amount=Number(count)
  if(!Number.isInteger(amount)||amount<1||amount>100)throw new Error('Số câu hỏi phải là số nguyên từ 1 đến 100.')
  if(topicId&&!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(topicId))throw new Error('Topic ID phải có định dạng UUID.')
  return {documentId,count:amount,difficulty:difficulty||null,topicId:topicId||null}
}
