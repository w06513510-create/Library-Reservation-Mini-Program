package org.dromara.message.utils;

import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.message.service.INotificationService;

/**
 * 站内通知发送助手（核心复用点）。
 * <p>供任意业务模块在触发点一行调用，向某 C 端用户发系统通知：
 * <pre>{@code NotificationHelper.send(receiverId, "订单已发货", "您的订单#1001已发货", "order", 1001L);}</pre>
 *
 * <p>两大保证：
 * <ol>
 *   <li><b>静默容错</b>：全程 try/catch，任何异常只记 {@code log.warn}，绝不外抛，绝不破坏调用方主流程。</li>
 *   <li><b>不污染主事务</b>：底层 {@link INotificationService#send} 以 {@code REQUIRES_NEW} 独立事务落库——
 *       调用方事务回滚不牵连已发通知，通知落库失败也不会把调用方事务标记为 rollback-only。</li>
 * </ol>
 * <p>权衡：REQUIRES_NEW 下，若主业务随后回滚，通知仍留存。若业务需强一致，请在业务提交后再调用本方法。
 *
 * @author ruoyi-template
 */
@Slf4j
public class NotificationHelper {

    private NotificationHelper() {
    }

    /**
     * 发送站内通知。
     *
     * @param receiverId 接收用户ID(app_user)；为 null 时静默跳过
     * @param title      标题
     * @param content    内容
     * @param bizType    关联业务类型(通用挂载点, 可空, 如 order/comment/system)
     * @param bizId      关联业务ID(通用挂载点, 可空)
     * @return 新通知ID；发送被跳过或失败时返回 null
     */
    public static Long send(Long receiverId, String title, String content, String bizType, Long bizId) {
        if (receiverId == null) {
            return null;
        }
        try {
            return SpringUtils.getBean(INotificationService.class)
                .send(receiverId, title, content, bizType, bizId);
        } catch (Exception e) {
            log.warn("站内通知发送失败(不影响主流程) receiverId={}, bizType={}, bizId={}, title={}, err={}",
                receiverId, bizType, bizId, title, e.getMessage());
            return null;
        }
    }

}
