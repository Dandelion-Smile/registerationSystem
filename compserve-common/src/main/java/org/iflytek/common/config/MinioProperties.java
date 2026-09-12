package org.iflytek.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties
{
    private String vendor;

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String region;

    private boolean useHttps;

    public String getVendor()
    {
        return vendor;
    }

    public void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public void setAccessKey(String accessKey)
    {
        this.accessKey = accessKey;
    }

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public boolean isUseHttps()
    {
        return useHttps;
    }

    public void setUseHttps(boolean useHttps)
    {
        this.useHttps = useHttps;
    }

    public String getResolvedEndpoint()
    {
        String value = endpoint == null ? "" : endpoint.trim();
        if (value.isEmpty())
        {
            return value;
        }
        if (!value.startsWith("http://") && !value.startsWith("https://"))
        {
            value = (useHttps ? "https://" : "http://") + value;
        }
        if (value.endsWith("/"))
        {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    public String getBaseUrl()
    {
        return getResolvedEndpoint() + "/" + bucketName;
    }
}
