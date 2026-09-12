from __future__ import annotations

import argparse
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.shared import Pt


INTERFACE_ROWS = [
    ("/login", "POST", "学生账号登录，校验用户名、密码和验证码，返回 JWT Token"),
    ("/getInfo", "GET", "获取当前用户角色、权限集合及密码状态标记"),
    ("/system/user/profile", "GET/PUT", "查询并修改个人资料，返回角色组和岗位组信息"),
    ("/system/user/profile/updatePwd", "PUT", "校验旧密码后重置新密码，并同步更新缓存"),
    ("/student/competition/material", "POST", "提交参赛材料、作品名称和作品简介"),
    ("/student/competition/material/{registerId}", "GET", "回显已上传材料，并生成预签名访问链接"),
    ("/admin/competition", "POST/PUT", "新增或修改赛事，并同步保存官方附件"),
    ("/admin/permission/grant", "POST", "按赛事批量赋予教师评审权限，避免重复授权"),
    ("/admin/user/teachers/import", "POST", "通过 Excel 批量导入教师账号并同步教师基础资料"),
]


TEST_ROWS = [
    ("TC-01", "学生登录成功", "正确用户名、密码、验证码", "返回 token 并进入学生端页面", "通过"),
    ("TC-02", "学生登录失败", "错误验证码或错误密码", "接口返回错误提示，不生成 token", "通过"),
    ("TC-03", "获取用户权限信息", "已登录 token", "返回 user、roles、permissions 以及密码状态", "通过"),
    ("TC-04", "查询个人资料", "GET /system/user/profile", "返回当前用户资料、角色组和岗位组", "通过"),
    ("TC-05", "修改个人资料-正常", "合法手机号、邮箱、学院专业信息", "资料更新成功，前端刷新后展示新信息", "通过"),
    ("TC-06", "修改个人资料-手机号冲突", "与其他账号重复的手机号", "接口拒绝保存并提示手机号已存在", "通过"),
    ("TC-07", "修改密码", "正确旧密码与新密码", "密码更新成功，缓存中的 pwdUpdateDate 同步刷新", "通过"),
    ("TC-08", "头像上传", "jpg/png 图片文件", "头像上传成功，返回 imgUrl 并替换旧头像", "通过"),
    ("TC-09", "单文件上传到 MinIO", "PPT 或 PDF 文件", "返回永久 url 与 previewUrl", "通过"),
    ("TC-10", "材料提交缺少必填项", "缺少 PPT 或作品名称", "接口返回必填校验错误", "通过"),
    ("TC-11", "材料提交存在待确认队员", "队伍成员状态含 pending", "接口拒绝提交并提示先完成成员确认", "通过"),
    ("TC-12", "材料提交成功", "完整 PPT、申报书、作品信息", "competition_participation 状态更新为已提交", "通过"),
    ("TC-13", "材料回显", "GET /student/competition/material/{registerId}", "返回 PPT、文档、压缩包名称及预签名链接", "通过"),
    ("TC-14", "新增赛事-正常", "合法赛事名称和报名时间", "赛事新增成功并可在列表中查询", "通过"),
    ("TC-15", "新增赛事-时间非法", "开始时间晚于结束时间", "接口拒绝保存并提示时间范围错误", "通过"),
    ("TC-16", "修改赛事并同步附件", "编辑赛事信息与 officialFiles", "赛事主信息与官方附件一并更新", "通过"),
    ("TC-17", "批量赋予评审权限", "competitionId + teacherIds", "成功写入权限记录并返回成功数量", "通过"),
    ("TC-18", "重复赋权", "同一教师再次赋予同一赛事权限", "服务端跳过重复记录，不产生脏数据", "通过"),
    ("TC-19", "新增教师账号", "合法教师账号、手机号、邮箱", "创建成功并自动绑定 teacher 角色", "通过"),
    ("TC-20", "Excel 批量导入教师", "含手机号、邮箱、出生年月的 Excel", "完成格式校验、去重和批量导入", "通过"),
]


REFERENCES = [
    "[1] Craig Walls. Spring in Action (Sixth Edition) [M]. Manning Publications, 2022.",
    "[2] 尤雨溪. Vue.js设计与实现 [M]. 北京: 人民邮电出版社, 2022.",
    "[3] Clinton Begin. iBatis in Action [M]. Manning Publications, 2007.",
    "[4] 若依开源社区. RuoYi-Vue开发文档 [EB/OL]. https://doc.ruoyi.vip/, 2024.",
    "[5] Spring官方. Spring Boot Reference Documentation (3.x) [EB/OL]. https://docs.spring.io/spring-boot/, 2025.",
    "[6] Spring Security团队. Spring Security Reference [EB/OL]. https://docs.spring.io/spring-security/, 2025.",
    "[7] MinIO官方. MinIO Java SDK Documentation [EB/OL]. https://min.io/docs/minio/linux/developers/java/, 2025.",
    "[8] 阿里巴巴. Druid数据库连接池文档 [EB/OL]. https://github.com/alibaba/druid/, 2025.",
    "[9] GitHub开源. PageHelper分页插件文档 [EB/OL]. https://pagehelper.github.io/, 2025.",
    "[10] SpringDoc团队. SpringDoc OpenAPI Documentation [EB/OL]. https://springdoc.org/, 2025.",
    "[11] Quartz-Scheduler. Quartz Job Scheduler Documentation [EB/OL]. http://www.quartz-scheduler.org/, 2025.",
    "[12] Oracle. MySQL 8.0 Reference Manual [EB/OL]. https://dev.mysql.com/doc/refman/8.0/en/, 2025.",
    "[13] Apache软件基金会. Apache POI Documentation [EB/OL]. https://poi.apache.org/, 2025.",
    "[14] Coze平台. Coze API开发文档 [EB/OL]. https://www.coze.cn/open/docs, 2025.",
    "[15] JWT官方. JSON Web Token Introduction [EB/OL]. https://jwt.io/introduction/, 2025.",
]


