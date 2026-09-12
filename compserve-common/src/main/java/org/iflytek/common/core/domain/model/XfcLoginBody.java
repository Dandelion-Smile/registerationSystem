package org.iflytek.common.core.domain.model;

/**
 * 第三届讯飞杯报名系统登录请求。
 * 仅复用既有账号密码，不与旧竞赛平台共享会话凭证。
 */
public class XfcLoginBody
{
    private String username;
    private String password;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
