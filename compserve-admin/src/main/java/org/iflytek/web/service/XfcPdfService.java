package org.iflytek.web.service;

import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.ParagraphAlignment;
import com.aspose.words.SaveFormat;
import com.aspose.words.Table;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.iflytek.common.exception.ServiceException;
import org.iflytek.system.mapper.XfcPdfFileMapper;
import org.iflytek.system.mapper.XfcRegistrationMapper;
import org.iflytek.system.service.XfcRegistrationDraftService;

/** 使用 Word 原始模板填充并导出讯飞杯可打印报名表。 */
@Service
public class XfcPdfService
{
    private static final Logger log = LoggerFactory.getLogger(XfcPdfService.class);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MEMBER_FIRST_ROW = 10;
    private static final int MEMBER_ROW_CAPACITY = 6;
    private static final int ADVISOR_FIRST_ROW = 19;
    private static final int ADVISOR_ROW_CAPACITY = 2;
    private static final int MATERIAL_FIRST_ROW = 23;
    /** 与报名表模板正文保持一致的填写样式；避免宋体小字号与黑体表格产生割裂感。 */
    private static final String VALUE_FONT = "SimHei";
    private static final double VALUE_FONT_SIZE = 9.0;

    @Autowired private XfcRegistrationDraftService draftService;
    @Autowired private XfcRegistrationMapper registrationMapper;
    @Autowired private XfcPdfFileMapper pdfFileMapper;
    @Value("${xfc.pdf.storagePath:./uploadPath/xfc-pdf}") private String storagePath;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generate(Long editorUserId)
    {
        Map<String, Object> data = draftService.current(editorUserId);
        draftService.validateForPdf(data);
        Map<String, Object> registration = map(data.get("registration"));
        Long registrationId = number(registration.get("id"));
        if (registrationId == null) throw new ServiceException("请先保存报名表信息后再生成 PDF");

        String registrationNo = text(registration.get("registrationNo"));
        if (registrationNo.isEmpty())
        {
            registrationNo = String.format("XFC2026-%05d", registrationId);
            registrationMapper.updateRegistrationNo(registrationId, registrationNo);
            registration.put("registrationNo", registrationNo);
        }
        int version = pdfFileMapper.selectNextVersion(registrationId);
        Path output = createPdf(data, registrationNo, version);
        Map<String, Object> file = new HashMap<>();
        file.put("registrationId", registrationId);
        file.put("version", version);
        file.put("filePath", output.toAbsolutePath().toString());
        pdfFileMapper.insertPdfFile(file);
        file.put("downloadUrl", "/api/xfc/registrations/" + file.get("id") + "/pdf");
        return file;
    }

    public Path resolveFile(Long fileId, Long editorUserId)
    {
        Map<String, Object> file = pdfFileMapper.selectPdfForCaptain(fileId, editorUserId);
        if (file == null) throw new ServiceException("报名表不存在或无权访问");
        Path path = Path.of(String.valueOf(file.get("filePath")));
        if (!Files.isRegularFile(path)) throw new ServiceException("报名表文件不存在，请重新生成");
        return path;
    }

    private Path createPdf(Map<String, Object> data, String registrationNo, int version)
    {
        try
        {
            Path directory = Path.of(storagePath).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            Path output = directory.resolve(registrationNo + "-v" + version + ".pdf");
            Path temporary = directory.resolve(registrationNo + "-v" + version + ".tmp.pdf");
            try (InputStream template = new ClassPathResource("xfc/registration-template.docx").getInputStream())
            {
                Document document = new Document(template);
                populateDocument(document, data, registrationNo, version);
                document.save(temporary.toString(), SaveFormat.PDF);
            }
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
            return output;
        }
        catch (Exception e)
        {
            // 前端保留统一、友好的错误提示；服务端必须记录原始异常，避免排版或文件问题被吞掉。
            log.error("报名表 PDF 生成失败: registrationNo={}, version={}", registrationNo, version, e);
            throw new ServiceException("报名表 PDF 生成失败，请稍后重试");
        }
    }

