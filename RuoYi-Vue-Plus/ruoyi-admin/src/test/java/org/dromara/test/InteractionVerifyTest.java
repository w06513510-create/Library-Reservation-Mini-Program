package org.dromara.test;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.app.domain.AppUser;
import org.dromara.app.domain.bo.AppRegisterBody;
import org.dromara.app.mapper.AppUserMapper;
import org.dromara.app.service.IAppUserService;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.interaction.constant.InteractionAction;
import org.dromara.interaction.domain.AppInteraction;
import org.dromara.interaction.mapper.AppInteractionMapper;
import org.dromara.interaction.service.IInteractionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通用互动模块（ruoyi-interaction）服务级集成验证：收藏/点赞/关注。
 * <p>`@SpringBootTest` 连真实 DB；`webEnvironment` 默认 MOCK 不占端口（与本机 8199 互不干扰）；
 * `@AfterAll` 清理测试数据。
 *
 * @author ruoyi-template
 */
@Tag("dev")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("通用互动 收藏/点赞/关注 集成验证")
public class InteractionVerifyTest {

    @Autowired
    private IInteractionService interactionService;
    @Autowired
    private IAppUserService appUserService;
    @Autowired
    private AppInteractionMapper interactionMapper;
    @Autowired
    private AppUserMapper appUserMapper;

    private Long me;
    private Long userA;
    private Long userB;

    private static final String PRODUCT = "product";
    private static final long P1 = 999_001L;
    private static final long P2 = 999_002L;
    private static final long P3 = 999_003L;

    private PageQuery pq() {
        return new PageQuery(10, 1); // 构造参数顺序为 (pageSize, pageNum)
    }

    @BeforeAll
    void setUp() {
        long base = System.currentTimeMillis() % 100_000_000L;
        me = register("139" + String.format("%08d", base % 100_000_000L));
        userA = register("139" + String.format("%08d", (base + 1) % 100_000_000L));
        userB = register("139" + String.format("%08d", (base + 2) % 100_000_000L));
    }

    private Long register(String phone) {
        AppRegisterBody bo = new AppRegisterBody();
        bo.setPhone(phone);
        bo.setPassword("app123456");
        return appUserService.register(bo);
    }

    @Test
    @Order(1)
    @DisplayName("点赞 toggle 幂等 + 计数")
    void likeToggle() {
        assertTrue(interactionService.toggle(me, InteractionAction.LIKE, PRODUCT, P1), "首次点赞应返回 true(已添加)");
        assertTrue(interactionService.has(me, InteractionAction.LIKE, PRODUCT, P1));
        assertEquals(1, interactionService.count(InteractionAction.LIKE, PRODUCT, P1));

        assertFalse(interactionService.toggle(me, InteractionAction.LIKE, PRODUCT, P1), "再次点赞应返回 false(已取消)");
        assertFalse(interactionService.has(me, InteractionAction.LIKE, PRODUCT, P1));
        assertEquals(0, interactionService.count(InteractionAction.LIKE, PRODUCT, P1));
    }

    @Test
    @Order(2)
    @DisplayName("收藏 hasBatch / countBatch / 我的列表")
    void favoriteBatch() {
        interactionService.toggle(me, InteractionAction.FAVORITE, PRODUCT, P1);
        interactionService.toggle(me, InteractionAction.FAVORITE, PRODUCT, P2);

        Set<Long> mine = interactionService.hasBatch(me, InteractionAction.FAVORITE, PRODUCT, List.of(P1, P2, P3));
        assertEquals(Set.of(P1, P2), mine, "hasBatch 应命中 P1、P2");

        Map<Long, Long> counts = interactionService.countBatch(InteractionAction.FAVORITE, PRODUCT, List.of(P1, P2, P3));
        assertEquals(1L, counts.get(P1));
        assertEquals(1L, counts.get(P2));
        assertNull(counts.get(P3), "P3 未被收藏，不应出现在计数结果");

        TableDataInfo<Long> myList = interactionService.pageMyBizIds(me, InteractionAction.FAVORITE, PRODUCT, pq());
        assertEquals(2, myList.getTotal(), "我的收藏共 2 条");
        assertTrue(myList.getRows().containsAll(List.of(P1, P2)));
    }

    @Test
    @Order(3)
    @DisplayName("关注 + 我的关注/粉丝(返回 AppUserVo)")
    void follow() {
        assertTrue(interactionService.toggle(me, InteractionAction.FOLLOW, InteractionAction.BIZ_TYPE_USER, userA));
        assertTrue(interactionService.toggle(me, InteractionAction.FOLLOW, InteractionAction.BIZ_TYPE_USER, userB));

        var following = interactionService.pageFollowing(me, pq());
        assertEquals(2, following.getTotal(), "我关注了 2 人");
        assertEquals(2, following.getRows().size(), "关注列表应解析出 2 个 AppUserVo");
        assertTrue(following.getRows().stream().anyMatch(v -> v.getId().equals(userA)));
        assertTrue(following.getRows().stream().anyMatch(v -> v.getId().equals(userB)));

        var followersOfA = interactionService.pageFollowers(userA, pq());
        assertEquals(1, followersOfA.getTotal(), "A 有 1 个粉丝");
        assertEquals(me, followersOfA.getRows().get(0).getId(), "A 的粉丝应是 me");
        assertEquals(1, interactionService.count(InteractionAction.FOLLOW, InteractionAction.BIZ_TYPE_USER, userA), "A 的粉丝数=1");
    }

    @AfterAll
    void cleanup() {
        TenantHelper.ignore(() -> {
            interactionMapper.delete(Wrappers.<AppInteraction>lambdaQuery()
                .in(AppInteraction::getUserId, List.of(me, userA, userB)));
            interactionMapper.delete(Wrappers.<AppInteraction>lambdaQuery()
                .in(AppInteraction::getBizId, List.of(me, userA, userB)));
            appUserMapper.delete(Wrappers.<AppUser>lambdaQuery()
                .in(AppUser::getId, List.of(me, userA, userB)));
            return null;
        });
    }

}
