package com.oj.platform.dto;

import java.util.Collections;
import java.util.List;

public class TranslationResponse {

    private String translatedText;
    private List<String> translations;
    private String sourceLanguage = "EN";
    private String targetLanguage;

    public TranslationResponse() {
    }

    public TranslationResponse(String translatedText) {
        this.translatedText = translatedText;
        this.translations = translatedText != null ? List.of(translatedText) : Collections.emptyList();
    }

    public TranslationResponse(String translatedText, String targetLanguage) {
        this.translatedText = translatedText;
        this.targetLanguage = targetLanguage;
        this.translations = translatedText != null ? List.of(translatedText) : Collections.emptyList();
    }

    public TranslationResponse(String targetLanguage, List<String> translations) {
        this.targetLanguage = targetLanguage;
        this.translations = translations;
        if (translations != null && !translations.isEmpty()) {
            this.translatedText = translations.get(0);
        }
    }

    public TranslationResponse(String translatedText, List<String> translations, String sourceLanguage, String targetLanguage) {
        this.translatedText = translatedText;
        this.translations = translations;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public List<String> getTranslations() {
        return translations;
    }

    public void setTranslations(List<String> translations) {
        this.translations = translations;
        if (this.translatedText == null && translations != null && !translations.isEmpty()) {
            this.translatedText = translations.get(0);
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
