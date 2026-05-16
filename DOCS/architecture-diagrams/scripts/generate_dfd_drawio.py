from pathlib import Path
import xml.etree.ElementTree as ET


ROOT = Path("/Users/sheng/Documents/code/hiking")
SRC_DIR = ROOT / "DOCS/architecture-diagrams/sources/数据流图"


ENTITY_STYLE = (
    "rounded=0;whiteSpace=wrap;html=1;strokeColor=#000000;fillColor=none;"
    "fontSize=22;fontStyle=1;align=center;verticalAlign=middle;strokeWidth=2;"
)
PROCESS_STYLE = (
    "rounded=1;arcSize=18;whiteSpace=wrap;html=1;strokeColor=#000000;fillColor=none;"
    "fontSize=22;align=center;verticalAlign=middle;strokeWidth=2;"
)
STORE_STYLE = (
    "shape=partialRectangle;whiteSpace=wrap;html=1;top=1;left=1;bottom=1;right=0;"
    "strokeColor=#000000;fillColor=none;fontSize=22;align=center;verticalAlign=middle;strokeWidth=2;"
)
EDGE_STYLE = (
    "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;"
    "strokeWidth=2;fontSize=20;endArrow=classic;endFill=1;"
)
TITLE_STYLE = (
    "text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;"
    "fontSize=26;fontStyle=1;"
)


def add_vertex(root_el, cid, value, x, y, w, h, style):
    cell = ET.SubElement(
        root_el,
        "mxCell",
        {"id": cid, "parent": "1", "style": style, "value": value, "vertex": "1"},
    )
    ET.SubElement(cell, "mxGeometry", {"x": str(x), "y": str(y), "width": str(w), "height": str(h), "as": "geometry"})
    return cell


def add_edge(root_el, cid, source, target, value="", points=None):
    cell = ET.SubElement(
        root_el,
        "mxCell",
        {"id": cid, "parent": "1", "source": source, "target": target, "style": EDGE_STYLE, "edge": "1", "value": value},
    )
    geo = ET.SubElement(cell, "mxGeometry", {"relative": "1", "as": "geometry"})
    if points:
        arr = ET.SubElement(geo, "Array", {"as": "points"})
        for x, y in points:
            ET.SubElement(arr, "mxPoint", {"x": str(x), "y": str(y)})
    return cell


def build_base(name, page_w=1900, page_h=1200):
    mxfile = ET.Element("mxfile", {"host": "Electron", "agent": "Codex"})
    diagram = ET.SubElement(mxfile, "diagram", {"name": name, "id": name})
    model = ET.SubElement(
        diagram,
        "mxGraphModel",
        {
            "dx": "1600",
            "dy": "900",
            "grid": "1",
            "gridSize": "10",
            "guides": "1",
            "tooltips": "1",
            "connect": "1",
            "arrows": "1",
            "fold": "1",
            "page": "1",
            "pageScale": "1",
            "pageWidth": str(page_w),
            "pageHeight": str(page_h),
            "background": "none",
            "math": "0",
            "shadow": "0",
        },
    )
    root = ET.SubElement(model, "root")
    ET.SubElement(root, "mxCell", {"id": "0"})
    ET.SubElement(root, "mxCell", {"id": "1", "parent": "0"})
    return mxfile, root


def save(mxfile, outfile):
    ET.indent(mxfile, space="  ")
    Path(outfile).write_text(ET.tostring(mxfile, encoding="unicode"))


