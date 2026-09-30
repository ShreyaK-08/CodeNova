package com.oj.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.platform.dto.DeepLLanguageDto;
import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;
import com.oj.platform.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DeepL Translation Provider.
 * Connects to DeepL API (Free or Pro) for high-accuracy neural machine translation
 * across 31 officially supported languages.
 */
@Component
public class DeepLTranslationProvider implements TranslationProvider {

    private static final Logger log = LoggerFactory.getLogger(DeepLTranslationProvider.class);

    public static final int MAX_TEXT_LENGTH = 5000;

    @Value("${app.deepl.api-key:${DEEPL_API_KEY:}}")
    private String apiKey;

    @Value("${app.deepl.api-url:${DEEPL_API_URL:}}")
    private String customApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public static final Set<String> SUPPORTED_TARGET_LANGUAGES = Set.of(
            "AR", "BG", "CS", "DA", "DE", "EL", "EN", "EN-GB", "EN-US",
            "ES", "ET", "FI", "FR", "HE", "HU", "ID", "IT", "JA",
            "KO", "LT", "LV", "NB", "NL", "NO", "PL", "PT", "PT-BR", "PT-PT",
            "RO", "RU", "SK", "SL", "SV", "TR", "UK", "ZH", "ZH-HANS", "ZH-HANT"
    );

    public static final List<DeepLLanguageDto> SUPPORTED_LANGUAGES_METADATA = List.of(
            new DeepLLanguageDto("en", "English", "English", "ltr", "DEEPL"),
            new DeepLLanguageDto("de", "German", "Deutsch", "ltr", "DEEPL"),
            new DeepLLanguageDto("es", "Spanish", "Español", "ltr", "DEEPL"),
            new DeepLLanguageDto("fr", "French", "Français", "ltr", "DEEPL"),
            new DeepLLanguageDto("it", "Italian", "Italiano", "ltr", "DEEPL"),
            new DeepLLanguageDto("ja", "Japanese", "日本語", "ltr", "DEEPL"),
            new DeepLLanguageDto("zh", "Chinese (Simplified)", "简体中文", "ltr", "DEEPL"),
            new DeepLLanguageDto("pt", "Portuguese", "Português", "ltr", "DEEPL"),
            new DeepLLanguageDto("ru", "Russian", "Русский", "ltr", "DEEPL"),
            new DeepLLanguageDto("nl", "Dutch", "Nederlands", "ltr", "DEEPL"),
            new DeepLLanguageDto("pl", "Polish", "Polski", "ltr", "DEEPL"),
            new DeepLLanguageDto("ar", "Arabic", "العربية", "rtl", "DEEPL"),
            new DeepLLanguageDto("ko", "Korean", "한국어", "ltr", "DEEPL"),
            new DeepLLanguageDto("tr", "Turkish", "Türkçe", "ltr", "DEEPL"),
            new DeepLLanguageDto("uk", "Ukrainian", "Українська", "ltr", "DEEPL"),
            new DeepLLanguageDto("sv", "Swedish", "Svenska", "ltr", "DEEPL"),
            new DeepLLanguageDto("da", "Danish", "Dansk", "ltr", "DEEPL"),
            new DeepLLanguageDto("fi", "Finnish", "Suomi", "ltr", "DEEPL"),
            new DeepLLanguageDto("no", "Norwegian", "Norsk", "ltr", "DEEPL"),
            new DeepLLanguageDto("cs", "Czech", "Čeština", "ltr", "DEEPL"),
            new DeepLLanguageDto("el", "Greek", "Ελληνικά", "ltr", "DEEPL"),
            new DeepLLanguageDto("ro", "Romanian", "Română", "ltr", "DEEPL"),
            new DeepLLanguageDto("hu", "Hungarian", "Magyar", "ltr", "DEEPL"),
            new DeepLLanguageDto("sk", "Slovak", "Slovenčina", "ltr", "DEEPL"),
            new DeepLLanguageDto("bg", "Bulgarian", "Български", "ltr", "DEEPL"),
            new DeepLLanguageDto("id", "Indonesian", "Bahasa Indonesia", "ltr", "DEEPL"),
            new DeepLLanguageDto("et", "Estonian", "Eesti", "ltr", "DEEPL"),
            new DeepLLanguageDto("lt", "Lithuanian", "Lietuvių", "ltr", "DEEPL"),
            new DeepLLanguageDto("lv", "Latvian", "Latviešu", "ltr", "DEEPL"),
            new DeepLLanguageDto("sl", "Slovenian", "Slovenščina", "ltr", "DEEPL"),
            new DeepLLanguageDto("he", "Hebrew", "עברית", "rtl", "DEEPL")
    );

