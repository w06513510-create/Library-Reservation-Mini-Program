export interface ReaderVO {
  /** 读者档案ID */
  id: string | number;
  /** 关联C端账号ID（app_user，1:1） */
  userId: number;
  /** 学号/校园卡号（实名唯一） */
  studentNo: string;
  /** 真实姓名 */
  realName: string;
  /** 院系 */
  college: string;
  /** 专业 */
  major: string;
  /** 当前信用分（0-100） */
  creditScore: number;
  /** 守信(履约)次数 */
  performCount: number;
  /** 是否黑名单：0否 1是 */
  blacklistFlag: number;
  /** 黑名单暂停到期时间 */
  blacklistEndTime: string;
  /** 状态：0正常 1受限 2停用 */
  status: number;
}

export interface ReaderForm extends BaseEntity {
  id?: string | number;
  userId?: number;
  studentNo?: string;
  realName?: string;
  college?: string;
  major?: string;
  creditScore?: number;
  performCount?: number;
  blacklistFlag?: number;
  blacklistEndTime?: string;
  status?: number;
}

export interface ReaderQuery extends PageQuery {
  studentNo?: string;
  realName?: string;
  blacklistFlag?: number;
}
