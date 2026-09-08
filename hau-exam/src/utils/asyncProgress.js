export const asyncStatusLabels={PENDING:'Đang chờ',PROCESSING:'Đang xử lý',COMPLETED:'Hoàn tất',FAILED:'Thất bại',RETRYING:'Đang thử lại',CANCELLED:'Đã hủy'}

export function formatElapsedTime(seconds){const value=Math.max(0,Math.floor(Number(seconds)||0));const hours=Math.floor(value/3600);const minutes=Math.floor((value%3600)/60);const secs=value%60;const pad=part=>String(part).padStart(2,'0');return hours?`${pad(hours)}:${pad(minutes)}:${pad(secs)}`:`${pad(minutes)}:${pad(secs)}`}
export function toEpochMs(value){if(!value)return null;const ms=value instanceof Date?value.getTime():new Date(value).getTime();return Number.isFinite(ms)?ms:null}
export function getElapsedSeconds(startedAt,endedAt,now=Date.now()){const start=toEpochMs(startedAt);if(start===null)return 0;const end=toEpochMs(endedAt)??now;return Math.max(0,Math.floor((end-start)/1000))}
export function realProgress(value){const number=Number(value);return Number.isFinite(number)&&number>=0&&number<=100?number:null}