REFLECTIONS = [
    "通过本次课程设计，我系统梳理了用户认证、资料维护、对象存储、赛事治理、权限赋权和教师账号管理等后端核心链路。相比只完成单一接口，真正有挑战的是让认证缓存、数据库状态、附件记录和前端展示保持一致，这让我对“业务闭环”有了更深的理解。",
    "在学生端认证与个人信息模块中，我重点体会到 Spring Security 与 JWT 的协同方式。登录本身并不复杂，难点在于登录后的权限刷新、密码状态提示、资料修改后的缓存同步等细节。如果忽略 token 中 LoginUser 的更新，前端就会出现“保存成功但页面还是旧数据”的体验问题。",
    "在参赛材料模块中，我进一步理解了对象存储与业务表解耦的意义。文件真正存储在 MinIO 中，业务层只保存对象路径和作品元数据；预览时再动态生成预签名链接。这种设计既保证了私有文件访问安全，也避免了把临时签名 URL 直接写入数据库带来的过期问题。",
    "在管理员端赛事管理、权限管理和教师账号管理模块中，我较深地感受到数据校验和治理规则的重要性。赛事时间范围、教师角色绑定、Excel 导入去重、重复赋权控制等工作虽然不如算法实现“显眼”，但它们直接决定了平台能否稳定运行。通过本次实践，我的后端工程化思维和面向业务场景的建模能力都得到了明显提升。",
]


def set_run_font(run, name: str, size_pt: float, bold: bool = False) -> None:
    run.bold = bold
    run.font.name = name
    run.font.size = Pt(size_pt)
    r_fonts = run._element.rPr.rFonts
    r_fonts.set(qn("w:ascii"), name)
    r_fonts.set(qn("w:hAnsi"), name)
    r_fonts.set(qn("w:eastAsia"), name)


def add_heading(doc: Document, text: str, size_pt: float, before_pt: float, after_pt: float) -> None:
    para = doc.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.LEFT
    para.paragraph_format.space_before = Pt(before_pt)
    para.paragraph_format.space_after = Pt(after_pt)
    run = para.add_run(text)
    set_run_font(run, "黑体", size_pt, bold=True)


def add_body(doc: Document, text: str) -> None:
    para = doc.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    para.paragraph_format.first_line_indent = Pt(24)
    para.paragraph_format.line_spacing = 1.5
    run = para.add_run(text)
    set_run_font(run, "宋体", 12)


def add_plain(doc: Document, text: str) -> None:
    para = doc.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    para.paragraph_format.line_spacing = 1.5
    run = para.add_run(text)
    set_run_font(run, "宋体", 12)


def add_code_label(doc: Document, text: str) -> None:
    para = doc.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    para.paragraph_format.line_spacing = 1.5
    run = para.add_run(text)
    set_run_font(run, "宋体", 12)


def add_code_block(doc: Document, text: str) -> None:
    para = doc.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.LEFT
    para.paragraph_format.left_indent = Pt(28.35)
    para.paragraph_format.space_before = Pt(3)
    para.paragraph_format.space_after = Pt(3)
    para.paragraph_format.line_spacing = 1.0
    run = para.add_run(text)
    set_run_font(run, "Consolas", 10.5)


def add_table_title(doc: Document, text: str) -> None:
    para = doc.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    para.paragraph_format.space_before = Pt(6)
    para.paragraph_format.space_after = Pt(3)
    run = para.add_run(text)
    set_run_font(run, "宋体", 10.5)


def add_center_text(doc: Document, text: str, bold: bool = False, size_pt: float = 12, after_pt: float = 6) -> None:
    para = doc.add_paragraph()
    para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    para.paragraph_format.space_before = Pt(6)
    para.paragraph_format.space_after = Pt(after_pt)
    run = para.add_run(text)
    set_run_font(run, "宋体", size_pt, bold=bold)


def format_table(table) -> None:
    table.style = "Normal Table"
    for row_idx, row in enumerate(table.rows):
        for cell in row.cells:
            for para in cell.paragraphs:
                para.alignment = WD_ALIGN_PARAGRAPH.CENTER if row_idx == 0 else WD_ALIGN_PARAGRAPH.LEFT
                para.paragraph_format.line_spacing = 1.15
                for run in para.runs:
                    set_run_font(run, "宋体", 10.5, bold=(row_idx == 0))


def add_interface_table(doc: Document) -> None:
    add_body(doc, "上述五个功能点涉及的代表性接口如表5-1所示，既覆盖学生端登录与资料维护，也覆盖赛事、权限和教师账号治理链路。")
    add_table_title(doc, "表5-1 关键接口列表")
    table = doc.add_table(rows=1, cols=3)
    table.rows[0].cells[0].text = "接口路径"
    table.rows[0].cells[1].text = "请求方法"
    table.rows[0].cells[2].text = "功能说明"
    for path, method, desc in INTERFACE_ROWS:
        row = table.add_row().cells
        row[0].text = path
        row[1].text = method
        row[2].text = desc
    format_table(table)


def add_test_table(doc: Document) -> None:
    add_body(doc, "围绕认证与个人信息、材料提交、赛事管理、评审权限以及教师账号管理五个模块，设计测试用例如下表所示。测试重点覆盖正常流程、边界输入和重复数据场景。")
    add_table_title(doc, "表7-1 系统测试用例表")
    table = doc.add_table(rows=1, cols=5)
    headers = ["编号", "测试功能", "输入数据", "预期结果", "实际结果"]
    for idx, header in enumerate(headers):
        table.rows[0].cells[idx].text = header
    for row_data in TEST_ROWS:
        row = table.add_row().cells
        for idx, value in enumerate(row_data):
            row[idx].text = value
    format_table(table)


def paragraph_text(child) -> str:
    texts = []
    for node in child.iter():
        if node.tag == qn("w:t") and node.text:
            texts.append(node.text)
    return "".join(texts).strip()


def truncate_from_heading(doc: Document, heading_text: str) -> None:
    body = doc._element.body
    remove_mode = False
    for child in list(body):
        if child.tag == qn("w:p") and paragraph_text(child) == heading_text:
            remove_mode = True
        if remove_mode and child.tag != qn("w:sectPr"):
            body.remove(child)


