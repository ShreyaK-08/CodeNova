package com.oj.platform.service;

import com.oj.platform.dto.DeepLLanguageDto;
import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;
import com.oj.platform.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Composite Multi-Provider Translation Service.
 * Unifies DeepL (for European and East Asian high-accuracy translation) and
 * Cloud / Fallback providers (for Indic and global regional languages) to support 100+ languages
 * without hardcoding or leaking API keys.
 */
@Service
@Primary
public class CompositeTranslationService implements TranslationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompositeTranslationService.class);

    private final DeepLTranslationProvider deepLProvider;
    private final FallbackTranslationProvider fallbackProvider;

    private final List<DeepLLanguageDto> aggregatedLanguages;
    private final Map<String, DeepLLanguageDto> languageLookup = new HashMap<>();

    @Autowired
    public CompositeTranslationService(DeepLTranslationProvider deepLProvider,
                                       FallbackTranslationProvider fallbackProvider) {
        this.deepLProvider = deepLProvider;
        this.fallbackProvider = fallbackProvider;

        // Aggregate languages union (DeepL + Fallback = 105+ unique languages)
        Map<String, DeepLLanguageDto> combined = new LinkedHashMap<>();
        for (DeepLLanguageDto dto : deepLProvider.getSupportedLanguages()) {
            combined.put(dto.getCode().toLowerCase(), dto);
        }
        for (DeepLLanguageDto dto : fallbackProvider.getSupportedLanguages()) {
            if (!combined.containsKey(dto.getCode().toLowerCase())) {
                combined.put(dto.getCode().toLowerCase(), dto);
            }
        }
        this.aggregatedLanguages = new ArrayList<>(combined.values());
        for (DeepLLanguageDto dto : this.aggregatedLanguages) {
            this.languageLookup.put(dto.getCode().toLowerCase(), dto);
        }

        if (this.aggregatedLanguages.size() < 100) {
            log.error("CompositeTranslationService validation notice: supported languages count is {} (expected >= 100).", this.aggregatedLanguages.size());
        } else {
            log.info("CompositeTranslationService initialized with {} supported languages (multi-provider: DeepL + Fallback).", this.aggregatedLanguages.size());
        }
    }

    @Override
    public TranslationResponse translate(TranslationRequest request) {
        if (request == null) {
            throw new BadRequestException("Translation request cannot be null");
        }
        String target = request.getTargetLanguage();
        if (target == null || target.trim().isEmpty()) {
            throw new BadRequestException("Target language is required");
        }

        String normalizedTarget = target.trim().toLowerCase();

        // 1. If supported by DeepL and DeepL is configured, prioritize DeepL
        if (deepLProvider.supportsLanguage(normalizedTarget)) {
            try {
                return deepLProvider.translate(request);
            } catch (BadRequestException e) {
                // If DeepL rejects (e.g. length), propagate or pass to fallback
                if (e.getMessage() != null && e.getMessage().contains("Unsupported target language")) {
                    return fallbackProvider.translate(request);
                }
                throw e;
            } catch (Exception e) {
                return fallbackProvider.translate(request);
            }
        }

        // 2. Otherwise route to fallback / cloud provider (e.g. Hindi, Kannada, Tamil, etc.)
        return fallbackProvider.translate(request);
    }

    @Override
    public String translateSingle(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty() || targetLanguage == null || "en".equalsIgnoreCase(targetLanguage)) {
            return text;
        }
        TranslationResponse resp = translate(new TranslationRequest(text, "en", targetLanguage));
        return resp.getTranslatedText() != null ? resp.getTranslatedText() : text;
    }

    @Override
    public String translate(String text, String targetLanguage) {
        return translateSingle(text, targetLanguage);
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.trim().isEmpty() || targetLanguage == null) {
            return text;
        }
        TranslationResponse resp = translate(new TranslationRequest(text, sourceLanguage, targetLanguage));
        return resp.getTranslatedText() != null ? resp.getTranslatedText() : text;
    }

    @Override
    public List<DeepLLanguageDto> getSupportedLanguages() {
        return Collections.unmodifiableList(aggregatedLanguages);
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("activeArchitecture", "Multi-Provider Composite (DeepL + Cloud/Fallback)");
        status.put("deeplProvider", deepLProvider.getStatus());
        status.put("fallbackProvider", fallbackProvider.getStatus());
        status.put("totalSupportedLanguages", aggregatedLanguages.size());
        status.put("isOperational", true);
        return status;
    }

    @Override
    public boolean isConfigured() {
        return deepLProvider.isConfigured() || fallbackProvider.isConfigured();
    }

    @Override
    public void clearCache() {
        deepLProvider.clearCache();
        fallbackProvider.clearCache();
    }
}
