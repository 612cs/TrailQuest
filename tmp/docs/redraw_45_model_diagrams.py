from pathlib import Path
from xml.sax.saxutils import escape


SRC_DIR = Path("DOCS/architecture-diagrams/sources/协同图")

FONT = 20
STROKE = 2


def style_box(extra=""):
    return (
        "rounded=1;arcSize=12;whiteSpace=wrap;html=1;"
        "strokeColor=#000000;fillColor=none;"
        f"fontSize={FONT};strokeWidth={STROKE};align=center;verticalAlign=middle;"
        + extra
    )


def style_title(extra=""):
    return (
        "rounded=0;whiteSpace=wrap;html=1;strokeColor=none;fillColor=none;"
        f"fontSize={FONT + 4};fontStyle=1;align=center;verticalAlign=middle;"
        + extra
    )


def style_label(extra=""):
    return (
        "rounded=0;whiteSpace=wrap;html=1;strokeColor=none;fillColor=none;"
        f"fontSize={FONT};align=center;verticalAlign=middle;"
        + extra
    )


def style_edge(dashed=False):
    return (
        "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;"
        "html=1;strokeColor=#000000;endArrow=block;endFill=1;"
        f"fontSize={FONT};strokeWidth={STROKE};"
        + ("dashed=1;" if dashed else "")
    )


def style_curve(dashed=False):
    return (
        "edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=auto;"
        "html=1;strokeColor=#000000;endArrow=block;endFill=1;"
        f"fontSize={FONT};strokeWidth={STROKE};"
        + ("dashed=1;" if dashed else "")
    )


def v(id_, value, x, y, w, h, style):
    return f'''        <mxCell id="{id_}" parent="1" style="{style}" value="{escape(value)}" vertex="1">
          <mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry" />
        </mxCell>'''


def e(id_, source, target, value="", dashed=False, points=None, curve=False):
    points_xml = ""
    if points:
        points_xml = "\n            <Array as=\"points\">\n"
        points_xml += "\n".join(f'              <mxPoint x="{x}" y="{y}" />' for x, y in points)
        points_xml += "\n            </Array>"
    style = style_curve(dashed) if curve else style_edge(dashed)
    return f'''        <mxCell id="{id_}" parent="1" source="{source}" target="{target}" style="{style}" value="{escape(value)}" edge="1">
          <mxGeometry relative="1" as="geometry">{points_xml}
          </mxGeometry>
        </mxCell>'''


def doc(name, cells, w=1780, h=920):
    return f'''<mxfile host="Electron" agent="Codex">
  <diagram id="{escape(name)}" name="{escape(name)}">
    <mxGraphModel dx="{w}" dy="{h}" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{w}" pageHeight="{h}" math="0" shadow="0" background="none">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
{chr(10).join(cells)}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
'''


def recommendation_diagram():
    cells = []
    cells.append(v("title", "大模型推荐链路设计", 580, 35, 620, 48, style_title()))
    cells.append(v("main_hint", "主链路：自然语言需求  →  结构化参数  →  候选路线  →  综合评分  →  推荐结果", 310, 90, 1160, 40, style_label("fontStyle=1;")))

    y = 220
    h = 110
    cells += [
        v("input", "用户输入q\n自然语言出行需求", 70, y, 210, h, style_box()),
        v("llm", "大模型语义解析\nf_llm(q)", 335, y, 220, h, style_box()),
        v("params", "参数集合P\n{l,t,d,r,s}", 610, y, 210, h, style_box()),
        v("retrieve", "数据库候选检索\n路线/标签/位置", 875, y, 230, h, style_box()),
        v("score", "综合评分模型\nαM+βU+γH+δE", 1160, y, 250, h, style_box()),
        v("output", "TopK推荐输出\n路线卡片+推荐理由", 1470, y, 230, h, style_box()),
    ]

    cells += [
        v("p_l", "l\n地点", 610, 390, 62, 62, style_box("fontStyle=1;")),
        v("p_t", "t\n时间", 690, 390, 62, 62, style_box("fontStyle=1;")),
        v("p_d", "d\n难度", 770, 390, 62, 62, style_box("fontStyle=1;")),
        v("p_r", "r\n距离/耗时", 850, 390, 96, 62, style_box("fontStyle=1;")),
        v("p_s", "s\n景观/负重", 965, 390, 100, 62, style_box("fontStyle=1;")),
    ]

    cells += [
        v("data_profile", "用户徒步画像\n经验等级/偏好/负重", 420, 615, 240, 80, style_box()),
        v("data_trail", "路线基础数据\n距离/海拔/难度/评分", 720, 615, 250, 80, style_box()),
        v("data_hot", "互动热度数据\n点赞/收藏/评论", 1030, 615, 240, 80, style_box()),
        v("data_env", "环境景观数据\n天气/景观概率/风险", 1325, 615, 260, 80, style_box()),
    ]

    cells += [
        v("score_m", "M_i\n基础匹配", 1125, 450, 115, 60, style_box("fontStyle=1;")),
        v("score_u", "U_i\n画像匹配", 1265, 450, 115, 60, style_box("fontStyle=1;")),
        v("score_h", "H_i\n互动热度", 1405, 450, 115, 60, style_box("fontStyle=1;")),
        v("score_e", "E_i\n环境适宜", 1545, 450, 115, 60, style_box("fontStyle=1;")),
    ]

    cells += [
        e("e1", "input", "llm", "输入"),
        e("e2", "llm", "params", "解析"),
        e("e3", "params", "retrieve", "查询条件"),
        e("e4", "retrieve", "score", "候选集"),
        e("e5", "score", "output", "排序结果"),
        e("e6", "params", "p_l", "", dashed=True),
        e("e7", "params", "p_t", "", dashed=True),
        e("e8", "params", "p_d", "", dashed=True),
        e("e9", "params", "p_r", "", dashed=True),
        e("e10", "params", "p_s", "", dashed=True),
        e("e11", "data_profile", "score_u", "", dashed=True, points=[(1265, 655)]),
        e("e12", "data_trail", "score_m", "", dashed=True, points=[(1180, 655)]),
        e("e13", "data_hot", "score_h", "", dashed=True, points=[(1460, 655)]),
        e("e14", "data_env", "score_e", "", dashed=True, points=[(1600, 655)]),
        e("e15", "score_m", "score", "", dashed=True),
        e("e16", "score_u", "score", "", dashed=True),
        e("e17", "score_h", "score", "", dashed=True),
        e("e18", "score_e", "score", "", dashed=True),
    ]

    cells.append(v("note", "说明：大模型负责语义理解与理由表达，路线检索和排序由后端确定性规则完成。", 390, 790, 1000, 42, style_label()))
    (SRC_DIR / "AI推荐与环境辅助决策协同架构图.drawio").write_text(
        doc("AI推荐与环境辅助决策协同架构图", cells), encoding="utf-8"
    )


