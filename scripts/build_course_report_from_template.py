from __future__ import annotations

import argparse
from pathlib import Path
from copy import deepcopy

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.shared import Pt


COURSE_NAME = "大数据分布式集群实践"
STUDENT_CLASS = "大数据2406"
TITLE = "智启赛途——高校竞赛AI智驭平台"
GUIDE_TEACHER = "刘田园"
GROUP_MEMBER = "无"
COURSE_TIME = "2026.6.22-2026.7.3"

MAIN_CONTENT = (
    "1、项目定位：面向学生、教师、管理员三类角色的高校竞赛智能服务平台，"
    "重点完成赛事管理、评审权限分配、教师账号管理、学生认证与材料提交等核心后端能力。\n"
    "2、基本功能：基于 Spring Boot、MyBatis、Spring Security、JWT、MySQL 与 MinIO "
    "实现用户认证、赛事新增编辑、评审赋权、教师导入、文件上传与预签名访问、教师评审与成绩管理等功能。\n"
    "3、扩展内容：围绕缓存同步、权限幂等控制、Excel 数据清洗、页面性能优化等问题开展调试分析，"
    "形成完整课程设计报告并支撑期末实训答辩。"
)

TASK_REQUIREMENTS = (
    "1、需求分析：完成高校竞赛管理平台的需求调研与角色边界划分，明确学生端、教师端、管理员端的主要业务流程。\n"
    "2、系统实现：采用前后端分离架构，完成管理员端赛事管理、评审权限管理、教师账号管理，以及学生端认证与参赛材料等后端功能开发。\n"
    "3、基础支撑：完成 MySQL 核心业务表设计，建立参赛记录、评审分配、评分记录、权限记录等表间关系；集成 MinIO 实现文件上传与安全访问。\n"
    "4、调试与测试：围绕 JWT 认证、赛事时间校验、重复赋权、Excel 导入及页面性能问题开展调试分析，并完成测试与运行结果整理。"
)

REFERENCES = [
    "[1] Craig Walls. Spring in Action (Sixth Edition)[M]. Manning Publications, 2022.",
    "[2] 尤雨溪. Vue.js设计与实现[M]. 北京：人民邮电出版社, 2022.",
    "[3] 若依开源社区. RuoYi-Vue开发文档[EB/OL]. https://doc.ruoyi.vip/.",
    "[4] Spring官方. Spring Boot Reference Documentation[EB/OL]. https://docs.spring.io/spring-boot/.",
    "[5] Spring Security团队. Spring Security Reference[EB/OL]. https://docs.spring.io/spring-security/.",
    "[6] MinIO官方. MinIO Java SDK Documentation[EB/OL]. https://min.io/docs/minio/.",
]


def set_run_font(run, name: str = "宋体", size_pt: float = 12, bold: bool | None = None) -> None:
    if bold is not None:
        run.bold = bold
    run.font.name = name
    run.font.size = Pt(size_pt)
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.rFonts
    if rfonts is None:
        from docx.oxml import OxmlElement

        rfonts = OxmlElement("w:rFonts")
        rpr.append(rfonts)
    rfonts.set(qn("w:ascii"), name)
    rfonts.set(qn("w:hAnsi"), name)
    rfonts.set(qn("w:eastAsia"), name)


def replace_cell_text(cell, text: str, align=WD_ALIGN_PARAGRAPH.CENTER, font="宋体", size=12, bold=False):
    cell.text = ""
    para = cell.paragraphs[0]
    para.alignment = align
    run = para.add_run(text)
    set_run_font(run, font, size, bold=bold)


def copy_paragraph_format(src_para, dst_para):
    dst_para.style = src_para.style
    dst_pf = dst_para.paragraph_format
    src_pf = src_para.paragraph_format
    dst_pf.left_indent = src_pf.left_indent
    dst_pf.right_indent = src_pf.right_indent
    dst_pf.first_line_indent = src_pf.first_line_indent
    dst_pf.space_before = src_pf.space_before
    dst_pf.space_after = src_pf.space_after
    dst_pf.line_spacing = src_pf.line_spacing
    dst_pf.keep_together = src_pf.keep_together
    dst_pf.keep_with_next = src_pf.keep_with_next
    dst_pf.page_break_before = src_pf.page_break_before
    dst_pf.widow_control = src_pf.widow_control
    dst_para.alignment = src_para.alignment


