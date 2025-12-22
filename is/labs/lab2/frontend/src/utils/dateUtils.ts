export const formatDateTime = (dateArray: number[]): string => {
  if (!dateArray?.length) return '';

  const [year, month, day, hour, minute, second, nano] = dateArray;
  const ms: number = Math.floor((nano || 0) / 1_000_000);

  const date = new Date(year, month - 1, day, hour, minute, second, ms);

  const days: string = date.getDate().toString().padStart(2, '0');
  const months: string = (date.getMonth() + 1).toString().padStart(2, '0');
  const years: number = date.getFullYear();
  const hours: string = date.getHours().toString().padStart(2, '0');
  const minutes: string = date.getMinutes().toString().padStart(2, '0');

  return `${days}.${months}.${years} ${hours}:${minutes}`;
}