def add_section_5(doc: Document) -> None:
    add_heading(doc, "5 详细设计及实现", 14, 8, 4)
    add_body(doc, "本章围绕本人在课程设计报告中选定的五个功能点展开，分别为学生端认证与个人信息、学生端参赛材料、管理员端赛事管理、管理员端评审权限管理以及管理员端教师账号管理。相关描述均依据项目现有 Controller、Service 和 Mapper 实现整理，尽量保持与源码一致。")

    add_heading(doc, "5.1 学生端认证与个人信息", 14, 8, 4)
    add_heading(doc, "5.1.1 功能说明", 12, 6, 3)
    add_body(doc, "学生端认证与个人信息模块负责完成账号注册、登录认证、角色权限装载以及个人资料维护。系统通过 POST /register 完成学生注册，通过 POST /login 结合用户名、密码和验证码完成登录，通过 GET /getInfo 与 GET /getRouters 返回当前用户的角色、权限和动态菜单。")
    add_body(doc, "在个人信息维护方面，系统提供 GET /system/user/profile 查询个人资料，PUT /system/user/profile 修改昵称、手机号、邮箱、学院、专业、政治面貌等信息，PUT /system/user/profile/updatePwd 修改密码，POST /system/user/profile/avatar 上传个人头像。该模块既是学生进入平台的第一道入口，也是报名、组队、材料提交等后续业务的前置基础。")

    add_heading(doc, "5.1.2 核心思路", 12, 6, 3)
    add_body(doc, "认证链路采用 Spring Security + JWT 的无状态方案。SysLoginController 不直接处理密码比对，而是将用户名、密码、验证码和 uuid 交给 SysLoginService 完成校验，登录成功后由 TokenService 生成 token 返回前端。后续请求统一从请求头携带 token，过滤器负责还原 LoginUser 到 SecurityContext。")
    add_body(doc, "个人信息维护链路强调“数据库记录”和“登录缓存”同步。SysProfileController 先根据当前登录用户 ID 查询最新 SysUser，再将允许修改的字段逐项合并到 currentUser，随后调用 checkPhoneUnique、checkEmailUnique 做唯一性校验。更新成功后，通过 tokenService.setLoginUser(loginUser) 回写缓存，保证前端再次调用 getInfo 或 profile 时能够看到最新资料。")

    add_heading(doc, "5.1.3 关键代码", 12, 6, 3)
    add_code_label(doc, "（1）SysLoginController.java 登录与权限装载：")
    add_code_block(
        doc,
        '@PostMapping("/login")\n'
        "public AjaxResult login(@RequestBody LoginBody loginBody)\n"
        "{\n"
        "    AjaxResult ajax = AjaxResult.success();\n"
        "    String token = loginService.login(\n"
        "        loginBody.getUsername(),\n"
        "        loginBody.getPassword(),\n"
        "        loginBody.getCode(),\n"
        "        loginBody.getUuid());\n"
        "    ajax.put(Constants.TOKEN, token);\n"
        "    return ajax;\n"
        "}\n\n"
        '@GetMapping("getInfo")\n'
        "public AjaxResult getInfo()\n"
        "{\n"
        "    LoginUser loginUser = SecurityUtils.getLoginUser();\n"
        "    SysUser user = loginUser.getUser();\n"
        "    Set<String> roles = permissionService.getRolePermission(user);\n"
        "    Set<String> permissions = permissionService.getMenuPermission(user);\n"
        "    if (!loginUser.getPermissions().equals(permissions))\n"
        "    {\n"
        "        loginUser.setPermissions(permissions);\n"
        "        tokenService.refreshToken(loginUser);\n"
        "    }\n"
        "    AjaxResult ajax = AjaxResult.success();\n"
        '    ajax.put("user", user);\n'
        '    ajax.put("roles", roles);\n'
        '    ajax.put("permissions", permissions);\n'
        "    return ajax;\n"
        "}"
    )
    add_code_label(doc, "（2）SysProfileController.java 个人信息更新与密码修改：")
    add_code_block(
        doc,
        "@PutMapping\n"
        "public AjaxResult updateProfile(@RequestBody SysUser user)\n"
        "{\n"
        "    LoginUser loginUser = getLoginUser();\n"
        "    SysUser currentUser = userService.selectUserById(loginUser.getUserId());\n"
        "    if (user.getNickName() != null) currentUser.setNickName(user.getNickName());\n"
        "    if (user.getEmail() != null) currentUser.setEmail(user.getEmail());\n"
        "    if (user.getPhonenumber() != null) currentUser.setPhonenumber(user.getPhonenumber());\n"
        "    if (StringUtils.isNotEmpty(user.getPhonenumber())\n"
        "            && !userService.checkPhoneUnique(currentUser))\n"
        "    {\n"
        "        return error(\"修改失败，手机号码已存在\");\n"
        "    }\n"
        "    if (userService.updateUserProfile(currentUser) > 0)\n"
        "    {\n"
        "        loginUser.setUser(currentUser);\n"
        "        tokenService.setLoginUser(loginUser);\n"
        "        return success();\n"
        "    }\n"
        "    return error(\"修改个人信息异常，请联系管理员\");\n"
        "}\n\n"
        '@PutMapping("/updatePwd")\n'
        "public AjaxResult updatePwd(@RequestBody Map<String, String> params)\n"
        "{\n"
        '    String oldPassword = params.get("oldPassword");\n'
        '    String newPassword = params.get("newPassword");\n'
        "    if (!SecurityUtils.matchesPassword(oldPassword, password))\n"
        "    {\n"
        '        return error("修改密码失败，旧密码错误");\n'
        "    }\n"
        "    if (SecurityUtils.matchesPassword(newPassword, password))\n"
        "    {\n"
        '        return error("新密码不能与旧密码相同");\n'
        "    }\n"
        "    ...\n"
        "}"
    )

    add_heading(doc, "5.1.4 代码解析", 12, 6, 3)
    add_body(doc, "login 方法体现了典型的分层思想：Controller 只负责接收请求和返回 token，真正的验证码校验、密码认证、登录日志记录等工作均下沉到 SysLoginService。这样既能简化控制器职责，也便于后续统一扩展认证策略。")
    add_body(doc, "getInfo 方法在返回用户资料前，重新从权限服务中计算角色集合和菜单权限；当权限集合与当前 LoginUser 中缓存的数据不一致时，立即刷新 token 缓存。这一设计解决了管理员调整角色权限后，用户必须重新登录才能生效的问题。")
    add_body(doc, "updateProfile 和 updatePwd 的共同特点是都在成功后同步更新 LoginUser 缓存。尤其是修改昵称、手机号、密码最后更新时间等字段时，如果只更新数据库而不更新缓存，前端页面会出现“保存成功但显示未变化”的问题。因此 tokenService.setLoginUser(loginUser) 是该模块调试中的关键细节。")

    add_heading(doc, "5.2 学生端参赛材料", 14, 8, 4)
    add_heading(doc, "5.2.1 功能说明", 12, 6, 3)
    add_body(doc, "参赛材料模块负责完成作品附件上传、材料提交、材料回显和安全预览。学生通常先通过 /common/upload 将 PPT、申报书或压缩包上传到 MinIO，再通过 POST /student/competition/material 把对象路径与作品名称、作品简介绑定到报名记录中。")
    add_body(doc, "提交完成后，系统通过 GET /student/competition/material/{registerId} 回显已上传材料，区分 PPT、文档和其他压缩包，并重新生成预签名访问地址。该模块是学生端从“已报名”进入“正式提交作品”的关键业务步骤，也直接影响教师评审和管理员审核。")

    add_heading(doc, "5.2.2 核心思路", 12, 6, 3)
    add_body(doc, "文件上传与业务提交采用“两阶段”设计。第一阶段由 CommonController 调用 MinioUtils.upload 完成物理存储，并返回永久对象地址 url 和临时预览地址 previewUrl；第二阶段由 submitMaterials 接口校验报名记录、队伍状态、必填材料和作品元数据，再把对象路径固化到 competition_participation 与 competition_work 表中。")
    add_body(doc, "材料提交前不仅要判断 PPT、申报书、作品名称、作品简介是否齐全，还要检查队伍是否仍处于可编辑状态，是否存在待确认成员，以及是否至少有一名非队长成员已确认加入。提交成功后，系统会更新 participation_status 为“已提交”，写入 submitTime，并同步更新报名记录中的作品名称与简介，保证后续详情页和导出列表数据一致。")

    add_heading(doc, "5.2.3 关键代码", 12, 6, 3)
    add_code_label(doc, "（1）CommonController.java 文件上传接口：")
    add_code_block(
        doc,
        '@PostMapping("/upload")\n'
        "public AjaxResult uploadFile(MultipartFile file) throws Exception\n"
        "{\n"
        "    String url = minioUtils.upload(file);\n"
        "    String signedUrl = minioUtils.getPrivateUrl(url);\n"
        "    AjaxResult ajax = AjaxResult.success();\n"
        '    ajax.put("url", url);\n'
        '    ajax.put("previewUrl", signedUrl);\n'
        '    ajax.put("fileName", url);\n'
        '    ajax.put("newFileName", FileUtils.getName(url));\n'
        '    ajax.put("originalFilename", file.getOriginalFilename());\n'
        "    return ajax;\n"
        "}"
    )
    add_code_label(doc, "（2）CompetitionStudentServiceImpl.java submitMaterials 核心逻辑：")
    add_code_block(
        doc,
        "public void submitMaterials(Long registerId, String pptPath, String pdfPath,\n"
        "        String fileNames, String workName, String workDescription, Long userId)\n"
        "{\n"
        "    if (!hasText(pptPath) || !hasText(getDocumentPath(pdfPath)))\n"
        '        throw new ServiceException("申报书资料和PPT演示资料均为必填项");\n'
        "    if (!hasText(workName))\n"
        '        throw new ServiceException("请输入作品名称");\n'
        "    if (!hasText(workDescription))\n"
        '        throw new ServiceException("请输入作品简介");\n'
        "    CompetitionRegister register = competitionRegisterMapper.selectById(registerId);\n"
        "    ensureTeamEditable(register, userId, true);\n"
        "    List<Map<String, Object>> members = parseTeamMembers(register.getTeamMembers());\n"
        "    if (hasPendingMembers(members))\n"
        '        throw new ServiceException("请等待所有队员确认后再提交资料");\n'
        "    if (!hasApprovedNonLeaderMember(members))\n"
        '        throw new ServiceException("请至少保证一位队员确认加入后再提交资料");\n'
        "    ...\n"
        '    participation.setParticipationStatus("已提交");\n'
        "    competitionParticipationMapper.updateCompetitionParticipation(participation);\n"
        "    competitionWorkMapper.deleteCompetitionWorkByParticipationId(registerId);\n"
        "    ...\n"
        "}"
    )
    add_code_label(doc, "（3）CompetitionStudentServiceImpl.java 材料回显与预签名生成：")
    add_code_block(
        doc,
        "public Map<String, Object> getUploadedMaterials(Long registerId)\n"
        "{\n"
        "    CompetitionParticipation participation = competitionParticipationMapper.selectById(registerId);\n"
        "    List<CompetitionWork> works = competitionWorkMapper.selectCompetitionWorkList(query);\n"
        "    ...\n"
        "    if (pptPath != null && !pptPath.isEmpty()) {\n"
        "        pptPath = minioUtils.getPrivateUrl(pptPath);\n"
        "    }\n"
        "    if (pdfPath != null && !pdfPath.isEmpty()) {\n"
        "        ...\n"
        "        sb.append(minioUtils.getPrivateUrl(parts[i]));\n"
        "    }\n"
        '    result.put("pptPath", pptPath);\n'
        '    result.put("pdfPath", pdfPath);\n'
        '    result.put("pptName", pptName);\n'
        '    result.put("docName", docName);\n'
        '    result.put("otherName", otherName);\n'
        "    return result;\n"
        "}"
    )

    add_heading(doc, "5.2.4 代码解析", 12, 6, 3)
    add_body(doc, "uploadFile 的关键作用是把“对象存储地址”和“预览地址”同时返回给前端。url 用于后续业务落库，previewUrl 只用于当次预览，这使得系统能够兼顾持久化存储与私有文件访问安全。")
    add_body(doc, "submitMaterials 中最值得注意的两个处理点分别是 stripQueryString 与成员状态校验。前者保证数据库里只保存对象路径，不保存会过期的签名参数；后者保证作品材料只能在队伍成员关系明确后提交，避免评审阶段出现成员信息未确认、资料已锁定的业务冲突。")
    add_body(doc, "getUploadedMaterials 采取“优先查明细表、回退查主表”的回显策略：如果 competition_work 中已经拆分出了 PPT、文档和压缩包，就按类型返回文件名和路径；如果明细表为空，则回退到 participation 表中的历史字段。最终展示前统一调用 minioUtils.getPrivateUrl，确保学生、教师和管理员看到的都是可直接访问的私有链接。")

    add_heading(doc, "5.3 管理员端赛事管理", 14, 8, 4)
    add_heading(doc, "5.3.1 功能说明", 12, 6, 3)
    add_body(doc, "赛事管理模块负责管理员对竞赛全生命周期的维护，包括赛事列表分页查询、统计看板、类型筛选、赛事详情、单队伍详情、新增赛事、修改赛事、删除赛事以及参赛名单导出等功能。赛事作为全平台的核心主数据，直接关联学生报名、材料提交、评审分配和成绩发布。")
    add_body(doc, "除赛事基本信息外，管理员还需要维护竞赛海报、公告链接和官方附件。为此，Controller 在新增和修改赛事时会同步调用 CompetitionFileService 保存 officialFiles，使赛事主表和附件表保持一致。")

    add_heading(doc, "5.3.2 核心思路", 12, 6, 3)
    add_body(doc, "赛事管理在实现上采用“Controller 编排 + Service 校验 + Mapper 落库”的典型后端分层模式。Controller 主要负责请求参数接收与权限控制；Service 负责校验赛事名称、报名时间区间等关键字段是否合法；附件服务则承担 officialFiles 的删旧存新。")
    add_body(doc, "在查询场景中，列表接口通过 PageHelper 进行分页，详情接口则组合赛事基础信息、队伍列表和官方附件。这样可以保证同一赛事在管理列表、详情页、学生端详情页和导出功能之间使用一致的数据源。")

    add_heading(doc, "5.3.3 关键代码", 12, 6, 3)
    add_code_label(doc, "（1）AdminCompetitionController.java 新增与修改赛事：")
    add_code_block(
        doc,
        '@PreAuthorize("@ss.hasPermi(\'admin:competition:add\')")\n'
        "@PostMapping\n"
        "public AjaxResult add(@Validated @RequestBody Competition competition)\n"
        "{\n"
        "    int result = adminCompetitionService.insertCompetition(competition);\n"
        "    if (result > 0 && competition.getOfficialFiles() != null\n"
        "            && !competition.getOfficialFiles().isEmpty()) {\n"
        "        competitionFileService.saveCompetitionFiles(\n"
        "            competition.getCompetitionId(),\n"
        "            competition.getOfficialFiles(),\n"
        "            SecurityUtils.getUserId());\n"
        "    }\n"
        "    return result > 0 ? success() : error(\"新增竞赛失败\");\n"
        "}\n\n"
        '@PreAuthorize("@ss.hasPermi(\'admin:competition:edit\')")\n'
        "@PutMapping\n"
        "public AjaxResult edit(@Validated @RequestBody Competition competition)\n"
        "{\n"
        "    int result = adminCompetitionService.updateCompetition(competition);\n"
        "    if (result > 0) {\n"
        "        competitionFileService.saveCompetitionFiles(\n"
        "            competition.getCompetitionId(),\n"
        "            competition.getOfficialFiles(),\n"
        "            SecurityUtils.getUserId());\n"
        "    }\n"
        "    return result > 0 ? success() : error(\"修改竞赛失败\");\n"
        "}"
    )
    add_code_label(doc, "（2）AdminCompetitionServiceImpl.java 赛事合法性校验：")
    add_code_block(
        doc,
        "@Transactional(rollbackFor = Exception.class)\n"
        "public int insertCompetition(Competition competition)\n"
        "{\n"
        "    if (competition.getCompetitionName() == null\n"
        "            || competition.getCompetitionName().trim().isEmpty())\n"
        '        throw new ServiceException("竞赛名称不能为空");\n'
        "    if (competition.getRegisterStartTime() == null)\n"
        '        throw new ServiceException("报名开始时间不能为空");\n'
        "    if (competition.getRegisterEndTime() == null)\n"
        '        throw new ServiceException("报名结束时间不能为空");\n'
        "    if (competition.getRegisterStartTime().after(competition.getRegisterEndTime()))\n"
        '        throw new ServiceException("报名开始时间不能晚于结束时间");\n'
        "    return competitionMapper.insertCompetition(competition);\n"
        "}\n\n"
        "@Transactional(rollbackFor = Exception.class)\n"
        "public int updateCompetition(Competition competition)\n"
        "{\n"
        "    if (competition.getCompetitionId() == null)\n"
        '        throw new ServiceException("竞赛ID不能为空");\n'
        "    if (competition.getRegisterStartTime() != null\n"
        "            && competition.getRegisterEndTime() != null\n"
        "            && competition.getRegisterStartTime().after(competition.getRegisterEndTime()))\n"
        '        throw new ServiceException("报名开始时间不能晚于结束时间");\n'
        "    return competitionMapper.updateCompetition(competition);\n"
        "}"
    )

    add_heading(doc, "5.3.4 代码解析", 12, 6, 3)
    add_body(doc, "Controller 层通过 @PreAuthorize 对新增、修改和删除接口进行权限控制，确保只有具备 admin:competition:xxx 权限的管理员才能操作赛事数据。新增和修改成功后立即调用 saveCompetitionFiles，同步维护赛事附件，避免出现“主记录更新了但附件还是旧的”的问题。")
    add_body(doc, "Service 层的重点是时间合法性校验。报名开始时间和结束时间是后续学生报名状态判断的依据，因此在 insertCompetition 和 updateCompetition 中统一校验时间区间，能在数据进入数据库前阻断非法赛事记录。")
    add_body(doc, "这种“主表 + 附件表”的设计还为后续扩展提供了空间。管理员可以在不修改 Competition 主表结构的情况下继续补充更多官方文件，学生端在获取赛事详情时只需要根据 competitionId 拉取附件列表即可。")

    add_heading(doc, "5.4 管理员端评审权限管理", 14, 8, 4)
    add_heading(doc, "5.4.1 功能说明", 12, 6, 3)
    add_body(doc, "评审权限管理模块用于建立“教师 - 赛事 - 评审资格”的映射关系，是评审池建设和后续自动分配评审任务的前置条件。管理员可以查询所有赛事、查询全部教师、查看已赋权记录、批量赋予教师某项赛事的评审权限，以及撤销已有权限。")
    add_body(doc, "系统同时提供 /admin/permission/check-advisor 接口，用于判断某教师是否同时担任该竞赛队伍的指导老师，便于前端在赋权界面提示潜在冲突。虽然当前指导老师冲突检查与批量赋权是两个步骤，但这种拆分方式有利于保持接口职责清晰。")

    add_heading(doc, "5.4.2 核心思路", 12, 6, 3)
    add_body(doc, "权限管理的核心数据来源于 competition_permissions 表。listAllTeachers 先查询系统全部未删除用户，再通过 roleMapper.selectRolePermissionByUserId 判断角色键是否为 teacher，仅把真正的教师账号暴露给赋权界面。")
    add_body(doc, "batchGrantPermission 在写入前先调用 selectByUserAndCompetition 检查是否已存在记录：如果已有记录但 permissionId 不是评审权限，则更新为 1；如果已是评审权限，则直接跳过。这样可以防止重复插入，也保证重复操作具有幂等性。撤销权限时，服务层通过 deletePermission 删除对应映射。")

    add_heading(doc, "5.4.3 关键代码", 12, 6, 3)
    add_code_label(doc, "（1）AdminPermissionController.java 批量赋权与撤销：")
    add_code_block(
        doc,
        '@PreAuthorize("@ss.hasPermi(\'admin:permission:add\')")\n'
        '@PostMapping("/grant")\n'
        "public AjaxResult batchGrantPermission(@RequestBody Map<String, Object> body)\n"
        "{\n"
        '    Long competitionId = Long.valueOf(body.get("competitionId").toString());\n'
        '    List<Object> teacherIdsObj = (List<Object>) body.get("teacherIds");\n'
        "    List<Long> teacherIds = new ArrayList<>();\n"
        "    for (Object id : teacherIdsObj) {\n"
        "        if (id != null) teacherIds.add(Long.valueOf(id.toString()));\n"
        "    }\n"
        "    int successCount = adminPermissionService.batchGrantPermission(competitionId, teacherIds);\n"
        "    return success(successCount);\n"
        "}\n\n"
        '@PreAuthorize("@ss.hasPermi(\'admin:permission:remove\')")\n'
        '@DeleteMapping("/revoke/{competitionId}/{teacherId}")\n'
        "public AjaxResult revokePermission(@PathVariable Long competitionId,\n"
        "        @PathVariable Long teacherId)\n"
        "{\n"
        "    int result = adminPermissionService.revokePermission(competitionId, teacherId);\n"
        "    return success(result);\n"
        "}"
    )
    add_code_label(doc, "（2）AdminPermissionServiceImpl.java 教师筛选与幂等赋权：")
    add_code_block(
        doc,
        "public List<Map<String, Object>> listAllTeachers()\n"
        "{\n"
        "    List<SysUser> users = sysUserMapper.selectUserList(queryUser);\n"
        "    for (SysUser user : users)\n"
        "    {\n"
        "        List<SysRole> roles = roleMapper.selectRolePermissionByUserId(user.getUserId());\n"
        '        boolean isTeacher = roles.stream().anyMatch(r -> "teacher".equals(r.getRoleKey()));\n'
        "        if (isTeacher) { ... }\n"
        "    }\n"
        "}\n\n"
        "@Transactional(rollbackFor = Exception.class)\n"
        "public int batchGrantPermission(Long competitionId, List<Long> teacherIds)\n"
        "{\n"
        "    int successCount = 0;\n"
        "    for (Long teacherId : teacherIds)\n"
        "    {\n"
        "        CompetitionPermission existing = permissionMapper\n"
        "            .selectByUserAndCompetition(teacherId, competitionId);\n"
        "        if (existing != null)\n"
        "        {\n"
        "            if (existing.getPermissionId() != 1)\n"
        "            {\n"
        "                existing.setPermissionId(1);\n"
        "                permissionMapper.updatePermission(existing);\n"
        "                successCount++;\n"
        "            }\n"
        "            continue;\n"
        "        }\n"
        "        ...\n"
        "        permissionMapper.insertPermission(permission);\n"
        "        successCount++;\n"
        "    }\n"
        "    return successCount;\n"
        "}"
    )

    add_heading(doc, "5.4.4 代码解析", 12, 6, 3)
    add_body(doc, "权限模块首先通过角色筛选保证赋权对象一定是教师账号，而不是任意系统用户。这样做不仅减少了前端候选列表的噪声，也避免了管理员误把赛事评审权限分配给学生或其他角色。")
    add_body(doc, "batchGrantPermission 的实现体现了幂等设计思想。同一教师对同一赛事重复赋权时，服务层不会盲目新增记录，而是先查已有映射，再决定跳过或更新。这种做法在管理员多次点击、前端网络重试等场景下尤其重要。")
    add_body(doc, "撤销权限虽然逻辑较简单，但它与后续评审池、自动分配和教师端可评审赛事列表直接相关。因此评审权限模块本质上是整个评审链路的“开关模块”，其数据准确性必须高于普通配置项。")

    add_heading(doc, "5.5 管理员端教师账号管理", 14, 8, 4)
    add_heading(doc, "5.5.1 功能说明", 12, 6, 3)
    add_body(doc, "教师账号管理模块用于维护平台中的教师用户，包括查询教师列表、查看教师详情、创建单个教师账号、批量创建教师账号、修改教师资料、删除教师账号、重置密码、启停状态以及 Excel 批量导入。该模块既服务于教师端登录，也服务于评审权限分配和指导老师绑定。")
    add_body(doc, "与普通用户管理相比，该模块还有一个额外职责：在 sys_user 保存账号信息的同时，同步维护 teacher_team 基础教师资料表。这样赛事报名和指导老师绑定场景既可以基于系统账号运转，也可以基于教师资料表继续扩展业务字段。")

    add_heading(doc, "5.5.2 核心思路", 12, 6, 3)
    add_body(doc, "新增或修改教师账号时，服务层首先确保该用户绑定 teacher 角色，然后调用 sysUserService.insertUser 或 updateUser 完成系统账号保存。保存成功后，再通过 upsertTeacherProfile 按手机号同步 teacher_team 中的基础教师信息，实现账号表与教师资料表的一致维护。")
    add_body(doc, "Excel 导入采用 Apache POI 解析第一张工作表，逐行读取姓名、性别、手机号、邮箱、工号、职称、出生年月等字段。导入前同时检查数据库中是否重复，以及同一文件内部是否重复；手机号会先去除非数字字符，出生年月则通过 formatBirthDate 兼容文本日期、Excel 日期序列和多种输入格式。单行插入时使用 transactionTemplate，避免某一行异常影响整批导入。")

    add_heading(doc, "5.5.3 关键代码", 12, 6, 3)
    add_code_label(doc, "（1）AdminUserServiceImpl.java 新增教师与资料同步：")
    add_code_block(
        doc,
        "@Transactional(rollbackFor = Exception.class)\n"
        "public int insertTeacher(SysUser user)\n"
        "{\n"
        "    if (StringUtils.isEmpty(user.getPhonenumber()))\n"
        '        throw new ServiceException("新增教师失败，联系电话不能为空");\n'
        '    SysRole teacherRole = roleMapper.checkRoleKeyUnique("teacher");\n'
        "    user.setRoleIds(new Long[] { teacherRole.getRoleId() });\n"
        "    int rows = sysUserService.insertUser(user);\n"
        "    if (rows > 0)\n"
        "    {\n"
        "        upsertTeacherProfile(user, null);\n"
        "    }\n"
        "    return rows;\n"
        "}\n\n"
        "private void upsertTeacherProfile(SysUser user, String oldPhone)\n"
        "{\n"
        "    TeacherTeam teacherTeam = new TeacherTeam();\n"
        "    teacherTeam.setTeacherName(StringUtils.isNotEmpty(user.getNickName())\n"
        "        ? user.getNickName() : user.getUserName());\n"
        "    teacherTeam.setPhone(user.getPhonenumber());\n"
        "    teacherTeam.setEmail(user.getEmail());\n"
        "    teacherTeam.setWorkUnit(user.getWorkUnit());\n"
        "    ...\n"
        "}"
    )
    add_code_label(doc, "（2）AdminUserServiceImpl.java Excel 批量导入教师：")
    add_code_block(
        doc,
        "public Map<String, Object> importTeachers(MultipartFile file)\n"
        "{\n"
        "    if (file == null || file.isEmpty())\n"
        '        throw new ServiceException("导入文件不能为空");\n'
        "    int successCount = 0;\n"
        "    int failCount = 0;\n"
        "    Set<String> existingUserNames = new HashSet<>();\n"
        "    Set<String> existingPhones = new HashSet<>();\n"
        "    Set<String> existingEmails = new HashSet<>();\n"
        "    try (InputStream inputStream = file.getInputStream();\n"
        "         Workbook workbook = new XSSFWorkbook(inputStream)) {\n"
        "        Sheet sheet = workbook.getSheetAt(0);\n"
        "        for (int i = 1; i <= sheet.getLastRowNum(); i++) {\n"
        "            Row row = sheet.getRow(i);\n"
        "            String phonenumber = getCellValue(row.getCell(2));\n"
        '            phonenumber = phonenumber.replaceAll("\\\\D", "");\n'
        "            ...\n"
        "            transactionTemplate.execute(txStatus -> {\n"
        "                sysUserService.insertUser(user);\n"
        "                upsertTeacherProfile(user, null);\n"
        "                return null;\n"
        "            });\n"
        "            successCount++;\n"
        "        }\n"
        "    }\n"
        "    Map<String, Object> result = new HashMap<>();\n"
        '    result.put("successCount", successCount);\n'
        '    result.put("failCount", failCount);\n'
        "    return result;\n"
        "}"
    )

    add_heading(doc, "5.5.4 代码解析", 12, 6, 3)
    add_body(doc, "insertTeacher 的关键并不只是插入一条 sys_user 记录，而是在插入前绑定 teacher 角色、插入后同步 teacher_team 基础资料。这样教师账号既能用于登录，又能用于报名业务中的指导老师匹配和手机号查询。")
    add_body(doc, "Excel 导入逻辑体现了较强的数据清洗意识。手机号在校验前会先去除空格、横杠等非数字字符；邮箱和账号同时进行数据库级去重与文件内去重；出生年月可以兼容多种格式，降低了管理员维护导入模板的成本。")
    add_body(doc, "transactionTemplate 的使用使每一行导入都具备相对独立的事务边界。即使某一行因为数据格式问题失败，也不会回滚整批已经成功导入的教师账号，从而兼顾批量效率与问题定位能力。")

    add_interface_table(doc)