    private void populateDocument(Document document, Map<String, Object> data, String registrationNo, int version) throws Exception
    {
        formatCaptainStatement(document);
        Map<String, Object> registration = map(data.get("registration"));
        Map<String, Object> captain = map(data.get("captain"));
        List<Map<String, Object>> members = maps(data.get("members"));
        List<Map<String, Object>> advisors = maps(data.get("advisors"));
        List<Map<String, Object>> materials = maps(data.get("materials"));
        Table table = registrationTable(document);
        String now = LocalDateTime.now().format(TIME);

        setPaddedCell(document, table, 1, 1, registrationNo);
        setPaddedCell(document, table, 1, 3, "第三届");
        setPaddedCell(document, table, 1, 5, trackName(text(registration.get("trackCode"))));
        setPaddedCell(document, table, 2, 1, text(registration.get("teamName")));
        setPaddedCell(document, table, 2, 3, "河南工业大学");
        setPaddedCell(document, table, 2, 5, text(captain.get("college")));
        setPaddedCell(document, table, 3, 1, now);
        setPaddedCell(document, table, 3, 3, "2026-10-15");
        setPaddedCell(document, table, 3, 5, "已生成");
        setPaddedCell(document, table, 4, 1, text(registration.get("workTitle")));
        setPaddedCell(document, table, 5, 1, text(registration.get("workSummary")));
        setPaddedCell(document, table, 7, 1, text(captain.get("name")));
        setPaddedCell(document, table, 7, 3, text(captain.get("studentNo")));
        setPaddedCell(document, table, 7, 5, text(captain.get("college")));
        setPaddedCell(document, table, 8, 1, text(captain.get("major")));
        setPaddedCell(document, table, 8, 3, text(captain.get("className")));
        setPaddedCell(document, table, 8, 5, text(captain.get("phone")));

        List<Map<String, Object>> team = new ArrayList<>();
        team.add(captain);
        team.addAll(members);
        for (int i = 0; i < team.size(); i++) fillTeamRow(document, table, MEMBER_FIRST_ROW + i, i + 1, team.get(i));

        int removedRows = MEMBER_ROW_CAPACITY - team.size();
        for (int row = MEMBER_FIRST_ROW + MEMBER_ROW_CAPACITY - 1; row >= MEMBER_FIRST_ROW + team.size(); row--)
        {
            table.getRows().get(row).remove();
        }
        int advisorFirstRow = ADVISOR_FIRST_ROW - removedRows;
        setCell(document, table, advisorFirstRow - 3, 0,
                "人员校验 √ 学号唯一  √ 未发现队长与队员角色冲突  √ 队伍人数符合要求",
                8, ParagraphAlignment.LEFT, true);
        for (int i = 0; i < advisors.size(); i++)
        {
            fillAdvisorRow(document, table, advisorFirstRow + i, String.valueOf(i + 1), advisors.get(i));
        }
        int removedAdvisorRows = ADVISOR_ROW_CAPACITY - advisors.size();
        for (int row = advisorFirstRow + ADVISOR_ROW_CAPACITY - 1; row >= advisorFirstRow + advisors.size(); row--)
        {
            table.getRows().get(row).remove();
        }

        int materialFirstRow = MATERIAL_FIRST_ROW - removedRows - removedAdvisorRows;
        fillMaterialRow(document, table, materialFirstRow, 1, "申报书", "必交", material(materials, "APPLICATION"));
        fillMaterialRow(document, table, materialFirstRow + 1, 2, "PPT 演示资料", "必交", material(materials, "PRESENTATION"));
        fillMaterialRow(document, table, materialFirstRow + 2, 3, "其他补充资料", "选交", material(materials, "SUPPLEMENT"));

        int footerRow = 28 - removedRows - removedAdvisorRows;
        setCell(document, table, footerRow, 0,
                "生成时间【" + now + "】  报名表版本【v" + version + "】  校验码【" + registrationNo + "】  状态【有效】",
                8, ParagraphAlignment.LEFT, true);
        document.updatePageLayout();
        if (document.getPageCount() != 1) throw new ServiceException("报名信息过长，无法生成单页报名表");
    }

