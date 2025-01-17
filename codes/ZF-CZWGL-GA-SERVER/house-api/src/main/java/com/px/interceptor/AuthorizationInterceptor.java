package com.px.interceptor;

import com.px.annotation.IgnoreAuth;
import com.px.entity.Token;
import com.px.redis.TokenService;
import com.px.utils.BusinessException;
import com.px.utils.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限(Token)验证
 *
 * @author 品讯科技
 */
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
    @Resource
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        IgnoreAuth annotation;
        if (handler instanceof HandlerMethod) {
            annotation = ((HandlerMethod) handler).getMethodAnnotation(IgnoreAuth.class);
        } else {
            return true;
        }

        //获取用户凭证
        String token = request.getHeader(Constant.LOGIN_TOKEN_KEY);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(Constant.LOGIN_TOKEN_KEY);
        }

        //凭证为空
        if (annotation == null && StringUtils.isBlank(token)) {
            throw new BusinessException(Constant.LOGIN_TOKEN_KEY + "不能为空", HttpStatus.UNAUTHORIZED.value());
        }
        //验证token
        Token tokenModel = tokenService.getToken(token);
        if (tokenService.checkToken(tokenModel)) {
            //如果token验证成功，将token对应的用户id存在request中，便于之后注入
            request.setAttribute(Constant.LOGIN_USER_KEY, tokenModel.getUserId());
            return true;
        } else if (annotation != null) {
            //如果有@IgnoreAuth注解，则通过
            return true;
        } else {
            throw new BusinessException(Constant.LOGIN_TOKEN_KEY + "失效，请重新登录", HttpStatus.UNAUTHORIZED.value());
        }
    }
}
