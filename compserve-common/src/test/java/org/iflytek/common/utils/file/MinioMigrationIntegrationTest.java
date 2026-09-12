package org.iflytek.common.utils.file;

import org.iflytek.common.config.MinioProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinioMigrationIntegrationTest
{
    private static final int VERIFY_TIMES = 50;

    private MinioUtils minioUtils;

    @BeforeEach
    void setUp() throws Exception
    {
        MinioProperties properties = new MinioProperties();
        properties.setVendor(readEnv("MINIO_VENDOR", "MinIO"));
        properties.setEndpoint(readEnv("MINIO_ENDPOINT", "http://117.72.113.94:9000/"));
        properties.setAccessKey(readEnv("MINIO_ACCESS_KEY", "minioadmin"));
        properties.setSecretKey(readEnv("MINIO_SECRET_KEY", "Minio@123456"));
        properties.setBucketName(readEnv("MINIO_BUCKET_NAME", "compserve"));
        properties.setRegion(readEnv("MINIO_REGION", "us-east-1"));
        properties.setUseHttps(Boolean.parseBoolean(readEnv("MINIO_USE_HTTPS", "false")));
        minioUtils = new MinioUtils(properties);
        minioUtils.init();
    }

    @Test
    void shouldVerifySingleObjectOperations50Times() throws Exception
    {
        int uploadSuccess = 0;
        int downloadSuccess = 0;
        int presignedSuccess = 0;
        int deleteSuccess = 0;
        long uploadTotalNs = 0L;
        long downloadTotalNs = 0L;
        long presignedTotalNs = 0L;
        long deleteTotalNs = 0L;
        List<String> cleanupUrls = new ArrayList<>();

        try
        {
            for (int i = 0; i < VERIFY_TIMES; i++)
            {
                String objectName = "migration/single/" + UUID.randomUUID() + ".txt";
                byte[] payload = ("single-" + i + "-" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8);

                long uploadStart = System.nanoTime();
                String url = minioUtils.upload(objectName, new ByteArrayInputStream(payload), payload.length, "text/plain");
                uploadTotalNs += System.nanoTime() - uploadStart;
                uploadSuccess++;
                cleanupUrls.add(url);

                long downloadStart = System.nanoTime();
                byte[] downloadBytes = minioUtils.download(url);
                downloadTotalNs += System.nanoTime() - downloadStart;
                assertArrayEquals(payload, downloadBytes);
                downloadSuccess++;

                long presignedStart = System.nanoTime();
                String presignedUrl = minioUtils.getPrivateUrl(url);
                presignedTotalNs += System.nanoTime() - presignedStart;
                assertTrue(presignedUrl.contains("X-Amz-Signature"));
                assertArrayEquals(payload, readBytes(presignedUrl));
                presignedSuccess++;

                long deleteStart = System.nanoTime();
                minioUtils.delete(url);
                deleteTotalNs += System.nanoTime() - deleteStart;
                deleteSuccess++;
                cleanupUrls.remove(url);

                assertThrows(Exception.class, () -> minioUtils.download(url));
            }
        }
        finally
        {
            for (String cleanupUrl : cleanupUrls)
            {
                try
                {
                    minioUtils.delete(cleanupUrl);
                }
                catch (Exception ignored)
                {
                }
            }
        }

        assertEquals(VERIFY_TIMES, uploadSuccess);
        assertEquals(VERIFY_TIMES, downloadSuccess);
        assertEquals(VERIFY_TIMES, presignedSuccess);
        assertEquals(VERIFY_TIMES, deleteSuccess);

        System.out.printf(
                "MINIO_VERIFY_SINGLE upload=%d download=%d presigned=%d delete=%d avgUploadMs=%.2f avgDownloadMs=%.2f avgPresignedMs=%.2f avgDeleteMs=%.2f%n",
                uploadSuccess,
                downloadSuccess,
                presignedSuccess,
                deleteSuccess,
                nanosToMillis(uploadTotalNs) / VERIFY_TIMES,
                nanosToMillis(downloadTotalNs) / VERIFY_TIMES,
                nanosToMillis(presignedTotalNs) / VERIFY_TIMES,
                nanosToMillis(deleteTotalNs) / VERIFY_TIMES
        );
    }

    @Test
    void shouldVerifyBatchOperations50Times() throws Exception
    {
        int batchSuccess = 0;
        long batchTotalNs = 0L;
        List<String> cleanupUrls = new ArrayList<>();

        try
        {
            for (int i = 0; i < VERIFY_TIMES; i++)
            {
                List<String> batchUrls = new ArrayList<>();
                for (int j = 0; j < 3; j++)
                {
                    String objectName = "migration/batch/" + i + "/" + UUID.randomUUID() + ".txt";
                    byte[] payload = ("batch-" + i + "-" + j + "-" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8);
                    String url = minioUtils.upload(objectName, new ByteArrayInputStream(payload), payload.length, "text/plain");
                    batchUrls.add(url);
                    cleanupUrls.add(url);
                }

                long batchStart = System.nanoTime();
                minioUtils.deleteBatch(batchUrls);
                batchTotalNs += System.nanoTime() - batchStart;
                batchSuccess++;

                for (String batchUrl : batchUrls)
                {
                    cleanupUrls.remove(batchUrl);
                    assertThrows(Exception.class, () -> minioUtils.download(batchUrl));
                }
            }
        }
        finally
        {
            for (String cleanupUrl : cleanupUrls)
            {
                try
                {
                    minioUtils.delete(cleanupUrl);
                }
                catch (Exception ignored)
                {
                }
            }
        }

        assertEquals(VERIFY_TIMES, batchSuccess);

        System.out.printf(
                "MINIO_VERIFY_BATCH batches=%d avgBatchDeleteMs=%.2f%n",
                batchSuccess,
                nanosToMillis(batchTotalNs) / VERIFY_TIMES
        );
    }

    private byte[] readBytes(String presignedUrl) throws Exception
    {
        HttpURLConnection connection = (HttpURLConnection) new URL(presignedUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(30000);
        int status = connection.getResponseCode();
        assertEquals(200, status);
        try (InputStream inputStream = connection.getInputStream())
        {
            return inputStream.readAllBytes();
        }
    }

    private double nanosToMillis(long nanos)
    {
        return nanos / 1_000_000.0;
    }

    private String readEnv(String name, String defaultValue)
    {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

}
