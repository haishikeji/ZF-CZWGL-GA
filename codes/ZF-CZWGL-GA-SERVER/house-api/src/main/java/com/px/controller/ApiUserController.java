package com.px.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.px.annotation.IgnoreAuth;
import com.px.annotation.LoginUser;
import com.px.entity.Message;
import com.px.entity.User;
import com.px.entity.UserAnswer;
import com.px.entity.UserQuestion;
import com.px.service.*;
import com.px.utils.AbstractAssert;
import com.px.utils.BusinessException;
import com.px.utils.Constant;
import com.px.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个人中心
 *
 * @author 品讯科技
 * @date 2024-08
 */
@Api(tags = "个人中心接口")
@RestController
@RequestMapping("/user")
public class ApiUserController extends ApiBaseAction {
    @Resource
    private UserService userService;
    @Resource
    private UserQuestionService userQuestionService;
    @Resource
    private UserAnswerService userAnswerService;
    @Resource
    private UserMenuService userMenuService;
    @Resource
    private MessageService messageService;

    /**
     * 个人中心
     */
    @IgnoreAuth
    @ApiOperation(value = "个人中心")
    @GetMapping("index")
    public Result index(@LoginUser Integer userId) {
        Map<String, Object> result = new HashMap<>(6);
        if (userId != null) {
            User user = userService.getById(userId);
            List<Integer> menuList = userMenuService.getMenuList(userId);
            user.setMenuList(menuList);
            int count = messageService.count(new QueryWrapper<Message>().eq(Message.USER_ID, userId).eq(Message.IS_READ, false));
            result.put("userInfo", user);
            result.put("message", count);
            result.put("login", true);
        } else {
            result.put("login", false);
        }
        return Result.ok(result);
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param answer
     * @param password
     * @return
     */
    @ApiOperation(value = "修改密码")
    @PostMapping("password")
    public Result password(@LoginUser Integer userId, String answer, String password) {
        userService.password(userId, answer, password);
        return Result.ok();
    }

    /**
     * 获取用户的安全问题
     *
     * @param userId
     * @return
     */
    @ApiOperation(value = "安全问题")
    @GetMapping("question")
    public Result question(@LoginUser Integer userId) {
        Map<String, Object> map = new HashMap<>(2);
        String question = userService.question(userId);
        map.put("question", question);
        return Result.ok(map);
    }

    /**
     * 安全问题列表
     *
     * @return
     */
    @IgnoreAuth
    @ApiOperation(value = "安全问题列表")
    @GetMapping("questionList")
    public Result questionList() {
        List<UserQuestion> list = userQuestionService.list();
        return Result.ok(list);
    }

    /**
     * 设置安全问题
     *
     * @param questionId
     * @param answer
     * @return
     */
    @ApiOperation(value = "设置安全问题")
    @PostMapping("setQuestion")
    public Result setQuestion(@LoginUser Integer userId, Integer questionId, String answer) {
        AbstractAssert.isNull(questionId, "请先选择安全问题");
        AbstractAssert.isBlank(answer, "请设置安全问题答案");
        int count = userAnswerService.count(new QueryWrapper<UserAnswer>().eq(UserAnswer.USER_ID, userId));
        if (count > 0) {
            throw new BusinessException("已设置安全问题，请勿重复设置");
        }
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuestionId(questionId);
        userAnswer.setAnswer(answer);
        userAnswer.setUserId(userId);
        userAnswer.setCreateTime(new Date());
        userAnswerService.save(userAnswer);
        return Result.ok();
    }

    /**
     * 用户权限列表
     *
     * @param userId
     * @return
     */
    @IgnoreAuth
    @ApiOperation(value = "用户权限列表")
    @GetMapping("menuList")
    public Result menuList(@LoginUser Integer userId) {
        Map<String, Object> map = new HashMap<>(4);
        User user = userService.getById(userId);
        if (user != null) {
            map.put("userType", user.getUserType());
        } else {
            map.put("userType", Constant.UserType.LESSOR.getValue());
        }
        List<Integer> menuList = userMenuService.getMenuList(userId);
        map.put("menuList", menuList);
        return Result.ok(map);
    }

    /**
     * 保存formId,用于发送模板消息
     *
     * @return
     */
    @ApiOperation(value = "保存formId,用于发送模板消息")
    @PostMapping("saveFormId")
    public Result saveFormId(@LoginUser Integer userId, String formId) {
        User user = new User();
        user.setUserId(userId);
        user.setFormId(formId);
        userService.updateById(user);
        return Result.ok();
    }
}
