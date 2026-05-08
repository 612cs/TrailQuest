from pathlib import Path
import re


SVG_DIR = Path("DOCS/thesis/figures/code-tables/chapter5")


for svg_path in sorted(SVG_DIR.glob("*.svg")):
    text = svg_path.read_text(encoding="utf-8")

    # Remove the full-canvas white background rectangle so the SVG renders transparent.
    text = re.sub(
        r'\n?<rect\s+x="0"\s+y="0"\s+width="[^"]+"\s+height="[^"]+"\s+fill="white"\s*/>\n?',
        "\n",
        text,
        count=1,
    )

    # Use the thesis-required English font and Word's 小四 size.
    text = re.sub(
        r'font-family="[^"]+"\s+font-size="[^"]+"',
        'font-family="Times New Roman" font-size="12pt"',
        text,
        count=1,
    )

    svg_path.write_text(text, encoding="utf-8")