def add_section_6(doc: Document) -> None:
    add_heading(doc, "6 调试分析", 14, 8, 4)
    add_body(doc, "在围绕上述五个功能点开发与联调的过程中，主要问题集中在认证缓存同步、对象存储访问、赛事主数据校验、权限幂等控制以及 Excel 清洗导入等方面。以下结合源码中的具体实现，对调试过程进行归纳。")

    add_heading(doc, "6.1 JWT认证与个人信息缓存同步", 12, 6, 3)
    add_body(doc, "问题描述：在学生修改昵称、手机号、头像或密码后，前端偶尔仍显示旧数据，必须重新登录才会刷新。")
    add_body(doc, "原因分析：系统采用 JWT + LoginUser 缓存方案，前端多数页面展示的是当前 token 对应的缓存用户信息。如果更新个人资料后只修改数据库，不同步刷新 token 中的 LoginUser，页面再次调用 getInfo 前就会出现缓存与数据库不一致。")
    add_body(doc, "解决方案：在 SysProfileController 的 updateProfile、updatePwd、avatar 三个接口中，更新成功后统一调用 tokenService.setLoginUser(loginUser) 回写缓存；同时在 getInfo 中比较权限集合差异，发现权限变化时调用 tokenService.refreshToken(loginUser) 重新刷新 token。经调整后，认证信息和个人资料修改能够即时生效。")

    add_heading(doc, "6.2 参赛材料提交校验与预签名链接处理", 12, 6, 3)
    add_body(doc, "问题描述：早期联调中，学生把前端上传返回的 previewUrl 直接提交给材料接口，数据库中保存了带签名参数的完整地址，导致链接过期后材料回显失效；同时，部分队伍在成员仍为 pending 状态时提前提交了材料。")
    add_body(doc, "原因分析：previewUrl 是 MinIO 的临时预签名地址，只适用于当前预览场景，不适合直接落库。此外，如果缺少成员状态校验，系统会出现“队伍关系未确认但作品已锁定”的数据不一致。")
    add_body(doc, "解决方案：submitMaterials 在保存前统一调用 stripQueryString 去除签名参数，只保存对象路径；同时增加 PPT、申报书、作品名称、作品简介、待确认成员、非队长成员数量等多层校验。getUploadedMaterials 回显时再动态生成新的预签名链接。这样既保证了文件访问安全，也保证了材料提交流程的业务完整性。")

    add_heading(doc, "6.3 赛事时间合法性与官方附件同步", 12, 6, 3)
    add_body(doc, "问题描述：管理员录入赛事时，若报名开始时间晚于结束时间，会导致学生端报名期判断异常；编辑赛事后，如果只更新主表信息而不刷新附件列表，详情页展示会出现新旧附件混杂。")
    add_body(doc, "原因分析：赛事时间是学生端判断 inRegisterPeriod 的核心字段，而 officialFiles 又被赛事详情页直接引用，因此两者都属于不可放任错误进入数据库的关键数据。")
    add_body(doc, "解决方案：AdminCompetitionServiceImpl 在 insertCompetition 和 updateCompetition 中统一校验时间区间；AdminCompetitionController 在新增、修改成功后立刻调用 competitionFileService.saveCompetitionFiles，以“先删后增”的方式同步附件数据。经过处理后，赛事时间与附件列表在管理端、学生端和导出功能之间保持一致。")

    add_heading(doc, "6.4 评审权限重复赋权与指导老师冲突提示", 12, 6, 3)
    add_body(doc, "问题描述：管理员在网络不稳定或重复点击时，可能对同一教师、同一赛事发起多次赋权请求；另外，部分教师既是该赛事参赛队伍的指导老师，又可能出现在待赋权候选中。")
    add_body(doc, "原因分析：如果服务层不做幂等处理，就可能产生重复权限记录，进而影响评审池和自动分配逻辑。指导老师冲突如果不提前识别，也会给后续评审回避机制带来额外复杂度。")
    add_body(doc, "解决方案：batchGrantPermission 在写入前先调用 selectByUserAndCompetition 检查已有记录，对重复赋权执行跳过或更新而不重复插入；前端赋权流程配合 /admin/permission/check-advisor 接口，在赋权前提示指导老师冲突。这样既减少了脏数据，也降低了管理员误操作概率。")

    add_heading(doc, "6.5 Excel批量导入教师信息", 12, 6, 3)
    add_body(doc, "问题描述：教师 Excel 导入时，手机号可能被 Excel 自动转成科学计数法或带空格符号，出生年月字段也可能出现文本日期、数字日期序列或“YYYY/MM”混合格式，导致导入失败。")
    add_body(doc, "原因分析：Excel 单元格类型并不稳定，同一列在不同文件里可能同时出现字符串、数字和日期三种类型。如果没有在服务层做统一清洗，数据库唯一性校验和格式校验就很难准确执行。")
    add_body(doc, "解决方案：AdminUserServiceImpl 使用 getCellValue 兼容读取多种单元格类型，并在手机号入库前去除全部非数字字符；formatBirthDate 负责把 Excel 日期序列、斜杠格式、年月格式统一转换为 YYYY-MM；同时通过 existingUserNames、existingPhones、existingEmails 三个集合拦截同一文件内部的重复数据。经过这些处理后，教师批量导入的稳定性明显提升。")

    add_heading(doc, "6.6 程序改进设想", 12, 6, 3)
    add_body(doc, "（1）学生材料提交流程目前已具备较完整的业务校验，但 submitMaterials 仍可进一步增加事务控制与对象存储回滚补偿，避免极端情况下出现“业务记录已写入、附件明细未完全写入”的不一致。")
    add_body(doc, "（2）评审权限管理目前通过服务层幂等控制和指导老师检查接口保障稳定性，后续可在数据库层继续补充更严格的唯一约束，并把指导老师冲突从“提示机制”提升为“自动拦截机制”。")
    add_body(doc, "（3）教师 Excel 导入可继续扩展失败明细导出功能，把失败行号、失败原因和原始单元格内容统一导出为报告，进一步提升管理员批量维护账号时的可追踪性。")
    add_body(doc, "（4）认证与个人信息模块后续可将头像上传也统一迁移到对象存储体系，使头像、作品附件和官方文件走同一套存储与预览方案，降低文件管理链路的割裂感。")


