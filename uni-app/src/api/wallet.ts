import { request } from '../utils/request';

/** 钱包视图对象（与后端 AppWalletVo 对应） */
export interface AppWalletVo {
  id: number;
  userId: number;
  balance: number;
  frozen: number;
  totalRecharge: number;
}

/** 资金流水视图对象（与后端 AppFundFlowVo 对应） */
export interface AppFundFlowVo {
  id: number;
  userId: number;
  /** 1入 2出 */
  direction: number;
  amount: number;
  balanceAfter: number;
  bizType?: string;
  bizNo?: string;
  remark?: string;
  createTime?: string;
}

/** 创建充值单返回（与后端 AppRechargeCreateVo 对应） */
export interface AppRechargeCreateVo {
  outTradeNo: string;
  channel: string;
  alipayConfigured: boolean;
  payForm?: string;
}

/** 我的钱包（不存在则后端初始化） */
export function apiGetWallet() {
  return request<AppWalletVo>({ url: '/app/wallet/me' });
}

/** 我的钱包对平自检：返回差额（0 为平） */
export function apiCheckInvariant() {
  return request<number>({ url: '/app/wallet/checkInvariant' });
}

/** 创建充值单 */
export function apiCreateRecharge(amount: number) {
  return request<AppRechargeCreateVo>({
    url: '/app/recharge/create',
    method: 'POST',
    params: { amount }
  });
}

/** 模拟即时到账（仅未配置支付宝时可用） */
export function apiSimulatePaid(outTradeNo: string) {
  return request<void>({
    url: '/app/recharge/simulatePaid',
    method: 'POST',
    params: { outTradeNo }
  });
}

/** 主动查单结算（已配置支付宝时，付款后调用） */
export function apiQueryRecharge(outTradeNo: string) {
  return request<boolean>({ url: '/app/recharge/query', params: { outTradeNo } });
}
