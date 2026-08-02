-- =============================================================
-- 图书馆定时任务 SnailJob 注册（导入 library_reservation，需先导 ry_job.sql 的 sj_* 表）
-- 执行器：LibraryAutoJobExecutor(@JobExecutor name="libraryAutoJob")
-- 组 ruoyi_group / namespace dev；每 30s 触发一轮自动处置
-- 前提：SnailJob 服务端(127.0.0.1:17888)已起且 application-dev.yml 中 snail-job.enabled=true
-- =============================================================
SET NAMES utf8mb4;

DELETE FROM sj_job WHERE biz_id = 'library-auto-job';

INSERT INTO sj_job
(id, namespace_id, biz_id, group_name, job_name, args_str, args_type, next_trigger_at, job_status, task_type,
 route_key, executor_type, executor_info, trigger_type, trigger_interval, block_strategy, executor_timeout,
 max_retry_times, parallel_num, retry_interval, bucket_index, resident, notify_ids, owner_id, labels, description,
 ext_attrs, deleted, create_dt, update_dt)
VALUES
(2, 'dev', 'library-auto-job', 'ruoyi_group', 'library-auto-job', NULL, 1, UNIX_TIMESTAMP() * 1000, 1, 1,
 4, 1, 'libraryAutoJob', 2, 30, 1, 60,
 3, 1, 1, 116, 0, '', 1, '', '图书馆自动处置:超时释放/违约判定/信用恢复',
 '', 0, NOW(), NOW());
