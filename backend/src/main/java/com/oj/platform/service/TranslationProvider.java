package com.oj.platform.service;

import com.oj.platform.dto.DeepLLanguageDto;
import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;

import java.util.List;
import java.util.Map;

/**
 * Provider interface for language translation engines.
 * Enables CodeNova to support 100+ languages through a multi-provider architecture:
 * DeepL for high-fidelity European & East Asian languages, and Fallback providers for
 * Indic and global languages not directly supported by DeepL.
 */
public interface TranslationProvider {

    /** The human-readable name of this translation engine (e.g., "DeepL API", "Cloud / Fallback"). */
    String getProviderName();

    /** Whether the required API key / configuration is present. */
    boolean isConfigured();

    /** Whether this provider can translate to the specified target language code. */
    boolean supportsLanguage(String targetLanguageCode);

    /** Executes translation for the given request. */
    TranslationResponse translate(TranslationRequest request);

    /** Returns all languages natively handled by this provider. */
    List<DeepLLanguageDto> getSupportedLanguages();

    /** Returns safe status information (never leaks credentials). */
    Map<String, Object> getStatus();
}
