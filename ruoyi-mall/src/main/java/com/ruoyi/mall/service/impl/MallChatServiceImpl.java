package com.ruoyi.mall.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.domain.MallChatAgent;
import com.ruoyi.mall.domain.MallChatConfig;
import com.ruoyi.mall.domain.MallChatMessage;
import com.ruoyi.mall.domain.MallChatSession;
import com.ruoyi.mall.mapper.MallChatAgentMapper;
import com.ruoyi.mall.mapper.MallChatConfigMapper;
import com.ruoyi.mall.mapper.MallChatMessageMapper;
import com.ruoyi.mall.mapper.MallChatSessionMapper;
import com.ruoyi.mall.service.IMallChatService;
import com.ruoyi.mall.service.IMallKnowledgeService;
import com.ruoyi.mall.service.support.MallChatAiClient;

@Service
public class MallChatServiceImpl implements IMallChatService
{
    private static final String MSG_SESSION = "\u4f1a\u8bdd\u4e0d\u5b58\u5728";
    private static final String MSG_FORBIDDEN = "\u65e0\u6743\u8bbf\u95ee\u8be5\u4f1a\u8bdd";
    private static final String MSG_CLOSED = "\u4f1a\u8bdd\u5df2\u5173\u95ed";
    private static final String MSG_EMPTY = "\u6d88\u606f\u4e0d\u80fd\u4e3a\u7a7a";
    private static final String MSG_AGENT = "\u60a8\u4e0d\u662f\u5ba2\u670d\u5750\u5e2d";

    @Autowired
    private MallChatConfigMapper configMapper;
    @Autowired
    private MallChatAgentMapper agentMapper;
    @Autowired
    private MallChatSessionMapper sessionMapper;
    @Autowired
    private MallChatMessageMapper messageMapper;
    @Autowired
    private MallChatAiClient aiClient;
    @Autowired
    private IMallKnowledgeService knowledgeService;

    @Override
    public MallChatConfig getConfig()
    {
        return configMapper.selectMallChatConfig();
    }

    @Override
    public int updateConfig(MallChatConfig config, String operator)
    {
        config.setUpdateBy(operator);
        return configMapper.updateMallChatConfig(config);
    }

    @Override
    public List<MallChatAgent> selectAgentList(MallChatAgent query)
    {
        return agentMapper.selectMallChatAgentList(query);
    }

    @Override
    public MallChatAgent selectAgentByUserId(Long userId)
    {
        return agentMapper.selectMallChatAgentByUserId(userId);
    }

    @Override
    public int addAgent(MallChatAgent agent, String operator)
    {
        if (agentMapper.selectMallChatAgentByUserId(agent.getUserId()) != null)
        {
            throw new ServiceException("\u8be5\u7528\u6237\u5df2\u662f\u5750\u5e2d");
        }
        if (agent.getMaxSessions() == null || agent.getMaxSessions() < 1)
        {
            agent.setMaxSessions(5);
        }
        if (agent.getSortOrder() == null)
        {
            agent.setSortOrder(0);
        }
        agent.setStatus("0");
        agent.setCreateBy(operator);
        return agentMapper.insertMallChatAgent(agent);
    }

    @Override
    public int updateAgent(MallChatAgent agent, String operator)
    {
        agent.setUpdateBy(operator);
        return agentMapper.updateMallChatAgent(agent);
    }

    @Override
    public int deleteAgent(Long agentId)
    {
        return agentMapper.deleteMallChatAgentById(agentId);
    }

    @Override
    public int setAgentOnline(Long userId, boolean online, String operator)
    {
        MallChatAgent agent = requireAgent(userId);
        MallChatAgent update = new MallChatAgent();
        update.setAgentId(agent.getAgentId());
        update.setStatus(online ? "1" : "0");
        update.setUpdateBy(operator);
        int rows = agentMapper.updateMallChatAgent(update);
        if (online)
        {
            tryAssignWaitingSessions();
        }
        return rows;
    }

    @Override
    public List<MallChatSession> selectSessionList(MallChatSession query)
    {
        return sessionMapper.selectMallChatSessionList(query);
    }