    private Table registrationTable(Document document)
    {
        NodeCollection tables = document.getChildNodes(NodeType.TABLE, true);
        if (tables.getCount() != 1) throw new ServiceException("报名表 Word 模板结构不正确");
        return (Table) tables.get(0);
    }

    /** 统一模板中队长声明区域的静态文字，保持与报名表正文一致的黑体加粗样式。 */
    private void formatCaptainStatement(Document document) throws Exception
    {
        NodeCollection paragraphs = document.getChildNodes(NodeType.PARAGRAPH, true);
        for (int i = 0; i < paragraphs.getCount(); i++)
        {
            com.aspose.words.Paragraph paragraph = (com.aspose.words.Paragraph) paragraphs.get(i);
            if (!isCaptainStatement(paragraph.getText())) continue;
            // 模板预设的右缩进会把较长的声明拆成两行，声明区域应完整显示在同一行。
            paragraph.getParagraphFormat().setRightIndent(0);
            NodeCollection runs = paragraph.getChildNodes(NodeType.RUN, true);
            for (int j = 0; j < runs.getCount(); j++)
            {
                com.aspose.words.Run run = (com.aspose.words.Run) runs.get(j);
                run.getFont().setName(VALUE_FONT);
                run.getFont().setNameFarEast("黑体");
                run.getFont().setSize(8);
                run.getFont().setBold(true);
                run.getFont().setItalic(false);
            }
        }

        NodeCollection shapes = document.getChildNodes(NodeType.SHAPE, true);
        for (int i = 0; i < shapes.getCount(); i++)
        {
            com.aspose.words.Shape shape = (com.aspose.words.Shape) shapes.get(i);
            if (shape.getText().contains("日期："))
            {
                // 日期原本位于过窄的文本框中；扩宽后可保持“日期：____年__月__日”完整单行显示。
                shape.setWidth(180);
                shape.setHeight(16);
            }
        }
    }

    private boolean isCaptainStatement(String value)
    {
        return value.contains("队长声明")
                || value.contains("报名信息真实且队员知悉参赛")
                || value.contains("同一人员未加入本届其他有效队伍")
                || value.contains("作品及材料不侵犯第三方权益")
                || value.contains("队长签名")
                || value.contains("审核结果")
                || value.contains("日期：");
    }

    private void fillTeamRow(Document document, Table table, int row, int index, Map<String, Object> person) throws Exception
    {
        setCell(document, table, row, 0, String.valueOf(index), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 1, text(person.get("name")), 8.5, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 2, text(person.get("studentNo")), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 3, text(person.get("college")), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 4, text(person.get("major")), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 5, text(person.get("className")), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 6, text(person.get("phone")), 8, ParagraphAlignment.CENTER, false);
    }

    private void fillAdvisorRow(Document document, Table table, int row, String index, Map<String, Object> advisor) throws Exception
    {
        setCell(document, table, row, 0, index, 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 1, text(advisor.get("name")), 8.5, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 2, text(advisor.get("organization")), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 3, text(advisor.get("title")), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 4, text(advisor.get("phone")), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 5, text(advisor.get("email")), 7.5, ParagraphAlignment.CENTER, false);
    }

    /** 填充模板中固定的三行参赛资料；附件正文保留在对象存储，此处只写可打印的元数据。 */
    private void fillMaterialRow(Document document, Table table, int row, int index, String category,
            String requirement, Map<String, Object> material) throws Exception
    {
        boolean uploaded = !material.isEmpty();
        setCell(document, table, row, 0, String.valueOf(index), 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 1, category, 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 2, uploaded ? displayFilename(text(material.get("originalFilename"))) : "—", 7.5, ParagraphAlignment.CENTER, true);
        setCell(document, table, row, 3, uploaded ? formatFileSize(material.get("fileSize")) : "—", 8, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 4, uploaded ? formatUploadTime(material.get("uploadedAt")) : "—", 7.5, ParagraphAlignment.CENTER, false);
        setCell(document, table, row, 5, uploaded ? "已上传（" + requirement + "）" : requirement, 7.5, ParagraphAlignment.CENTER, false);
    }

