package com.aidsa.platform.dto;

public class SubmissionRequest {

    private Long problemId;
    private String language;
    private String code;

    public SubmissionRequest() {
    }

    public SubmissionRequest(
            Long problemId,
            String language,
            String code
    ) {
        this.problemId = problemId;
        this.language = language;
        this.code = code;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}