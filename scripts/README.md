# scripts · 可复用工具脚本（随基座复制到每个新项目）

> 这些是**跨项目复用的开发/演示辅助脚本**，作为基座 `Template` 的一部分——复制基座起新项目时**自带**，改好顶部 `CONFIG` 即可就地跑。
>
> **母本在这里**（`D:\codeSpace_shop\Template\scripts\`）。改进脚本改这里的母本；SOP 里只有指过来的**索引**（[SOP/templates 索引](../../SOP/templates/README.md)），不再另存副本。

## 脚本清单

| 脚本 | 用途 | 依赖 / 用法 |
| --- | --- | --- |
| `run.bat` | **后端一键启动（已实测）**：固定 JDK17 + 本地仓库 `D:\Maven\.m2\repository`，用 reactor 打自包含 **fat jar**（`mvn -pl ruoyi-admin -am -Dmaven.test.skip=true package`，不带 `clean` 以避开 flatten 坑）再 `java -jar`——约 20s 起、零下载、效果同 IDEA。脚本会自动切到 `..\RuoYi-Vue-Plus`。 | 双击运行。`run.bat`=有 jar 直接起、无则先构建；`run.bat build`=改了后端代码后重构建再起；`run.bat run prod`=第二参数覆盖 profile（默认 dev）。端口 **8199**。**先起 MySQL/Redis（上传还需 MinIO）**。 |
| `run-frontend.bat` | **前端一键启动（已实测）**：固定 Node，切到 `..\plus-ui` 跑 `npm run dev`（Vite dev server）。本项目用 **npm**（有 `package-lock.json`），**不是 yarn**（stock `plus-ui\bin\run-web.bat` 用 `yarn dev` 在本机跑不起来）；`node_modules` 缺失会自动 `npm install`（淘宝镜像）。纯 ASCII，双击 / headless 均可跑。 | 双击运行。端口 **8188**（`VITE_APP_PORT`）。改 `.env` 需**重启本脚本**（Vite 启动时才读 env）。 |
| `上传媒体到MinIO_图片视频.py` | 单图/多图/视频：下载(URL 或本机文件)→传 MinIO→写回业务字段（**存 URL**：单值一个 URL、多值逗号拼接 URL；与前端 `MediaUpload`/后端 `/resource/media/upload` 同一约定）。演示数据保证图文对应用。 | `pip install minio pymysql requests`；填好顶部 `CONFIG`/`ITEMS` 后 `PYTHONUTF8=1 PYTHONIOENCODING=utf-8 python 上传媒体到MinIO_图片视频.py` |

> 新增脚本：放本目录 → 更新本表 → 同步 [SOP/templates 索引](../../SOP/templates/README.md)。