def random_forest_diagram():
    cells = []
    cells.append(v("title", "随机森林景观预测模型设计", 540, 35, 700, 48, style_title()))
    cells.append(v("hint", "主链路：环境数据  →  特征向量X  →  多棵决策树  →  概率平均  →  景观等级与推荐联动", 280, 90, 1220, 40, style_label("fontStyle=1;")))

    cells += [
        v("route", "路线空间信息\n坐标/轨迹/海拔", 80, 190, 220, 85, style_box()),
        v("weather", "天气预报\n温度/湿度/云量/降水", 80, 310, 220, 85, style_box()),
        v("astro", "天文与光污染\n日月时间/观测条件", 80, 430, 220, 85, style_box()),

        v("feature", "特征工程\n构造特征向量X", 390, 300, 240, 110, style_box("fontStyle=1;")),
        v("x_items", "X=[温度,湿度,风速,云量,降水,\n海拔,露点差,日出日落时间...]", 365, 455, 290, 75, style_box()),

        v("forest_area", "随机森林模型", 740, 165, 420, 410, style_box("fontStyle=1;")),
        v("tree1", "决策树1\nh_1(X)", 790, 245, 120, 80, style_box()),
        v("tree2", "决策树2\nh_2(X)", 930, 245, 120, 80, style_box()),
        v("tree3", "决策树K\nh_K(X)", 1070, 245, 120, 80, style_box()),
        v("vote", "概率平均\nP(y=1|X)=1/KΣh_k(X)", 865, 420, 230, 85, style_box()),

        v("level", "等级划分\n低/中/高", 1265, 250, 170, 80, style_box("fontStyle=1;")),
        v("cards", "景观预测卡片\n云海/雾凇/冰挂/日出", 1510, 195, 210, 85, style_box()),
        v("recommend", "推荐链路联动\n环境得分E_i", 1510, 360, 210, 85, style_box()),
        v("warn", "出行提示\n窗口建议/风险说明", 1510, 525, 210, 85, style_box()),

        v("snapshot", "特征快照\n便于回溯", 500, 700, 190, 70, style_box()),
        v("labels", "人工观测标签\n训练与校验", 790, 700, 210, 70, style_box()),
        v("feedback", "用户反馈记录\n优化推荐解释", 1110, 700, 230, 70, style_box()),
    ]

    cells += [
        e("e1", "route", "feature", "空间特征"),
        e("e2", "weather", "feature", "气象特征"),
        e("e3", "astro", "feature", "天文特征"),
        e("e4", "feature", "x_items", "展开"),
        e("e5", "feature", "tree1", "X"),
        e("e6", "feature", "tree2", "X"),
        e("e7", "feature", "tree3", "X"),
        e("e8", "tree1", "vote", ""),
        e("e9", "tree2", "vote", ""),
        e("e10", "tree3", "vote", ""),
        e("e11", "vote", "level", "预测概率"),
        e("e12", "level", "cards", "展示"),
        e("e13", "level", "recommend", "排序特征"),
        e("e14", "recommend", "warn", "解释生成"),
        e("e15", "feature", "snapshot", "保存", dashed=True),
        e("e16", "labels", "forest_area", "训练校验", dashed=True, points=[(895, 650), (950, 600)]),
        e("e17", "feedback", "recommend", "反馈优化", dashed=True, points=[(1225, 650), (1615, 650), (1615, 445)]),
    ]

    cells.append(v("note", "说明：随机森林输出景观出现概率，等级结果同时服务路线详情页展示和AI推荐排序。", 360, 825, 1080, 42, style_label()))
    (SRC_DIR / "景观预测与推荐链路协同示意图.drawio").write_text(
        doc("景观预测与推荐链路协同示意图", cells), encoding="utf-8"
    )


recommendation_diagram()
random_forest_diagram()
