package com.ruoyi.web.controller.mall;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.domain.MallChatAgent;
import com.ruoyi.mall.domain.MallChatConfig;
import com.ruoyi.mall.domain.MallChatSession;
import com.ruoyi.mall.service.IMallChatService;
import com.ruoyi.mall.service.support.MallChatMediaUpload;

@RestController
@RequestMapping("/mall/chat")
public class MallChatController extends BaseController
{
    @Autowired
    private IMallChatService chatService;
    @Autowired
    private MallChatMediaUpload chatMediaUpload;

    @PreAuthorize("@ss.hasPermi('mall:chat:config')")
    @GetMapping("/config")
    public AjaxResult getConfig()
    {
        MallChatConfig config = chatService.getConfig();
        if (config != null && com.ruoyi.common.utils.StringUtils.isNotEmpty(config.getAiApiKey()))
        {
            config.setAiApiKey("****");
        }
        return success(config);
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:config')")
    @PutMapping("/config")
    public AjaxResult updateConfig(@RequestBody MallChatConfig config)
    {
        if (config.getAiApiKey() != null && config.getAiApiKey().contains("****"))
        {
            config.setAiApiKey(null);
        }
        return toAjax(chatService.updateConfig(config, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:agent')")
    @GetMapping("/agent/list")
    public TableDataInfo agentList(MallChatAgent query)
    {
        startPage();
        List<MallChatAgent> list = chatService.selectAgentList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:agent')")
    @PostMapping("/agent")
    public AjaxResult addAgent(@RequestBody MallChatAgent agent)
    {
        return toAjax(chatService.addAgent(agent, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:agent')")
    @PutMapping("/agent")
    public AjaxResult updateAgent(@RequestBody MallChatAgent agent)
    {
        return toAjax(chatService.updateAgent(agent, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:agent')")
    @DeleteMapping("/agent/{agentId}")
    public AjaxResult deleteAgent(@PathVariable Long agentId)
    {
        return toAjax(chatService.deleteAgent(agentId));
    }

    @GetMapping("/agent/me")
    public AjaxResult myAgent()
    {
        return success(chatService.selectAgentByUserId(getUserId()));
    }

    @PostMapping("/agent/online")
    public AjaxResult setOnline(@RequestParam boolean online)
    {
        return toAjax(chatService.setAgentOnline(getUserId(), online, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:list')")
    @GetMapping("/session/list")
    public TableDataInfo sessionList(MallChatSession query)
    {
        chatService.tryAssignWaitingSessions();
        startPage();
        List<MallChatSession> list = chatService.selectSessionList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:query')")
    @GetMapping("/messages")
    public AjaxResult messages(@RequestParam Long sessionId, @RequestParam(required = false) Long afterId)
    {
        return success(chatService.listMessages(sessionId, afterId, getUserId(), true));
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:reply')")
    @PostMapping("/reply")
    public AjaxResult reply(@RequestBody ReplyBody body)
    {
        return success(chatService.sendAgentMessage(getUserId(), getUsername(), body.getSessionId(), body.getContent(),
            body.getMsgType()));
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:reply')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws Exception
    {
        String path = chatMediaUpload.upload(file);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("fileName", path);
        ajax.put("msgType", MallChatMediaUpload.detectMsgType(path));
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('mall:chat:close')")
    @PutMapping("/close/{sessionId}")
    public AjaxResult close(@PathVariable Long sessionId)
    {
        return toAjax(chatService.closeSession(sessionId, getUserId(), true));
    }

    public static class ReplyBody
    {
        private Long sessionId;
        private String content;
        private String msgType;

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getMsgType() { return msgType; }
        public void setMsgType(String msgType) { this.msgType = msgType; }
    }
}
