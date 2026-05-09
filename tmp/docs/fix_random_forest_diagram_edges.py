from pathlib import Path
from xml.sax.saxutils import escape


SRC = Path("DOCS/architecture-diagrams/sources/协同图/景观预测与推荐链路协同示意图.drawio")

FONT = 20
STROKE = 2


def style_box(extra=""):
    return (
        "rounded=1;arcSize=12;whiteSpace=wrap;html=1;strokeColor=#000000;fillColor=none;"
        f"fontSize={FONT};strokeWidth={STROKE};align=center;verticalAlign=middle;" + extra
    )


def style_title(extra=""):
    return (
        "rounded=0;whiteSpace=wrap;html=1;strokeColor=none;fillColor=none;"
        f"fontSize={FONT};fontStyle=1;align=center;verticalAlign=middle;" + extra
    )


def style_label(extra=""):
    return (
        "rounded=0;whiteSpace=wrap;html=1;strokeColor=none;fillColor=none;"
        f"fontSize={FONT};align=center;verticalAlign=middle;" + extra
    )


def edge_style(dashed=False):
    return (
        "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;"
        "strokeColor=#000000;endArrow=block;endFill=1;"
        f"fontSize={FONT};strokeWidth={STROKE};"
        + ("dashed=1;" if dashed else "")
    )


def v(id_, value, x, y, w, h, style):
    return f'''        <mxCell id="{id_}" parent="1" style="{style}" value="{escape(value)}" vertex="1">
          <mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry" />
        </mxCell>'''


def e(id_, source, target, value="", dashed=False, points=None):
    points_xml = ""
    if points:
        points_xml = "\n            <Array as=\"points\">\n"
        points_xml += "\n".join(f'              <mxPoint x="{x}" y="{y}" />' for x, y in points)
        points_xml += "\n            </Array>"
    return f'''        <mxCell id="{id_}" parent="1" source="{source}" target="{target}" style="{edge_style(dashed)}" value="{escape(value)}" edge="1">
          <mxGeometry relative="1" as="geometry">{points_xml}
          </mxGeometry>
        </mxCell>'''


def doc(cells):
    return f'''<mxfile host="Electron" agent="Codex">
  <diagram id="景观预测与推荐链路协同示意图" name="景观预测与推荐链路协同示意图">
    <mxGraphModel dx="1780" dy="920" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1780" pageHeight="920" math="0" shadow="0" background="none">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
{chr(10).join(cells)}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
'''


cells = []
cells.append(v("title", "随机森林景观预测模型设计", 540, 35, 700, 48, style_title()))
cells.append(v("hint", "主链路：环境数据 → 特征向量X → 多棵决策树并行预测 → 概率平均 → 景观等级与推荐联动", 250, 92, 1280, 40, style_label("fontStyle=1;")))

cells += [
    v("route", "路线空间信息\n坐标/轨迹/海拔", 80, 190, 220, 85, style_box()),
    v("weather", "天气预报\n温度/湿度/云量/降水", 80, 310, 220, 85, style_box()),
    v("astro", "天文与光污染\n日月时间/观测条件", 80, 430, 220, 85, style_box()),

    v("feature", "特征工程\n构造特征向量X", 390, 305, 240, 110, style_box("fontStyle=1;")),
    v("x_items", "X=[温度,湿度,风速,云量,降水,\n海拔,露点差,日出日落时间...]", 355, 515, 310, 75, style_box()),

    v("forest_area", "随机森林模型", 735, 165, 455, 445, style_box("fontStyle=1;")),
    v("tree1", "决策树1\nh_1(X)", 785, 220, 125, 76, style_box()),
    v("tree2", "决策树2\nh_2(X)", 930, 330, 125, 76, style_box()),
    v("tree3", "决策树K\nh_K(X)", 1075, 220, 125, 76, style_box()),
    v("vote", "概率平均\nP(y=1|X)=1/KΣh_k(X)", 865, 500, 230, 80, style_box()),

    v("level", "等级划分\n低/中/高", 1265, 300, 170, 80, style_box("fontStyle=1;")),
    v("cards", "景观预测卡片\n云海/雾凇/冰挂/日出", 1510, 205, 210, 85, style_box()),
    v("recommend", "推荐链路联动\n环境得分E_i", 1510, 365, 210, 85, style_box()),
    v("warn", "出行提示\n窗口建议/风险说明", 1510, 525, 210, 85, style_box()),

    v("snapshot", "特征快照\n便于回溯", 500, 720, 190, 70, style_box()),
    v("labels", "人工观测标签\n训练与校验", 790, 720, 210, 70, style_box()),
    v("feedback", "用户反馈记录\n优化推荐解释", 1110, 720, 230, 70, style_box()),
]

cells += [
    e("e1", "route", "feature", "空间特征"),
    e("e2", "weather", "feature", "气象特征"),
    e("e3", "astro", "feature", "天文特征"),
    e("e4", "feature", "x_items", "展开"),

    # One feature vector X is sent to every decision tree in parallel.
    e("e5", "feature", "tree1", "X", points=[(700, 250)]),
    e("e6", "feature", "tree2", "X", points=[(700, 368)]),
    e("e7", "feature", "tree3", "X", points=[(700, 465), (1138, 465)]),

    e("e8", "tree1", "vote", "h_1(X)", points=[(845, 470)]),
    e("e9", "tree2", "vote", "h_2(X)"),
    e("e10", "tree3", "vote", "h_K(X)", points=[(1138, 470)]),
    e("e11", "vote", "level", "预测概率"),
    e("e12", "level", "cards", "详情展示"),
    e("e13", "level", "recommend", "排序特征"),
    e("e14", "recommend", "warn", "解释生成"),
    e("e15", "feature", "snapshot", "保存", dashed=True, points=[(510, 660)]),
    e("e16", "labels", "forest_area", "训练校验", dashed=True, points=[(895, 660), (965, 620)]),
    e("e17", "feedback", "recommend", "反馈优化", dashed=True, points=[(1225, 665), (1615, 665), (1615, 450)]),
]

cells.append(v("note", "说明：同一个特征向量X会并行输入多棵决策树，各树输出概率后再进行平均，最终形成景观等级。", 310, 835, 1160, 42, style_label()))

SRC.write_text(doc(cells), encoding="utf-8")