    @Override
    @Transactional
    public MallChatSession getOrCreateSession(Long userId, String userName)
    {
        MallChatSession exist = sessionMapper.selectOpenSessionByUserId(userId);
        if (exist != null)
        {
            if (MallChatSession.STATUS_WAITING.equals(exist.getStatus()))
            {
                tryAssignSession(exist);
                exist = sessionMapper.selectMallChatSessionById(exist.getSessionId());
            }
            return exist;
        }
        MallChatConfig config = getConfig();
        MallChatSession session = new MallChatSession();
        session.setUserId(userId);
        session.setUserName(userName);
        session.setStatus(MallChatSession.STATUS_AI);
        sessionMapper.insertMallChatSession(session);
        String welcome = config != null && StringUtils.isNotEmpty(config.getWelcomeMessage())
            ? config.getWelcomeMessage()
            : "\u60a8\u597d\uff0c\u6b22\u8fce\u54a8\u8be2\u56fd\u6e05\u5546\u57ce\u3002";
        saveMessage(session.getSessionId(), MallChatMessage.SENDER_AI, null, "\u667a\u80fd\u5ba2\u670d", welcome,
            MallChatMessage.MSG_TEXT);
        return sessionMapper.selectMallChatSessionById(session.getSessionId());
    }

    @Override
    public List<MallChatMessage> listMessages(Long sessionId, Long afterId, Long userId, boolean admin)
    {
        MallChatSession session = requireSession(sessionId);
        assertAccess(session, userId, admin);
        if (MallChatSession.STATUS_WAITING.equals(session.getStatus()))
        {
            tryAssignSession(session);
        }
        return messageMapper.selectMessagesBySessionId(sessionId, afterId);
    }

    @Override
    @Transactional
    public MallChatMessage sendUserMessage(Long userId, String userName, Long sessionId, String content, String msgType)
    {
        msgType = normalizeMsgType(msgType);
        if (StringUtils.isEmpty(content))
        {
            throw new ServiceException(MSG_EMPTY);
        }
        MallChatSession session = sessionId != null ? requireSession(sessionId) : getOrCreateSession(userId, userName);
        assertAccess(session, userId, false);
        ensureOpen(session);
        MallChatMessage userMsg = saveMessage(session.getSessionId(), MallChatMessage.SENDER_USER, userId, userName,
            content.trim(), msgType);
        if (isMediaMessage(msgType))
        {
            session = refreshSession(session.getSessionId());
            if (shouldAiReply(session))
            {
                String tip = MallChatMessage.MSG_VIDEO.equals(msgType)
                    ? "\u5df2\u6536\u5230\u60a8\u53d1\u9001\u7684\u89c6\u9891\u3002\u5982\u9700\u4eba\u5de5\u67e5\u770b\u8bf7\u56de\u590d\u300c\u8f6c\u4eba\u5de5\u300d\u3002"
                    : "\u5df2\u6536\u5230\u60a8\u53d1\u9001\u7684\u56fe\u7247\u3002\u5982\u9700\u4eba\u5de5\u67e5\u770b\u8bf7\u56de\u590d\u300c\u8f6c\u4eba\u5de5\u300d\u3002";
                saveMessage(session.getSessionId(), MallChatMessage.SENDER_AI, null, "\u667a\u80fd\u5ba2\u670d", tip,
                    MallChatMessage.MSG_TEXT);
            }
            return userMsg;
        }
        if (shouldTransfer(content, getConfig()))
        {
            transferToHuman(userId, session.getSessionId());
            session = refreshSession(session.getSessionId());
            if (!shouldAiReply(session))
            {
                return userMsg;
            }
        }
        else if (MallChatSession.STATUS_WAITING.equals(session.getStatus()))
        {
            tryAssignSession(session);
            session = refreshSession(session.getSessionId());
        }
        if (shouldAiReply(session))
        {
            List<MallChatMessage> history = messageMapper.selectMessagesBySessionId(session.getSessionId(), null);
            String ragContext = knowledgeService.retrieveContext(content.trim());
            String aiText = aiClient.reply(getConfig(), history, content.trim(), ragContext);
            saveMessage(session.getSessionId(), MallChatMessage.SENDER_AI, null, "\u667a\u80fd\u5ba2\u670d", aiText,
                MallChatMessage.MSG_TEXT);
        }
        return userMsg;
    }

