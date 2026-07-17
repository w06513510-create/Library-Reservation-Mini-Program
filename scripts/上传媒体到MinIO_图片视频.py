# -*- coding: utf-8 -*-
"""
上传媒体到 MinIO（单图 / 多图 / 视频）→ 登记 sys_oss → 写回业务表字段
========================================================================
用途：RuoYi-Vue-Plus 项目里几乎每个业务都要传图/视频（商品封面、相册多图、
      认证材料、举证图、宠物近况、演示视频…）。本脚本把"下载 → 传 MinIO →
      登记 sys_oss → 写回业务字段"这一整段做成可复用模板。

配合 SOP 05 "图文必须对应"铁律：
    图片/视频内容必须真实反映实体本身（标"水杯"就得是水杯，绝不张冠李戴）。
    素材先用联网搜索 WebSearch/WebFetch 找到合适的，填进 ITEMS 的 srcs；
    ★ 填之前确认"这确实是这个实体"，入库后再抽样核对一次。

字段存法（本基座实测约定，别记反）：
    - 单值字段（cover / image_url / video，varchar 255/500）：存【一个 ossId】。
    - 多值字段（images 等，varchar(1000)，注释"逗号OSS"）：存【逗号拼接的 ossId】。
    - plus-ui 的 ImageUpload / FileUpload 用 listToString 绑的都是 ossId（不是 URL），
      前端展示时再由 ossId 换 URL。所以写回默认 store='oss_id'。
      （极少数字段直接存 URL 时，把该 field 的 "store" 改成 'url'。）
    - sys_oss.oss_id 是 bigint 主键、非自增 → 本脚本自造雪花风格 id。

依赖：
    pip install minio pymysql requests
运行（强制 UTF-8，避免 GBK 崩）：
    PYTHONUTF8=1 PYTHONIOENCODING=utf-8 python 上传媒体到MinIO_图片视频.py

======== 每个项目要改的都在 CONFIG 和 ITEMS 里，已用【改这里】标注 ========
"""

import io
import os
import time
import mimetypes
from datetime import datetime

import requests
import pymysql
from minio import Minio

# ============================== CONFIG（【改这里】按项目改） ==============================
CONFIG = {
    # ---- MinIO（本机基准见 SOP 01；桶要与 sys_oss_config 的 minio 行对齐）----
    "minio_endpoint": "localhost:9000",       # 不带 http://
    "minio_access_key": "ruoyi",
    "minio_secret_key": "ruoyi123",
    "minio_secure": False,                     # 本机 http 用 False
    "minio_bucket": "ruoyi",
    # 对外访问 URL 前缀：要和后端 sys_oss_config 配置一致，否则应用里点开会 404。
    "public_url_base": "http://localhost:9000/ruoyi",

    # ---- MySQL（一库一项目，库名【改这里】）----
    "mysql_host": "localhost",
    "mysql_port": 3306,
    "mysql_user": "root",
    "mysql_password": "123456",
    "mysql_db": "your_project_db",             # 【改这里】如 campus_secondhand / pet_adoption

    # ---- sys_oss 登记用的默认值（一般不用改）----
    "oss_create_dept": 103,                    # 默认部门 id
    "oss_create_by": 1,                        # admin 用户 id
    "oss_tenant_id": "000000",
}

# ============================== ITEMS（【改这里】填实体→字段→素材映射） ==============================
# 每条 = 一行业务记录。fields 里一行一个要写的列：
#   col   业务列名
#   srcs  素材来源列表（http(s) URL 或本机绝对路径），URL 先用 WebSearch/WebFetch 搜到再填
#   multi True=多值字段(逗号拼接 ossId)；缺省 False=单值字段(取 srcs[0])
#   store 'oss_id'(默认，plus-ui 约定) 或 'url'(字段直接存 URL 时)
# table/id_col 缺省用 DEFAULT_*，跨表时在该条里单独写。
DEFAULT_TABLE = "biz_product"                  # 【改这里】主表名
DEFAULT_ID_COL = "product_id"                  # 【改这里】主键列

ITEMS = [
    # {
    #     "biz_id": 1, "name": "保温水杯",
    #     "fields": [
    #         {"col": "cover",  "srcs": ["https://example.com/cup.jpg"]},                 # 单图封面
    #         {"col": "images", "srcs": ["https://.../c1.jpg", "https://.../c2.jpg"], "multi": True},  # 多图
    #         {"col": "video",  "srcs": [r"D:\downloads\cup_demo.mp4"]},                  # 视频(本机文件)
    #     ],
    # },
]
# =====================================================================================