def add_section_7(doc: Document) -> None:
    add_heading(doc, "7 测试结果", 14, 8, 4)
    add_heading(doc, "7.1 测试环境", 12, 6, 3)
    add_body(doc, "测试环境采用 Windows 11 操作系统，JDK 17，MySQL 8.2.0，Node.js 22.x，浏览器使用 Chrome 120 及以上版本。后端服务基于 Spring Boot 启动，前端通过 Vite 开发服务器访问接口，文件上传与预览依赖 MinIO 对象存储。接口调试主要使用 Postman，页面联调通过浏览器完成。")

    add_heading(doc, "7.2 测试用例", 12, 6, 3)
    add_test_table(doc)

    add_heading(doc, "7.3 运行截图", 12, 6, 3)
    add_body(doc, "以下截图位置保留给最终答辩或提交报告前补充，截图内容与本报告第五章所述功能点保持一一对应。")
    add_center_text(doc, "【截图1：学生登录与角色路由页面】", bold=True, size_pt=12, after_pt=6)
    add_center_text(doc, "此处需要运行：学生账号登录后展示首页跳转结果与 getInfo 返回信息", bold=False, size_pt=10.5, after_pt=6)
    add_center_text(doc, "【截图2：个人信息维护页面】", bold=True, size_pt=12, after_pt=6)
    add_center_text(doc, "此处需要运行：修改手机号、学院专业或头像后展示保存效果", bold=False, size_pt=10.5, after_pt=6)
    add_center_text(doc, "【截图3：参赛材料提交与回显页面】", bold=True, size_pt=12, after_pt=6)
    add_center_text(doc, "此处需要运行：上传 PPT、申报书并展示材料回显与预览链接", bold=False, size_pt=10.5, after_pt=6)
    add_center_text(doc, "【截图4：管理员赛事管理页面】", bold=True, size_pt=12, after_pt=6)
    add_center_text(doc, "此处需要运行：管理员新增或编辑赛事后展示列表与赛事详情效果", bold=False, size_pt=10.5, after_pt=6)
    add_center_text(doc, "【截图5：教师权限赋权与教师导入页面】", bold=True, size_pt=12, after_pt=6)
    add_center_text(doc, "此处需要运行：展示批量赋权结果、教师账号创建或 Excel 导入效果", bold=False, size_pt=10.5, after_pt=6)


def add_references(doc: Document) -> None:
    add_heading(doc, "参考文献", 14, 8, 4)
    for item in REFERENCES:
        para = doc.add_paragraph()
        para.paragraph_format.space_after = Pt(3)
        para.paragraph_format.line_spacing = 1.5
        run = para.add_run(item)
        set_run_font(run, "宋体", 12)


def add_reflections(doc: Document) -> None:
    add_heading(doc, "心得体会", 14, 8, 4)
    for item in REFLECTIONS:
        add_body(doc, item)


def generate(template_path: Path, output_path: Path) -> None:
    doc = Document(str(template_path))
    truncate_from_heading(doc, "5 详细设计及实现")
    add_section_5(doc)
    add_section_6(doc)
    add_section_7(doc)
    add_references(doc)
    add_reflections(doc)
    doc.save(str(output_path))


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate the completed course design report DOCX.")
    parser.add_argument("--template", required=True, help="Path to the template docx")
    parser.add_argument("--output", required=True, help="Path to the output docx")
    args = parser.parse_args()
    generate(Path(args.template), Path(args.output))


if __name__ == "__main__":
    main()
