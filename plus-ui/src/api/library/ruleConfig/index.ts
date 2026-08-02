import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { RuleConfigVO, RuleConfigForm, RuleConfigQuery } from '@/api/library/ruleConfig/types';

/** 查询规则配置列表 */
export const listRuleConfig = (query?: RuleConfigQuery): AxiosPromise<RuleConfigVO[]> => {
  return request({
    url: '/library/ruleConfig/list',
    method: 'get',
    params: query
  });
};

/** 查询规则配置详细 */
export const getRuleConfig = (id: string | number): AxiosPromise<RuleConfigVO> => {
  return request({
    url: '/library/ruleConfig/' + id,
    method: 'get'
  });
};

/** 新增规则配置 */
export const addRuleConfig = (data: RuleConfigForm) => {
  return request({
    url: '/library/ruleConfig',
    method: 'post',
    data: data
  });
};

/** 修改规则配置 */
export const updateRuleConfig = (data: RuleConfigForm) => {
  return request({
    url: '/library/ruleConfig',
    method: 'put',
    data: data
  });
};

/** 删除规则配置 */
export const delRuleConfig = (id: string | number | Array<string | number>) => {
  return request({
    url: '/library/ruleConfig/' + id,
    method: 'delete'
  });
};
