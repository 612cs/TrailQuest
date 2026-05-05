#!/usr/bin/env python3
"""Render Chapter 5 Word code tables as SVG images and replace them in DOCX.

The thesis previously used Word tables to hold Chapter 5 code snippets. This
script extracts those tables, renders every snippet into a same-width SVG with
a fixed monospace font size, and replaces the tables with the generated SVGs.
"""

from __future__ import annotations

import argparse
import math
import os
from copy import deepcopy
from pathlib import Path
from typing import Iterable
from zipfile import ZIP_DEFLATED, ZipFile
from xml.sax.saxutils import escape

from lxml import etree


BASE = Path(__file__).resolve().parents[3]
DEFAULT_INPUT = BASE / "DOCS/thesis/manuscripts/陈胜论文_格式修正版_代码SVG三线表版_v14.docx"
DEFAULT_OUTPUT = BASE / "DOCS/thesis/manuscripts/陈胜论文_格式修正版_代码SVG三线表版_v16.docx"
DEFAULT_SVG_DIR = BASE / "DOCS/thesis/figures/code-tables/chapter5"

NS = {
    "w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main",
    "r": "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
    "a": "http://schemas.openxmlformats.org/drawingml/2006/main",
    "wp": "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing",
    "pic": "http://schemas.openxmlformats.org/drawingml/2006/picture",
    "rel": "http://schemas.openxmlformats.org/package/2006/relationships",
}

W = f"{{{NS['w']}}}"
R = f"{{{NS['r']}}}"
A = f"{{{NS['a']}}}"
WP = f"{{{NS['wp']}}}"
PIC = f"{{{NS['pic']}}}"
REL_NS = NS["rel"]

EMU_PER_INCH = 914400
DISPLAY_WIDTH_EMU = int(5.85 * EMU_PER_INCH)
CODE_FONT_SIZE = 14
CODE_LINE_HEIGHT = 19
CODE_PADDING_X = 10
CODE_PADDING_TOP = 14
CODE_PADDING_BOTTOM = 12
CODE_CHAR_WIDTH = 8.7


def text_of(el: etree._Element) -> str:
    return "".join(el.xpath(".//w:t/text()", namespaces=NS)).strip()


def iter_chapter5_children(body: etree._Element) -> Iterable[etree._Element]:
    in_chapter = False
    for child in body:
        txt = text_of(child)
        if txt.startswith("5系统总体实现"):
            in_chapter = True
        elif txt.startswith("6系统测试与优化"):
            in_chapter = False
        if in_chapter:
            yield child


def table_lines(tbl: etree._Element) -> list[str]:
    lines: list[str] = []
    for p in tbl.xpath(".//w:p", namespaces=NS):
        lines.append("".join(p.xpath(".//w:t/text()", namespaces=NS)))
    while lines and lines[-1] == "":
        lines.pop()
    return lines


def is_code_table(tbl: etree._Element) -> bool:
    lines = table_lines(tbl)
    if not lines:
        return False
    first = lines[0].strip()
    return first.startswith("核心代码：") or first in {"智能推荐算法", "天气与景观预测算法"}


def svg_escape_line(line: str) -> str:
    return escape(line, {'"': "&quot;", "'": "&apos;"})


def render_code_svg(lines: list[str], width: int, output: Path) -> tuple[int, int]:
    height = CODE_PADDING_TOP + len(lines) * CODE_LINE_HEIGHT + CODE_PADDING_BOTTOM
    parts = [
        '<?xml version="1.0"?>',
        '<svg xmlns="http://www.w3.org/2000/svg" '
        f'width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        f'<rect x="0" y="0" width="{width}" height="{height}" fill="white"/>',
        f'<g font-family="Menlo, Monaco, Consolas, Courier New, monospace" font-size="{CODE_FONT_SIZE}px">',
    ]
    for index, line in enumerate(lines):
        y = CODE_PADDING_TOP + CODE_FONT_SIZE + index * CODE_LINE_HEIGHT
        parts.append(
            f'<text x="{CODE_PADDING_X}" y="{y}" xml:space="preserve">{svg_escape_line(line)}</text>'
        )
    parts.append("</g>")
    parts.append("</svg>")
    output.write_text("\n".join(parts), encoding="utf-8")
    return width, height


