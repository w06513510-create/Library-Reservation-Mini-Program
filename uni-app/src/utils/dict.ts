// ============================================================
// 本地字典：label + tag 色映射（零字典接口请求）。
// 基座只给"引擎" + 一个示例字典；各业务字典表自行补充。
// ============================================================

export interface DictOption {
  label: string;
  /** Element 风格标签色：primary/success/info/warning/danger */
  tag: 'primary' | 'success' | 'info' | 'warning' | 'danger';
}
export type DictMap = Record<string | number, DictOption>;

/** 标签色值映射 */
export const TAG_COLOR: Record<string, string> = {
  primary: '#409eff',
  success: '#67c23a',
  info: '#909399',
  warning: '#e6a23c',
  danger: '#f56c6c'
};

/** 取字典文案；查不到回退为原值字符串 */
export function dictLabel(map: DictMap, value: string | number): string {
  const v = map[value];
  return v ? v.label : (value == null ? '' : String(value));
}

/** 取字典标签色值；查不到回退 info 灰 */
export function dictColor(map: DictMap, value: string | number): string {
  const v = map[value];
  return v ? TAG_COLOR[v.tag] || TAG_COLOR.info : TAG_COLOR.info;
}

/** 金额格式化 ¥xx.xx */
export function money(v: number | string | null | undefined): string {
  const n = Number(v ?? 0);
  return '¥' + (isNaN(n) ? '0.00' : n.toFixed(2));
}

// —— 示例字典：审核状态（业务字典请仿此在各自项目定义）——
export const AUDIT_STATUS: DictMap = {
  0: { label: '待审核', tag: 'warning' },
  1: { label: '已通过', tag: 'success' },
  2: { label: '已驳回', tag: 'danger' }
};
