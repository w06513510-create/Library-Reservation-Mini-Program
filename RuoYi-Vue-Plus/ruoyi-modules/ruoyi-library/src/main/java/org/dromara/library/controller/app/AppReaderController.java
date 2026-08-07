package org.dromara.library.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.app.utils.AppLoginHelper;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.library.domain.CreditLog;
import org.dromara.library.domain.Reader;
import org.dromara.library.domain.bo.AppealBo;
import org.dromara.library.domain.bo.RuleConfigBo;
import org.dromara.library.domain.bo.ViolationBo;
import org.dromara.library.domain.vo.AppealVo;
import org.dromara.library.domain.vo.CreditLogVo;
import org.dromara.library.domain.vo.ReaderVo;
import org.dromara.library.domain.vo.RuleConfigVo;
import org.dromara.library.domain.vo.ViolationVo;
import org.dromara.library.mapper.CreditLogMapper;
import org.dromara.library.mapper.ReaderMapper;
import org.dromara.library.service.IAppealService;
import org.dromara.library.service.IRuleConfigService;
import org.dromara.library.service.IViolationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * C端(小程序) 读者中心 Controller —— 我的档案 / 信用分与流水 / 违约 / 申诉 / 规则说明。
 * <p>readerId 一律取当前登录读者（{@link AppLoginHelper#getUserId()}），不接受前端传入他人 id。
 * 注意：读者档案按 {@code user_id} 定位（IReaderService 的 buildQueryWrapper 不按 userId 过滤，故直连 Mapper）。
 *
 * @author library
 */
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/library/reader")
public class AppReaderController {

    private final ReaderMapper readerMapper;
    private final CreditLogMapper creditLogMapper;
    private final IViolationService violationService;
    private final IAppealService appealService;
    private final IRuleConfigService ruleConfigService;

    /** 我的读者档案（含信用分 / 黑名单 / 守信次数） */
    @GetMapping("/profile")
    public R<ReaderVo> profile() {
        Long me = AppLoginHelper.getUserId();
        ReaderVo vo = readerMapper.selectVoOne(Wrappers.<Reader>lambdaQuery().eq(Reader::getUserId, me));
        return R.ok(vo);
    }

    /** 我的信用流水（倒序；一致性不变式 credit_score = clamp(Σ delta, 0, 100)） */
    @GetMapping("/credit/logs")
    public R<List<CreditLogVo>> creditLogs() {
        Long me = AppLoginHelper.getUserId();
        List<CreditLogVo> list = creditLogMapper.selectVoList(Wrappers.<CreditLog>lambdaQuery()
            .eq(CreditLog::getReaderId, me)
            .orderByDesc(CreditLog::getCreateTime));
        return R.ok(list);
    }

    /** 我的违约记录（status: 0有效 1已解除） */
    @GetMapping("/violations")
    public TableDataInfo<ViolationVo> violations(@RequestParam(required = false) Integer status, PageQuery pageQuery) {
        ViolationBo bo = new ViolationBo();
        bo.setReaderId(AppLoginHelper.getUserId());
        bo.setStatus(status);
        return violationService.queryPageList(bo, pageQuery);
    }

    /** 我的申诉记录 */
    @GetMapping("/appeals")
    public TableDataInfo<AppealVo> appeals(PageQuery pageQuery) {
        AppealBo bo = new AppealBo();
        bo.setReaderId(AppLoginHelper.getUserId());
        return appealService.queryPageList(bo, pageQuery);
    }

    /** 发起申诉：只能对本人的有效违约申诉（Service 再校验违约有效 + 无重复待审） */
    @PostMapping("/appeal")
    public R<Void> appeal(@RequestBody AppealBo bo) {
        Long me = AppLoginHelper.getUserId();
        if (bo.getViolationId() == null) {
            throw new ServiceException("违约记录不能为空");
        }
        ViolationVo v = violationService.queryById(bo.getViolationId());
        if (v == null || !Objects.equals(v.getReaderId(), me)) {
            throw new ServiceException("无权申诉他人违约", 403);
        }
        bo.setId(null);
        bo.setReaderId(me);
        return appealService.submit(bo) ? R.ok() : R.fail();
    }

    /** 规则说明（只读展示：签到窗 / 暂离 / 就餐保留 / 取消上限 等，让读者知道规则） */
    @GetMapping("/rules")
    public R<List<RuleConfigVo>> rules() {
        return R.ok(ruleConfigService.queryList(new RuleConfigBo()));
    }

}