def next_rel_id(rels_root: etree._Element) -> int:
    max_id = 0
    for rel in rels_root.findall(f"{{{REL_NS}}}Relationship"):
        rid = rel.get("Id", "")
        if rid.startswith("rId") and rid[3:].isdigit():
            max_id = max(max_id, int(rid[3:]))
    return max_id + 1


def next_docpr_id(doc_root: etree._Element) -> int:
    max_id = 0
    for node in doc_root.xpath(".//wp:docPr", namespaces=NS):
        value = node.get("id")
        if value and value.isdigit():
            max_id = max(max_id, int(value))
    return max_id + 1


def ensure_svg_content_type(ct_root: etree._Element) -> None:
    for node in ct_root:
        if node.tag.endswith("Default") and node.get("Extension") == "svg":
            node.set("ContentType", "image/svg+xml")
            return
    etree.SubElement(ct_root, "Default", Extension="svg", ContentType="image/svg+xml")


def add_image_relationship(rels_root: etree._Element, rel_id: str, media_name: str) -> None:
    etree.SubElement(
        rels_root,
        f"{{{REL_NS}}}Relationship",
        Id=rel_id,
        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
        Target=f"media/{media_name}",
    )


def make_image_paragraph(rel_id: str, docpr_id: int, title: str, width_px: int, height_px: int) -> etree._Element:
    cx = DISPLAY_WIDTH_EMU
    cy = int(DISPLAY_WIDTH_EMU * height_px / width_px)

    p = etree.Element(W + "p", nsmap={"w": NS["w"], "r": NS["r"]})
    p_pr = etree.SubElement(p, W + "pPr")
    etree.SubElement(p_pr, W + "jc", {W + "val": "center"})
    r = etree.SubElement(p, W + "r")
    drawing = etree.SubElement(r, W + "drawing")
    inline = etree.SubElement(
        drawing,
        WP + "inline",
        distT="0",
        distB="0",
        distL="0",
        distR="0",
        nsmap={"wp": NS["wp"], "a": NS["a"], "pic": NS["pic"]},
    )
    etree.SubElement(inline, WP + "extent", cx=str(cx), cy=str(cy))
    etree.SubElement(inline, WP + "effectExtent", l="0", t="0", r="0", b="0")
    etree.SubElement(inline, WP + "docPr", id=str(docpr_id), name=title, descr=title)
    c_nv = etree.SubElement(inline, WP + "cNvGraphicFramePr")
    etree.SubElement(c_nv, A + "graphicFrameLocks", noChangeAspect="1")

    graphic = etree.SubElement(inline, A + "graphic")
    graphic_data = etree.SubElement(
        graphic,
        A + "graphicData",
        uri="http://schemas.openxmlformats.org/drawingml/2006/picture",
    )
    pic = etree.SubElement(graphic_data, PIC + "pic")
    nv_pic_pr = etree.SubElement(pic, PIC + "nvPicPr")
    etree.SubElement(nv_pic_pr, PIC + "cNvPr", id="0", name=title)
    etree.SubElement(nv_pic_pr, PIC + "cNvPicPr")
    blip_fill = etree.SubElement(pic, PIC + "blipFill")
    etree.SubElement(blip_fill, A + "blip", {R + "embed": rel_id})
    stretch = etree.SubElement(blip_fill, A + "stretch")
    etree.SubElement(stretch, A + "fillRect")
    sp_pr = etree.SubElement(pic, PIC + "spPr")
    xfrm = etree.SubElement(sp_pr, A + "xfrm")
    etree.SubElement(xfrm, A + "off", x="0", y="0")
    etree.SubElement(xfrm, A + "ext", cx=str(cx), cy=str(cy))
    prst = etree.SubElement(sp_pr, A + "prstGeom", prst="rect")
    etree.SubElement(prst, A + "avLst")
    return p


def make_text_paragraph(text: str, bold: bool = False) -> etree._Element:
    p = etree.Element(W + "p")
    r = etree.SubElement(p, W + "r")
    if bold:
        rpr = etree.SubElement(r, W + "rPr")
        etree.SubElement(rpr, W + "b")
    t = etree.SubElement(r, W + "t")
    t.text = text
    return p


