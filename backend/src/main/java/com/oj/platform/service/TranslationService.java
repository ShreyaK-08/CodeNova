package com.oj.platform.service;

import com.oj.platform.dto.DeepLLanguageDto;
import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;

import java.util.List;
import java.util.Map;

public interface TranslationService {
    TranslationResponse translate(TranslationRequest request);
    String translateSingle(String text, String targetLanguage);
    String translate(String text, String targetLanguage);
    String translate(String text, String sourceLanguage, String targetLanguage);
    List<DeepLLanguageDto> getSupportedLanguages();
    Map<String, Object> getStatus();
    boolean isConfigured();
    void clearCache();
}
