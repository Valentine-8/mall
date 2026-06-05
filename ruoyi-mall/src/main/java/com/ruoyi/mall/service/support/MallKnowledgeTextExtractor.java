package com.ruoyi.mall.service.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Component
public class MallKnowledgeTextExtractor
{
    public String extract(File file, String fileType) throws Exception
    {
        String type = fileType == null ? "" : fileType.toLowerCase();
        if ("txt".equals(type) || "md".equals(type))
        {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }
        if ("docx".equals(type))
        {
            return extractDocx(new FileInputStream(file));
        }
        if ("pdf".equals(type))
        {
            return extractPdf(file);
        }
        throw new ServiceException("Unsupported file type: " + type);
    }

    private String extractDocx(InputStream in) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(in))
        {
            for (XWPFParagraph p : doc.getParagraphs())
            {
                String text = p.getText();
                if (StringUtils.isNotEmpty(text))
                {
                    sb.append(text.trim()).append("\n");
                }
            }
        }
        return sb.toString().trim();
    }

    private String extractPdf(File file) throws Exception
    {
        try (PDDocument doc = PDDocument.load(file))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc).trim();
        }
    }
}
