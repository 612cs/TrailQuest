#!/usr/bin/env python3
"""Normalize thesis table content and figure/table captions formatting."""

from __future__ import annotations

import argparse
import os
import re
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

from lxml import etree


BASE = Path(__file__).resolve().parents[3]
DEFAULT_INPUT = BASE / "DOCS/thesis/manuscripts/revisions/陈胜论文_格式修正版_代码SVG三线表版_v17.docx"
DEFAULT_OUTPUT = BASE / "DOCS/thesis/manuscripts/revisions/陈胜论文_格式修正版_代码SVG三线表版_v18.docx"

NS = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
W = f"{{{NS['w']}}}"

CAPTION_RE = re.compile(r"^[图表]\d+\.\d+\s*")
TABLE_FONT_SZ = "21"   # 10.5pt, 五号
CAPTION_FONT_SZ = "24"  # 12pt, 小四
THREE_LINE_BORDER_SZ = "4"  # 0.5pt


def qn(name: str) -> str:
    return W + name


def get_or_create(parent: etree._Element, tag: str) -> etree._Element:
    child = parent.find(qn(tag))
    if child is None:
        child = etree.SubElement(parent, qn(tag))
    return child


def set_run_fonts(rpr: etree._Element, size: str, bold: bool | None = None) -> None:
    rfonts = get_or_create(rpr, "rFonts")
    rfonts.set(qn("ascii"), "Times New Roman")
    rfonts.set(qn("hAnsi"), "Times New Roman")
    rfonts.set(qn("cs"), "Times New Roman")
    rfonts.set(qn("eastAsia"), "宋体")

    sz = get_or_create(rpr, "sz")
    sz.set(qn("val"), size)
    szcs = get_or_create(rpr, "szCs")
    szcs.set(qn("val"), size)

    if bold is True:
        get_or_create(rpr, "b")
        get_or_create(rpr, "bCs")
    elif bold is False:
        for tag in ("b", "bCs"):
            node = rpr.find(qn(tag))
            if node is not None:
                rpr.remove(node)


def set_para_center_no_indent(p: etree._Element) -> None:
    ppr = get_or_create(p, "pPr")
    jc = get_or_create(ppr, "jc")
    jc.set(qn("val"), "center")
    ind = get_or_create(ppr, "ind")
    ind.set(qn("firstLine"), "0")
    ind.set(qn("firstLineChars"), "0")
    ind.set(qn("left"), "0")
    ind.set(qn("right"), "0")


def apply_caption_format(p: etree._Element) -> None:
    ppr = get_or_create(p, "pPr")
    pstyle = get_or_create(ppr, "pStyle")
    pstyle.set(qn("val"), "7")
    set_para_center_no_indent(p)
    for r in p.findall(qn("r")):
        rpr = get_or_create(r, "rPr")
        set_run_fonts(rpr, CAPTION_FONT_SZ, bold=True)


def apply_table_paragraph_format(p: etree._Element) -> None:
    set_para_center_no_indent(p)
    for r in p.findall(qn("r")):
        rpr = get_or_create(r, "rPr")
        set_run_fonts(rpr, TABLE_FONT_SZ, bold=None)


def set_border(border_parent: etree._Element, edge: str, val: str, size: str | None = None) -> None:
    border = get_or_create(border_parent, edge)
    border.set(qn("val"), val)
    if size is not None:
        border.set(qn("sz"), size)
    elif qn("sz") in border.attrib:
        del border.attrib[qn("sz")]
    border.set(qn("space"), "0")
    border.set(qn("color"), "000000")


def normalize_three_line_table(tbl: etree._Element) -> None:
    tbl_pr = get_or_create(tbl, "tblPr")
    tbl_borders = get_or_create(tbl_pr, "tblBorders")
    set_border(tbl_borders, "top", "single", THREE_LINE_BORDER_SZ)
    set_border(tbl_borders, "bottom", "single", THREE_LINE_BORDER_SZ)
    set_border(tbl_borders, "left", "none", "0")
    set_border(tbl_borders, "right", "none", "0")
    set_border(tbl_borders, "insideH", "none", "0")
    set_border(tbl_borders, "insideV", "none", "0")

    rows = tbl.findall(qn("tr"))
    if not rows:
        return

    for row_index, tr in enumerate(rows):
        is_header = row_index == 0
        is_last = row_index == len(rows) - 1
        for tc in tr.findall(qn("tc")):
            tc_pr = get_or_create(tc, "tcPr")
            tc_borders = get_or_create(tc_pr, "tcBorders")
            set_border(tc_borders, "left", "nil")
            set_border(tc_borders, "right", "nil")
            set_border(tc_borders, "top", "nil")
            if is_header:
                set_border(tc_borders, "bottom", "single", THREE_LINE_BORDER_SZ)
            elif is_last:
                set_border(tc_borders, "bottom", "single", THREE_LINE_BORDER_SZ)
            else:
                set_border(tc_borders, "bottom", "nil")


def update_caption_style(styles_root: etree._Element) -> None:
    style = styles_root.find(f".//{qn('style')}[@{qn('styleId')}='7']")
    if style is None:
        return
    ppr = get_or_create(style, "pPr")
    jc = get_or_create(ppr, "jc")
    jc.set(qn("val"), "center")
    ind = get_or_create(ppr, "ind")
    ind.set(qn("firstLine"), "0")
    ind.set(qn("firstLineChars"), "0")
    ind.set(qn("left"), "0")
    ind.set(qn("right"), "0")

    rpr = get_or_create(style, "rPr")
    set_run_fonts(rpr, CAPTION_FONT_SZ, bold=True)


def build(input_docx: Path, output_docx: Path) -> None:
    with ZipFile(input_docx, "r") as zin:
        zip_infos = zin.infolist()
        zip_data = {info.filename: zin.read(info.filename) for info in zip_infos}

    doc_root = etree.fromstring(zip_data["word/document.xml"])
    styles_root = etree.fromstring(zip_data["word/styles.xml"])
    update_caption_style(styles_root)

    for p in doc_root.xpath(".//w:p", namespaces=NS):
        text = "".join(p.xpath(".//w:t/text()", namespaces=NS)).strip()
        if CAPTION_RE.match(text):
            apply_caption_format(p)

    for tc in doc_root.xpath(".//w:tc", namespaces=NS):
        tc_pr = get_or_create(tc, "tcPr")
        v_align = get_or_create(tc_pr, "vAlign")
        v_align.set(qn("val"), "center")
        for p in tc.findall(f".//{qn('p')}"):
            apply_table_paragraph_format(p)

    for tbl in doc_root.xpath(".//w:tbl", namespaces=NS):
        normalize_three_line_table(tbl)

    zip_data["word/document.xml"] = etree.tostring(
        doc_root, xml_declaration=True, encoding="UTF-8", standalone="yes"
    )
    zip_data["word/styles.xml"] = etree.tostring(
        styles_root, xml_declaration=True, encoding="UTF-8", standalone="yes"
    )

    tmp = output_docx.with_suffix(".docx.tmp")
    with ZipFile(tmp, "w", compression=ZIP_DEFLATED) as zout:
        for info in zip_infos:
            name = info.filename
            zout.writestr(info, zip_data[name])
    os.replace(tmp, output_docx)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    build(args.input, args.output)
    print(args.output)


if __name__ == "__main__":
    main()