def build_level0():
    mxfile, root = build_base("系统DFD0层", 1800, 1100)
    add_vertex(root, "title", "户外路线智能推荐平台数据流图0层", 40, 20, 560, 40, TITLE_STYLE)

    add_vertex(root, "user", "户外用户", 120, 220, 150, 90, ENTITY_STYLE)
    add_vertex(root, "admin", "系统管理员", 120, 620, 150, 90, ENTITY_STYLE)
    add_vertex(root, "deepseek", "DeepSeek\n大语言模型", 1450, 170, 170, 90, ENTITY_STYLE)
    add_vertex(root, "amap", "高德地图API", 1450, 390, 170, 90, ENTITY_STYLE)
    add_vertex(root, "weather", "气象数据API", 1450, 610, 170, 90, ENTITY_STYLE)
    add_vertex(root, "system", "户外路线智能推荐平台", 720, 390, 280, 110, PROCESS_STYLE)

    add_edge(root, "e1", "user", "system", "注册登录信息、路线检索条件、推荐需求、发布信息、互动请求")
    add_edge(root, "e2", "system", "user", "认证结果、路线信息、推荐结果、天气景观信息、互动反馈")
    add_edge(root, "e3", "admin", "system", "审核管理指令、用户治理指令、运营配置指令")
    add_edge(root, "e4", "system", "admin", "审核结果、统计汇总、操作日志")
    add_edge(root, "e5", "system", "deepseek", "自然语言推荐请求、对话上下文")
    add_edge(root, "e6", "deepseek", "system", "语义解析结果、推荐解释")
    add_edge(root, "e7", "system", "amap", "地理编码请求、路线位置查询")
    add_edge(root, "e8", "amap", "system", "地理位置信息、地图数据")
    add_edge(root, "e9", "system", "weather", "天气查询请求")
    add_edge(root, "e10", "weather", "system", "天气与环境数据")
    return mxfile


