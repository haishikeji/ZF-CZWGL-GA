package com.px.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.px.annotation.RecordLog;
import com.px.entity.SysMenu;
import com.px.entity.SysRole;
import com.px.service.SysMenuService;
import com.px.service.SysRoleMenuService;
import com.px.service.SysRoleService;
import com.px.utils.Result;
import com.px.utils.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static com.px.utils.Constant.SUPER_ADMIN;

/**
 * 系统角色相关
 *
 * @author 品讯科技
 * @date 2024-08
 */
@RestController
@RequestMapping(value = "sys/role")
public class SysRoleController extends AbstractController {
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private SysMenuService sysMenuService;
    @Resource
    private SysRoleMenuService sysRoleMenuService;

    /**
     * 跳转到角色管理页面
     *
     * @return
     */
    @GetMapping
    public ModelAndView role() {
        return new ModelAndView("system/roleList");
    }

    /**
     * 角色管理list
     *
     * @param request
     * @return
     */
    @GetMapping(value = "list")
    public Result list(HttpServletRequest request) {
        //如果不是超级管理员，则只查询自己创建的角色列表
        Page<SysRole> page = this.getPage();
        String roleName = request.getParameter("roleName");
        Wrapper<SysRole> wrapper = new QueryWrapper<SysRole>()
                .eq(StringUtils.isNotBlank(roleName), SysRole.ROLE_NAME, roleName)
                .eq(!getUserId().equals(SUPER_ADMIN), SysRole.CREATE_USER_ID, getUserId());
        return Result.ok(sysRoleService.page(page, wrapper));
    }

    /**
     * 角色新增修改页面
     *
     * @param roleId
     * @return
     */
    @GetMapping(value = "edit")
    public Result edit(@RequestParam(required = false) Integer roleId) {
        SysRole role = new SysRole();
        if (roleId != null) {
            role = sysRoleService.getById(roleId);
        }
        // 查询角色对应的菜单
        List<Integer> menuIdList = sysRoleMenuService.queryMenuIdList(roleId);
        role.setMenuIdList(menuIdList);
        return Result.ok(role);
    }

    /**
     * 角色拥有的菜单
     *
     * @return
     */
    @GetMapping(value = "menuList")
    public Result menuList() {
        // 登录用户拥有的菜单
        List<SysMenu> menuList = sysMenuService.queryUserMenuList(getUserId());
        return Result.ok(menuList);
    }

    /**
     * 新增修改角色权限
     *
     * @param role
     * @return
     */
    @RecordLog("保存角色")
    @PostMapping(value = "save")
    public Result save(SysRole role, Integer[] layuiTreeCheck) {
        if (layuiTreeCheck != null) {
            role.setMenuIdList(Arrays.asList(layuiTreeCheck));
        }
        ValidatorUtil.validateEntity(role);
        role.setCreateUserId(getUserId());
        sysRoleService.saveRole(role);
        return Result.ok();
    }

    /**
     * 删除角色
     *
     * @param roleIds
     * @return
     */
    @RecordLog("删除角色")
    @PostMapping(value = "delete")
    public Result delete(@RequestParam(value = "roleIds[]") Integer[] roleIds) {
        sysRoleService.deleteBatch(roleIds);
        return Result.ok();
    }
}
