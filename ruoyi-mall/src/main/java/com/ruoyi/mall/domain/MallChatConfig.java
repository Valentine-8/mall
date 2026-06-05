package com.ruoyi.mall.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class MallChatConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;
    private String aiEnabled;
    private String aiApiUrl;
    private String aiApiKey;
    private String aiModel;
    private String systemPrompt;
    private String welcomeMessage;
    private String transferKeywords;
    private String kbEnabled;
    private String embeddingModel;
    private Integer ragTopK;
    private Integer chunkSize;
    private Integer chunkOverlap;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public String getAiEnabled() { return aiEnabled; }
    public void setAiEnabled(String aiEnabled) { this.aiEnabled = aiEnabled; }
    public String getAiApiUrl() { return aiApiUrl; }
    public void setAiApiUrl(String aiApiUrl) { this.aiApiUrl = aiApiUrl; }
    public String getAiApiKey() { return aiApiKey; }
    public void setAiApiKey(String aiApiKey) { this.aiApiKey = aiApiKey; }
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
    public String getTransferKeywords() { return transferKeywords; }
    public void setTransferKeywords(String transferKeywords) { this.transferKeywords = transferKeywords; }
    public String getKbEnabled() { return kbEnabled; }
    public void setKbEnabled(String kbEnabled) { this.kbEnabled = kbEnabled; }
    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
    public Integer getRagTopK() { return ragTopK; }
    public void setRagTopK(Integer ragTopK) { this.ragTopK = ragTopK; }
    public Integer getChunkSize() { return chunkSize; }
    public void setChunkSize(Integer chunkSize) { this.chunkSize = chunkSize; }
    public Integer getChunkOverlap() { return chunkOverlap; }
    public void setChunkOverlap(Integer chunkOverlap) { this.chunkOverlap = chunkOverlap; }
}
