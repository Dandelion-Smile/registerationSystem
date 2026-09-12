package org.iflytek.web.config;

import jakarta.annotation.PostConstruct;
import org.iflytek.common.config.MinioProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MinioConfigLogger
{
    private static final Logger log = LoggerFactory.getLogger(MinioConfigLogger.class);

    private final MinioProperties minioProperties;

    public MinioConfigLogger(MinioProperties minioProperties)
    {
        this.minioProperties = minioProperties;
    }

    @PostConstruct
    public void print()
    {
        String akMasked = mask(minioProperties.getAccessKey());
        log.info(
                "MinIO config: vendor={}, endpoint={}, bucket={}, region={}, useHttps={}, accessKey={}",
                minioProperties.getVendor(),
                minioProperties.getResolvedEndpoint(),
                minioProperties.getBucketName(),
                minioProperties.getRegion(),
                minioProperties.isUseHttps(),
                akMasked
        );
        if (isEmpty(minioProperties.getAccessKey()))
        {
            log.warn("MinIO accessKey is empty. Check environment variables MINIO_ACCESS_KEY or application.yml");
        }
    }

    private String mask(String v)
    {
        if (v == null || v.isEmpty()) return "(empty)";
        return v.length() > 4 ? "***" + v.substring(v.length() - 4) : "***";
    }

    private boolean isEmpty(String v)
    {
        return v == null || v.isEmpty();
    }
}
