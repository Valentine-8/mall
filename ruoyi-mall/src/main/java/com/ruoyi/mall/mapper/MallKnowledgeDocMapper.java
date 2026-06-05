package com.ruoyi.mall.mapper;

import java.util.List;
import com.ruoyi.mall.domain.MallKnowledgeDoc;

public interface MallKnowledgeDocMapper
{
    MallKnowledgeDoc selectMallKnowledgeDocById(Long docId);

    List<MallKnowledgeDoc> selectMallKnowledgeDocList(MallKnowledgeDoc query);

    int insertMallKnowledgeDoc(MallKnowledgeDoc doc);

    int updateMallKnowledgeDoc(MallKnowledgeDoc doc);

    int deleteMallKnowledgeDocById(Long docId);
}
