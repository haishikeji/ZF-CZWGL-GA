package com.px.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.px.entity.SysUser;
import com.px.utils.ShiroUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller公共组件
 *
 * @author 品讯科技
 * @date 2024-08
 */
public abstract class AbstractController {

    @Autowired
    protected HttpServletRequest request;

    public SysUser getUser() {
        return ShiroUtil.getCurrentUser();
    }

    public Integer getUserId() {
        return getUser().getUserId();
    }

    /**
     * 构造分页参数
     */
    public <T> Page<T> getPage() {
        //页号 默认1
        int current = 1;
        //每页数据条数 默认10条
        int size = 10;
        String page = request.getParameter("page");
        String limit = request.getParameter("limit");
        if (StringUtils.isNumeric(page)) {
            current = Integer.parseInt(page);
        }
        if (StringUtils.isNumeric(limit)) {
            size = Integer.parseInt(limit);
        }
        return new Page<>(current, size);
    }
}
