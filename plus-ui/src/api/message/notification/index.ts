import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { NotificationVO, NotificationQuery, NotificationSendForm } from './types';

// 查询站内通知列表
export function listNotification(query: NotificationQuery): AxiosPromise<NotificationVO[]> {
  return request({
    url: '/message/notification/list',
    method: 'get',
    params: query
  });
}

// 发送站内通知
export function sendNotification(data: NotificationSendForm) {
  return request({
    url: '/message/notification/send',
    method: 'post',
    data: data
  });
}