_id_counter = 0


def log(msg):
    print(msg, flush=True)


def new_oss_id() -> int:
    """造一个雪花风格的 bigint 主键（时间戳左移 + 自增，够唯一即可）。"""
    global _id_counter
    _id_counter = (_id_counter + 1) % 4096
    return (int(time.time() * 1000) << 12) | _id_counter


def ensure_bucket(client: Minio, bucket: str):
    if not client.bucket_exists(bucket):
        client.make_bucket(bucket)
        log(f"[minio] 创建桶 {bucket}")


def fetch_bytes(src: str) -> tuple[bytes, str]:
    """返回 (字节, 后缀含点)。src 支持 http(s) URL 或本机绝对路径。"""
    if src.lower().startswith(("http://", "https://")):
        r = requests.get(src, timeout=60, headers={"User-Agent": "Mozilla/5.0"})
        r.raise_for_status()
        ctype = r.headers.get("Content-Type", "").split(";")[0].strip()
        ext = mimetypes.guess_extension(ctype) or os.path.splitext(src.split("?")[0])[1] or ".jpg"
        return (r.content, ".jpg" if ext == ".jpe" else ext)
    with open(src, "rb") as f:
        return f.read(), (os.path.splitext(src)[1] or ".jpg")


def upload_one(client: Minio, conn, src: str, name: str) -> tuple[int, str]:
    """上传单个素材 → 登记 sys_oss。返回 (oss_id, url)。"""
    data, ext = fetch_bytes(src)
    key = f'{datetime.now().strftime("%Y/%m/%d")}/{new_oss_id()}{ext}'
    ctype = mimetypes.types_map.get(ext, "application/octet-stream")
    client.put_object(CONFIG["minio_bucket"], key, io.BytesIO(data), length=len(data), content_type=ctype)
    url = f'{CONFIG["public_url_base"].rstrip("/")}/{key}'

    oss_id = new_oss_id()
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO sys_oss
                (oss_id, tenant_id, file_name, original_name, file_suffix, url,
                 service, create_dept, create_by, create_time)
            VALUES (%s, %s, %s, %s, %s, %s, 'minio', %s, %s, NOW())
            """,
            (oss_id, CONFIG["oss_tenant_id"], key, f"{name or 'media'}{ext}", ext, url,
             CONFIG["oss_create_dept"], CONFIG["oss_create_by"]),
        )
    log(f"  [oss] {name} <- {os.path.basename(src.split('?')[0])}  oss_id={oss_id}")
    return oss_id, url


def process_item(client: Minio, conn, item) -> None:
    table = item.get("table", DEFAULT_TABLE)
    id_col = item.get("id_col", DEFAULT_ID_COL)
    biz_id, name = item["biz_id"], item.get("name", "")
    log(f"[{table}#{biz_id}] {name}")

    for field in item["fields"]:
        col, srcs = field["col"], field["srcs"]
        multi = field.get("multi", False)
        store = field.get("store", "oss_id")

        pairs = [upload_one(client, conn, s, name) for s in srcs]      # [(oss_id, url), ...]
        vals = [(oid if store == "oss_id" else url) for oid, url in pairs]
        bind = ",".join(str(v) for v in vals) if multi else str(vals[0])

        with conn.cursor() as cur:
            cur.execute(
                f"UPDATE `{table}` SET `{col}` = %s WHERE `{id_col}` = %s",
                (bind, biz_id),
            )
        log(f"  [db] {col} -> {bind}")


def main():
    if not ITEMS:
        log("ITEMS 为空——先按实体填好 biz_id / fields（col + srcs，多值加 multi=True）。")
        return

    client = Minio(
        CONFIG["minio_endpoint"], access_key=CONFIG["minio_access_key"],
        secret_key=CONFIG["minio_secret_key"], secure=CONFIG["minio_secure"],
    )
    ensure_bucket(client, CONFIG["minio_bucket"])

    conn = pymysql.connect(
        host=CONFIG["mysql_host"], port=CONFIG["mysql_port"],
        user=CONFIG["mysql_user"], password=CONFIG["mysql_password"],
        database=CONFIG["mysql_db"], charset="utf8mb4", autocommit=False,
    )
    try:
        for item in ITEMS:
            try:
                process_item(client, conn, item)
                conn.commit()
            except Exception as e:
                conn.rollback()
                log(f"[跳过] biz_id={item.get('biz_id')} 失败：{e}")
        log("完成。别忘了：入库后再抽样核对一次图/视频是否真对应实体。")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
