package org.dromara.interaction.constant;

import org.dromara.common.core.exception.ServiceException;

import java.util.Set;

/**
 * 互动动作常量。
 *
 * @author ruoyi-template
 */
public final class InteractionAction {

    /** 收藏 */
    public static final String FAVORITE = "favorite";
    /** 点赞 */
    public static final String LIKE = "like";
    /** 关注 */
    public static final String FOLLOW = "follow";

    /** 关注"人"时的业务类型（biz_id = 目标 app_user 的 id） */
    public static final String BIZ_TYPE_USER = "user";

    private static final Set<String> ALL = Set.of(FAVORITE, LIKE, FOLLOW);

    private InteractionAction() {
    }

    /** 校验动作合法，非法抛异常 */
    public static void validate(String action) {
        if (action == null || !ALL.contains(action)) {
            throw new ServiceException("非法互动动作: " + action + "（仅支持 favorite/like/follow）");
        }
    }

}
