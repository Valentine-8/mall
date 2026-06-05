package com.ruoyi.mall.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.mall.domain.MallChatConfig;
import com.ruoyi.mall.domain.MallKnowledgeChunk;
import com.ruoyi.mall.domain.MallKnowledgeDoc;
import com.ruoyi.mall.mapper.MallChatConfigMapper;
import com.ruoyi.mall.mapper.MallKnowledgeChunkMapper;
import com.ruoyi.mall.mapper.MallKnowledgeDocMapper;
import com.ruoyi.mall.service.IMallKnowledgeService;
import com.ruoyi.mall.service.support.MallKnowledgeChunker;
import com.ruoyi.mall.service.support.MallKnowledgeEmbeddingClient;
import com.ruoyi.mall.service.support.MallKnowledgeRetriever;
import com.ruoyi.mall.service.support.MallKnowledgeTextExtractor;

@Service
public class MallKnowledgeServiceImpl implements IMallKnowledgeService
{
    private static final String[] ALLOWED_EXT = { "txt", "md", "docx", "pdf" };
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    @Autowired
    private MallKnowledgeDocMapper docMapper;
    @Autowired
    private MallKnowledgeChunkMapper chunkMapper;
    @Autowired
    private MallChatConfigMapper configMapper;
    @Autowired
    private MallKnowledgeTextExtractor textExtractor;
    @Autowired
    private MallKnowledgeEmbeddingClient embeddingClient;
    @Autowired
    private MallKnowledgeRetriever retriever;

    @Override
    public List<MallKnowledgeDoc> selectDocList(MallKnowledgeDoc query)
    {
        return docMapper.selectMallKnowledgeDocList(query);
    }

    @Override
    public MallKnowledgeDoc selectDocById(Long docId)
    {
        return docMapper.selectMallKnowledgeDocById(docId);
    }

