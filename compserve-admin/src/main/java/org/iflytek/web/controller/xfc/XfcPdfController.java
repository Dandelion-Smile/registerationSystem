package org.iflytek.web.controller.xfc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.utils.SecurityUtils;
import org.iflytek.web.service.XfcPdfService;

/** 讯飞杯报名表 PDF 生成、预览与下载。 */
@RestController
@RequestMapping("/api/xfc/registrations")
public class XfcPdfController
{
    @Autowired private XfcPdfService pdfService;

    @PostMapping("/current/pdf")
    public AjaxResult generate()
    {
        return AjaxResult.success(pdfService.generate(SecurityUtils.getUserId()));
    }

    @GetMapping("/{id}/pdf")
    public void preview(@PathVariable Long id, HttpServletResponse response) throws IOException
    {
        Path file = pdfService.resolveFile(id, SecurityUtils.getUserId());
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "inline; filename=registration-form.pdf");
        response.setContentLengthLong(Files.size(file));
        Files.copy(file, response.getOutputStream());
    }
}
