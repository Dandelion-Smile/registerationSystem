package org.iflytek.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 独立于旧竞赛平台的讯飞杯令牌配置。 */
@Component
public class XfcTokenProperties
{
    @Value("${xfc.token.header:X-XFC-Authorization}")
    private String header;

    @Value("${xfc.token.secret:}")
    private String secret;

    @Value("${xfc.token.expireTime:120}")
    private int expireTime;

    public String getHeader()
    {
        return header;
    }

    public String getSecret()
    {
        return secret;
    }

    public int getExpireTime()
    {
        return expireTime;
    }
}