    @Override
    @Transactional
    public MallKnowledgeDoc uploadDocument(MultipartFile file, String operator)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("\u8bf7\u9009\u62e9\u6587\u4ef6");
        }
        if (file.getSize() > MAX_SIZE)
        {
            throw new ServiceException("\u6587\u4ef6\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc7 10MB");
        }
        String ext = FileUploadUtils.getExtension(file).toLowerCase();
        if (!isAllowed(ext))
        {
            throw new ServiceException("\u4ec5\u652f\u6301 txt / md / docx / pdf");
        }
        String baseDir = RuoYiConfig.getProfile() + "/knowledge";
        String storedPath;
        try
        {
            storedPath = FileUploadUtils.upload(baseDir, file, ALLOWED_EXT, false);
        }
        catch (Exception e)
        {
            throw new ServiceException("\u6587\u4ef6\u4e0a\u4f20\u5931\u8d25: " + e.getMessage());
        }
        String title = file.getOriginalFilename();
        if (StringUtils.isNotEmpty(title) && title.contains("."))
        {
            title = title.substring(0, title.lastIndexOf('.'));
        }
        MallKnowledgeDoc doc = new MallKnowledgeDoc();
        doc.setTitle(StringUtils.isNotEmpty(title) ? title : file.getOriginalFilename());
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(ext);
        doc.setFilePath(storedPath);
        doc.setStatus(MallKnowledgeDoc.STATUS_PROCESSING);
        doc.setChunkCount(0);
        doc.setCreateBy(operator);
        docMapper.insertMallKnowledgeDoc(doc);
        indexDocument(doc, operator);
        return docMapper.selectMallKnowledgeDocById(doc.getDocId());
    }

    @Override
    @Transactional
    public int deleteDoc(Long docId)
    {
        chunkMapper.deleteChunksByDocId(docId);
        return docMapper.deleteMallKnowledgeDocById(docId);
    }

    @Override
    @Transactional
    public MallKnowledgeDoc reindexDoc(Long docId, String operator)
    {
        MallKnowledgeDoc doc = docMapper.selectMallKnowledgeDocById(docId);
        if (doc == null)
        {
            throw new ServiceException("\u6587\u6863\u4e0d\u5b58\u5728");
        }
        doc.setStatus(MallKnowledgeDoc.STATUS_PROCESSING);
        doc.setErrorMsg("");
        doc.setUpdateBy(operator);
        docMapper.updateMallKnowledgeDoc(doc);
        chunkMapper.deleteChunksByDocId(docId);
        indexDocument(doc, operator);
        return docMapper.selectMallKnowledgeDocById(docId);
    }

    @Override
    public String retrieveContext(String query)
    {
        MallChatConfig config = configMapper.selectMallChatConfig();
        return retriever.buildContext(config, query);
    }

    private void indexDocument(MallKnowledgeDoc doc, String operator)
    {
        MallChatConfig config = configMapper.selectMallChatConfig();
        int chunkSize = config != null && config.getChunkSize() != null ? config.getChunkSize() : 500;
        int overlap = config != null && config.getChunkOverlap() != null ? config.getChunkOverlap() : 80;
        File file = new File(RuoYiConfig.getProfile() + StringUtils.substringAfter(doc.getFilePath(), Constants.RESOURCE_PREFIX));
        if (!file.exists())
        {
            failDoc(doc, operator, "\u6587\u4ef6\u4e0d\u5b58\u5728");
            return;
        }
        try
        {
            String text = textExtractor.extract(file, doc.getFileType());
            if (StringUtils.isEmpty(text))
            {
                failDoc(doc, operator, "\u6587\u6863\u5185\u5bb9\u4e3a\u7a7a");
                return;
            }
            List<String> parts = MallKnowledgeChunker.split(text, chunkSize, overlap);
            if (parts.isEmpty())
            {
                failDoc(doc, operator, "\u5207\u7247\u5931\u8d25");
                return;
            }
            if (parts.size() > 200)
            {
                failDoc(doc, operator, "\u5207\u7247\u8fc7\u591a\uff08\u8d85\u8fc7 200 \u5757\uff09\uff0c\u8bf7\u7f29\u77ed\u6587\u6863");
                return;
            }
            List<double[]> vectors = embeddingClient.embed(config, parts);
            boolean hasEmbedding = vectors.size() == parts.size();
            for (int i = 0; i < parts.size(); i++)
            {
                MallKnowledgeChunk chunk = new MallKnowledgeChunk();
                chunk.setDocId(doc.getDocId());
                chunk.setChunkIndex(i);
                chunk.setContent(parts.get(i));
                chunk.setKeywords(MallKnowledgeChunker.extractKeywords(parts.get(i)));
                if (hasEmbedding)
                {
                    chunk.setEmbedding(JSON.toJSONString(vectors.get(i)));
                }
                chunkMapper.insertMallKnowledgeChunk(chunk);
            }
            doc.setStatus(MallKnowledgeDoc.STATUS_OK);
            doc.setChunkCount(parts.size());
            doc.setErrorMsg("");
            doc.setUpdateBy(operator);
            docMapper.updateMallKnowledgeDoc(doc);
        }
        catch (Exception e)
        {
            failDoc(doc, operator, e.getMessage());
        }
    }

    private void failDoc(MallKnowledgeDoc doc, String operator, String msg)
    {
        doc.setStatus(MallKnowledgeDoc.STATUS_FAIL);
        doc.setChunkCount(0);
        String err = msg == null ? "index failed" : msg;
        doc.setErrorMsg(err.length() > 480 ? err.substring(0, 480) : err);
        doc.setUpdateBy(operator);
        docMapper.updateMallKnowledgeDoc(doc);
        chunkMapper.deleteChunksByDocId(doc.getDocId());
    }

    private boolean isAllowed(String ext)
    {
        for (String allowed : ALLOWED_EXT)
        {
            if (allowed.equals(ext))
            {
                return true;
            }
        }
        return false;
    }
}
