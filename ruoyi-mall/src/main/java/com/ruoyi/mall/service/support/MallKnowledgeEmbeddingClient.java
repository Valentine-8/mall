package com.ruoyi.mall.service.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.domain.MallChatConfig;

@Component
public class MallKnowledgeEmbeddingClient
{
    private static final Logger log = LoggerFactory.getLogger(MallKnowledgeEmbeddingClient.class);
    private static final int BATCH_SIZE = 16;

    public boolean canEmbed(MallChatConfig config)
    {
        return config != null
            && StringUtils.isNotEmpty(config.getAiApiUrl())
            && StringUtils.isNotEmpty(config.getAiApiKey());
    }

    public List<double[]> embed(MallChatConfig config, List<String> texts)
    {
        List<double[]> result = new ArrayList<>();
        if (texts == null || texts.isEmpty() || !canEmbed(config))
        {
            return result;
        }
        for (int i = 0; i < texts.size(); i += BATCH_SIZE)
        {
            int end = Math.min(i + BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(i, end);
            List<double[]> part = embedBatch(config, batch);
            if (part.size() != batch.size())
            {
                log.warn("Embedding batch size mismatch: expected {}, got {}", batch.size(), part.size());
                return new ArrayList<>();
            }
            result.addAll(part);
        }
        return result;
    }

    public double[] embedOne(MallChatConfig config, String text)
    {
        List<double[]> list = embed(config, java.util.Collections.singletonList(text));
        return list.isEmpty() ? null : list.get(0);
    }

    private List<double[]> embedBatch(MallChatConfig config, List<String> texts)
    {
        List<double[]> vectors = new ArrayList<>();
        try
        {
            String url = config.getAiApiUrl();
            if (!url.endsWith("/embeddings"))
            {
                url = url.endsWith("/") ? url + "embeddings" : url + "/embeddings";
            }
            Map<String, Object> body = new HashMap<>();
            String model = StringUtils.isNotEmpty(config.getEmbeddingModel())
                ? config.getEmbeddingModel() : "text-embedding-v3";
            body.put("model", model);
            body.put("input", texts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getAiApiKey());
            HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(body), headers);
            RestTemplate rest = new RestTemplate();
            ResponseEntity<String> response = rest.postForEntity(url, entity, String.class);
            JSONObject json = JSON.parseObject(response.getBody());
            JSONArray data = json.getJSONArray("data");
            if (data == null)
            {
                return vectors;
            }
            for (int i = 0; i < data.size(); i++)
            {
                JSONObject item = data.getJSONObject(i);
                JSONArray arr = item.getJSONArray("embedding");
                if (arr == null)
                {
                    continue;
                }
                double[] vec = new double[arr.size()];
                for (int j = 0; j < arr.size(); j++)
                {
                    vec[j] = arr.getDoubleValue(j);
                }
                vectors.add(vec);
            }
        }
        catch (Exception e)
        {
            log.warn("Embedding request failed: {}", e.getMessage());
        }
        return vectors;
    }
}