def clone_paragraph_content(src_para, dst_para):
    copy_paragraph_format(src_para, dst_para)
    dst_para._element.clear_content()
    for run in src_para.runs:
        new_run = dst_para.add_run(run.text)
        if run.bold is not None:
            new_run.bold = run.bold
        if run.italic is not None:
            new_run.italic = run.italic
        if run.underline is not None:
            new_run.underline = run.underline
        if run.font.name:
            new_run.font.name = run.font.name
        if run.font.size:
            new_run.font.size = run.font.size
        set_run_font(
            new_run,
            run.font.name or "宋体",
            (run.font.size.pt if run.font.size else 12),
            bold=(run.bold if run.bold is not None else False),
        )


def find_paragraph_index(doc: Document, text: str) -> int:
    for idx, para in enumerate(doc.paragraphs):
        if para.text.strip() == text:
            return idx
    raise ValueError(f"Paragraph not found: {text}")


def update_cover_tables(doc: Document):
    t0 = doc.tables[0]
    replace_cell_text(t0.cell(0, 1), COURSE_NAME)
    replace_cell_text(t0.cell(1, 1), STUDENT_CLASS)
    # 姓名、学号保留模板中的可填写位，避免误填
    replace_cell_text(t0.cell(4, 1), GUIDE_TEACHER)
    replace_cell_text(t0.cell(5, 1), COURSE_TIME)

    t1 = doc.tables[1]
    replace_cell_text(t1.cell(0, 1), "XXX")
    replace_cell_text(t1.cell(0, 4), STUDENT_CLASS)
    replace_cell_text(t1.cell(0, 7), "24121020062")
    for c in range(1, 8):
        replace_cell_text(t1.cell(1, c), TITLE)
    replace_cell_text(t1.cell(2, 1), "工程设计")
    for c in range(3, 6):
        replace_cell_text(t1.cell(2, c), "课题来源")
    replace_cell_text(t1.cell(2, 6), "小组课题")
    replace_cell_text(t1.cell(2, 7), "小组课题")
    replace_cell_text(t1.cell(3, 1), GUIDE_TEACHER)
    for c in range(3, 6):
        replace_cell_text(t1.cell(3, c), "同组姓名")
    replace_cell_text(t1.cell(3, 6), GROUP_MEMBER)
    replace_cell_text(t1.cell(3, 7), GROUP_MEMBER)
    for c in range(1, 8):
        replace_cell_text(t1.cell(4, c), MAIN_CONTENT, align=WD_ALIGN_PARAGRAPH.LEFT)
    for c in range(1, 8):
        replace_cell_text(t1.cell(5, c), TASK_REQUIREMENTS, align=WD_ALIGN_PARAGRAPH.LEFT)
    for c in range(1, 8):
        replace_cell_text(t1.cell(6, c), "\n".join(REFERENCES), align=WD_ALIGN_PARAGRAPH.LEFT)


def replace_paragraph_range_with_source(template: Document, source: Document, tpl_start_text: str, tpl_end_text: str, src_start_text: str, src_end_text: str):
    tpl_start = find_paragraph_index(template, tpl_start_text)
    tpl_end = find_paragraph_index(template, tpl_end_text)
    src_start = find_paragraph_index(source, src_start_text)
    src_end = find_paragraph_index(source, src_end_text)

    # Replace overlapping paragraphs one by one; template range assumed large enough.
    target_count = tpl_end - tpl_start
    source_count = src_end - src_start

    # If source shorter, blank remaining paragraphs after copy.
    for offset in range(min(target_count, source_count)):
        clone_paragraph_content(source.paragraphs[src_start + offset], template.paragraphs[tpl_start + offset])

    if source_count < target_count:
        for offset in range(source_count, target_count):
            p = template.paragraphs[tpl_start + offset]
            p.text = ""

    # If source longer, insert before tpl_end.
    if source_count > target_count:
        insert_before = template.paragraphs[tpl_end]._element
        for src_idx in range(src_start + target_count, src_end):
            new_p = deepcopy(source.paragraphs[src_idx]._element)
            insert_before.addprevious(new_p)


def build_doc(template_path: Path, source_path: Path, output_path: Path):
    tpl = Document(str(template_path))
    src = Document(str(source_path))

    update_cover_tables(tpl)

    # Replace chapter 5 with source chapter 5
    replace_paragraph_range_with_source(
        tpl,
        src,
        "5 MariaDB 设计与实现",
        "6 可视化设计与分析结果",
        "5 详细设计及实现",
        "6 调试分析",
    )

    # Replace chapter 6 with source chapter 6
    replace_paragraph_range_with_source(
        tpl,
        src,
        "6 可视化设计与分析结果",
        "7 DeepSeek 健康提示",
        "6 调试分析",
        "7 测试结果",
    )

    # Adjust heading titles in template chapter area if copied text introduced different numbers already.
    tpl.save(str(output_path))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--template", required=True)
    parser.add_argument("--source", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    build_doc(Path(args.template), Path(args.source), Path(args.output))


if __name__ == "__main__":
    main()
