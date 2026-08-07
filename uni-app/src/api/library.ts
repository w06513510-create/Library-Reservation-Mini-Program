import { request, type Resp } from '../utils/request';

// ============================================================
// 图书馆 C 端接口封装 —— 对接后端 /app/library/*（薄封装复用后台 Service）
// 列表接口返回 {code,msg,rows,total}；单体/动作返回 {code,msg,data}
// ============================================================

// ---------- 类型（与后端 VO 对齐，按需取字段） ----------
export interface FloorVo {
  id: number;
  floorName: string;
  venueId?: number;
  status?: number;
}

export interface SeatStatusVo {
  id: number;
  seatNo: string;
  areaId: number;
  areaName?: string;
  seatType?: number;
  hasPower?: number;
  posX?: number;
  posY?: number;
  seatStatus?: number;
  occupied?: boolean;
  // 所属桌子（一桌多座成组渲染）
  deskId?: number;
  deskNo?: string;
  capacity?: number;
  offsetX?: number;
  offsetY?: number;
}

export interface ReservationVo {
  id: number;
  readerId: number;
  seatId: number;
  seatNo?: string;
  reserveDate?: string;
  startTime?: string;
  endTime?: string;
  status: number; // 0待签到 1使用中 2暂离中 3已完成 4已取消 5已违约
  checkInTime?: string;
}

export interface ReaderVo {
  id: number;
  userId: number;
  studentNo: string;
  realName: string;
  college?: string;
  major?: string;
  creditScore: number;
  performCount?: number;
  blacklistFlag?: number;
  status?: number;
}

export interface CreditLogVo {
  id: number;
  readerId: number;
  delta: number;
  reasonType: number;
  reasonDesc?: string;
  scoreAfter: number;
  createTime?: string;
}

export interface ViolationVo {
  id: number;
  readerId: number;
  violationType: number;
  deductScore?: number;
  occurTime?: string;
  status: number; // 0有效 1已解除
}

export interface RuleConfigVo {
  id: number;
  ruleGroup?: string;
  ruleKey: string;
  ruleName?: string;
  ruleValue?: string;
  unit?: string;
  remark?: string;
}

export interface BookVo {
  id: number;
  title: string;
  author?: string;
  isbn?: string;
  publisher?: string;
  coverUrl?: string;
  callNo?: string;
  totalQty?: number;
  availQty?: number;
}

export interface LoanVo {
  id: number;
  readerId: number;
  bookTitle?: string;
  borrowTime?: string;
  dueTime?: string;
  status: number;
}

export interface HoldVo {
  id: number;
  readerId: number;
  bookId: number;
  bookTitle?: string;
  queueNo?: number;
  status: number;
}

// ---------- 选座预约 ----------
export const seatApi = {
  floors: () => request<FloorVo[]>({ url: '/app/library/seat/floors' }),
  status: (floorId: number, startTime?: string, endTime?: string) =>
    request<SeatStatusVo[]>({ url: '/app/library/seat/status', params: { floorId, startTime, endTime } }),
  reserve: (data: { seatId: number; reserveDate: string; startTime: string; endTime: string; source?: number }) =>
    request<void>({ url: '/app/library/seat/reserve', method: 'POST', data }),
  myReservations: (params: Record<string, any>) =>
    request<ReservationVo[]>({ url: '/app/library/seat/reservations', params }),
  checkIn: (id: number) => request<void>({ url: `/app/library/seat/checkIn/${id}`, method: 'PUT' }),
  away: (id: number) => request<void>({ url: `/app/library/seat/away/${id}`, method: 'PUT' }),
  back: (id: number) => request<void>({ url: `/app/library/seat/back/${id}`, method: 'PUT' }),
  leave: (id: number) => request<void>({ url: `/app/library/seat/leave/${id}`, method: 'PUT' }),
  cancel: (id: number) => request<void>({ url: `/app/library/seat/cancel/${id}`, method: 'PUT' })
};

// ---------- 读者中心 ----------
export const readerApi = {
  profile: () => request<ReaderVo>({ url: '/app/library/reader/profile' }),
  creditLogs: () => request<CreditLogVo[]>({ url: '/app/library/reader/credit/logs' }),
  violations: (params: Record<string, any>) =>
    request<ViolationVo[]>({ url: '/app/library/reader/violations', params }),
  appeals: (params: Record<string, any>) => request<any[]>({ url: '/app/library/reader/appeals', params }),
  appeal: (violationId: number, reason: string) =>
    request<void>({ url: '/app/library/reader/appeal', method: 'POST', data: { violationId, reason } }),
  rules: () => request<RuleConfigVo[]>({ url: '/app/library/reader/rules' })
};

// ---------- 图书 ----------
export const bookApi = {
  list: (params: Record<string, any>) => request<BookVo[]>({ url: '/app/library/book/list', params }),
  detail: (id: number) => request<BookVo>({ url: `/app/library/book/${id}` }),
  loans: (params: Record<string, any>) => request<LoanVo[]>({ url: '/app/library/book/loans', params }),
  renew: (loanId: number) => request<void>({ url: `/app/library/book/renew/${loanId}`, method: 'PUT' }),
  hold: (bookId: number) => request<void>({ url: `/app/library/book/hold/${bookId}`, method: 'POST' }),
  holds: (params: Record<string, any>) => request<HoldVo[]>({ url: '/app/library/book/holds', params }),
  cancelHold: (holdId: number) => request<void>({ url: `/app/library/book/hold/cancel/${holdId}`, method: 'PUT' })
};

export type { Resp };
