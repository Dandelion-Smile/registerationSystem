package org.iflytek.common.utils.file;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.annotation.PostConstruct;
import org.iflytek.common.config.MinioProperties;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.common.utils.uuid.IdUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class MinioUtils
{
    private final MinioProperties properties;

    private MinioClient minioClient;

    public MinioUtils(MinioProperties properties)
    {
        this.properties = properties;
    }

    @PostConstruct
    public void init() throws Exception
    {
        minioClient = MinioClient.builder()
                .endpoint(properties.getResolvedEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .region(properties.getRegion())
                .build();
    }

    public String upload(MultipartFile file) throws Exception
    {
        String ext = FileUploadUtils.getExtension(file);
        String objectName = org.iflytek.common.utils.DateUtils.datePath() + "/" + IdUtils.fastSimpleUUID() + "." + ext;
        try (InputStream is = file.getInputStream())
        {
            return upload(objectName, is, file.getSize(), file.getContentType());
        }
    }

    public String upload(String objectName, InputStream inputStream, long size, String contentType) throws Exception
    {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(objectName)
                        .stream(inputStream, size, -1)
                        .contentType(StringUtils.isEmpty(contentType) ? "application/octet-stream" : contentType)
                        .build()
        );
        return buildObjectUrl(objectName);
    }

    public byte[] download(String fileKeyOrUrl) throws Exception
    {
        String objectName = resolveObjectName(fileKeyOrUrl);
        try (GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(objectName)
                        .build()
        ))
        {
            return response.readAllBytes();
        }
    }

    public void delete(String fileKeyOrUrl) throws Exception
    {
        String objectName = resolveObjectName(fileKeyOrUrl);
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(properties.getBucketName())
                        .object(objectName)
                        .build()
        );
    }

    public void deleteBatch(List<String> fileKeysOrUrls) throws Exception
    {
        List<DeleteObject> objects = new ArrayList<>();
        for (String fileKeyOrUrl : fileKeysOrUrls)
        {
            String objectName = resolveObjectName(fileKeyOrUrl);
            if (StringUtils.isNotEmpty(objectName))
            {
                objects.add(new DeleteObject(objectName));
            }
        }
        if (objects.isEmpty())
        {
            return;
        }
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(properties.getBucketName())
                        .objects(objects)
                        .build()
        );
        for (Result<DeleteError> result : results)
        {
            result.get();
        }
    }

    public String getPrivateUrl(String fileKeyOrUrl)
    {
        if (StringUtils.isEmpty(fileKeyOrUrl))
        {
            return "";
        }
        try
        {
            String objectName = resolveObjectName(fileKeyOrUrl);
            if (StringUtils.isEmpty(objectName))
            {
                return fileKeyOrUrl;
            }
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .expiry((int) Duration.ofHours(1).getSeconds())
                            .build()
            );
        }
        catch (Exception e)
        {
            return fileKeyOrUrl;
        }
    }

    public String getEndpoint()
    {
        return properties.getResolvedEndpoint();
    }

    public String getBucketName()
    {
        return properties.getBucketName();
    }

    public String getRegion()
    {
        return properties.getRegion();
    }

    public boolean isUseHttps()
    {
        return properties.isUseHttps();
    }

    private String buildObjectUrl(String objectName)
    {
        return properties.getBaseUrl() + "/" + objectName;
    }

    private String resolveObjectName(String fileKeyOrUrl)
    {
        if (StringUtils.isEmpty(fileKeyOrUrl))
        {
            return "";
        }
        if (!fileKeyOrUrl.contains("://"))
        {
            return trimLeadingSlash(fileKeyOrUrl);
        }

        URI uri = URI.create(fileKeyOrUrl);
        String currentHost = normalizeHost(properties.getResolvedEndpoint());
        String targetHost = normalizeHost(uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : ""));
        if (!currentHost.equals(targetHost))
        {
            return "";
        }

        String path = trimLeadingSlash(uri.getPath());
        String bucketPrefix = properties.getBucketName() + "/";
        if (path.startsWith(bucketPrefix))
        {
            return path.substring(bucketPrefix.length());
        }
        return path;
    }

    private String trimLeadingSlash(String value)
    {
        if (value == null)
        {
            return "";
        }
        String result = value;
        while (result.startsWith("/"))
        {
            result = result.substring(1);
        }
        return result;
    }

    private String normalizeHost(String url)
    {
        URI uri = URI.create(url);
        String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        int port = uri.getPort();
        String suffix = port > 0 ? ":" + port : "";
        return scheme + "://" + uri.getHost() + suffix;
    }
}