    @Override
    @Transactional
    public MallChatMessage sendAgentMessage(Long agentUserId, String agentName, Long sessionId, String content,
        String msgType)
    {
        msgType = normalizeMsgType(msgType);
        if (StringUtils.isEmpty(content))
        {
            throw new ServiceException(MSG_EMPTY);
        }
        MallChatAgent agent = requireAgent(agentUserId);
        MallChatSession session = requireSession(sessionId);
        ensureOpen(session);
        if (session.getAgentId() != null && !session.getAgentId().equals(agent.getAgentId()))
        {
            throw new ServiceException(MSG_FORBIDDEN);
        }
        if (MallChatSession.STATUS_AI.equals(session.getStatus())
            || MallChatSession.STATUS_WAITING.equals(session.getStatus()))
        {
            assignAgentToSession(session, agent);
            session = sessionMapper.selectMallChatSessionById(sessionId);
        }
        if (!MallChatSession.STATUS_HUMAN.equals(session.getStatus()))
        {
            throw new ServiceException("\u5f53\u524d\u4f1a\u8bdd\u4e0d\u5728\u4eba\u5de5\u63a5\u5f85\u72b6\u6001");
        }
        String name = StringUtils.isNotEmpty(agent.getNickName()) ? agent.getNickName() : agentName;
        return saveMessage(sessionId, MallChatMessage.SENDER_AGENT, agentUserId, name, content.trim(), msgType);
    }

    @Override
    @Transactional
    public MallChatSession transferToHuman(Long userId, Long sessionId)
    {
        MallChatSession session = requireSession(sessionId);
        assertAccess(session, userId, false);
        ensureOpen(session);
        if (MallChatSession.STATUS_HUMAN.equals(session.getStatus()))
        {
            return session;
        }
        if (MallChatSession.STATUS_WAITING.equals(session.getStatus()))
        {
            tryAssignSession(session);
            session = refreshSession(sessionId);
            if (MallChatSession.STATUS_HUMAN.equals(session.getStatus()))
            {
                return session;
            }
            return session;
        }
        List<MallChatAgent> agents = agentMapper.selectAvailableAgents();
        if (agents == null || agents.isEmpty())
        {
            return session;
        }
        MallChatSession update = new MallChatSession();
        update.setSessionId(sessionId);
        update.setStatus(MallChatSession.STATUS_WAITING);
        sessionMapper.updateMallChatSession(update);
        saveMessage(sessionId, MallChatMessage.SENDER_SYSTEM, null, "\u7cfb\u7edf",
            "\u6b63\u5728\u4e3a\u60a8\u5206\u914d\u4eba\u5de5\u5ba2\u670d\uff0c\u8bf7\u7a0d\u5019\u2026", MallChatMessage.MSG_TEXT);
        session = refreshSession(sessionId);
        tryAssignSession(session);
        return refreshSession(sessionId);
    }

    @Override
    @Transactional
    public int closeSession(Long sessionId, Long operatorUserId, boolean admin)
    {
        MallChatSession session = requireSession(sessionId);
        if (!admin)
        {
            assertAccess(session, operatorUserId, false);
        }
        if (MallChatSession.STATUS_CLOSED.equals(session.getStatus()))
        {
            return 1;
        }
        if (session.getAgentId() != null)
        {
            agentMapper.decreaseActiveSessions(session.getAgentId());
        }
        MallChatSession update = new MallChatSession();
        update.setSessionId(sessionId);
        update.setStatus(MallChatSession.STATUS_CLOSED);
        sessionMapper.updateMallChatSession(update);
        saveMessage(sessionId, MallChatMessage.SENDER_SYSTEM, null, "\u7cfb\u7edf", "\u4f1a\u8bdd\u5df2\u7ed3\u675f\uff0c\u611f\u8c22\u54a8\u8be2\u3002",
            MallChatMessage.MSG_TEXT);
        return 1;
    }

    @Override
    public void tryAssignWaitingSessions()
    {
        List<MallChatSession> waiting = sessionMapper.selectWaitingSessions();
        for (MallChatSession session : waiting)
        {
            tryAssignSession(session);
        }
    }

    private void tryAssignSession(MallChatSession session)
    {
        if (session == null || !MallChatSession.STATUS_WAITING.equals(session.getStatus()))
        {
            return;
        }
        List<MallChatAgent> agents = agentMapper.selectAvailableAgents();
        if (agents == null || agents.isEmpty())
        {
            return;
        }
        assignAgentToSession(session, agents.get(0));
    }

