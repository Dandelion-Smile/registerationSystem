package org.iflytek.framework.security.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.common.core.domain.model.LoginUser;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.framework.web.service.UserDetailsServiceImpl;
import org.iflytek.framework.web.service.XfcTokenService;
import org.iflytek.system.service.ISysUserService;

/** 仅处理 /api/xfc/** 请求中的 X-XFC-Authorization 令牌。 */
@Component
public class XfcAuthenticationTokenFilter extends OncePerRequestFilter
{
    @Autowired
    private XfcTokenService xfcTokenService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
    {
        return !request.getRequestURI().startsWith("/api/xfc/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        Long userId = xfcTokenService.getUserId(request);
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            SysUser user = userService.selectUserById(userId);
            if (StringUtils.isNotNull(user) && "0".equals(user.getStatus()) && "0".equals(user.getDelFlag()))
            {
                LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
