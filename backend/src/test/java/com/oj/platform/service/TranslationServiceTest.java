package com.oj.platform.service;

import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranslationServiceTest {

    private TranslationServiceImpl translationService;

    @BeforeEach
    void setUp() {
        translationService = new TranslationServiceImpl();
    }

    @Test
    void testEnglishToEnglish_ReturnsOriginalImmediately() {
        TranslationRequest request = new TranslationRequest("en", List.of("Dashboard", "Problems", "Contests"));
        TranslationResponse response = translationService.translate(request);

        assertNotNull(response);
        assertEquals("en", response.getTargetLanguage());
        assertEquals(3, response.getTranslations().size());
        assertEquals("Dashboard", response.getTranslations().get(0));
        assertEquals("Problems", response.getTranslations().get(1));
        assertEquals("Contests", response.getTranslations().get(2));
    }

    @Test
    void testEmptyRequest_ReturnsEmptyList() {
        TranslationRequest request = new TranslationRequest("fr", List.of());
        TranslationResponse response = translationService.translate(request);

        assertNotNull(response);
        assertTrue(response.getTranslations().isEmpty());
    }

    @Test
    void testWithoutApiKey_GracefulFallbackReturnsOriginals() {
        TranslationRequest request = new TranslationRequest("fr", List.of("Dashboard", "Problems"));
        TranslationResponse response = translationService.translate(request);

        assertNotNull(response);
        assertEquals(2, response.getTranslations().size());
        // In absence of API key, must not throw an exception, but gracefully fall back
        assertEquals("Dashboard", response.getTranslations().get(0));
        assertEquals("Problems", response.getTranslations().get(1));
    }

    @Test
    void testTranslateSingle_NullOrEnglish() {
        assertEquals("Hello", translationService.translateSingle("Hello", "en"));
        assertNull(translationService.translateSingle(null, "fr"));
        assertEquals("", translationService.translateSingle("", "fr"));
    }
}
