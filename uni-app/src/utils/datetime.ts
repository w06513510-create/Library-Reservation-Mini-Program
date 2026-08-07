// ============================================================
// 统一中国时间(Asia/Shanghai, UTC+8)工具
// 原理：Date.now() 是 UTC 纪元毫秒(与设备/服务器时区无关)，+8h 后用 getUTC* 读出即"中国墙钟"。
// 这样无论本机在哪个时区(本机是 GMT+4)，前端算出的"今天/现在"都是中国时间，与后端 Asia/Shanghai 对齐。
// ============================================================

function p(n: number): string {
  return n < 10 ? '0' + n : '' + n;
}

/** 中国时间的"当前时刻"（其 getUTC* 字段即中国墙钟值） */
function cnDate(offsetDays = 0): Date {
  return new Date(Date.now() + 8 * 3600 * 1000 + offsetDays * 86400 * 1000);
}

/** 中国当天 YYYY-MM-DD */
export function chinaToday(): string {
  const d = cnDate();
  return `${d.getUTCFullYear()}-${p(d.getUTCMonth() + 1)}-${p(d.getUTCDate())}`;
}

/** 中国当天 +n 天的 YYYY-MM-DD（n 可为负） */
export function chinaDatePlus(n: number): string {
  const d = cnDate(n);
  return `${d.getUTCFullYear()}-${p(d.getUTCMonth() + 1)}-${p(d.getUTCDate())}`;
}

/** 中国当前"距零点分钟数"（0–1439），用于与时段起止比较 */
export function chinaMinutesOfDay(): number {
  const d = cnDate();
  return d.getUTCHours() * 60 + d.getUTCMinutes();
}

/** 中国当前小时（0–23），用于问候语等 */
export function chinaHour(): number {
  return cnDate().getUTCHours();
}

/** "HH:mm[:ss]" → 距零点分钟数 */
export function hmToMinutes(hm: string): number {
  const [h, m] = hm.split(':');
  return Number(h) * 60 + Number(m);
}
