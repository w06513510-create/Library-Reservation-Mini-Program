package org.dromara.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * C端分页示例接口（演示前端 useList composable 的完整链路；<b>可整类删除</b>）。
 * <p>需登录(app_user)后访问；返回 25 条假数据用于分页/触底加载演示。
 *
 * @author ruoyi-template
 */
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/demo")
public class AppDemoController {

    private static final int TOTAL = 25;

    /** 返回一页假数据(共 25 条)，供前端 useList 分页演示 */
    @GetMapping("/page")
    public TableDataInfo<Map<String, Object>> page(PageQuery pageQuery) {
        int pageNum = pageQuery.getPageNum() == null ? 1 : pageQuery.getPageNum();
        int pageSize = pageQuery.getPageSize() == null ? 10 : pageQuery.getPageSize();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(TOTAL, from + pageSize);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = from; i < to; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i + 1);
            row.put("title", "示例数据 #" + (i + 1));
            row.put("status", i % 3);
            rows.add(row);
        }
        TableDataInfo<Map<String, Object>> rsp = new TableDataInfo<>();
        rsp.setCode(200);
        rsp.setMsg("查询成功");
        rsp.setRows(rows);
        rsp.setTotal(TOTAL);
        return rsp;
    }

}
