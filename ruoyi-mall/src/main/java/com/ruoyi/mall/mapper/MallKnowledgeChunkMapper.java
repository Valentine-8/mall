package com.ruoyi.mall.mapper;

import java.util.List;
import com.ruoyi.mall.domain.MallKnowledgeChunk;

public interface MallKnowledgeChunkMapper
{
    List<MallKnowledgeChunk> selectActiveChunks();

    int insertMallKnowledgeChunk(MallKnowledgeChunk chunk);

    int deleteChunksByDocId(Long docId);
}
