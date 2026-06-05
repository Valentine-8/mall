package com.ruoyi.mall.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class MallKnowledgeDoc extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String STATUS_PROCESSING = "0";
    public static final String STATUS_OK = "1";
    public static final String STATUS_FAIL = "2";

    private Long docId;
    private String title;
    private String fileName;
    private String fileType;
    private String filePath;
    private String status;
    private Integer chunkCount;
    private String errorMsg;
    private String delFlag;

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
