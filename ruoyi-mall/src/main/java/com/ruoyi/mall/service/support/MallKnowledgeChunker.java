package com.ruoyi.mall.service.support;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.utils.StringUtils;

public final class MallKnowledgeChunker
{
    private MallKnowledgeChunker() {}

    public static List<String> split(String text, int chunkSize, int overlap)
    {
        List<String> chunks = new ArrayList<>();
        if (StringUtils.isEmpty(text))
        {
            return chunks;
        }
        int size = chunkSize > 100 ? chunkSize : 500;
        int ov = overlap >= 0 && overlap < size ? overlap : 80;
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').trim();
        String[] paragraphs = normalized.split("\n{2,}");
        StringBuilder current = new StringBuilder();
        for (String para : paragraphs)
        {
            String p = para.trim();
            if (p.isEmpty())
            {
                continue;
            }
            if (current.length() + p.length() + 1 <= size)
            {
                if (current.length() > 0)
                {
                    current.append("\n");
                }
                current.append(p);
            }
            else
            {
                flushLongText(chunks, current, size, ov);
                current = new StringBuilder(p);
                while (current.length() > size)
                {
                    chunks.add(current.substring(0, size));
                    current.delete(0, size - ov);
                }
            }
        }
        flushLongText(chunks, current, size, ov);
        return chunks;
    }

    private static void flushLongText(List<String> chunks, StringBuilder current, int size, int overlap)
    {
        if (current.length() == 0)
        {
            return;
        }
        while (current.length() > size)
        {
            chunks.add(current.substring(0, size));
            current.delete(0, size - overlap);
        }
        if (current.length() > 0)
        {
            chunks.add(current.toString().trim());
            current.setLength(0);
        }
    }

    public static String extractKeywords(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            return "";
        }
        String sample = content.length() > 200 ? content.substring(0, 200) : content;
        return sample.replaceAll("\\s+", " ").trim();
    }
}