def build_level1():
    mxfile, root = build_base("系统DFD1层", 2500, 1500)
    add_vertex(root, "title", "户外路线智能推荐平台数据流图1层", 40, 20, 560, 40, TITLE_STYLE)

    # external entities
    add_vertex(root, "user", "户外用户", 80, 360, 170, 90, ENTITY_STYLE)
    add_vertex(root, "admin", "系统管理员", 80, 1070, 170, 90, ENTITY_STYLE)
    add_vertex(root, "deepseek", "DeepSeek\n大语言模型", 2140, 150, 190, 90, ENTITY_STYLE)
    add_vertex(root, "amap", "高德地图API", 2140, 430, 190, 90, ENTITY_STYLE)
    add_vertex(root, "weather", "气象数据API", 2140, 700, 190, 90, ENTITY_STYLE)

    # processes
    add_vertex(root, "p1", "1. 用户与权限管理", 420, 120, 260, 80, PROCESS_STYLE)
    add_vertex(root, "p2", "2. 路线与轨迹管理", 420, 380, 260, 80, PROCESS_STYLE)
    add_vertex(root, "p4", "4. 社区互动管理", 420, 690, 260, 80, PROCESS_STYLE)
    add_vertex(root, "p5", "5. 后台治理与运营", 420, 1020, 280, 90, PROCESS_STYLE)
    add_vertex(root, "p3", "3. 智能推荐与景观预测", 1330, 360, 320, 100, PROCESS_STYLE)

    # data stores
    add_vertex(root, "d1", "D1 用户数据", 820, 100, 220, 60, STORE_STYLE)
    add_vertex(root, "d2", "D2 路线数据", 820, 330, 220, 60, STORE_STYLE)
    add_vertex(root, "d3", "D3 轨迹与媒体数据", 820, 470, 240, 60, STORE_STYLE)
    add_vertex(root, "d4", "D4 社区互动数据", 820, 690, 240, 60, STORE_STYLE)
    add_vertex(root, "d6", "D6 后台日志数据", 820, 1050, 240, 60, STORE_STYLE)
    add_vertex(root, "d5", "D5 AI会话数据", 1730, 260, 220, 60, STORE_STYLE)

    # user/auth
    add_edge(root, "u1", "user", "p1", "注册/登录/资料维护", [(300, 180)])
    add_edge(root, "u2", "p1", "user", "认证结果/用户资料", [(300, 250)])
    add_edge(root, "u3", "p1", "d1", "用户资料/权限状态")
    add_edge(root, "u4", "d1", "p1", "用户信息/画像信息")

    # trail/track
    add_edge(root, "t1", "user", "p2", "检索条件/发布信息/轨迹上传", [(320, 420)])
    add_edge(root, "t2", "p2", "user", "路线列表/详情/发布结果", [(300, 500)])
    add_edge(root, "t3", "p2", "d2", "路线信息/审核状态")
    add_edge(root, "t4", "d2", "p2", "路线基础数据")
    add_edge(root, "t5", "p2", "d3", "轨迹数据/媒体资源")
    add_edge(root, "t6", "d3", "p2", "轨迹文件/封面相册")

    # recommendation
    add_edge(root, "r1", "user", "p3", "推荐需求/天气查询", [(320, 360), (320, 330), (1180, 330)])
    add_edge(root, "r2", "p3", "user", "推荐结果/景观建议", [(1180, 500), (320, 500), (320, 560)])
    add_edge(root, "r3", "p3", "d1", "用户画像查询", [(1180, 300), (1120, 300), (1120, 160), (930, 160)])
    add_edge(root, "r4", "d1", "p3", "用户画像数据", [(1120, 130), (1120, 270), (1180, 270)])
    add_edge(root, "r5", "p3", "d2", "候选路线检索", [(1180, 340), (1100, 340), (1100, 360), (930, 360)])
    add_edge(root, "r6", "d2", "p3", "路线特征数据", [(1120, 330), (1120, 390), (1180, 390)])
    add_edge(root, "r7", "p3", "d5", "会话写入/上下文缓存")
    add_edge(root, "r8", "d5", "p3", "历史会话/上下文摘要")
    add_edge(root, "r9", "p3", "deepseek", "自然语言解析请求", [(1690, 320), (1690, 195), (2060, 195)])
    add_edge(root, "r10", "deepseek", "p3", "意图解析结果/推荐解释", [(2060, 250), (1690, 250), (1690, 300)])
    add_edge(root, "r11", "p3", "amap", "位置查询请求", [(1710, 410), (1710, 475), (2060, 475)])
    add_edge(root, "r12", "amap", "p3", "位置坐标/地理信息", [(2060, 430), (1710, 430), (1710, 450)])
    add_edge(root, "r13", "p3", "weather", "天气查询请求", [(1740, 520), (1740, 745), (2060, 745)])
    add_edge(root, "r14", "weather", "p3", "天气与环境数据", [(2060, 700), (1740, 700), (1740, 500)])

    # community
    add_edge(root, "c1", "user", "p4", "评论/点赞/收藏/社区浏览", [(320, 690)])
    add_edge(root, "c2", "p4", "user", "互动反馈/社区内容", [(300, 760)])
    add_edge(root, "c3", "p4", "d4", "评论数据/点赞收藏记录")
    add_edge(root, "c4", "d4", "p4", "评论列表/互动状态")
    add_edge(root, "c5", "p4", "d2", "路线热度更新", [(760, 640), (760, 360)])

    # admin
    add_edge(root, "a1", "admin", "p5", "审核/治理/运营配置", [(320, 1060)])
    add_edge(root, "a2", "p5", "admin", "审核结果/统计报表", [(300, 1140)])
    add_edge(root, "a3", "p5", "d1", "用户状态更新", [(760, 1060), (760, 160)])
    add_edge(root, "a4", "p5", "d2", "路线审核状态/上下架", [(780, 1080), (780, 360)])
    add_edge(root, "a5", "p5", "d4", "评论治理结果", [(800, 1090), (800, 720)])
    add_edge(root, "a6", "p5", "d6", "操作日志写入")
    add_edge(root, "a7", "d6", "p5", "历史日志/统计数据")

    return mxfile


def main():
    save(build_level0(), SRC_DIR / "系统DFD0层.drawio")
    save(build_level1(), SRC_DIR / "系统DFD1层.drawio")


if __name__ == "__main__":
    main()
