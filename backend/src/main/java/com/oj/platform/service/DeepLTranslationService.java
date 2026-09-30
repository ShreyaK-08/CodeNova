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
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeepLTranslationService implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(DeepLTranslationService.class);

    public static final int MAX_TEXT_LENGTH = 5000;
    public static final String DEFAULT_SOURCE_LANGUAGE = "EN";

    @Value("${app.deepl.api-key:${DEEPL_API_KEY:}}")
    private String apiKey;

    @Value("${app.deepl.api-url:${DEEPL_API_URL:}}")
    private String customApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Thread-safe in-memory cache: "targetLang::sourceLang::text" -> "translatedText"
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    // Officially supported DeepL target languages
    public static final Set<String> SUPPORTED_TARGET_LANGUAGES = Set.of(
            "AR", "BG", "CS", "DA", "DE", "EL", "EN", "EN-GB", "EN-US",
            "ES", "ET", "FI", "FR", "HE", "HU", "ID", "IT", "JA",
            "KO", "LT", "LV", "NB", "NL", "NO", "PL", "PT", "PT-BR", "PT-PT",
            "RO", "RU", "SK", "SL", "SV", "TR", "UK", "ZH", "ZH-HANS", "ZH-HANT"
    );

    // DeepL Supported Languages Metadata for UI population
    private static final List<DeepLLanguageDto> SUPPORTED_LANGUAGES_METADATA = List.of(
            new DeepLLanguageDto("en", "English", "English", "ltr"),
            new DeepLLanguageDto("de", "German", "Deutsch", "ltr"),
            new DeepLLanguageDto("es", "Spanish", "Español", "ltr"),
            new DeepLLanguageDto("fr", "French", "Français", "ltr"),
            new DeepLLanguageDto("it", "Italian", "Italiano", "ltr"),
            new DeepLLanguageDto("ja", "Japanese", "日本語", "ltr"),
            new DeepLLanguageDto("zh", "Chinese (Simplified)", "简体中文", "ltr"),
            new DeepLLanguageDto("pt", "Portuguese", "Português", "ltr"),
            new DeepLLanguageDto("ru", "Russian", "Русский", "ltr"),
            new DeepLLanguageDto("nl", "Dutch", "Nederlands", "ltr"),
            new DeepLLanguageDto("pl", "Polish", "Polski", "ltr"),
            new DeepLLanguageDto("ar", "Arabic", "العربية", "rtl"),
            new DeepLLanguageDto("ko", "Korean", "한국어", "ltr"),
            new DeepLLanguageDto("tr", "Turkish", "Türkçe", "ltr"),
            new DeepLLanguageDto("uk", "Ukrainian", "Українська", "ltr"),
            new DeepLLanguageDto("sv", "Swedish", "Svenska", "ltr"),
            new DeepLLanguageDto("da", "Danish", "Dansk", "ltr"),
            new DeepLLanguageDto("fi", "Finnish", "Suomi", "ltr"),
            new DeepLLanguageDto("no", "Norwegian", "Norsk", "ltr"),
            new DeepLLanguageDto("cs", "Czech", "Čeština", "ltr"),
            new DeepLLanguageDto("el", "Greek", "Ελληνικά", "ltr"),
            new DeepLLanguageDto("ro", "Romanian", "Română", "ltr"),
            new DeepLLanguageDto("hu", "Hungarian", "Magyar", "ltr"),
            new DeepLLanguageDto("sk", "Slovak", "Slovenčina", "ltr"),
            new DeepLLanguageDto("bg", "Bulgarian", "Български", "ltr"),
            new DeepLLanguageDto("id", "Indonesian", "Bahasa Indonesia", "ltr"),
            new DeepLLanguageDto("et", "Estonian", "Eesti", "ltr"),
            new DeepLLanguageDto("lt", "Lithuanian", "Lietuvių", "ltr"),
            new DeepLLanguageDto("lv", "Latvian", "Latviešu", "ltr"),
            new DeepLLanguageDto("sl", "Slovenian", "Slovenščina", "ltr"),
            new DeepLLanguageDto("he", "Hebrew", "עברית", "rtl")
    );

    @Autowired
    public DeepLTranslationService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    public DeepLTranslationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
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
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !"none".equalsIgnoreCase(apiKey.trim());
    }

    @Override
    public List<DeepLLanguageDto> getSupportedLanguages() {
        return Collections.unmodifiableList(SUPPORTED_LANGUAGES_METADATA);
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("provider", "DeepL API");
        status.put("configured", isConfigured());
        status.put("status", isConfigured() ? "active" : "unconfigured");
        status.put("endpoint", getEndpoint());
        status.put("cachedEntries", cache.size());
        status.put("supportedLanguagesCount", SUPPORTED_LANGUAGES_METADATA.size());
        return status;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    public boolean isLanguageSupported(String langCode) {
        if (langCode == null || langCode.trim().isEmpty()) {
            return false;
        }
        return SUPPORTED_TARGET_LANGUAGES.contains(langCode.trim().toUpperCase());
    }

    public String normalizeTargetLanguage(String targetLanguage) {
        if (targetLanguage == null) return "EN-US";
        String upper = targetLanguage.trim().toUpperCase();
        if ("EN".equals(upper)) {
            return "EN-US";
        }
        if ("PT".equals(upper)) {
            return "PT-BR";
        }
        if ("NO".equals(upper)) {
            return "NB";
        }
        return upper;
    }

    public String normalizeSourceLanguage(String sourceLanguage) {
        if (sourceLanguage == null || sourceLanguage.trim().isEmpty()) {
            return null; // DeepL will auto-detect
        }
        String upper = sourceLanguage.trim().toUpperCase();
        if (upper.startsWith("EN")) {
            return "EN";
        }
        if (upper.startsWith("PT")) {
            return "PT";
        }
        if ("NO".equals(upper) || "NB".equals(upper)) {
            return "NB";
        }
        return upper;
    }

    @Override
    public TranslationResponse translate(TranslationRequest request) {
        if (request == null) {
            throw new BadRequestException("Translation request cannot be null");
        }

        // 1. Validate Target Language
        if (request.getTargetLanguage() == null || request.getTargetLanguage().trim().isEmpty()) {
            throw new BadRequestException("Target language is required");
        }

        String rawTarget = request.getTargetLanguage().trim();
        if (!isLanguageSupported(rawTarget)) {
            throw new BadRequestException("Unsupported target language: " + rawTarget);
        }

        String targetLang = normalizeTargetLanguage(rawTarget);
        String sourceLang = normalizeSourceLanguage(request.getSourceLanguage());

        // 2. Extract and Validate Text
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

        // Length validation
        for (String text : inputTexts) {
            if (text != null && text.length() > MAX_TEXT_LENGTH) {
                throw new BadRequestException("Text exceeds maximum allowed length of " + MAX_TEXT_LENGTH + " characters");
            }
        }

        // Check identical source and target (e.g. EN to EN-US)
        if (sourceLang != null && targetLang.startsWith(sourceLang)) {
            return new TranslationResponse(
                    inputTexts.get(0),
                    inputTexts,
                    sourceLang,
                    targetLang
            );
        }

        // 3. Process Caching and Batching
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

        // 4. Translate Uncached Texts via DeepL
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

    @Override
    public String translateSingle(String text, String targetLanguage) {
        return translate(text, targetLanguage);
    }

    @Override
    public String translate(String text, String targetLanguage) {
        return translate(text, "EN", targetLanguage);
    }

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.trim().isEmpty() || targetLanguage == null) {
            return text;
        }

        String normTarget = normalizeTargetLanguage(targetLanguage);
        String normSource = normalizeSourceLanguage(sourceLanguage);

        if (normSource != null && normTarget.startsWith(normSource)) {
            return text;
        }

        String cacheKey = normTarget + "::" + (normSource != null ? normSource : "AUTO") + "::" + text;
        String cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            TranslationResponse resp = translate(new TranslationRequest(text, sourceLanguage, targetLanguage));
            return resp.getTranslatedText();
        } catch (Exception e) {
            log.warn("DeepL translation fallback for text '{}': {}", text, e.getMessage());
            return text;
        }
    }

    private List<String> fetchFromDeepLOrFallback(List<String> texts, String targetLang, String sourceLang) {
        if (!isConfigured()) {
            log.debug("DEEPL_API_KEY is not configured. Serving original text without translation.");
            return new ArrayList<>(texts);
        }

        try {
            return callDeepLApi(texts, targetLang, sourceLang);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("DeepL API rate limit reached (HTTP 429). Falling back to original text.");
            } else if (e.getStatusCode().value() == 403 || e.getStatusCode().value() == 456) {
                log.warn("DeepL API authorization/quota error (HTTP {}). Falling back to original text.", e.getStatusCode().value());
            } else {
                log.warn("DeepL API HTTP client error (HTTP {}). Falling back to original text.", e.getStatusCode().value());
            }
        } catch (ResourceAccessException e) {
            log.warn("DeepL API network/timeout error: {}. Falling back to original text.", e.getMessage());
        } catch (Exception e) {
            log.warn("DeepL API request failed: {}. Falling back to original text.", e.getMessage());
        }

        // Fallback: return original texts without crashing
        return new ArrayList<>(texts);
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
