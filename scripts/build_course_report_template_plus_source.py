from __future__ import annotations

import argparse
from copy import deepcopy
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.shared import Pt


COURSE_NAME = "大数据分布式集群实践"
STUDENT_CLASS = "大数据2406"
STUDENT_NAME = "XXX"
STUDENT_NO = "24121020062"
GUIDE_TEACHER = "刘田园"
GROUP_MEMBER = "无"
COURSE_TIME = "2026.6.22-2026.7.3"
TITLE = "智启赛途——高校竞赛AI智驭平台"

MAIN_CONTENT = (
    "1、项目定位：面向学生、教师、管理员三类角色的高校竞赛智能服务平台，重点完成赛事管理、评审权限分配、教师账号管理、学生认证与材料提交等核心后端能力。\n"
    "2、基本功能：基于 Spring Boot、MyBatis、Spring Security、JWT、MySQL 与 MinIO 实现用户认证、赛事新增编辑、评审赋权、教师导入、文件上传与预签名访问、教师评审与成绩管理等功能。\n"
    "3、扩展内容：围绕缓存同步、权限幂等控制、Excel 数据清洗、页面性能优化等问题开展调试分析，形成完整课程设计报告并支撑期末实训答辩。"
)

TASK_REQUIREMENTS = (
    "1、需求分析：完成高校竞赛管理平台的需求调研与角色边界划分，明确学生端、教师端、管理员端的主要业务流程。\n"
    "2、系统实现：采用前后端分离架构，完成管理员端赛事管理、评审权限管理、教师账号管理，以及学生端认证与参赛材料等后端功能开发。\n"
    "3、基础支撑：完成 MySQL 核心业务表设计，建立参赛记录、评审分配、评分记录、权限记录等表间关系；集成 MinIO 实现文件上传与安全访问。\n"
    "4、调试与测试：围绕 JWT 认证、赛事时间校验、重复赋权、Excel 导入及页面性能问题开展调试分析，并完成测试与运行结果整理。"
)

REFERENCES = "\n".join(
    [
        "[1] Craig Walls. Spring in Action (Sixth Edition)[M]. Manning Publications, 2022.",
        "[2] 尤雨溪. Vue.js设计与实现[M]. 北京：人民邮电出版社, 2022.",
        "[3] 若依开源社区. RuoYi-Vue开发文档[EB/OL]. https://doc.ruoyi.vip/.",
        "[4] Spring官方. Spring Boot Reference Documentation[EB/OL]. https://docs.spring.io/spring-boot/.",
        "[5] Spring Security团队. Spring Security Reference[EB/OL]. https://docs.spring.io/spring-security/.",
        "[6] MinIO官方. MinIO Java SDK Documentation[EB/OL]. https://min.io/docs/minio/.",
    ]
)


def set_run_font(run, name: str = "宋体", size_pt: float = 12, bold: bool | None = None):
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
    p = cell.paragraphs[0]
    p.alignment = align
    run = p.add_run(text)
    set_run_font(run, font, size, bold=bold)


def update_cover_tables(doc: Document):
    t0 = doc.tables[0]
    replace_cell_text(t0.cell(0, 1), COURSE_NAME)
    replace_cell_text(t0.cell(1, 1), STUDENT_CLASS)
    replace_cell_text(t0.cell(2, 1), STUDENT_NAME)
    replace_cell_text(t0.cell(3, 1), STUDENT_NO)
    replace_cell_text(t0.cell(4, 1), GUIDE_TEACHER)
    replace_cell_text(t0.cell(5, 1), COURSE_TIME)

    t1 = doc.tables[1]
    replace_cell_text(t1.cell(0, 1), STUDENT_NAME)
    replace_cell_text(t1.cell(0, 4), STUDENT_CLASS)
    replace_cell_text(t1.cell(0, 7), STUDENT_NO)
    for c in range(1, 8):
        replace_cell_text(t1.cell(1, c), TITLE)
    replace_cell_text(t1.cell(2, 1), "工程设计")
    replace_cell_text(t1.cell(2, 6), "小组课题")
    replace_cell_text(t1.cell(2, 7), "小组课题")
    replace_cell_text(t1.cell(3, 1), GUIDE_TEACHER)
    replace_cell_text(t1.cell(3, 6), GROUP_MEMBER)
    replace_cell_text(t1.cell(3, 7), GROUP_MEMBER)
    for c in range(1, 8):
        replace_cell_text(t1.cell(4, c), MAIN_CONTENT, align=WD_ALIGN_PARAGRAPH.LEFT)
    for c in range(1, 8):
        replace_cell_text(t1.cell(5, c), TASK_REQUIREMENTS, align=WD_ALIGN_PARAGRAPH.LEFT)
    for c in range(1, 8):
        replace_cell_text(t1.cell(6, c), REFERENCES, align=WD_ALIGN_PARAGRAPH.LEFT)


def find_body_child_index_by_paragraph_text(doc: Document, target_text: str) -> int:
    body = doc._element.body
    for idx, child in enumerate(body):
        if child.tag == qn("w:p"):
            texts = "".join(node.text or "" for node in child.iter() if node.tag == qn("w:t")).strip()
            if texts == target_text:
                return idx
    raise ValueError(f"Cannot find paragraph: {target_text}")


def replace_body_with_source(template: Document, source: Document):
    tpl_body = template._element.body
    src_body = source._element.body

    tpl_start_idx = find_body_child_index_by_paragraph_text(template, "1 项目背景与需求分析")
    src_start_idx = find_body_child_index_by_paragraph_text(source, "1 需求分析")

    # remove template content from chapter 1 onward, keep sectPr
    removable = list(tpl_body)[tpl_start_idx:]
    for child in removable:
        if child.tag != qn("w:sectPr"):
            tpl_body.remove(child)

    sect_pr = tpl_body[-1]
    for child in list(src_body)[src_start_idx:]:
        if child.tag == qn("w:sectPr"):
            continue
        sect_pr.addprevious(deepcopy(child))


def build(template_path: Path, source_path: Path, output_path: Path):
    template = Document(str(template_path))
    source = Document(str(source_path))
    update_cover_tables(template)
    replace_body_with_source(template, source)
    template.save(str(output_path))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--template", required=True)
    parser.add_argument("--source", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    build(Path(args.template), Path(args.source), Path(args.output))


if __name__ == "__main__":
    main()
