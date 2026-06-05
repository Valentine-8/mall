package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.ruoyi.mall.domain.MallChatSession;
import com.ruoyi.mall.service.IMallChatService;
import com.ruoyi.mall.service.support.MallChatMediaUpload;

@RestController
@RequestMapping("/app/mall/chat")
public class AppMallChatController extends BaseController
{
    @Autowired
    private IMallChatService chatService;
    @Autowired
    private MallChatMediaUpload chatMediaUpload;

    @GetMapping("/session")
    public AjaxResult session()
    {
        MallChatSession session = chatService.getOrCreateSession(getUserId(), getUsername());
        return success(session);
    }

    @GetMapping("/messages")
    public AjaxResult messages(@RequestParam Long sessionId, @RequestParam(required = false) Long afterId)
    {
        return success(chatService.listMessages(sessionId, afterId, getUserId(), false));
    }

    @PostMapping("/send")
    public AjaxResult send(@RequestBody SendBody body)
    {
        return success(chatService.sendUserMessage(getUserId(), getUsername(), body.getSessionId(), body.getContent(),
            body.getMsgType()));
    }

    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws Exception
    {
        String path = chatMediaUpload.upload(file);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("fileName", path);
        ajax.put("msgType", MallChatMediaUpload.detectMsgType(path));
        return ajax;
    }

    @PostMapping("/transfer")
    public AjaxResult transfer(@RequestBody SessionBody body)
    {
        return success(chatService.transferToHuman(getUserId(), body.getSessionId()));
    }

    @PutMapping("/close")
    public AjaxResult close(@RequestBody SessionBody body)
    {
        return toAjax(chatService.closeSession(body.getSessionId(), getUserId(), false));
    }

    public static class SendBody
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

    public static class SessionBody
    {
        private Long sessionId;

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    }
}
