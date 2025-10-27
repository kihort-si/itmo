export const formatCurrency = (value: number): string => {
  if (isNaN(value)) return '';
  return value.toLocaleString('ru-RU') + ' $';
}