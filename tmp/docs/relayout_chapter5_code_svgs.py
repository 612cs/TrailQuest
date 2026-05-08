from pathlib import Path
import re


SVG_DIR = Path("DOCS/thesis/figures/code-tables/chapter5")
FONT_SIZE = "20pt"
TOP = 34
LINE_HEIGHT = 32
BOTTOM = 24


for svg_path in sorted(SVG_DIR.glob("*.svg")):
    text = svg_path.read_text(encoding="utf-8")
    line_count = len(re.findall(r"<text\b", text))
    if line_count == 0:
        continue

    new_height = TOP + (line_count - 1) * LINE_HEIGHT + BOTTOM

    text = re.sub(r'height="\d+"', f'height="{new_height}"', text, count=1)
    text = re.sub(r'viewBox="0 0 (\d+) \d+"', rf'viewBox="0 0 \1 {new_height}"', text, count=1)
    text = re.sub(
        r'font-family="Times New Roman" font-size="[^"]+"',
        f'font-family="Times New Roman" font-size="{FONT_SIZE}"',
        text,
        count=1,
    )

    counter = {"i": 0}

    def replace_y(match):
        y = TOP + counter["i"] * LINE_HEIGHT
        counter["i"] += 1
        return f'y="{y}"'

    text = re.sub(r'y="\d+"', replace_y, text)
    svg_path.write_text(text, encoding="utf-8")
