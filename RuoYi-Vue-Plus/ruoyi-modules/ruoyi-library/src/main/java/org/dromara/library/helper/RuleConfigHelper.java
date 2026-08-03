package org.dromara.library.helper;

import lombok.RequiredArgsConstructor;
import org.dromara.library.domain.RuleConfig;
import org.dromara.library.mapper.RuleConfigMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则配置读取助手（把 biz_rule_config 的键值对接线进业务）。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>内存缓存 + 惰性加载：首次读取时把全表加载进 {@link #cache}，键为 {@code group.key}。</li>
 *   <li>写时失效：{@code RuleConfigServiceImpl} 增删改后调 {@link #refresh()}，下次读取自动重载。</li>
 *   <li>兜底默认：表为空 / 键缺失 / 值非法时返回调用方给的默认值——保证未接线/未灌种子时行为与旧硬编码一致。</li>
 * </ul>
 * 本项目单租户（000000），读取发生在约座请求或 SnailJob（已置 000000 租户上下文）内，故不区分租户缓存。
 *
 * @author library
 */
@Component
@RequiredArgsConstructor
public class RuleConfigHelper {

    private final RuleConfigMapper ruleConfigMapper;

    /** 分组常量（与 sql/seed_rule_config.sql 及 biz_rule_config.rule_group 严格一致） */
    public static final String GROUP_SEAT = "seat";
    public static final String GROUP_CREDIT = "credit";

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private volatile boolean loaded = false;

    private void loadIfNeeded() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    Map<String, String> fresh = new ConcurrentHashMap<>();
                    List<RuleConfig> list = ruleConfigMapper.selectList(null);
                    for (RuleConfig rc : list) {
                        if (rc.getRuleGroup() != null && rc.getRuleKey() != null && rc.getRuleValue() != null) {
                            fresh.put(rc.getRuleGroup() + "." + rc.getRuleKey(), rc.getRuleValue());
                        }
                    }
                    cache.clear();
                    cache.putAll(fresh);
                    loaded = true;
                }
            }
        }
    }

    /** 让缓存失效（下次读取重载）。配置被增删改后调用。 */
    public void refresh() {
        loaded = false;
    }

    /** 取字符串值，缺失返回默认。 */
    public String getStr(String group, String key, String def) {
        loadIfNeeded();
        String v = cache.get(group + "." + key);
        return (v == null || v.isBlank()) ? def : v;
    }

    /** 取整数值（分钟/分值/天数/次数等），非法返回默认。 */
    public int getInt(String group, String key, int def) {
        String v = getStr(group, key, null);
        if (v == null) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 把 {@code HH:mm} 形式的配置值转成「当日分钟数」（如 11:00 → 660），供就餐时段判定。
     * 值缺失或格式非法返回 {@code null}。
     */
    public Integer getMinuteOfDay(String group, String key) {
        String v = getStr(group, key, null);
        if (v == null || !v.contains(":")) {
            return null;
        }
        try {
            String[] p = v.trim().split(":");
            return Integer.parseInt(p[0].trim()) * 60 + Integer.parseInt(p[1].trim());
        } catch (Exception e) {
            return null;
        }
    }
}
