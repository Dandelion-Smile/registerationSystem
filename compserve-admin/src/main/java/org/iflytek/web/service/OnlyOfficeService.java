package org.iflytek.web.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.iflytek.common.utils.file.MinioUtils;
import org.iflytek.web.model.dto.OnlyOfficeConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OnlyOfficeService
{
    private static final Logger log = LoggerFactory.getLogger(OnlyOfficeService.class);

    @Value("${onlyoffice.secret:}")
    private String onlyOfficeSecret;

    private final MinioUtils minioUtils;

    public OnlyOfficeService(MinioUtils minioUtils)
    {
        this.minioUtils = minioUtils;
    }

    public OnlyOfficeConfigDto generateConfig(String url, String name, String userParams, String proxyBaseUrl)
    {
        OnlyOfficeConfigDto dto = new OnlyOfficeConfigDto();
        String officeUrl;
        if (isDirectPreviewUrl(url))
        {
            officeUrl = buildAbsolutePreviewUrl(url, proxyBaseUrl);
        }
        else
        {
            String signedUrl = url.contains("?") ? url : minioUtils.getPrivateUrl(url);
            officeUrl = signedUrl;
            if (proxyBaseUrl != null && !proxyBaseUrl.trim().isEmpty())
            {
                officeUrl = proxyBaseUrl + "/common/preview/url?url=" + URLEncoder.encode(signedUrl, StandardCharsets.UTF_8);
            }
        }
        String ext = name != null && name.contains(".") ? name.substring(name.lastIndexOf(".") + 1).toLowerCase() : "";
        String type = "word";
        if ("pdf".equals(ext)) type = "pdf";
        if ("ppt".equals(ext) || "pptx".equals(ext)) type = "slide";
        if ("xls".equals(ext) || "xlsx".equals(ext)) type = "cell";
        dto.setDocumentType(type);

        Map<String, Object> document = new HashMap<>();
        document.put("fileType", ext.isEmpty() ? "docx" : ext);
        // key 基于 URL 哈希，不包含时间戳，使 ONLYOFFICE 可复用已转换的文档缓存。
        // 当文件内容变更时（如重新上传），URL 会变化，key 自然不同。
        document.put("key", Integer.toHexString(url.hashCode()));
        document.put("title", name);
        document.put("url", officeUrl);
        dto.setDocument(document);

        Map<String, Object> editorConfig = new HashMap<>();
        Map<String, Object> customization = new HashMap<>();
        customization.put("chat", false);
        customization.put("feedback", false);
        customization.put("help", false);
        customization.put("compactToolbar", false);
        customization.put("hideRightMenu", true);
        customization.put("forcesave", false);
        Map<String, Object> user = new HashMap<>();
        user.put("id", "teacher-preview");
        user.put("name", "指导老师");
        editorConfig.put("customization", customization);
        editorConfig.put("mode", "view");
        editorConfig.put("user", user);
        dto.setEditorConfig(editorConfig);

        dto.setToken(buildToken(dto));
        return dto;
    }

    private boolean isDirectPreviewUrl(String url)
    {
        if (url == null)
        {
            return false;
        }
        return url.startsWith("http://")
                || url.startsWith("https://")
                || url.startsWith("/common/preview/");
    }

    private String buildAbsolutePreviewUrl(String url, String proxyBaseUrl)
    {
        if (url == null)
        {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://"))
        {
            return url;
        }
        String base = proxyBaseUrl == null ? "" : proxyBaseUrl.trim();
        if (base.endsWith("/") && url.startsWith("/"))
        {
            return base.substring(0, base.length() - 1) + url;
        }
        if (!base.endsWith("/") && !url.startsWith("/"))
        {
            return base + "/" + url;
        }
        return base + url;
    }

    private String buildToken(OnlyOfficeConfigDto dto)
    {
        if (onlyOfficeSecret == null || onlyOfficeSecret.trim().isEmpty())
        {
            log.warn("OnlyOffice secret is empty, token will be blank");
            return "";
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("document", dto.getDocument());
        payload.put("editorConfig", dto.getEditorConfig());
        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(payload)
                .signWith(SignatureAlgorithm.HS256, onlyOfficeSecret.getBytes(StandardCharsets.UTF_8))
                .compact();
        log.info("OnlyOffice token generated: secretLen={}, tokenLen={}, fileKey={}",
                onlyOfficeSecret.length(), token.length(), dto.getDocument().get("key"));
        return token;
    }

    private void logUrlProbe(String label, String targetUrl)
    {
        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(targetUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            String contentType = conn.getContentType();
            int len = conn.getContentLength();
            log.info("{} status={}, contentType={}, contentLength={}", label, code, contentType, len);
            conn.disconnect();
        }
        catch (Exception e)
        {
            log.error("{} failed: {}", label, e.getMessage());
        }
    }
}