    @Autowired
    public DeepLTranslationProvider() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
        this.restTemplate.getMessageConverters().add(0, new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));
        this.objectMapper = new ObjectMapper();
    }

    public DeepLTranslationProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("DEEPL_API_KEY configured: {}, endpoint: {}", isConfigured(), getEndpoint());
    }

    @Override
    public String getProviderName() {
        return "DeepL API";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !"none".equalsIgnoreCase(apiKey.trim());
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setCustomApiUrl(String customApiUrl) {
        this.customApiUrl = customApiUrl;
    }

    public String getEndpoint() {
        if (customApiUrl != null && !customApiUrl.trim().isEmpty()
                && !customApiUrl.contains("your-account") && !customApiUrl.contains("deepl.com/en/")) {
            return customApiUrl.trim();
        }
        if (apiKey != null && apiKey.trim().endsWith(":fx")) {
            return "https://api-free.deepl.com/v2/translate";
        }
        return "https://api.deepl.com/v2/translate";
    }

    @Override
    public boolean supportsLanguage(String targetLanguageCode) {
        if (targetLanguageCode == null || targetLanguageCode.trim().isEmpty()) {
            return false;
        }
        return SUPPORTED_TARGET_LANGUAGES.contains(targetLanguageCode.trim().toUpperCase());
    }

    @Override
    public List<DeepLLanguageDto> getSupportedLanguages() {
        return Collections.unmodifiableList(SUPPORTED_LANGUAGES_METADATA);
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("provider", getProviderName());
        status.put("configured", isConfigured());
        status.put("status", isConfigured() ? "active" : "unconfigured");
        status.put("endpoint", getEndpoint());
        status.put("cachedEntries", cache.size());
        status.put("supportedLanguagesCount", SUPPORTED_LANGUAGES_METADATA.size());
        return status;
    }

    public void clearCache() {
        cache.clear();
    }

    public String normalizeTargetLanguage(String targetLanguage) {
        if (targetLanguage == null) return "EN-US";
        String upper = targetLanguage.trim().toUpperCase();
        if ("EN".equals(upper)) return "EN-US";
        if ("PT".equals(upper)) return "PT-BR";
        if ("NO".equals(upper)) return "NB";
        if ("ZH".equals(upper)) return "ZH-HANS";
        return upper;
    }

    public String normalizeSourceLanguage(String sourceLanguage) {
        if (sourceLanguage == null || sourceLanguage.trim().isEmpty()) {
            return null;
        }
        String upper = sourceLanguage.trim().toUpperCase();
        if (upper.startsWith("EN")) return "EN";
        if (upper.startsWith("PT")) return "PT";
        if ("NO".equals(upper) || "NB".equals(upper)) return "NB";
        if ("ZH".equals(upper)) return "ZH";
        return upper;
    }

    @Override
    public TranslationResponse translate(TranslationRequest request) {
        if (request == null) {
            throw new BadRequestException("Translation request cannot be null");
        }
        if (request.getTargetLanguage() == null || request.getTargetLanguage().trim().isEmpty()) {
            throw new BadRequestException("Target language is required");
        }

        String rawTarget = request.getTargetLanguage().trim();
        if (!supportsLanguage(rawTarget)) {
            throw new BadRequestException("Unsupported target language: " + rawTarget);
        }

        String targetLang = normalizeTargetLanguage(rawTarget);
        String sourceLang = normalizeSourceLanguage(request.getSourceLanguage());

        List<String> inputTexts = new ArrayList<>();
        if (request.getTexts() != null && !request.getTexts().isEmpty()) {
            inputTexts.addAll(request.getTexts());
        } else if (request.getText() != null) {
            if (request.getText().trim().isEmpty()) {
                throw new BadRequestException("Text to translate cannot be empty");
            }
            inputTexts.add(request.getText());
        } else {
            throw new BadRequestException("Text to translate cannot be empty");
        }

        for (String text : inputTexts) {
            if (text != null && text.length() > MAX_TEXT_LENGTH) {
                throw new BadRequestException("Text exceeds maximum allowed length of " + MAX_TEXT_LENGTH + " characters");
            }
        }

        if (sourceLang != null && targetLang.startsWith(sourceLang)) {
            return new TranslationResponse(inputTexts.get(0), inputTexts, sourceLang, targetLang);
        }

        List<String> results = new ArrayList<>(inputTexts.size());
        List<String> uncachedTexts = new ArrayList<>();
        List<Integer> uncachedIndices = new ArrayList<>();

        for (int i = 0; i < inputTexts.size(); i++) {
            String text = inputTexts.get(i);
            if (text == null || text.trim().isEmpty()) {
                results.add(text != null ? text : "");
                continue;
            }

            String cacheKey = targetLang + "::" + (sourceLang != null ? sourceLang : "AUTO") + "::" + text;
            String cached = cache.get(cacheKey);

            if (cached != null) {
                results.add(cached);
            } else {
                results.add(null);
                uncachedTexts.add(text);
                uncachedIndices.add(i);
            }
        }

        if (!uncachedTexts.isEmpty()) {
            List<String> translatedBatch = fetchFromDeepLOrFallback(uncachedTexts, targetLang, sourceLang);
            for (int k = 0; k < uncachedTexts.size(); k++) {
                int originalIndex = uncachedIndices.get(k);
                String translated = (k < translatedBatch.size() && translatedBatch.get(k) != null)
                        ? translatedBatch.get(k)
                        : uncachedTexts.get(k);

                String cacheKey = targetLang + "::" + (sourceLang != null ? sourceLang : "AUTO") + "::" + uncachedTexts.get(k);
                cache.put(cacheKey, translated);
                results.set(originalIndex, translated);
            }
        }

        String firstTranslated = results.isEmpty() ? "" : results.get(0);
        return new TranslationResponse(firstTranslated, results, sourceLang != null ? sourceLang : "AUTO", targetLang);
    }

    private List<String> fetchFromDeepLOrFallback(List<String> texts, String targetLang, String sourceLang) {
        if (!isConfigured()) {
            throw new IllegalStateException("DEEPL_API_KEY is not configured.");
        }

        try {
            return callDeepLApi(texts, targetLang, sourceLang);
        } catch (HttpClientErrorException e) {
            log.warn("DeepL API error (HTTP {}): {}. Escalating to fallback provider.", e.getStatusCode().value(), e.getMessage());
            throw new RuntimeException("DeepL API HTTP " + e.getStatusCode().value() + " error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.warn("DeepL API network/timeout error: {}. Escalating to fallback provider.", e.getMessage());
            throw new RuntimeException("DeepL API network timeout", e);
        } catch (Exception e) {
            log.warn("DeepL API request failed: {}. Escalating to fallback provider.", e.getMessage());
            throw new RuntimeException("DeepL translation failed: " + e.getMessage(), e);
        }
    }

    public List<String> callDeepLApi(List<String> texts, String targetLang, String sourceLang) throws Exception {
        String endpoint = getEndpoint();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "DeepL-Auth-Key " + apiKey.trim());

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("text", texts);
        requestBody.put("target_lang", targetLang);
        if (sourceLang != null && !sourceLang.trim().isEmpty()) {
            requestBody.put("source_lang", sourceLang);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return parseDeepLResponse(response.getBody(), texts);
        }

        log.warn("Unexpected status code from DeepL API: {}", response.getStatusCode());
        return new ArrayList<>(texts);
    }

    public List<String> parseDeepLResponse(String responseBody, List<String> fallbackTexts) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode translationsNode = root.path("translations");

            if (translationsNode.isArray()) {
                List<String> results = new ArrayList<>();
                for (int i = 0; i < translationsNode.size(); i++) {
                    String translatedText = translationsNode.get(i).path("text").asText();
                    String unescaped = HtmlUtils.htmlUnescape(translatedText);
                    results.add(unescaped);
                }
                return results;
            }
        } catch (Exception e) {
            log.warn("Failed to parse DeepL API JSON response: {}", e.getMessage());
        }
        return new ArrayList<>(fallbackTexts);
    }
}
