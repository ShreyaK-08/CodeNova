package com.oj.platform.dto;

import java.util.List;

public class TranslationRequest {

    private String text;
    private List<String> texts;
    private String sourceLanguage = "EN";
    private String targetLanguage;

    public TranslationRequest() {
    }

    public TranslationRequest(String text, String targetLanguage) {
        this.text = text;
        this.targetLanguage = targetLanguage;
        this.sourceLanguage = "EN";
    }

    public TranslationRequest(String text, String sourceLanguage, String targetLanguage) {
        this.text = text;
        this.sourceLanguage = sourceLanguage != null ? sourceLanguage : "EN";
        this.targetLanguage = targetLanguage;
    }

    public TranslationRequest(String targetLanguage, List<String> texts) {
        this.targetLanguage = targetLanguage;
        this.texts = texts;
        this.sourceLanguage = "EN";
        if (texts != null && !texts.isEmpty()) {
            this.text = texts.get(0);
        }
    }

    public TranslationRequest(String targetLanguage, String sourceLanguage, List<String> texts) {
        this.targetLanguage = targetLanguage;
        this.sourceLanguage = sourceLanguage != null ? sourceLanguage : "EN";
        this.texts = texts;
        if (texts != null && !texts.isEmpty()) {
            this.text = texts.get(0);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
        if (this.text == null && texts != null && !texts.isEmpty()) {
            this.text = texts.get(0);
        }
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
