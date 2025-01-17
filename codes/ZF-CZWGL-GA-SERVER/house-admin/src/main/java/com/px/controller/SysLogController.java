package com.px.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.px.entity.SysLog;
import com.px.service.SysLogService;
import com.px.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志管理
 *
 * @author 品讯科技
 * @date 2024-08
 */
@RequestMapping(value = "sys/log")
@RestController
public class SysLogController extends AbstractController {
    @Resource
    private SysLogService sysLogService;

    /**
     * 日志列表
     * @return
     */
    @GetMapping
    public ModelAndView log() {
        return new ModelAndView("system/logList");
    }

    /**
     * 获取日志列表数据
     *
     * @return
     */
    @GetMapping(value = "list")
    public Result list(HttpServletRequest request) {
        Page<SysLog> page = this.getPage();
        Map<String, Object> queryMap = new HashMap<>(4);
        queryMap.put("username", request.getParameter("username"));
        queryMap.put("operation", request.getParameter("operation"));
        return Result.ok(sysLogService.getPageList(page, queryMap));
    }
}
