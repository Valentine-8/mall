package com.ruoyi.mall.service.support;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;

@Component
public class MallChatMediaUpload
{
    private static final long MAX_IMAGE = 5 * 1024 * 1024L;
    private static final long MAX_VIDEO = 20 * 1024 * 1024L;
    private static final String[] IMAGE_EXT = { "jpg", "jpeg", "png", "gif", "webp", "bmp" };
    private static final String[] VIDEO_EXT = { "mp4", "webm", "mov" };

    public String upload(MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("\u8bf7\u9009\u62e9\u6587\u4ef6");
        }
        String ext = FileUploadUtils.getExtension(file).toLowerCase();
        boolean image = isAllowed(ext, IMAGE_EXT);
        boolean video = isAllowed(ext, VIDEO_EXT);
        if (!image && !video)
        {
            throw new ServiceException("\u4ec5\u652f\u6301\u56fe\u7247 jpg/png/gif/webp \u6216\u89c6\u9891 mp4/webm");
        }
        long limit = video ? MAX_VIDEO : MAX_IMAGE;
        if (file.getSize() > limit)
        {
            throw new ServiceException(video ? "\u89c6\u9891\u4e0d\u80fd\u8d85\u8fc7 20MB" : "\u56fe\u7247\u4e0d\u80fd\u8d85\u8fc7 5MB");
        }
        String[] allowed = image ? IMAGE_EXT : VIDEO_EXT;
        String baseDir = RuoYiConfig.getProfile() + "/chat";
        return FileUploadUtils.upload(baseDir, file, allowed, false);
    }

    public static String resolveMsgType(String ext)
    {
        ext = ext == null ? "" : ext.toLowerCase();
        for (String e : VIDEO_EXT)
        {
            if (e.equals(ext))
            {
                return "video";
            }
        }
        return "image";
    }

    public static String detectMsgType(String storedPath)
    {
        if (StringUtils.isEmpty(storedPath))
        {
            return "text";
        }
        int dot = storedPath.lastIndexOf('.');
        if (dot < 0)
        {
            return "image";
        }
        return resolveMsgType(storedPath.substring(dot + 1));
    }

    private boolean isAllowed(String ext, String[] list)
    {
        for (String item : list)
        {
            if (item.equals(ext))
            {
                return true;
            }
        }
        return false;
    }
}
