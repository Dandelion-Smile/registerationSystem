package org.iflytek.framework.web.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.iflytek.common.core.domain.model.LoginUser;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.framework.config.XfcTokenProperties;

/** 为讯飞杯签发和解析独立 JWT；不读取或写入旧平台 Token/Redis 键。 */
@Component
public class XfcTokenService
{
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private XfcTokenProperties properties;

    public String createToken(LoginUser loginUser)
    {
        String secret = requireSecret();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpireTime() * 60L * 1000L);
        return Jwts.builder()
                .setSubject(String.valueOf(loginUser.getUserId()))
                .claim("username", loginUser.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Long getUserId(HttpServletRequest request)
    {
        String token = request.getHeader(properties.getHeader());
        if (StringUtils.isEmpty(token))
        {
            return null;
        }
        if (token.startsWith(BEARER_PREFIX))
        {
            token = token.substring(BEARER_PREFIX.length());
        }
        try
        {
            Claims claims = Jwts.parser().setSigningKey(requireSecret()).parseClaimsJws(token).getBody();
            return Long.valueOf(claims.getSubject());
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    public String getHeader()
    {
        return properties.getHeader();
    }

    private String requireSecret()
    {
        String secret = properties.getSecret();
        if (StringUtils.isEmpty(secret) || secret.getBytes(StandardCharsets.UTF_8).length < 64)
        {
            throw new IllegalStateException("XFC_TOKEN_SECRET 必须配置为至少 64 字节的随机值");
        }
        return secret;
    }
}
