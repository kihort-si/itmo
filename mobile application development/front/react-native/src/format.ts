

export type Ccy = 'RUB' | 'USD' | 'EUR';

function localeNum(n: number, dp: number): string {

  const fixed = n.toFixed(dp);
  const [intPart, decPart] = fixed.split('.');

  const isNeg = intPart.startsWith('-');
  const intAbs = isNeg ? intPart.slice(1) : intPart;
  const withSep = intAbs.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
  const intOut = (isNeg ? '-' : '') + withSep;
  return decPart != null ? `${intOut}.${decPart}` : intOut;
}

export function fmtNum(n: number | null | undefined, dp = 2): string {
  if (n == null || isNaN(n as number)) return '—';
  return localeNum(n as number, dp);
}

export function fmtPct(n: number | null | undefined, dp = 2, sign = true): string {
  if (n == null || isNaN(n as number)) return '—';
  const s = (n as number).toFixed(dp);
  return (sign && (n as number) > 0 ? '+' : '') + s + '%';
}

export function fmtSigned(n: number | null | undefined, dp = 2): string {
  if (n == null || isNaN(n as number)) return '—';
  return ((n as number) > 0 ? '+' : '') + fmtNum(n as number, dp);
}

export function fmtMoney(n: number | null | undefined, ccy: Ccy, dp?: number): string {
  if (n == null || isNaN(n as number)) return '—';
  const sym = ccy === 'RUB' ? '₽' : ccy === 'USD' ? '$' : '€';
  const decimals = dp != null ? dp : 2;
  const s = localeNum(n as number, decimals);
  return ccy === 'RUB' ? `${s} ${sym}` : `${sym}${s}`;
}

export function fmtVol(n: number | null | undefined): string {
  if (n == null) return '—';
  const v = n as number;
  if (v >= 1e9) return (v / 1e9).toFixed(2) + 'B';
  if (v >= 1e6) return (v / 1e6).toFixed(2) + 'M';
  if (v >= 1e3) return (v / 1e3).toFixed(1) + 'K';
  return Math.round(v).toString();
}
