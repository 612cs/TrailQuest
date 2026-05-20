#!/usr/bin/env python3
"""Normalize CJK/Latin spacing in thesis body text while preserving citations."""

from __future__ import annotations

import os
import re
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

from lxml import etree


BASE = Path(__file__).resolve().parents[3]
DOCX_PATH = BASE / "DOCS/thesis/manuscripts/final/陈胜论文.docx"

NS = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
W = f"{{{NS['w']}}}"

CJK = r"\u4e00-\u9fff"

TERM_REPLACEMENTS = [
    ("Vue3", "Vue 3"),
    ("Vue 2", "Vue 2"),
    ("SpringBoot", "Spring Boot"),
    ("spring boot", "Spring Boot"),
    ("TypeScript", "TypeScript"),
    ("TailwindCSS", "Tailwind CSS"),
    ("DaisyUI", "DaisyUI"),
    ("JavaScript", "JavaScript"),
    ("MySQL", "MySQL"),
    ("Redis", "Redis"),
    ("DeepSeek", "DeepSeek"),
    ("FunctionCalling", "Function Calling"),
    ("Function Calling", "Function Calling"),
    ("CompositionAPI", "Composition API"),
    ("Composition API", "Composition API"),
    ("OptionsAPI", "Options API"),
    ("Options API", "Options API"),
    ("RESTful", "RESTful"),
    ("WebSocket", "WebSocket"),
    ("GeoJSON", "GeoJSON"),
    ("JSON", "JSON"),
    ("API", "API"),
    ("GIS", "GIS"),
    ("AI", "AI"),
    ("LLM", "LLM"),
    ("POI", "POI"),
    ("UGC", "UGC"),
    ("JWT", "JWT"),
    ("DOM", "DOM"),
    ("CDN", "CDN"),
    ("FCP", "FCP"),
    ("LCP", "LCP"),
    ("JMeter", "JMeter"),
]


def qn(name: str) -> str:
    return W + name


def normalize_text(text: str) -> str:
    original = text
    for old, new in TERM_REPLACEMENTS:
        text = text.replace(old, new)

    # Add a half-width space between Chinese and Latin/number tokens.
    text = re.sub(fr"(?<=[{CJK}])(?=[A-Za-z0-9])", " ", text)
    text = re.sub(fr"(?<=[A-Za-z0-9])(?=[{CJK}])", " ", text)

    text = text.replace("AI 与 GIS", "AI 与 GIS")

    # Keep common academic labels compact.
    text = re.sub(r"图\s+(\d+(?:\.\d+)*)", r"图\1", text)
    text = re.sub(r"表\s+(\d+(?:\.\d+)*)", r"表\1", text)
    text = re.sub(r"算法\s+(\d+(?:-\d+)?)", r"算法\1", text)

    # Avoid adding spaces inside reference/citation markers.
    text = re.sub(r"\[\s+(\d)", r"[\1", text)
    text = re.sub(r"(\d)\s+\]", r"\1]", text)
    text = re.sub(r"(\d)\s+-\s+(\d)", r"\1-\2", text)

    # Normalize repeated spacing without touching paragraph indentation tabs.
    text = re.sub(r" {2,}", " ", text)
    if original.startswith(" ") and not text.startswith(" "):
        text = " " + text
    return text


def element_text(element: etree._Element) -> str:
    return "".join(element.xpath(".//w:t/text()", namespaces=NS)).strip()


def is_inside_reference_list(body_children: list[etree._Element], index: int, ref_index: int | None, ack_index: int | None) -> bool:
    if ref_index is None:
        return False
    end = ack_index if ack_index is not None else len(body_children)
    return ref_index < index < end


def should_skip_text_node(t_node: etree._Element) -> bool:
    # Citation hyperlinks are already carefully formatted as superscript jumps.
    parent = t_node.getparent()
    while parent is not None:
        if parent.tag == qn("hyperlink"):
            return True
        parent = parent.getparent()
    return False


def build() -> None:
    with ZipFile(DOCX_PATH, "r") as zin:
        zip_infos = zin.infolist()
        zip_data = {info.filename: zin.read(info.filename) for info in zip_infos}

    root = etree.fromstring(zip_data["word/document.xml"])
    body = root.find(qn("body"))
    if body is None:
        raise RuntimeError("document.xml 缺少 body")
    children = list(body)

    ref_index = None
    ack_index = None
    for idx, child in enumerate(children):
        text = element_text(child)
        if text == "参考文献":
            ref_index = idx
        elif text == "致  谢":
            ack_index = idx

    changed = 0
    for idx, child in enumerate(children):
        if is_inside_reference_list(children, idx, ref_index, ack_index):
            continue
        for t_node in child.xpath(".//w:t", namespaces=NS):
            if should_skip_text_node(t_node):
                continue
            old = t_node.text or ""
            new = normalize_text(old)
            if new != old:
                t_node.text = new
                if new.startswith(" ") or new.endswith(" "):
                    t_node.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
                changed += 1

    zip_data["word/document.xml"] = etree.tostring(
        root, xml_declaration=True, encoding="UTF-8", standalone="yes"
    )
    tmp = DOCX_PATH.with_suffix(".docx.tmp")
    with ZipFile(tmp, "w", compression=ZIP_DEFLATED) as zout:
        for info in zip_infos:
            zout.writestr(info, zip_data[info.filename])
    os.replace(tmp, DOCX_PATH)
    print(f"{DOCX_PATH} changed_text_nodes={changed}")


if __name__ == "__main__":
    build()
