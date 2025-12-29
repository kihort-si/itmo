export const formatDateTime = (dateValue: string | number[]): string => {
  if (!dateValue) return '';

  let date: Date;

  if (Array.isArray(dateValue)) {
    const [year, month, day, hour, minute, second, nano] = dateValue;
    const ms: number = Math.floor((nano || 0) / 1_000_000);
    date = new Date(year, month - 1, day, hour, minute, second, ms);
  } else if (typeof dateValue === 'string') {
    date = new Date(dateValue);
  } else {
    return '';
  }

  if (isNaN(date.getTime())) {
    return '';
  }

  const days: string = date.getDate().toString().padStart(2, '0');
  const months: string = (date.getMonth() + 1).toString().padStart(2, '0');
  const years: number = date.getFullYear();
  const hours: string = date.getHours().toString().padStart(2, '0');
  const minutes: string = date.getMinutes().toString().padStart(2, '0');

  return `${days}.${months}.${years} ${hours}:${minutes}`;
}