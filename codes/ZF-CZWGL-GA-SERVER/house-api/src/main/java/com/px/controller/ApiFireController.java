package com.px.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.px.annotation.LoginUser;
import com.px.entity.*;
import com.px.service.*;
import com.px.utils.BusinessException;
import com.px.utils.Constant;
import com.px.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

/**
 * 消防隐患
 *
 * @author 品讯科技
 * @date 2024-08
 */
@Api(tags = "消防隐患接口")
@RestController
@RequestMapping("/fire")
public class ApiFireController extends ApiBaseAction {
    @Resource
    private FireService fireService;
    @Resource
    private UserService userService;
    @Resource
    private DangerService dangerService;
    @Resource
    private MessageService messageService;
    @Resource
    private StreetService streetService;

    /**
     * 提交消防隐患
     */
    @ApiOperation(value = "提交消防隐患")
    @PostMapping("save")
    public Result save(Fire fire, @LoginUser Integer userId) {
        fire.setUserId(userId);
        fireService.saveFire(fire);
        if (fire.getTransactorId() != null) {
            Message message = new Message();
            message.setMessageType(Constant.MessageType.FIRE.getValue());
            message.setUserId(fire.getTransactorId());
            message.setCreateTime(new Date());
            message.setContent("您有一条隐患处理待办");
            message.setLinkId(fire.getFireId());
            messageService.sendMessage(message);
        }
        return Result.ok(fire);
    }

    /**
     * 消防隐患列表
     *
     * @return
     */
    @ApiOperation(value = "消防隐患列表")
    @GetMapping("list")
    public Result list(@LoginUser Integer userId) {
        Map<String, Object> queryMap = new HashMap<>(2);
        queryMap.put("userId", userId);
        IPage<Fire> page = super.getPage();
        IPage<Fire> pageList = fireService.getPageList(page, queryMap);
        for (Fire fire : pageList.getRecords()) {
            if (StringUtils.isNotBlank(fire.getImages())) {
                fire.setImageList(fire.getImages().split(","));
            }
        }
        return Result.ok(pageList);
    }

    /**
     * 消防隐患详情
     *
     * @param fireId
     * @return
     */
    @ApiOperation(value = "消防隐患详情")
    @GetMapping("detail")
    public Result detail(Integer fireId) {
        Fire fire = fireService.getById(fireId);
        if (fire == null) {
            throw new BusinessException("数据不存在");
        }
        if (StringUtils.isNotBlank(fire.getImages())) {
            fire.setImageList(fire.getImages().split(","));
        }
        if (StringUtils.isNotBlank(fire.getCheckedIds())) {
            List<Danger> checkedList = dangerService.list(new QueryWrapper<Danger>().in(Danger.DANGER_ID, Arrays.asList(fire.getCheckedIds().split(","))));
            fire.setCheckedList(checkedList);
        }
        if (fire.getTransactorId() != null) {
            User transactor = userService.getById(fire.getTransactorId());
            fire.setTransactorName(transactor.getPost() + transactor.getName());
        }
        if (fire.getUserId() != null) {
            User user = userService.getById(fire.getUserId());
            fire.setUserName(user.getPost() + user.getName());
        }
        if(StringUtils.isNotBlank(fire.getTown())){
            Street street=streetService.getById(fire.getTown());
            fire.setTownName(street.getName());
        }
        if(StringUtils.isNotBlank(fire.getVillage())){
            Street street=streetService.getById(fire.getVillage());
            fire.setVillageName(street.getName());
        }
        return Result.ok(fire);
    }

    /**
     * 处理消防隐患
     *
     * @param fire
     * @return
     */
    @ApiOperation(value = "处理消防隐患")
    @PostMapping("audit")
    public Result audit(Fire fire) {
        fire.setStatus(Constant.DisposeStatus.COMPLETE.getValue());
        fireService.saveFire(fire);
        fire = fireService.getById(fire.getFireId());
        if (fire.getUserId() != null) {
            Message message = new Message();
            message.setMessageType(Constant.MessageType.FIRE.getValue());
            message.setUserId(fire.getUserId());
            message.setCreateTime(new Date());
            message.setContent("您有一条隐患处理已完成，请查收");
            message.setLinkId(fire.getFireId());
            messageService.sendMessage(message);
        }
        return Result.ok(fire);
    }
}