package org.iflytek.framework.web.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.iflytek.common.core.domain.model.LoginUser;
import org.iflytek.common.exception.ServiceException;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.framework.security.context.AuthenticationContextHolder;

/** 复用原账号密码认证器，但只签发讯飞杯 Token。 */
@Component
public class XfcLoginService
{
    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysLoginService sysLoginService;

    @Autowired
    private XfcTokenService xfcTokenService;

    public String login(String username, String password)
    {
        sysLoginService.loginPreCheck(username, password);
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return xfcTokenService.createToken(loginUser);
        }
        catch (Exception ignored)
        {
            // 对不存在、停用、密码错误统一反馈，避免暴露账号状态。
            throw new ServiceException("账号或密码错误，请重新输入");
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
    }
}
