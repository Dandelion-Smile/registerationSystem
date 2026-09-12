package org.iflytek.web.controller.xfc;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.utils.SecurityUtils;
import org.iflytek.system.service.XfcRegistrationDraftService;

/** 讯飞杯报名草稿接口。 */
@RestController
@RequestMapping("/api/xfc/registration")
public class XfcRegistrationController
{
    @Autowired
    private XfcRegistrationDraftService draftService;

    @GetMapping("/current")
    public AjaxResult current()
    {
        return AjaxResult.success(draftService.current(SecurityUtils.getUserId()));
    }

    @PutMapping("/draft")
    public AjaxResult saveDraft(@RequestBody Map<String, Object> request)
    {
        return AjaxResult.success(draftService.save(SecurityUtils.getUserId(), request));
    }

    @PostMapping("/materials/{materialType}")
    public AjaxResult uploadMaterial(@PathVariable String materialType, @RequestParam("file") MultipartFile file) throws Exception
    {
        return AjaxResult.success(draftService.uploadMaterial(SecurityUtils.getUserId(), materialType, file));
    }
}