    /** 清除目标单元格旧内容后，以模板原有段落格式写入文本。 */
    private void setCell(Document document, Table table, int row, int cell, String value) throws Exception
    {
        setCell(document, table, row, cell, value, VALUE_FONT_SIZE, ParagraphAlignment.LEFT, true);
    }

    /** 基础信息使用可见的一个空格作为留白，不依赖单元格内边距。 */
    private void setPaddedCell(Document document, Table table, int row, int cell, String value) throws Exception
    {
        setCell(document, table, row, cell, displayValue(value));
    }

    private void setCell(Document document, Table table, int row, int cell, String value,
            double fontSize, int alignment, boolean wrapText) throws Exception
    {
        com.aspose.words.Cell target = table.getRows().get(row).getCells().get(cell);
        // 模板中的资料单元格带有段落缩进；清空并重新创建段落，避免左对齐后仍出现大块空白。
        target.removeAllChildren();
        target.ensureMinimum();
        target.getCellFormat().setWrapText(wrapText);
        target.getCellFormat().setLeftPadding(0);
        // 与模板中的字段标题保持同一水平线，避免填写内容贴在单元格上方。
        target.getCellFormat().setVerticalAlignment(com.aspose.words.CellVerticalAlignment.CENTER);
        DocumentBuilder builder = new DocumentBuilder(document);
        builder.moveTo(target.getFirstParagraph());
        builder.getParagraphFormat().setAlignment(alignment);
        builder.getParagraphFormat().setLeftIndent(0);
        builder.getParagraphFormat().setRightIndent(0);
        builder.getParagraphFormat().setFirstLineIndent(0);
        builder.getFont().setName(VALUE_FONT);
        builder.getFont().setNameFarEast("黑体");
        builder.getFont().setSize(fontSize);
        builder.getFont().setBold(true);
        builder.getFont().setItalic(false);
        builder.write(value);
    }

    private String trackName(String code)
    {
        return switch (code)
        {
            case "FOOD" -> "AI+粮食";
            case "INDUSTRY" -> "AI+工业";
            case "CITY" -> "AI+城市";
            case "EDUCATION" -> "AI+教育";
            case "MEDICAL" -> "AI+医疗";
            default -> "—";
        };
    }

    private Map<String, Object> material(List<Map<String, Object>> materials, String materialType)
    {
        for (Map<String, Object> material : materials)
        {
            if (materialType.equals(text(material.get("materialType")))) return material;
        }
        return new HashMap<>();
    }

    private String displayFilename(String filename)
    {
        return filename.length() <= 38 ? filename : filename.substring(0, 35) + "…";
    }

    /** PDF 展示专用的前导空格；不修改数据库中的原始字段值。 */
    private String displayValue(String value)
    {
        return value == null || value.isEmpty() ? "" : "\u00A0" + value;
    }

    private String formatFileSize(Object value)
    {
        if (!(value instanceof Number number)) return "—";
        long size = number.longValue();
        if (size < 1024L * 1024L) return Math.max(1L, Math.round(size / 1024.0)) + " KB";
        return String.format(java.util.Locale.ROOT, size >= 10L * 1024L * 1024L ? "%.0f MB" : "%.1f MB", size / 1024.0 / 1024.0);
    }

    private String formatUploadTime(Object value)
    {
        if (value instanceof LocalDateTime time) return time.format(TIME);
        if (value instanceof Date time) return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
        String result = text(value).replace('T', ' ');
        return result.length() > 16 ? result.substring(0, 16) : result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) { return value instanceof Map<?, ?> raw ? (Map<String, Object>) raw : new HashMap<>(); }
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> maps(Object value) { return value instanceof List<?> list ? (List<Map<String, Object>>) (List<?>) list : new ArrayList<>(); }
    private Long number(Object value) { return value instanceof Number number ? number.longValue() : null; }
    private String text(Object value) { return value == null ? "" : String.valueOf(value).replaceAll("[\\r\\n]+", " ").trim(); }
}
