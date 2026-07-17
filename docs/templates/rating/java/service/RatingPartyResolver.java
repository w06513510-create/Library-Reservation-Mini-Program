package org.dromara.biz.service;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 评价方向解析器（扩展点）——把"根据业务单解析评价方向与双方"这段业务耦合逻辑从通用评价服务里解耦。
 * <p>提炼自跑腿 evaluate：比较当前登录用户与订单双方(studentId/runnerId)，定出 evalRole 与 toUserId。
 * <p><b>用法：</b>每种 bizType 各写一个 {@code @Component} 实现，在 {@link #resolve} 里：
 * ① 校验业务单存在且处于"可评价"状态（如已完成）；② 判断当前用户是甲还是乙，定出方向与被评价人；
 * ③ 无权评价则抛 {@code ServiceException}。
 *
 * @author ruoyi-template
 */
public interface RatingPartyResolver {

    /** 负责的业务类型，对应 rating.biz_type */
    String bizType();

    /**
     * 校验并解析评价方向与双方。
     *
     * @param bizId         被评业务主键
     * @param currentUserId 当前登录用户
     * @return 解析结果（方向/被评价人/单号）
     */
    Party resolve(Long bizId, Long currentUserId);

    /**
     * 解析结果
     */
    @Data
    @AllArgsConstructor
    class Party {
        /** 评价方向(1甲评乙 2乙评甲;单向固定1) */
        private Integer evalRole;
        /** 被评价人 */
        private Long toUserId;
        /** 业务单号(冗余展示用,可空) */
        private String bizNo;
    }

}
