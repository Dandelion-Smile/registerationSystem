package org.iflytek.web.controller.xfc;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.common.core.domain.model.LoginUser;
import org.iflytek.common.core.domain.model.XfcLoginBody;
import org.iflytek.common.utils.SecurityUtils;
import org.iflytek.framework.web.service.XfcLoginService;

/** 讯飞杯独立认证与个人资料接口。 */
@RestController
@RequestMapping("/api/xfc")
public class XfcAuthController
{
    @Autowired
    private XfcLoginService xfcLoginService;

    @PostMapping("/auth/login")
    public AjaxResult login(@RequestBody XfcLoginBody body)
    {
        String token = xfcLoginService.login(body.getUsername(), body.getPassword());
        AjaxResult result = AjaxResult.success();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        return result;
    }

    @GetMapping("/profile")
    public AjaxResult profile()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("name", user.getStudentName());
        profile.put("studentNo", user.getStudentNo());
        profile.put("college", user.getCollegeName());
        profile.put("major", user.getMajorName());
        profile.put("className", user.getClassName());
        profile.put("phone", user.getPhonenumber());
        return AjaxResult.success(profile);
    }
}
