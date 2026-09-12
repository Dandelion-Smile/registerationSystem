package org.iflytek.system.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.iflytek.common.exception.ServiceException;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.system.mapper.XfcRegistrationMapper;
import org.iflytek.common.utils.file.FileUploadUtils;
import org.iflytek.common.utils.file.MinioUtils;
import org.springframework.web.multipart.MultipartFile;

/** 讯飞杯草稿：只允许当前报名编辑人读取和写入自己的草稿。 */
@Service
public class XfcRegistrationDraftService
{
    private static final int MAX_MEMBERS = 5;
    private static final int MAX_ADVISORS = 2;
    private static final int MAX_WORK_SUMMARY_LENGTH = 200;

    @Autowired
    private XfcRegistrationMapper mapper;

    @Autowired
    private MinioUtils minioUtils;

    private static final String APPLICATION = "APPLICATION";
    private static final String PRESENTATION = "PRESENTATION";
    private static final String SUPPLEMENT = "SUPPLEMENT";
    private static final String[] WORD_EXTENSIONS = { "doc", "docx" };
    private static final String[] PPT_EXTENSIONS = { "ppt", "pptx" };

    public Map<String, Object> current(Long captainUserId)
    {
        Map<String, Object> registration = mapper.selectCurrentDraft(captainUserId);
        // 页面加载只能读取。此前这里会创建草稿并插入队长快照，刷新页面时会重复插入成员。
        return registration == null ? emptyResponse(captainUserId) : response(registration);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> save(Long captainUserId, Map<String, Object> request)
    {
        validateWorkSummary(text(request.get("workSummary")));
        Map<String, Object> registration = mapper.selectCurrentDraft(captainUserId);
        if (registration == null)
        {
            registration = new HashMap<>();
            registration.put("captainUserId", captainUserId);
            registration.put("trackCode", text(request.get("trackCode")));
            registration.put("teamName", text(request.get("teamName")));
            registration.put("workTitle", text(request.get("workTitle")));
            registration.put("workSummary", text(request.get("workSummary")));
            registration.put("statementTruth", bool(request.get("statementTruth")));
            registration.put("statementEligibility", bool(request.get("statementEligibility")));
            registration.put("statementCopyright", bool(request.get("statementCopyright")));
            mapper.insertRegistration(registration);
        }
        else
        {
            registration.put("trackCode", text(request.get("trackCode")));
            registration.put("teamName", text(request.get("teamName")));
            registration.put("workTitle", text(request.get("workTitle")));
            registration.put("workSummary", text(request.get("workSummary")));
            registration.put("statementTruth", bool(request.get("statementTruth")));
            registration.put("statementEligibility", bool(request.get("statementEligibility")));
            registration.put("statementCopyright", bool(request.get("statementCopyright")));
            mapper.updateDraft(registration);
        }

        Long registrationId = asLong(registration.get("id"));
        List<Map<String, Object>> members = maps(request.get("members"));
        List<Map<String, Object>> advisors = maps(request.get("advisors"));
        Map<String, Object> captain = oneMap(request.get("captain"));
        validateLimits(captain, members, advisors);

        mapper.deleteMembers(registrationId);
        mapper.deleteAdvisors(registrationId);
        ensureCaptainSnapshot(registrationId, captain);
        int sortNo = 1;
        for (Map<String, Object> member : members)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("registrationId", registrationId);
            item.put("role", "MEMBER");
            item.put("userId", null);
            item.put("studentNo", nonNullText(member.get("studentNo")));
            item.put("name", nonNullText(member.get("name")));
            item.put("college", nonNullText(member.get("college")));
            item.put("major", nonNullText(member.get("major")));
            item.put("className", nonNullText(member.get("className")));
            item.put("phone", nonNullText(member.get("phone")));
            item.put("sortNo", sortNo++);
            mapper.insertMember(item);
        }
        int advisorSort = 1;
        for (Map<String, Object> advisor : advisors)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("registrationId", registrationId);
            item.put("name", nonNullText(advisor.get("name")));
            item.put("organization", nonNullText(advisor.get("organization")));
            item.put("title", nonNullText(advisor.get("title")));
            item.put("phone", nonNullText(advisor.get("phone")));
            item.put("email", nonNullText(advisor.get("email")));
            item.put("sortNo", advisorSort++);
            mapper.insertAdvisor(item);
        }
        return response(mapper.selectCurrentDraft(captainUserId));
    }

    /** 上传并替换当前报名的一类资料；同类资料始终只保留最新版本。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadMaterial(Long captainUserId, String materialType, MultipartFile file) throws Exception
    {
        validateMaterialType(materialType);
        if (file == null || file.isEmpty()) throw new ServiceException("请选择要上传的文件");
        FileUploadUtils.assertAllowed(file, allowedExtensions(materialType));

        Map<String, Object> registration = mapper.selectCurrentDraft(captainUserId);
        if (registration == null)
        {
            registration = createEmptyRegistration(captainUserId);
        }
        Long registrationId = asLong(registration.get("id"));
        Map<String, Object> previous = mapper.selectMaterial(registrationId, materialType);
        String url = minioUtils.upload(file);
        Map<String, Object> material = new HashMap<>();
        material.put("registrationId", registrationId);
        material.put("materialType", materialType);
        material.put("originalFilename", safeFilename(file.getOriginalFilename()));
        material.put("fileUrl", url);
        material.put("fileSize", file.getSize());
        material.put("contentType", nonNullText(file.getContentType()));
        mapper.upsertMaterial(material);
        if (previous != null && previous.get("fileUrl") != null)
        {
            try { minioUtils.delete(String.valueOf(previous.get("fileUrl"))); } catch (Exception ignored) { }
        }
        return materialResponse(mapper.selectMaterial(registrationId, materialType));
    }

    /**
     * 队长由报名编辑人手动填写，不必与当前登录账号相同。
     */
    private void ensureCaptainSnapshot(Long registrationId, Map<String, Object> input)
    {
        Map<String, Object> captain = new HashMap<>();
        captain.put("registrationId", registrationId);
        captain.put("role", "CAPTAIN");
        captain.put("userId", null);
        captain.put("studentNo", field(input, "studentNo", ""));
        captain.put("name", field(input, "name", ""));
        captain.put("college", field(input, "college", ""));
        captain.put("major", field(input, "major", ""));
        captain.put("className", field(input, "className", ""));
        captain.put("phone", field(input, "phone", ""));
        captain.put("sortNo", 0);
        mapper.insertMember(captain);
    }

    /** 在生成 PDF 前再次校验，避免绕过页面限制或使用旧草稿生成超限报名表。 */
    public void validateForPdf(Map<String, Object> data)
    {
        Map<String, Object> captain = oneMap(data.get("captain"));
        List<Map<String, Object>> members = maps(data.get("members"));
        List<Map<String, Object>> advisors = maps(data.get("advisors"));
        validateLimits(captain, members, advisors);
        if (text(captain.get("name")) == null || text(captain.get("studentNo")) == null)
        {
            throw new ServiceException("生成报名表前，请填写队长姓名和学号");
        }
        Map<String, Object> registration = oneMap(data.get("registration"));
        Long registrationId = registration == null ? null : asLong(registration.get("id"));
        if (registrationId == null || mapper.selectMaterial(registrationId, APPLICATION) == null
                || mapper.selectMaterial(registrationId, PRESENTATION) == null)
        {
            throw new ServiceException("生成报名表前，请上传必交的申报书和 PPT 演示资料");
        }
    }

    private void validateLimits(Map<String, Object> captain, List<Map<String, Object>> members,
            List<Map<String, Object>> advisors)
    {
        if (members.size() > MAX_MEMBERS)
        {
            throw new ServiceException("队员最多添加 " + MAX_MEMBERS + " 人");
        }
        if (advisors.size() > MAX_ADVISORS)
        {
            throw new ServiceException("指导教师最多添加 " + MAX_ADVISORS + " 人");
        }
        java.util.Set<String> studentNos = new java.util.HashSet<>();
        addStudentNo(studentNos, captain == null ? null : captain.get("studentNo"));
        for (Map<String, Object> member : members)
        {
            addStudentNo(studentNos, member.get("studentNo"));
        }
    }

    private void validateWorkSummary(String workSummary)
    {
        if (workSummary != null && workSummary.length() > MAX_WORK_SUMMARY_LENGTH)
        {
            throw new ServiceException("作品简介最多填写 " + MAX_WORK_SUMMARY_LENGTH + " 字");
        }
    }

    private void addStudentNo(java.util.Set<String> studentNos, Object value)
    {
        String studentNo = text(value);
        if (studentNo != null && !studentNos.add(studentNo))
        {
            throw new ServiceException("队长和队员的学号不能重复");
        }
    }

    private Map<String, Object> response(Map<String, Object> registration)
    {
        Long id = asLong(registration.get("id"));
        Map<String, Object> result = new HashMap<>();
        result.put("registration", registration);
        List<Map<String, Object>> allMembers = mapper.selectMembers(id);
        Map<String, Object> captain = new HashMap<>();
        List<Map<String, Object>> members = new ArrayList<>();
        for (Map<String, Object> member : allMembers)
        {
            if ("CAPTAIN".equals(member.get("role"))) captain = member;
            else members.add(member);
        }
        result.put("captain", captain);
        result.put("members", members);
        result.put("advisors", mapper.selectAdvisors(id));
        List<Map<String, Object>> materials = new ArrayList<>();
        for (Map<String, Object> material : mapper.selectMaterials(id)) materials.add(materialResponse(material));
        result.put("materials", materials);
        return result;
    }

    private Map<String, Object> emptyResponse(Long captainUserId)
    {
        Map<String, Object> registration = new HashMap<>();
        registration.put("captainUserId", captainUserId);
        registration.put("trackCode", null);
        registration.put("teamName", null);
        registration.put("workTitle", null);
        registration.put("workSummary", null);
        registration.put("statementTruth", false);
        registration.put("statementEligibility", false);
        registration.put("statementCopyright", false);
        Map<String, Object> result = new HashMap<>();
        result.put("registration", registration);
        result.put("captain", new HashMap<>());
        result.put("members", new ArrayList<>());
        result.put("advisors", new ArrayList<>());
        result.put("materials", new ArrayList<>());
        return result;
    }

    private Map<String, Object> createEmptyRegistration(Long captainUserId)
    {
        Map<String, Object> registration = new HashMap<>();
        registration.put("captainUserId", captainUserId);
        registration.put("trackCode", null);
        registration.put("teamName", null);
        registration.put("workTitle", null);
        registration.put("workSummary", null);
        registration.put("statementTruth", false);
        registration.put("statementEligibility", false);
        registration.put("statementCopyright", false);
        mapper.insertRegistration(registration);
        return registration;
    }

    private void validateMaterialType(String materialType)
    {
        if (!APPLICATION.equals(materialType) && !PRESENTATION.equals(materialType) && !SUPPLEMENT.equals(materialType))
            throw new ServiceException("不支持的资料类别");
    }

    private String[] allowedExtensions(String materialType)
    {
        if (APPLICATION.equals(materialType)) return WORD_EXTENSIONS;
        if (PRESENTATION.equals(materialType)) return PPT_EXTENSIONS;
        return org.iflytek.common.utils.file.MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION;
    }

    private Map<String, Object> materialResponse(Map<String, Object> material)
    {
        if (material == null) return null;
        Map<String, Object> result = new HashMap<>(material);
        result.remove("fileUrl");
        return result;
    }

    private String safeFilename(String filename)
    {
        String result = filename == null ? "未命名文件" : filename.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_").trim();
        return result.length() > 150 ? result.substring(0, 150) : result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> maps(Object value)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (!(value instanceof List<?> list)) return result;
        for (Object item : list)
        {
            if (item instanceof Map<?, ?> raw)
            {
                Map<String, Object> map = new HashMap<>();
                raw.forEach((key, val) -> { if (key != null) map.put(String.valueOf(key), val); });
                result.add(map);
            }
        }
        return result;
    }

    private Map<String, Object> oneMap(Object value)
    {
        if (!(value instanceof Map<?, ?> raw)) return null;
        Map<String, Object> result = new HashMap<>();
        raw.forEach((key, val) -> { if (key != null) result.put(String.valueOf(key), val); });
        return result;
    }

    private boolean bool(Object value) { return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value)); }
    private String text(Object value) { String valueText = value == null ? null : String.valueOf(value).trim(); return StringUtils.isEmpty(valueText) ? null : valueText; }
    private String nonNullText(Object value) { String valueText = text(value); return valueText == null ? "" : valueText; }
    private String field(Map<String, Object> input, String key, String fallback) { return input != null && input.containsKey(key) ? nonNullText(input.get(key)) : nonNullText(fallback); }
    private Long asLong(Object value) { return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value)); }
}