    private void assignAgentToSession(MallChatSession session, MallChatAgent agent)
    {
        MallChatSession update = new MallChatSession();
        update.setSessionId(session.getSessionId());
        update.setAgentId(agent.getAgentId());
        update.setAgentName(StringUtils.isNotEmpty(agent.getNickName()) ? agent.getNickName() : agent.getUserName());
        update.setStatus(MallChatSession.STATUS_HUMAN);
        sessionMapper.updateMallChatSession(update);
        agentMapper.increaseActiveSessions(agent.getAgentId());
        saveMessage(session.getSessionId(), MallChatMessage.SENDER_SYSTEM, null, "\u7cfb\u7edf",
            "\u5ba2\u670d " + update.getAgentName() + " \u5df2\u4e3a\u60a8\u670d\u52a1\uff0c\u8bf7\u63d0\u51fa\u60a8\u7684\u95ee\u9898\u3002",
            MallChatMessage.MSG_TEXT);
    }

    private MallChatMessage saveMessage(Long sessionId, String senderType, Long senderId, String senderName,
        String content, String msgType)
    {
        MallChatMessage message = new MallChatMessage();
        message.setSessionId(sessionId);
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setMsgType(normalizeMsgType(msgType));
        message.setContent(content);
        messageMapper.insertMallChatMessage(message);
        MallChatSession touch = new MallChatSession();
        touch.setSessionId(sessionId);
        touch.setLastMessage(previewMessage(content, message.getMsgType()));
        touch.setLastMessageTime(new Date());
        sessionMapper.updateMallChatSession(touch);
        return message;
    }

    private String normalizeMsgType(String msgType)
    {
        if (StringUtils.isEmpty(msgType))
        {
            return MallChatMessage.MSG_TEXT;
        }
        return msgType;
    }

    private boolean isMediaMessage(String msgType)
    {
        return MallChatMessage.MSG_IMAGE.equals(msgType) || MallChatMessage.MSG_VIDEO.equals(msgType);
    }

    private String previewMessage(String content, String msgType)
    {
        if (MallChatMessage.MSG_IMAGE.equals(msgType))
        {
            return "[\u56fe\u7247]";
        }
        if (MallChatMessage.MSG_VIDEO.equals(msgType))
        {
            return "[\u89c6\u9891]";
        }
        return content.length() > 200 ? content.substring(0, 200) : content;
    }

    private MallChatSession refreshSession(Long sessionId)
    {
        return sessionMapper.selectMallChatSessionById(sessionId);
    }

    /** AI replies until a human agent has taken the session. */
    private boolean shouldAiReply(MallChatSession session)
    {
        if (session == null)
        {
            return false;
        }
        if (MallChatSession.STATUS_HUMAN.equals(session.getStatus())
            || MallChatSession.STATUS_CLOSED.equals(session.getStatus()))
        {
            return false;
        }
        return true;
    }

    private boolean shouldTransfer(String content, MallChatConfig config)
    {
        if (config == null || StringUtils.isEmpty(config.getTransferKeywords()))
        {
            return content.contains("\u8f6c\u4eba\u5de5") || content.contains("\u4eba\u5de5");
        }
        return Arrays.stream(config.getTransferKeywords().split("[,\uff0c]"))
            .map(String::trim)
            .filter(StringUtils::isNotEmpty)
            .anyMatch(content::contains);
    }

    private MallChatSession requireSession(Long sessionId)
    {
        MallChatSession session = sessionMapper.selectMallChatSessionById(sessionId);
        if (session == null)
        {
            throw new ServiceException(MSG_SESSION);
        }
        return session;
    }

    private MallChatAgent requireAgent(Long userId)
    {
        MallChatAgent agent = agentMapper.selectMallChatAgentByUserId(userId);
        if (agent == null)
        {
            throw new ServiceException(MSG_AGENT);
        }
        return agent;
    }

    private void assertAccess(MallChatSession session, Long userId, boolean admin)
    {
        if (admin)
        {
            return;
        }
        if (!session.getUserId().equals(userId))
        {
            throw new ServiceException(MSG_FORBIDDEN);
        }
    }

    private void ensureOpen(MallChatSession session)
    {
        if (MallChatSession.STATUS_CLOSED.equals(session.getStatus()))
        {
            throw new ServiceException(MSG_CLOSED);
        }
    }
}
