package org.iflytek.web.controller.common;

import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.web.model.dto.OnlyOfficeConfigDto;
import org.iflytek.web.service.OnlyOfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/common/onlyoffice")
public class OnlyOfficeController extends BaseController {

    @Autowired
    private OnlyOfficeService onlyOfficeService;

    @Value("${onlyoffice.publicBaseUrl:}")
    private String onlyOfficePublicBaseUrl;

    @GetMapping("/config")
    public AjaxResult getConfig(@RequestParam("url") String url,
                                @RequestParam(value = "name", required = false) String name,
                                HttpServletRequest request) {
        if (name == null) {
            name = url.substring(url.lastIndexOf("/") + 1);
            if (name.contains("?")) {
                name = name.substring(0, name.indexOf("?"));
            }
        }
        String proxyBaseUrl = onlyOfficePublicBaseUrl == null ? "" : onlyOfficePublicBaseUrl.trim();
        if (proxyBaseUrl.isEmpty()) {
            String scheme = request.getHeader("X-Forwarded-Proto");
            if (scheme == null || scheme.isEmpty()) {
                scheme = request.getScheme();
            }
            String host = request.getHeader("X-Forwarded-Host");
            if (host == null || host.isEmpty()) {
                host = request.getServerName();
                if (request.getServerPort() > 0 && request.getServerPort() != 80 && request.getServerPort() != 443) {
                    host += ":" + request.getServerPort();
                }
            }
            String forwardedPrefix = request.getHeader("X-Forwarded-Prefix");
            if (forwardedPrefix == null) {
                forwardedPrefix = "";
            } else {
                forwardedPrefix = forwardedPrefix.trim();
            }
            proxyBaseUrl = scheme + "://" + host + forwardedPrefix;
        }
        OnlyOfficeConfigDto config = onlyOfficeService.generateConfig(url, name, null, proxyBaseUrl);
        return success(config);
    }
}
