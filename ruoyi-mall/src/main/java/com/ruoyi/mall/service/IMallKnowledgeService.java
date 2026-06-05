package com.ruoyi.mall.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.mall.domain.MallKnowledgeDoc;

public interface IMallKnowledgeService
{
    List<MallKnowledgeDoc> selectDocList(MallKnowledgeDoc query);

    MallKnowledgeDoc selectDocById(Long docId);

    MallKnowledgeDoc uploadDocument(MultipartFile file, String operator);

    int deleteDoc(Long docId);

    MallKnowledgeDoc reindexDoc(Long docId, String operator);

    String retrieveContext(String query);
}
