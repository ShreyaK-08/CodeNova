package com.oj.platform.service;

import com.oj.platform.dto.DeepLLanguageDto;
import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompositeTranslationServiceTest {

    @Mock
    private DeepLTranslationProvider deepLProvider;

    @Mock
    private FallbackTranslationProvider fallbackProvider;

    private CompositeTranslationService compositeService;

    @BeforeEach
    void setUp() {
        when(deepLProvider.getSupportedLanguages()).thenReturn(List.of(
                new DeepLLanguageDto("en", "English", "English", "ltr"),
                new DeepLLanguageDto("de", "German", "Deutsch", "ltr"),
                new DeepLLanguageDto("es", "Spanish", "Español", "ltr")
        ));
        when(fallbackProvider.getSupportedLanguages()).thenReturn(List.of(
                new DeepLLanguageDto("hi", "Hindi", "हिन्दी", "ltr"),
                new DeepLLanguageDto("kn", "Kannada", "ಕನ್ನಡ", "ltr"),
                new DeepLLanguageDto("ta", "Tamil", "தமிழ்", "ltr")
        ));

        compositeService = new CompositeTranslationService(deepLProvider, fallbackProvider);
    }

    @Test
    @DisplayName("Aggregates languages from both DeepL and Fallback providers")
    void testSupportedLanguagesAggregation() {
        List<DeepLLanguageDto> langs = compositeService.getSupportedLanguages();
        assertNotNull(langs);
        assertEquals(6, langs.size());
        assertTrue(langs.stream().anyMatch(l -> "de".equals(l.getCode())));
        assertTrue(langs.stream().anyMatch(l -> "kn".equals(l.getCode())));
    }

    @Test
    @DisplayName("Routes DeepL-supported languages to DeepL provider")
    void testRoutesToDeepL() {
        when(deepLProvider.supportsLanguage("de")).thenReturn(true);
        TranslationResponse mockResp = new TranslationResponse("Hallo", List.of("Hallo"), "EN", "DE");
        when(deepLProvider.translate(any(TranslationRequest.class))).thenReturn(mockResp);

        TranslationRequest request = new TranslationRequest("Hello", "EN", "DE");
        TranslationResponse response = compositeService.translate(request);

        assertNotNull(response);
        assertEquals("Hallo", response.getTranslatedText());
        verify(deepLProvider, times(1)).translate(any(TranslationRequest.class));
        verify(fallbackProvider, never()).translate(any(TranslationRequest.class));
    }

    @Test
    @DisplayName("Routes Indic and other non-DeepL languages to Fallback provider")
    void testRoutesToFallback() {
        when(deepLProvider.supportsLanguage("kn")).thenReturn(false);
        TranslationResponse mockResp = new TranslationResponse("ನಮಸ್ಕಾರ", List.of("ನಮಸ್ಕಾರ"), "en", "kn");
        when(fallbackProvider.translate(any(TranslationRequest.class))).thenReturn(mockResp);

        TranslationRequest request = new TranslationRequest("Hello", "en", "kn");
        TranslationResponse response = compositeService.translate(request);

        assertNotNull(response);
        assertEquals("ನಮಸ್ಕಾರ", response.getTranslatedText());
        verify(deepLProvider, never()).translate(any(TranslationRequest.class));
        verify(fallbackProvider, times(1)).translate(any(TranslationRequest.class));
    }

    @Test
    @DisplayName("Status endpoint returns aggregated metrics safely without credentials")
    void testSafeStatusReporting() {
        when(deepLProvider.getStatus()).thenReturn(Map.of("provider", "DeepL API", "configured", false));
        when(fallbackProvider.getStatus()).thenReturn(Map.of("provider", "Cloud / Fallback", "configured", false));

        Map<String, Object> status = compositeService.getStatus();
        assertNotNull(status);
        assertEquals("Multi-Provider Composite (DeepL + Cloud/Fallback)", status.get("activeArchitecture"));
        assertEquals(6, status.get("totalSupportedLanguages"));
        assertTrue((Boolean) status.get("isOperational"));
    }
}
