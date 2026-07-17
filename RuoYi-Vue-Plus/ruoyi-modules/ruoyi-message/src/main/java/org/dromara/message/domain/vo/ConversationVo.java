package org.dromara.message.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 会话列表视图对象（非数据库表；由 app_message 按对端聚合而来）
 *
 * @author ruoyi-template
 */
@Data
public class ConversationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 对端用户ID */
    private Long peerId;

    /** 对端昵称 */
    private String peerNickname;

    /** 对端头像 */
    private String peerAvatar;

    /** 最近一条消息内容 */
    private String lastContent;

    /** 最近一条消息时间 */
    private Date lastTime;

    /** 该会话中对端发我且未读的条数 */
    private Integer unread;

}
