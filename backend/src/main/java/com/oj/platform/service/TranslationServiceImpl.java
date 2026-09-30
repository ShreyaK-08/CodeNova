package com.oj.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TranslationServiceImpl implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationServiceImpl.class);

    private static final String GOOGLE_TRANSLATE_V2_URL = "https://translation.googleapis.com/language/translate/v2";

    @Value("${app.google.translation.api-key:${GOOGLE_TRANSLATION_API_KEY:}}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Thread-safe in-memory cache: "targetLang::originalText" -> "translatedText"
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public TranslationServiceImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(12000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public TranslationResponse translate(TranslationRequest request) {
        if (request == null || request.getTexts() == null || request.getTexts().isEmpty()) {
            return new TranslationResponse(
                    request != null ? request.getTargetLanguage() : "en",
                    Collections.emptyList()
            );
        }

        String targetLang = request.getTargetLanguage() != null ? request.getTargetLanguage().trim().toLowerCase() : "en";
        String sourceLang = request.getSourceLanguage() != null && !request.getSourceLanguage().trim().isEmpty()
                ? request.getSourceLanguage().trim().toLowerCase()
                : "en";

        List<String> inputTexts = request.getTexts();

        // If target is English and source is English, return directly
        if ("en".equals(targetLang) && "en".equals(sourceLang)) {
            return new TranslationResponse(targetLang, new ArrayList<>(inputTexts));
        }

        List<String> resultTranslations = new ArrayList<>(inputTexts.size());
        List<String> uncachedTexts = new ArrayList<>();
        List<Integer> uncachedIndices = new ArrayList<>();

        // 1. Check in-memory cache
        for (int i = 0; i < inputTexts.size(); i++) {
            String text = inputTexts.get(i);
            if (text == null || text.trim().isEmpty()) {
                resultTranslations.add(text != null ? text : "");
                continue;
            }

            String cacheKey = targetLang + "::" + text;
            String cached = cache.get(cacheKey);

            if (cached != null) {
                resultTranslations.add(cached);
            } else {
                resultTranslations.add(null); // placeholder
                uncachedTexts.add(text);
                uncachedIndices.add(i);
            }
        }

        // 2. If there are uncached texts, translate them in batches
        if (!uncachedTexts.isEmpty()) {
            List<String> translatedBatch = fetchFromGoogleOrFallback(uncachedTexts, targetLang, sourceLang);
            for (int k = 0; k < uncachedTexts.size(); k++) {
                int originalIndex = uncachedIndices.get(k);
                String translated = (k < translatedBatch.size() && translatedBatch.get(k) != null)
                        ? translatedBatch.get(k)
                        : uncachedTexts.get(k);

                // Store in cache
                String cacheKey = targetLang + "::" + uncachedTexts.get(k);
                cache.put(cacheKey, translated);

                resultTranslations.set(originalIndex, translated);
            }
        }

        return new TranslationResponse(targetLang, resultTranslations);
    }

    @Override
    public String translateSingle(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty() || targetLanguage == null || "en".equalsIgnoreCase(targetLanguage)) {
            return text;
        }

        String cacheKey = targetLanguage.trim().toLowerCase() + "::" + text;
        String cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        TranslationResponse resp = translate(new TranslationRequest(targetLanguage, List.of(text)));
        if (resp.getTranslations() != null && !resp.getTranslations().isEmpty()) {
            return resp.getTranslations().get(0);
        }
        return text;
    }

    private List<String> fetchFromGoogleOrFallback(List<String> texts, String targetLang, String sourceLang) {
        if (apiKey != null && !apiKey.trim().isEmpty() && !"none".equalsIgnoreCase(apiKey.trim())) {
            try {
                return callGoogleTranslationApi(texts, targetLang, sourceLang);
            } catch (Exception e) {
                log.warn("Google Cloud Translation API request failed: {}. Falling back gracefully.", e.getMessage());
            }
        } else {
            log.debug("GOOGLE_TRANSLATION_API_KEY is not configured. Serving fallback/original text for target '{}'.", targetLang);
        }

        // Fallback: return original texts without crashing
        return new ArrayList<>(texts);
    }

    private List<String> callGoogleTranslationApi(List<String> texts, String targetLang, String sourceLang) throws Exception {
        String url = GOOGLE_TRANSLATE_V2_URL + "?key=" + apiKey.trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Google Cloud Translation v2 accepts multiple "q" values
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("q", texts);
        requestBody.put("target", targetLang);
        if (sourceLang != null && !sourceLang.isEmpty()) {
            requestBody.put("source", sourceLang);
        }
        requestBody.put("format", "text");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode translationsArr = root.path("data").path("translations");

            if (translationsArr.isArray()) {
                List<String> result = new ArrayList<>();
                for (int i = 0; i < translationsArr.size(); i++) {
                    String translatedText = translationsArr.get(i).path("translatedText").asText();
                    // Unescape any HTML entities like &#39;, &quot;, &amp;
                    String unescaped = HtmlUtils.htmlUnescape(translatedText);
                    result.add(unescaped);
                }
                return result;
            }
        }

        log.warn("Unexpected response structure from Google Cloud Translation API: {}", response.getBody());
        return new ArrayList<>(texts);
    }

    @Override
    public String translate(String text, String targetLanguage) {
        return translateSingle(text, targetLanguage);
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        return translateSingle(text, targetLanguage);
    }

    @Override
    public List<com.oj.platform.dto.DeepLLanguageDto> getSupportedLanguages() {
        return List.of();
    }

    @Override
    public Map<String, Object> getStatus() {
        return Map.of("provider", "Legacy", "configured", isConfigured());
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !"none".equalsIgnoreCase(apiKey.trim());
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