def split_title_and_code(lines: list[str]) -> tuple[str | None, list[str]]:
    if not lines:
        return None, []
    first = lines[0].strip()
    if first.startswith("核心代码："):
        return first, lines[1:]
    if first in {"智能推荐算法", "天气与景观预测算法"}:
        return None, lines[1:]
    return None, lines


def build(input_docx: Path, output_docx: Path, svg_dir: Path) -> list[Path]:
    svg_dir.mkdir(parents=True, exist_ok=True)

    with ZipFile(input_docx, "r") as zin:
        zip_infos = zin.infolist()
        zip_data = {info.filename: zin.read(info.filename) for info in zip_infos}

    doc_root = etree.fromstring(zip_data["word/document.xml"])
    rels_root = etree.fromstring(zip_data["word/_rels/document.xml.rels"])
    ct_root = etree.fromstring(zip_data["[Content_Types].xml"])
    body = doc_root.find(W + "body")
    if body is None:
        raise RuntimeError("word/document.xml has no body")

    code_tables = [child for child in iter_chapter5_children(body) if child.tag == W + "tbl" and is_code_table(child)]
    if not code_tables:
        raise RuntimeError("No Chapter 5 code tables found")

    extracted = [table_lines(tbl) for tbl in code_tables]
    prepared = [split_title_and_code(lines) for lines in extracted]
    code_only = [lines for _, lines in prepared]
    max_len = max(max((len(line) for line in lines), default=1) for lines in code_only)
    svg_width = max(860, math.ceil(max_len * CODE_CHAR_WIDTH) + CODE_PADDING_X * 2)

    ensure_svg_content_type(ct_root)
    rel_counter = next_rel_id(rels_root)
    docpr_counter = next_docpr_id(doc_root)
    generated: list[Path] = []
    replacements: dict[etree._Element, list[etree._Element]] = {}

    for index, (tbl, (title, lines)) in enumerate(zip(code_tables, prepared), start=1):
        image_title = title or f"第 5 章代码块 {index}"
        filename = f"ch5_code_{index:02d}.svg"
        svg_path = svg_dir / filename
        width, height = render_code_svg(lines, svg_width, svg_path)
        generated.append(svg_path)

        rel_id = f"rId{rel_counter}"
        rel_counter += 1
        add_image_relationship(rels_root, rel_id, filename)
        new_nodes: list[etree._Element] = []
        if title:
            new_nodes.append(make_text_paragraph(title, bold=False))
        new_nodes.append(make_image_paragraph(rel_id, docpr_counter, image_title, width, height))
        replacements[tbl] = new_nodes
        docpr_counter += 1
        zip_data[f"word/media/{filename}"] = svg_path.read_bytes()

    for tbl, new_nodes in replacements.items():
        parent = tbl.getparent()
        if parent is None:
            raise RuntimeError("Code table has no parent")
        index = parent.index(tbl)
        parent.remove(tbl)
        for offset, node in enumerate(new_nodes):
            parent.insert(index + offset, node)

    zip_data["word/document.xml"] = etree.tostring(
        doc_root, xml_declaration=True, encoding="UTF-8", standalone="yes"
    )
    zip_data["word/_rels/document.xml.rels"] = etree.tostring(
        rels_root, xml_declaration=True, encoding="UTF-8", standalone="yes"
    )
    zip_data["[Content_Types].xml"] = etree.tostring(
        ct_root, xml_declaration=True, encoding="UTF-8", standalone="yes"
    )

    tmp = output_docx.with_suffix(".docx.tmp")
    with ZipFile(tmp, "w", compression=ZIP_DEFLATED) as zout:
        written = set()
        for info in zip_infos:
            name = info.filename
            if name in zip_data:
                zout.writestr(info, zip_data[name])
                written.add(name)
        for name, data in zip_data.items():
            if name not in written:
                zout.writestr(name, data)
    os.replace(tmp, output_docx)
    return generated


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--svg-dir", type=Path, default=DEFAULT_SVG_DIR)
    args = parser.parse_args()

    generated = build(args.input, args.output, args.svg_dir)
    print(args.output)
    for path in generated:
        print(path)


if __name__ == "__main__":
    main()
