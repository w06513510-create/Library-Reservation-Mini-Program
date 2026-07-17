# scripts · 可复用工具脚本（随基座复制到每个新项目）

> 这些是**跨项目复用的开发/演示辅助脚本**，作为基座 `Template` 的一部分——复制基座起新项目时**自带**，改好顶部 `CONFIG` 即可就地跑。
>
> **母本在这里**（`D:\code_shop\Template\scripts\`）。改进脚本改这里的母本；SOP 里只有指过来的**索引**（[SOP/templates 索引](../../SOP/templates/README.md)），不再另存副本。

## 脚本清单

| 脚本 | 用途 | 依赖 / 用法 |
| --- | --- | --- |
| `上传媒体到MinIO_图片视频.py` | 单图/多图/视频：下载(URL 或本机文件)→传 MinIO→登记 `sys_oss`→写回业务字段（**单值存一个 ossId，多值存逗号拼接 ossId**，plus-ui 约定）。演示数据保证图文对应用。 | `pip install minio pymysql requests`；填好顶部 `CONFIG`/`ITEMS` 后 `PYTHONUTF8=1 PYTHONIOENCODING=utf-8 python 上传媒体到MinIO_图片视频.py` |

> 新增脚本：放本目录 → 更新本表 → 同步 [SOP/templates 索引](../../SOP/templates/README.md)。
