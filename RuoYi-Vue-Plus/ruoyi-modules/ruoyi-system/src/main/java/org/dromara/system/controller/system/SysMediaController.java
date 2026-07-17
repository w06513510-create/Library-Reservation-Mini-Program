package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.factory.OssFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 通用媒体上传 Controller（图片 / 视频，登录即可用）——模板内置通用能力。
 * <p>
 * 任何已登录用户（后台管理员或 C 端 app_user）都可调用：直接走 ruoyi-common-oss
 * 上传到 MinIO/OSS 并返回可访问 url，无需 {@code system:oss:upload} 权限
 * （区别于需要该权限的 {@link SysOssController}）。前端把返回的 url 存入业务字段
 * （如 images 逗号分隔、video 单值）。
 * <p>
 * 前置条件：<br>
 * 1) 视频较大，已把 {@code spring.servlet.multipart.max-file-size} 调至 100MB；<br>
 * 2) MinIO 需启动，且 {@code sys_oss_config} 的 minio 行凭据/桶名对齐、status=0。
 *
 * @author ruoyi
 */
@SaCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/media")
public class SysMediaController {

    /** 允许的图片扩展名 */
    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    /** 允许的视频扩展名 */
    private static final Set<String> VIDEO_EXT = Set.of("mp4", "mov", "m4v", "webm", "avi", "mkv");
    /** 图片大小上限 10MB */
    private static final long MAX_IMAGE = 10L * 1024 * 1024;
    /** 视频大小上限 100MB */
    private static final long MAX_VIDEO = 100L * 1024 * 1024;

    /**
     * 上传单个图片 / 视频，返回 {@code { url, fileName, type }}。
     * 按扩展名白名单校验类型，按类型分别限制大小。
     *
     * @param file 上传的图片或视频文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String suffix = (originalName != null && originalName.contains("."))
            ? originalName.substring(originalName.lastIndexOf(".")) : "";
        String ext = suffix.replace(".", "").toLowerCase();

        boolean isImage = IMAGE_EXT.contains(ext);
        boolean isVideo = VIDEO_EXT.contains(ext);
        if (!isImage && !isVideo) {
            throw new ServiceException("仅支持图片(jpg/png/gif/webp 等)或视频(mp4/mov/webm 等)");
        }
        long max = isVideo ? MAX_VIDEO : MAX_IMAGE;
        if (file.getSize() > max) {
            throw new ServiceException(isVideo ? "视频不能超过 100MB" : "图片不能超过 10MB");
        }

        OssClient storage = OssFactory.instance();
        UploadResult result;
        try {
            result = storage.uploadSuffix(file.getBytes(), suffix, file.getContentType());
        } catch (IOException e) {
            throw new ServiceException("文件上传失败：" + e.getMessage());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("url", result.getUrl());
        data.put("fileName", result.getFilename());
        data.put("type", isVideo ? "video" : "image");
        return R.ok(data);
    }

}
