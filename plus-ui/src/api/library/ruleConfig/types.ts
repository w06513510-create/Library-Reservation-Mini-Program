export interface RuleConfigVO {
  /** 规则配置ID */
  id: string | number;
  /** 规则分组：seat座位/book图书/credit信用/task定时任务 */
  ruleGroup: string;
  /** 规则键 */
  ruleKey: string;
  /** 规则值 */
  ruleValue: string;
  /** 说明 */
  remark: string;
}

export interface RuleConfigForm extends BaseEntity {
  id?: string | number;
  ruleGroup?: string;
  ruleKey?: string;
  ruleValue?: string;
  remark?: string;
}

export interface RuleConfigQuery extends PageQuery {
  ruleGroup?: string;
  ruleKey?: string;
}
