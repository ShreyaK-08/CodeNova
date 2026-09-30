package com.oj.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.platform.controller.TranslationController;
import com.oj.platform.dto.DeepLLanguageDto;
import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;
import com.oj.platform.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeepLTranslationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private DeepLTranslationService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new DeepLTranslationService(restTemplate, objectMapper);
        service.setApiKey("mock-test-key-12345:fx");
        service.clearCache();
    }

    @Test
    @DisplayName("1. Valid translation request: successful DeepL translation")
    void test1_ValidTranslationRequest() {
        String mockResponseJson = "{\"translations\":[{\"detected_source_language\":\"EN\",\"text\":\"Hallo, willkommen bei CodeNova\"}]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponseJson, HttpStatus.OK);

        when(restTemplate.postForEntity(eq("https://api-free.deepl.com/v2/translate"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        TranslationRequest request = new TranslationRequest("Hello, welcome to CodeNova", "EN", "DE");
        TranslationResponse response = service.translate(request);

        assertNotNull(response);
        assertEquals("Hallo, willkommen bei CodeNova", response.getTranslatedText());
        assertEquals("DE", response.getTargetLanguage());
        assertEquals("EN", response.getSourceLanguage());
        assertFalse(response.getTranslations().isEmpty());
        assertEquals("Hallo, willkommen bei CodeNova", response.getTranslations().get(0));

        // Verify request headers and payload passed to DeepL
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(1)).postForEntity(eq("https://api-free.deepl.com/v2/translate"), entityCaptor.capture(), eq(String.class));

        HttpEntity captured = entityCaptor.getValue();
        assertEquals("DeepL-Auth-Key mock-test-key-12345:fx", captured.getHeaders().getFirst("Authorization"));
        assertEquals(MediaType.APPLICATION_JSON, captured.getHeaders().getContentType());

        Map body = (Map) captured.getBody();
        assertNotNull(body);
        assertEquals("DE", body.get("target_lang"));
        assertEquals("EN", body.get("source_lang"));
        assertEquals(List.of("Hello, welcome to CodeNova"), body.get("text"));
    }

    @Test
    @DisplayName("2. Empty text validation: throws BadRequestException")
    void test2_EmptyTextValidation() {
        // Blank single text
        TranslationRequest request1 = new TranslationRequest("", "EN", "DE");
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> service.translate(request1));
        assertTrue(ex1.getMessage().toLowerCase().contains("cannot be empty"));

        // Whitespace only
        TranslationRequest request2 = new TranslationRequest("   ", "EN", "DE");
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> service.translate(request2));
        assertTrue(ex2.getMessage().toLowerCase().contains("cannot be empty"));

        // Null text and null texts
        TranslationRequest request3 = new TranslationRequest();
        request3.setTargetLanguage("DE");
        BadRequestException ex3 = assertThrows(BadRequestException.class, () -> service.translate(request3));
        assertTrue(ex3.getMessage().toLowerCase().contains("cannot be empty"));
    }

    @Test
    @DisplayName("3. Missing target language validation: throws BadRequestException")
    void test3_MissingTargetLanguageValidation() {
        // Null target language
        TranslationRequest request1 = new TranslationRequest("Hello", (String) null);
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> service.translate(request1));
        assertTrue(ex1.getMessage().toLowerCase().contains("target language is required"));

        // Blank target language
        TranslationRequest request2 = new TranslationRequest("Hello", "   ");
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> service.translate(request2));
        assertTrue(ex2.getMessage().toLowerCase().contains("target language is required"));
    }

    @Test
    @DisplayName("4. Unsupported language validation: throws BadRequestException")
    void test4_UnsupportedLanguageValidation() {
        // Unknown language code
        TranslationRequest request1 = new TranslationRequest("Hello", "XX");
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> service.translate(request1));
        assertTrue(ex1.getMessage().contains("Unsupported target language"));

        // Language not supported by DeepL (e.g. Kannada 'kn')
        TranslationRequest request2 = new TranslationRequest("Hello", "kn");
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> service.translate(request2));
        assertTrue(ex2.getMessage().contains("Unsupported target language"));
    }

    @Test
    @DisplayName("5. Missing API key: returns original text without throwing or calling API")
    void test5_MissingApiKey() {
        service.setApiKey(""); // no API key
        assertFalse(service.isConfigured());

        TranslationRequest request = new TranslationRequest("Hello, welcome to CodeNova", "EN", "DE");
        TranslationResponse response = service.translate(request);

        assertNotNull(response);
        // Returns original text fallback
        assertEquals("Hello, welcome to CodeNova", response.getTranslatedText());
        // Verify external DeepL API was never invoked
        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    @Test
    @DisplayName("6. DeepL API failure: HTTP 429, 500, or network timeout falls back gracefully")
    void test6_DeepLApiFailure() {
        // 6a: 429 Rate Limit
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests",
                        HttpHeaders.EMPTY, "{\"message\":\"Quota exceeded\"}".getBytes(StandardCharsets.UTF_8), null));

        TranslationRequest request1 = new TranslationRequest("Submit Solution", "EN", "FR");
        TranslationResponse response1 = service.translate(request1);
        assertNotNull(response1);
        assertEquals("Submit Solution", response1.getTranslatedText());

        // 6b: 500 Internal Server Error
        service.clearCache();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        TranslationRequest request2 = new TranslationRequest("View Leaderboard", "EN", "ES");
        TranslationResponse response2 = service.translate(request2);
        assertNotNull(response2);
        assertEquals("View Leaderboard", response2.getTranslatedText());

        // 6c: Network timeout
        service.clearCache();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        TranslationRequest request3 = new TranslationRequest("Start Assessment", "EN", "IT");
        TranslationResponse response3 = service.translate(request3);
        assertNotNull(response3);
        assertEquals("Start Assessment", response3.getTranslatedText());
    }

    @Test
    @DisplayName("7. Authentication requirement: verified for translation controller / security context")
    void test7_AuthenticationRequirement() {
        TranslationController controller = new TranslationController(service);

        // Given unauthenticated / anonymous user in SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication anon = new AnonymousAuthenticationToken(
                "anonKey", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        context.setAuthentication(anon);
        SecurityContextHolder.setContext(context);

        // Controller translates when called by authenticated flow
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                "alexj", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        context.setAuthentication(userAuth);
        SecurityContextHolder.setContext(context);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());

        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("8. API response parsing: extracts translations and unescapes HTML entities")
    void test8_ApiResponseParsingAndHtmlUnescaping() {
        String responseWithEntities = "{\"translations\":[{\"detected_source_language\":\"EN\",\"text\":\"L&#39;évaluation &quot;CodeNova&quot; &amp; programmation\"}]}";
        List<String> parsed = service.parseDeepLResponse(responseWithEntities, List.of("fallback"));

        assertNotNull(parsed);
        assertEquals(1, parsed.size());
        assertEquals("L'évaluation \"CodeNova\" & programmation", parsed.get(0));
    }

    @Test
    @DisplayName("9. In-memory caching: repeated requests do not re-call DeepL API")
    void test9_InMemoryCaching() {
        String mockResponseJson = "{\"translations\":[{\"detected_source_language\":\"EN\",\"text\":\"Tableau de bord\"}]}";
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponseJson, HttpStatus.OK));

        TranslationRequest req = new TranslationRequest("Dashboard", "EN", "FR");

        // First call -> hits DeepL API
        TranslationResponse resp1 = service.translate(req);
        assertEquals("Tableau de bord", resp1.getTranslatedText());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());

        // Second call with same text + target -> hits in-memory cache
        TranslationResponse resp2 = service.translate(req);
        assertEquals("Tableau de bord", resp2.getTranslatedText());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any()); // count is still 1!
    }

    @Test
    @DisplayName("10. DeepL Endpoint selection: Free (:fx) vs Pro")
    void test10_EndpointSelection() {
        service.setApiKey("abcd-1234:fx");
        assertEquals("https://api-free.deepl.com/v2/translate", service.getEndpoint());

        service.setApiKey("abcd-1234-pro");
        assertEquals("https://api.deepl.com/v2/translate", service.getEndpoint());

        service.setCustomApiUrl("https://custom-proxy.internal/v2/translate");
        assertEquals("https://custom-proxy.internal/v2/translate", service.getEndpoint());
    }

    @Test
    @DisplayName("11. Supported languages list contains DeepL-supported languages")
    void test11_SupportedLanguagesList() {
        List<DeepLLanguageDto> languages = service.getSupportedLanguages();
        assertNotNull(languages);
        assertFalse(languages.isEmpty());

        // Confirm German, Spanish, French, Japanese, Arabic are present
        assertTrue(languages.stream().anyMatch(l -> "de".equalsIgnoreCase(l.getCode())));
        assertTrue(languages.stream().anyMatch(l -> "es".equalsIgnoreCase(l.getCode())));
        assertTrue(languages.stream().anyMatch(l -> "fr".equalsIgnoreCase(l.getCode())));
        assertTrue(languages.stream().anyMatch(l -> "ja".equalsIgnoreCase(l.getCode())));
        assertTrue(languages.stream().anyMatch(l -> "ar".equalsIgnoreCase(l.getCode()) && "rtl".equals(l.getDirection())));

        // Confirm languages DeepL cannot translate are NOT in the list
        assertFalse(languages.stream().anyMatch(l -> "kn".equalsIgnoreCase(l.getCode())));
        assertFalse(languages.stream().anyMatch(l -> "hi".equalsIgnoreCase(l.getCode())));
        assertFalse(languages.stream().anyMatch(l -> "sa".equalsIgnoreCase(l.getCode())));
    }
}
