package org.iflytek.web.controller.common;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.common.utils.file.MinioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.iflytek.common.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/common/preview")
public class FilePreviewController {

    private static final Logger log = LoggerFactory.getLogger(FilePreviewController.class);
    private static final long TEMP_PREVIEW_EXPIRE_MILLIS = TimeUnit.MINUTES.toMillis(30);
    private static final Map<String, TempPreviewFile> TEMP_PREVIEW_FILES = new ConcurrentHashMap<>();

    @Autowired
    private MinioUtils minioUtils;

    @Value("${minio.endpoint:}")
    private String minioEndpoint;
    
    @Value("${preview.allowed-hosts:}")
    private String allowedHosts;

    /**
     * Proxy file content to avoid CORS or for secure access
     */
    @GetMapping("/url")
    public void getFile(@RequestParam("url") String url,
                        @RequestParam(value = "download", required = false) String download,
                        @RequestParam(value = "name", required = false) String name,
                        HttpServletResponse response) {
        try {
            String signedUrl = url;
            if (url.contains("?")) {
                 // already signed or has params
            } else {
                 signedUrl = minioUtils.getPrivateUrl(url);
            }
            // Restrict outbound to configured MinIO endpoint host only
            try {
                URL u = new URL(url);
                String hostUrl = u.getHost() + (u.getPort() > 0 ? (":" + u.getPort()) : "");
                if (!isHostAllowed(hostUrl)) {
                    response.setStatus(403);
                    return;
                }
            } catch (Exception ignore) {}

            URL remoteUrl = new URL(signedUrl);
            HttpURLConnection conn = (HttpURLConnection) remoteUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);

            response.setContentType(conn.getContentType());
            response.setContentLength(conn.getContentLength());
            String responseName = StringUtils.isNotBlank(name) ? name : new File(new URI(url).getPath()).getName();
            responseName = sanitizeDownloadName(responseName);
            if ("1".equals(download) || "true".equalsIgnoreCase(download)) {
                response.setHeader("Content-Disposition", buildContentDisposition("attachment", responseName));
            } else if (StringUtils.isNotBlank(responseName)) {
                response.setHeader("Content-Disposition", buildContentDisposition("inline", responseName));
            }
            
            IOUtils.copy(conn.getInputStream(), response.getOutputStream());
        } catch (Exception e) {
            log.error("Proxy file failed", e);
            response.setStatus(500);
        }
    }

    private String sanitizeDownloadName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        return fileName.replace("\\", "_").replace("/", "_").replace("\"", "");
    }

    private String buildContentDisposition(String disposition, String fileName) {
        String encodedName = java.net.URLEncoder
                .encode(fileName, java.nio.charset.StandardCharsets.UTF_8)
                .replace("+", "%20");
        return disposition + "; filename*=UTF-8''" + encodedName;
    }
    
    private boolean isHostAllowed(String hostPort) {
        try {
            URL e = new URL(minioEndpoint);
            String endpointHost = e.getHost() + (e.getPort() > 0 ? (":" + e.getPort()) : "");
            if (endpointHost.equalsIgnoreCase(hostPort)) return true;
        } catch (Exception ignore) {}
        if (allowedHosts == null || allowedHosts.trim().isEmpty()) return false;
        String hp = hostPort.toLowerCase();
        for (String raw : allowedHosts.split(",")) {
            String p = raw.trim().toLowerCase();
            if (p.isEmpty()) continue;
            if (p.startsWith("*.")) {
                String suffix = p.substring(1); // ".example.com" including dot
                if (hp.endsWith(suffix)) return true;
            } else if (hp.equals(p)) {
                return true;
            }
        }
        return false;
    }

    private static class TempPreviewFile {
        private final Path path;
        private final String name;
        private final String contentType;
        private final long expireAt;

        private TempPreviewFile(Path path, String name, String contentType, long expireAt) {
            this.path = path;
            this.name = name;
            this.contentType = contentType;
            this.expireAt = expireAt;
        }
    }

    private void cleanupExpiredTempPreviewFiles() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, TempPreviewFile>> iterator = TEMP_PREVIEW_FILES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TempPreviewFile> entry = iterator.next();
            TempPreviewFile file = entry.getValue();
            if (file == null || file.expireAt < now) {
                iterator.remove();
                if (file != null && file.path != null) {
                    try {
                        Files.deleteIfExists(file.path);
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    private String detectMimeType(String fileName) {
        try {
            String mimeType = Files.probeContentType(Path.of(fileName));
            if (StringUtils.isNotBlank(mimeType)) {
                return mimeType;
            }
        } catch (Exception ignore) {}
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".txt") || lower.endsWith(".log") || lower.endsWith(".md") || lower.endsWith(".csv")
                || lower.endsWith(".json") || lower.endsWith(".xml")) {
            return "text/plain; charset=UTF-8";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".mp4")) return "video/mp4";
        return "application/octet-stream";
    }

    private String normalizePreviewType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".ppt") || lower.endsWith(".pptx")
                || lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
            return "office";
        }
        if (lower.endsWith(".pdf")) return "office";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp")) {
            return "image";
        }
        if (lower.endsWith(".txt") || lower.endsWith(".log") || lower.endsWith(".md")
                || lower.endsWith(".csv") || lower.endsWith(".json") || lower.endsWith(".xml")) {
            return "text";
        }
        if (lower.endsWith(".mp4")) return "video";
        return "download";
    }

    private String registerTempPreviewFile(Path path, String name, String contentType) {
        cleanupExpiredTempPreviewFiles();
        String token = UUID.randomUUID().toString().replace("-", "");
        TEMP_PREVIEW_FILES.put(token, new TempPreviewFile(path, name, contentType, System.currentTimeMillis() + TEMP_PREVIEW_EXPIRE_MILLIS));
        return token;
    }

    private TempPreviewFile getTempPreviewFile(String id) {
        cleanupExpiredTempPreviewFiles();
        return TEMP_PREVIEW_FILES.get(id);
    }

    private Path downloadZipToTemp(String url) throws IOException {
        String signedUrl = url.contains("?") ? url : minioUtils.getPrivateUrl(url);
        Path tempFile = Files.createTempFile("preview_zip_", ".zip");
        FileUtils.copyURLToFile(new URL(signedUrl), tempFile.toFile());
        return tempFile;
    }

    private String findSofficeCommand() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String[] commonPaths = {
                    "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
                    "C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe"
            };
            for (String path : commonPaths) {
                if (Files.exists(Path.of(path))) {
                    return path;
                }
            }
            return "soffice.exe";
        }
        return "soffice";
    }

    private Path convertOfficeFileToPdf(Path inputFile) throws IOException, InterruptedException {
        Path tempOutDir = Files.createTempDirectory("preview_pdf_out_");
        List<String> command = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            command.add("cmd");
            command.add("/c");
        }
        command.add(findSofficeCommand());
        command.add("--headless");
        command.add("--convert-to");
        command.add("pdf");
        command.add("--outdir");
        command.add(tempOutDir.toString());
        command.add(inputFile.toAbsolutePath().toString());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroy();
            throw new IOException("Conversion timed out. Output: " + output);
        }
        if (process.exitValue() != 0) {
            throw new IOException("Conversion failed with exit code " + process.exitValue() + ". Output: " + output);
        }

        String originalName = inputFile.getFileName().toString();
        String pdfName = originalName.contains(".")
                ? originalName.substring(0, originalName.lastIndexOf(".")) + ".pdf"
                : originalName + ".pdf";
        Path pdfPath = tempOutDir.resolve(pdfName);
        if (Files.exists(pdfPath)) {
            return pdfPath;
        }
        try (var stream = Files.list(tempOutDir)) {
            Optional<Path> anyPdf = stream.filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".pdf")).findFirst();
            if (anyPdf.isPresent()) {
                return anyPdf.get();
            }
        }
        throw new IOException("PDF conversion output not found");
    }

    private String registerConvertedPdf(Path pdfPath, String originalName) throws IOException {
        String displayName = originalName != null && originalName.contains(".")
                ? originalName.substring(0, originalName.lastIndexOf(".")) + ".pdf"
                : "preview.pdf";
        return registerTempPreviewFile(pdfPath, displayName, "application/pdf");
    }

    private Charset resolveZipCharset(Path tempZip) {
        Charset charset = StandardCharsets.UTF_8;
        try (ZipFile zipFile = new ZipFile(tempZip.toFile(), charset)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) { entries.nextElement(); }
            return charset;
        } catch (IllegalArgumentException | java.util.zip.ZipException e) {
            return Charset.forName("GBK");
        } catch (IOException e) {
            return charset;
        }
    }

    /**
     * Get ZIP file tree structure
     */
    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('student') or @ss.hasRole('admin')")
    @GetMapping("/zip/tree")
    public AjaxResult getZipTree(@RequestParam("url") String url) {
        Path tempFile = null;
        try {
            tempFile = downloadZipToTemp(url);

            List<Map<String, Object>> tree = new ArrayList<>();
            java.nio.charset.Charset charset = resolveZipCharset(tempFile);

            try (ZipFile zipFile = new ZipFile(tempFile.toFile(), charset)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    Map<String, Object> node = new HashMap<>();
                    node.put("path", entry.getName());
                    node.put("isDirectory", entry.isDirectory());
                    node.put("size", entry.getSize());
                    tree.add(node);
                }
            }
            
            return AjaxResult.success(tree);

        } catch (Exception e) {
            log.error("Read ZIP failed", e);
            return AjaxResult.error("Read ZIP failed: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {}
            }
        }
    }

    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('student') or @ss.hasRole('admin')")
    @GetMapping("/zip/preview")
    public AjaxResult prepareZipFilePreview(@RequestParam("url") String url,
                                            @RequestParam("path") String path) {
        Path tempZip = null;
        try {
            tempZip = downloadZipToTemp(url);
            Charset charset = resolveZipCharset(tempZip);
            try (ZipFile zipFile = new ZipFile(tempZip.toFile(), charset)) {
                ZipEntry entry = zipFile.getEntry(path);
                if (entry == null || entry.isDirectory()) {
                    return AjaxResult.error("压缩包内文件不存在");
                }
                String fileName = Path.of(entry.getName()).getFileName().toString();
                String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : ".tmp";
                Path extracted = Files.createTempFile("zip_preview_item_", suffix);
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    Files.copy(inputStream, extracted, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                String type = normalizePreviewType(fileName);
                String mimeType = detectMimeType(fileName);
                String originalToken = registerTempPreviewFile(extracted, fileName, mimeType);
                String previewPath = "/common/preview/temp?id=" + originalToken;
                String downloadPath = previewPath + "&download=1";
                boolean canPreview = !"download".equals(type);

                Map<String, Object> result = new HashMap<>();
                result.put("name", fileName);
                result.put("type", type);
                result.put("canPreview", canPreview);
                result.put("previewUrl", previewPath);
                result.put("downloadUrl", downloadPath);
                return AjaxResult.success(result);
            }
        } catch (Exception e) {
            log.error("Prepare ZIP preview failed", e);
            return AjaxResult.error("该压缩包内文件暂不支持在线预览");
        } finally {
            if (tempZip != null) {
                try {
                    Files.deleteIfExists(tempZip);
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Get specific file content from ZIP
     * Returns a stream of the file content
     */
    // @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('student') or @ss.hasRole('admin')")
    @GetMapping("/zip/file")
    public void getZipFile(@RequestParam("url") String url, @RequestParam("path") String path, HttpServletResponse response) {
        log.info("Received request to get file from ZIP. URL: {}, Path: {}", url, path);
        Path tempFile = null;
        try {
            String signedUrl = url.contains("?") ? url : minioUtils.getPrivateUrl(url);
            log.info("Downloading ZIP from: {}", signedUrl);
            
            // Download to temp
            tempFile = Files.createTempFile("preview_zip_content_", ".zip");
            URL remoteUrl = new URL(signedUrl);
            FileUtils.copyURLToFile(remoteUrl, tempFile.toFile());
            log.info("ZIP downloaded to: {}, size: {}", tempFile, Files.size(tempFile));

            java.nio.charset.Charset charset = java.nio.charset.StandardCharsets.UTF_8;
            try {
                try (ZipFile zipFile = new ZipFile(tempFile.toFile(), charset)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) { entries.nextElement(); }
                }
            } catch (IllegalArgumentException | java.util.zip.ZipException e) {
                charset = java.nio.charset.Charset.forName("GBK");
            }

            try (ZipFile zipFile = new ZipFile(tempFile.toFile(), charset)) {
                log.info("Attempting to find entry: {}", path);
                ZipEntry entry = zipFile.getEntry(path);
                if (entry == null) {
                    log.warn("Entry not found in ZIP: {}", path);
                    // Try to list all entries to debug
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        log.info("Available entry: {}", entries.nextElement().getName());
                    }
                    
                    response.setStatus(404);
                    return;
                }
                
                log.info("Found entry: {}, size: {}", entry.getName(), entry.getSize());
                
                // Set content type based on file extension
                String mimeType = Files.probeContentType(Path.of(entry.getName()));
                if (mimeType == null) {
                    // Fallback
                    if (path.endsWith(".pdf")) mimeType = "application/pdf";
                    else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mimeType = "image/jpeg";
                    else if (path.endsWith(".png")) mimeType = "image/png";
                    else if (path.endsWith(".mp4")) mimeType = "video/mp4";
                    else if (path.endsWith(".doc") || path.endsWith(".docx")) mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    else if (path.endsWith(".ppt") || path.endsWith(".pptx")) mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                    else if (path.endsWith(".xls") || path.endsWith(".xlsx")) mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    else mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);
                response.setContentLengthLong(entry.getSize());
                
                try (InputStream is = zipFile.getInputStream(entry)) {
                    IOUtils.copy(is, response.getOutputStream());
                }
            }

        } catch (Exception e) {
            log.error("Read ZIP file content failed", e);
            response.setStatus(500);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {}
            }
        }
    }

    @GetMapping("/temp")
    public void getTempPreviewFile(@RequestParam("id") String id,
                                   @RequestParam(value = "download", required = false) String download,
                                   HttpServletResponse response) {
        cleanupExpiredTempPreviewFiles();
        TempPreviewFile file = TEMP_PREVIEW_FILES.get(id);
        if (file == null || file.path == null || !Files.exists(file.path)) {
            response.setStatus(404);
            return;
        }
        try {
            response.setContentType(StringUtils.isBlank(file.contentType) ? "application/octet-stream" : file.contentType);
            response.setHeader("Content-Disposition",
                    buildContentDisposition(("1".equals(download) || "true".equalsIgnoreCase(download)) ? "attachment" : "inline",
                            sanitizeDownloadName(file.name)));
            response.setContentLengthLong(Files.size(file.path));
            Files.copy(file.path, response.getOutputStream());
        } catch (Exception e) {
            log.error("Read temp preview file failed", e);
            response.setStatus(500);
        }
    }

    /**
     * Convert Office file to PDF and stream back
     */
    @PreAuthorize("@ss.hasRole('teacher') or @ss.hasRole('student') or @ss.hasRole('admin')")
    @GetMapping("/convert/pdf")
    public void convertToPdf(@RequestParam("url") String url, HttpServletResponse response) {
        Path tempIn = null;
        Path tempPdf = null;
        try {
            String extension = StringUtils.substringAfterLast(url.split("\\?")[0], ".");
            if (StringUtils.isEmpty(extension)) extension = "tmp";
            tempIn = Files.createTempFile("preview_office_", "." + extension);
            if (url.startsWith("/common/preview/temp?id=")) {
                String token = url.substring(url.indexOf("id=") + 3);
                if (token.contains("&")) {
                    token = token.substring(0, token.indexOf("&"));
                }
                TempPreviewFile tempPreviewFile = getTempPreviewFile(token);
                if (tempPreviewFile == null || tempPreviewFile.path == null || !Files.exists(tempPreviewFile.path)) {
                    throw new FileNotFoundException("临时预览文件不存在");
                }
                Files.copy(tempPreviewFile.path, tempIn, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else {
                String signedUrl = url.startsWith("http://") || url.startsWith("https://")
                        ? url
                        : (url.contains("?") ? url : minioUtils.getPrivateUrl(url));
                FileUtils.copyURLToFile(new URL(signedUrl), tempIn.toFile());
            }

            tempPdf = convertOfficeFileToPdf(tempIn);
            response.setContentType("application/pdf");
            Files.copy(tempPdf, response.getOutputStream());

        } catch (Exception e) {
            log.error("Convert to PDF failed", e);
            response.setStatus(500);
            try {
                response.getWriter().write("Conversion failed: " + e.getMessage());
            } catch (IOException ignored) {}
        } finally {
            try {
                if (tempIn != null) Files.deleteIfExists(tempIn);
                if (tempPdf != null) {
                    Path parent = tempPdf.getParent();
                    if (parent != null) {
                        FileUtils.deleteDirectory(parent.toFile());
                    }
                }
            } catch (IOException ignored) {}
        }
    }
}
