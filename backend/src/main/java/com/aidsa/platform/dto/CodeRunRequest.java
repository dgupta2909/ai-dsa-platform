package com.aidsa.platform.dto;

public class CodeRunRequest {

    private String language;
    private String code;

    public CodeRunRequest() {
    }

    public CodeRunRequest(String language, String code) {
        this.language = language;
        this.code = code;
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