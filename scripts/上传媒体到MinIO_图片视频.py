# -*- coding: utf-8 -*-
"""
上传媒体到 MinIO（单图 / 多图 / 视频）→ 写回业务表字段（存 URL）
========================================================================
用途：RuoYi-Vue-Plus 项目里几乎每个业务都要传图/视频（商品封面、相册多图、
      认证材料、举证图、宠物近况、演示视频…）。本脚本把"下载 → 传 MinIO →
      写回业务字段"这一整段做成可复用模板，给演示数据灌图用。

配合 SOP 05 "图文必须对应"铁律：
    图片/视频内容必须真实反映实体本身（标"水杯"就得是水杯，绝不张冠李戴）。
    素材先用联网搜索 WebSearch/WebFetch 找到合适的，填进 ITEMS 的 srcs；
    ★ 填之前确认"这确实是这个实体"，入库后再抽样核对一次。

字段存法（本模板的 URL 约定，与前端 MediaUpload / 后端 /resource/media/upload 一致）：
    - 业务媒体字段【直接存可访问 URL】，不是 ossId、不登记 sys_oss。
    - 单值字段（cover / image_url / video）：存【一个 URL】。
    - 多值字段（images 等 varchar(1000)）：存【逗号拼接的多个 URL】。
    - 读侧：图片 <ImagePreview :src="row.images" />、视频 <VideoView :src="row.video" />。
    ⚠️ 生成的 URL 前缀要与后端 sys_oss_config 对齐（见 public_url_base），
       否则应用里点开图/视频会 404。

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
}

# ============================== ITEMS（【改这里】填实体→字段→素材映射） ==============================
# 每条 = 一行业务记录。fields 里一行一个要写的列：
#   col   业务列名
#   srcs  素材来源列表（http(s) URL 或本机绝对路径），URL 先用 WebSearch/WebFetch 搜到再填
#   multi True=多值字段(逗号拼接多 URL)；缺省 False=单值字段(取 srcs[0])
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

_counter = 0


def log(msg):
    print(msg, flush=True)


def unique_key(ext: str) -> str:
    """按日期分目录、时间戳+自增保证唯一的对象 key。"""
    global _counter
    _counter = (_counter + 1) % 100000
    stamp = f'{int(time.time() * 1000)}{_counter:05d}'
    return f'{datetime.now().strftime("%Y/%m/%d")}/{stamp}{ext}'


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


def upload_one(client: Minio, src: str, name: str) -> str:
    """上传单个素材到 MinIO，返回可访问 URL。"""
    data, ext = fetch_bytes(src)
    key = unique_key(ext)
    ctype = mimetypes.types_map.get(ext, "application/octet-stream")
    client.put_object(CONFIG["minio_bucket"], key, io.BytesIO(data), length=len(data), content_type=ctype)
    url = f'{CONFIG["public_url_base"].rstrip("/")}/{key}'
    log(f"  [minio] {name} <- {os.path.basename(src.split('?')[0])}  -> {url}")
    return url


def process_item(client: Minio, conn, item) -> None:
    table = item.get("table", DEFAULT_TABLE)
    id_col = item.get("id_col", DEFAULT_ID_COL)
    biz_id, name = item["biz_id"], item.get("name", "")
    log(f"[{table}#{biz_id}] {name}")

    for field in item["fields"]:
        col, srcs = field["col"], field["srcs"]
        multi = field.get("multi", False)

        urls = [upload_one(client, s, name) for s in srcs]
        bind = ",".join(urls) if multi else urls[0]

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
