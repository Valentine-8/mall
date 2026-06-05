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
import com.ruoyi.mall.domain.MallChatMessage;

@Component
public class MallChatAiClient
{
    private static final Logger log = LoggerFactory.getLogger(MallChatAiClient.class);

    public String reply(MallChatConfig config, List<MallChatMessage> history, String userMessage, String ragContext)
    {
        if (config == null || !"1".equals(config.getAiEnabled()))
        {
            return fallbackReply(userMessage);
        }
        if (StringUtils.isEmpty(config.getAiApiUrl()) || StringUtils.isEmpty(config.getAiApiKey()))
        {
            return fallbackReply(userMessage);
        }
        try
        {
            String url = config.getAiApiUrl();
            if (!url.endsWith("/chat/completions"))
            {
                url = url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
            }
            Map<String, Object> body = new HashMap<>();
            body.put("model", StringUtils.isNotEmpty(config.getAiModel()) ? config.getAiModel() : "gpt-4o-mini");
            List<Map<String, String>> messages = new ArrayList<>();
            String systemContent = config.getSystemPrompt();
            if (StringUtils.isNotEmpty(ragContext))
            {
                systemContent = (StringUtils.isNotEmpty(systemContent) ? systemContent + "\n\n" : "")
                    + "\u53c2\u8003\u4ee5\u4e0b\u77e5\u8bc6\u5e93\u7247\u6bb5\u56de\u7b54\uff0c\u82e5\u77db\u76fe\u4ee5\u77e5\u8bc6\u5e93\u4e3a\u51c6\uff1a\n---\n"
                    + ragContext;
            }
            if (StringUtils.isNotEmpty(systemContent))
            {
                Map<String, String> system = new HashMap<>();
                system.put("role", "system");
                system.put("content", systemContent);
                messages.add(system);
            }
            if (history != null)
            {
                for (MallChatMessage msg : history)
                {
                    if (MallChatMessage.SENDER_SYSTEM.equals(msg.getSenderType()))
                    {
                        continue;
                    }
                    Map<String, String> item = new HashMap<>();
                    if (MallChatMessage.SENDER_USER.equals(msg.getSenderType()))
                    {
                        item.put("role", "user");
                    }
                    else
                    {
                        item.put("role", "assistant");
                    }
                    item.put("content", formatHistoryContent(msg));
                    messages.add(item);
                }
            }
            Map<String, String> current = new HashMap<>();
            current.put("role", "user");
            current.put("content", userMessage);
            messages.add(current);
            body.put("messages", messages);
            body.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getAiApiKey());
            HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(body), headers);
            RestTemplate rest = new RestTemplate();
            ResponseEntity<String> response = rest.postForEntity(url, entity, String.class);
            JSONObject json = JSON.parseObject(response.getBody());
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty())
            {
                return choices.getJSONObject(0).getJSONObject("message").getString("content");
            }
        }
        catch (Exception e)
        {
            log.warn("AI chat request failed: {}", e.getMessage());
        }
        return fallbackReply(userMessage);
    }

    private String formatHistoryContent(MallChatMessage msg)
    {
        if (MallChatMessage.MSG_IMAGE.equals(msg.getMsgType()))
        {
            return "[\u7528\u6237\u53d1\u9001\u4e86\u56fe\u7247]";
        }
        if (MallChatMessage.MSG_VIDEO.equals(msg.getMsgType()))
        {
            return "[\u7528\u6237\u53d1\u9001\u4e86\u89c6\u9891]";
        }
        return msg.getContent();
    }

    public String fallbackReply(String userMessage)
    {
        String text = userMessage == null ? "" : userMessage.toLowerCase();
        if (text.contains("\u8f6c\u4eba\u5de5") || text.contains("\u4eba\u5de5") || text.contains("\u5ba2\u670d"))
        {
            return "\u5f53\u524d\u6682\u65e0\u4eba\u5de5\u5ba2\u670d\u5728\u7ebf\uff0c\u6211\u7ee7\u7eed\u4e3a\u60a8\u670d\u52a1\u3002\u60a8\u53ef\u7ee7\u7eed\u63d0\u95ee\uff0c\u6216\u7a0d\u540e\u518d\u8bd5\u300c\u8f6c\u4eba\u5de5\u300d\u3002";
        }
        if (text.contains("\u8ba2\u5355"))
        {
            return "\u60a8\u53ef\u5728\u300c\u6211\u7684\u300d-\u300c\u6211\u7684\u8ba2\u5355\u300d\u4e2d\u67e5\u770b\u8ba2\u5355\u72b6\u6001\u3002\u5982\u9700\u4eba\u5de5\u534f\u52a9\u8bf7\u56de\u590d\u300c\u8f6c\u4eba\u5de5\u300d\u3002";
        }
        if (text.contains("\u9000") || text.contains("\u6362"))
        {
            return "\u9000\u6362\u8d27\u8bf7\u8054\u7cfb\u4eba\u5de5\u5ba2\u670d\u5904\u7406\uff0c\u56de\u590d\u300c\u8f6c\u4eba\u5de5\u300d\u5373\u53ef\u3002";
        }
        if (text.contains("\u7269\u6d41") || text.contains("\u53d1\u8d27"))
        {
            return "\u4ed8\u6b3e\u540e\u5546\u5bb6\u4f1a\u5c3d\u5feb\u53d1\u8d27\uff0c\u60a8\u53ef\u5728\u8ba2\u5355\u8be6\u60c5\u67e5\u770b\u7269\u6d41\u8fdb\u5ea6\u3002";
        }
        return "\u611f\u8c22\u54a8\u8be2\uff01\u6211\u662f\u667a\u80fd\u5ba2\u670d\uff0c\u53ef\u4e3a\u60a8\u89e3\u7b54\u5546\u54c1\u4e0e\u8ba2\u5355\u95ee\u9898\u3002\u9700\u8981\u4eba\u5de5\u8bf7\u56de\u590d\u300c\u8f6c\u4eba\u5de5\u300d\u3002";
    }
}
