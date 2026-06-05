package com.ruoyi.mall.service.support;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.domain.MallChatConfig;
import com.ruoyi.mall.domain.MallKnowledgeChunk;
import com.ruoyi.mall.mapper.MallKnowledgeChunkMapper;

@Component
public class MallKnowledgeRetriever
{
    @Autowired
    private MallKnowledgeChunkMapper chunkMapper;
    @Autowired
    private MallKnowledgeEmbeddingClient embeddingClient;

    public String buildContext(MallChatConfig config, String query)
    {
        if (config == null || !"1".equals(config.getKbEnabled()) || StringUtils.isEmpty(query))
        {
            return "";
        }
        int topK = config.getRagTopK() != null && config.getRagTopK() > 0 ? config.getRagTopK() : 3;
        List<MallKnowledgeChunk> chunks = chunkMapper.selectActiveChunks();
        if (chunks.isEmpty())
        {
            return "";
        }
        List<ScoredChunk> scored = scoreChunks(config, query, chunks);
        scored.sort(Comparator.comparingDouble(ScoredChunk::getScore).reversed());
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (ScoredChunk sc : scored)
        {
            if (sc.getScore() <= 0 || count >= topK)
            {
                break;
            }
            if (sb.length() > 0)
            {
                sb.append("\n---\n");
            }
            sb.append(sc.getChunk().getContent());
            count++;
        }
        return sb.toString();
    }

    private List<ScoredChunk> scoreChunks(MallChatConfig config, String query, List<MallKnowledgeChunk> chunks)
    {
        List<ScoredChunk> result = new ArrayList<>();
        if (embeddingClient.canEmbed(config))
        {
            double[] qVec = embeddingClient.embedOne(config, query);
            if (qVec != null)
            {
                for (MallKnowledgeChunk chunk : chunks)
                {
                    double[] cVec = parseEmbedding(chunk.getEmbedding());
                    double score = cVec != null ? cosine(qVec, cVec) : keywordScore(query, chunk);
                    result.add(new ScoredChunk(chunk, score));
                }
                return result;
            }
        }
        for (MallKnowledgeChunk chunk : chunks)
        {
            result.add(new ScoredChunk(chunk, keywordScore(query, chunk)));
        }
        return result;
    }

    private double[] parseEmbedding(String json)
    {
        if (StringUtils.isEmpty(json))
        {
            return null;
        }
        try
        {
            JSONArray arr = JSON.parseArray(json);
            double[] vec = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++)
            {
                vec[i] = arr.getDoubleValue(i);
            }
            return vec;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private double cosine(double[] a, double[] b)
    {
        if (a == null || b == null || a.length != b.length || a.length == 0)
        {
            return 0;
        }
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++)
        {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0)
        {
            return 0;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private double keywordScore(String query, MallKnowledgeChunk chunk)
    {
        String q = query.toLowerCase();
        String content = chunk.getContent() == null ? "" : chunk.getContent().toLowerCase();
        String keywords = chunk.getKeywords() == null ? "" : chunk.getKeywords().toLowerCase();
        double score = 0;
        for (String term : q.split("\\s+"))
        {
            if (term.length() < 2)
            {
                continue;
            }
            if (content.contains(term))
            {
                score += 2;
            }
            if (keywords.contains(term))
            {
                score += 1;
            }
        }
        if (score == 0 && content.contains(q))
        {
            score = 1;
        }
        return score;
    }

    private static class ScoredChunk
    {
        private final MallKnowledgeChunk chunk;
        private final double score;

        ScoredChunk(MallKnowledgeChunk chunk, double score)
        {
            this.chunk = chunk;
            this.score = score;
        }

        MallKnowledgeChunk getChunk() { return chunk; }
        double getScore() { return score; }
    }
}
