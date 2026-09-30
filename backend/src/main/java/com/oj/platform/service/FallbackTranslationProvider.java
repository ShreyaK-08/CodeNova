package com.oj.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.platform.dto.DeepLLanguageDto;
import com.oj.platform.dto.TranslationRequest;
import com.oj.platform.dto.TranslationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fallback & Cloud Translation Provider.
 * Extends CodeNova's translation capabilities to 105+ languages, including Indian regional
 * languages (Hindi, Kannada, Tamil, Telugu, Malayalam, Bengali, Gujarati, Marathi, Punjabi, Urdu, etc.)
 * and other world languages not natively covered by DeepL.
 *
 * Connects to Google Cloud Translation API if configured via GOOGLE_TRANSLATION_API_KEY,
 * and falls back gracefully without throwing 400s or crashing.
 */
@Component
public class FallbackTranslationProvider implements TranslationProvider {

    private static final Logger log = LoggerFactory.getLogger(FallbackTranslationProvider.class);

    private static final String GOOGLE_TRANSLATE_V2_URL = "https://translation.googleapis.com/language/translate/v2";

    @Value("${app.google.translation.api-key:${GOOGLE_TRANSLATION_API_KEY:}}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    // Expanded regional and world languages supported by fallback provider (75+ languages)
    public static final List<DeepLLanguageDto> FALLBACK_LANGUAGES = List.of(
            // Indian / Indic Regional Languages
            new DeepLLanguageDto("hi", "Hindi", "हिन्दी", "ltr", "FALLBACK"),
            new DeepLLanguageDto("kn", "Kannada", "ಕನ್ನಡ", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ta", "Tamil", "தமிழ்", "ltr", "FALLBACK"),
            new DeepLLanguageDto("te", "Telugu", "తెలుగు", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ml", "Malayalam", "മലയാളം", "ltr", "FALLBACK"),
            new DeepLLanguageDto("bn", "Bengali", "বাংলা", "ltr", "FALLBACK"),
            new DeepLLanguageDto("mr", "Marathi", "मराठी", "ltr", "FALLBACK"),
            new DeepLLanguageDto("gu", "Gujarati", "ગુજરાતી", "ltr", "FALLBACK"),
            new DeepLLanguageDto("pa", "Punjabi", "ਪੰਜਾਬੀ", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ur", "Urdu", "اردو", "rtl", "FALLBACK"),
            new DeepLLanguageDto("or", "Odia", "ଓଡ଼ିଆ", "ltr", "FALLBACK"),
            new DeepLLanguageDto("as", "Assamese", "অসমীয়া", "ltr", "FALLBACK"),
            new DeepLLanguageDto("sa", "Sanskrit", "संस्कृतम्", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ne", "Nepali", "नेपाली", "ltr", "FALLBACK"),
            new DeepLLanguageDto("si", "Sinhala", "සිංහල", "ltr", "FALLBACK"),

            // Asian Languages
            new DeepLLanguageDto("vi", "Vietnamese", "Tiếng Việt", "ltr", "FALLBACK"),
            new DeepLLanguageDto("th", "Thai", "ไทย", "ltr", "FALLBACK"),
            new DeepLLanguageDto("my", "Burmese", "မြန်မာစာ", "ltr", "FALLBACK"),
            new DeepLLanguageDto("km", "Khmer", "ភាសាខ្មែរ", "ltr", "FALLBACK"),
            new DeepLLanguageDto("lo", "Lao", "ພາສາລາວ", "ltr", "FALLBACK"),
            new DeepLLanguageDto("tl", "Tagalog (Filipino)", "Tagalog", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ms", "Malay", "Bahasa Melayu", "ltr", "FALLBACK"),
            new DeepLLanguageDto("mn", "Mongolian", "Монгол", "ltr", "FALLBACK"),

            // Middle Eastern & African Languages
            new DeepLLanguageDto("fa", "Persian (Farsi)", "فارسی", "rtl", "FALLBACK"),
            new DeepLLanguageDto("sw", "Swahili", "Kiswahili", "ltr", "FALLBACK"),
            new DeepLLanguageDto("am", "Amharic", "አማርኛ", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ha", "Hausa", "Hausa", "ltr", "FALLBACK"),
            new DeepLLanguageDto("yo", "Yoruba", "Yorùbá", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ig", "Igbo", "Asụsụ Igbo", "ltr", "FALLBACK"),
            new DeepLLanguageDto("zu", "Zulu", "isiZulu", "ltr", "FALLBACK"),
            new DeepLLanguageDto("xh", "Xhosa", "isiXhosa", "ltr", "FALLBACK"),
            new DeepLLanguageDto("af", "Afrikaans", "Afrikaans", "ltr", "FALLBACK"),
            new DeepLLanguageDto("so", "Somali", "Soomaaliga", "ltr", "FALLBACK"),

            // European & Caucasian Languages
            new DeepLLanguageDto("hy", "Armenian", "Հայերեն", "ltr", "FALLBACK"),
            new DeepLLanguageDto("az", "Azerbaijani", "Azərbaycan", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ka", "Georgian", "ქართული", "ltr", "FALLBACK"),
            new DeepLLanguageDto("kk", "Kazakh", "Қазақша", "ltr", "FALLBACK"),
            new DeepLLanguageDto("uz", "Uzbek", "Oʻzbekcha", "ltr", "FALLBACK"),
            new DeepLLanguageDto("tg", "Tajik", "Тоҷикӣ", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ky", "Kyrgyz", "Кыргызча", "ltr", "FALLBACK"),
            new DeepLLanguageDto("is", "Icelandic", "Íslenska", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ga", "Irish", "Gaeilge", "ltr", "FALLBACK"),
            new DeepLLanguageDto("cy", "Welsh", "Cymraeg", "ltr", "FALLBACK"),
            new DeepLLanguageDto("eu", "Basque", "Euskara", "ltr", "FALLBACK"),
            new DeepLLanguageDto("gl", "Galician", "Galego", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ca", "Catalan", "Català", "ltr", "FALLBACK"),
            new DeepLLanguageDto("sq", "Albanian", "Shqip", "ltr", "FALLBACK"),
            new DeepLLanguageDto("mk", "Macedonian", "Македонски", "ltr", "FALLBACK"),
            new DeepLLanguageDto("bs", "Bosnian", "Bosanski", "ltr", "FALLBACK"),
            new DeepLLanguageDto("hr", "Croatian", "Hrvatski", "ltr", "FALLBACK"),
            new DeepLLanguageDto("sr", "Serbian", "Српски", "ltr", "FALLBACK"),
            new DeepLLanguageDto("be", "Belarusian", "Беларуская", "ltr", "FALLBACK"),
            new DeepLLanguageDto("mt", "Maltese", "Malti", "ltr", "FALLBACK"),
            new DeepLLanguageDto("lb", "Luxembourgish", "Lëtzebuergesch", "ltr", "FALLBACK"),
            new DeepLLanguageDto("eo", "Esperanto", "Esperanto", "ltr", "FALLBACK"),
            new DeepLLanguageDto("la", "Latin", "Latina", "ltr", "FALLBACK"),
            new DeepLLanguageDto("fy", "Frisian", "Frysk", "ltr", "FALLBACK"),
            new DeepLLanguageDto("gd", "Scots Gaelic", "Gàidhlig", "ltr", "FALLBACK"),
            new DeepLLanguageDto("yi", "Yiddish", "ייִדיש", "rtl", "FALLBACK"),
            new DeepLLanguageDto("ku", "Kurdish", "Kurdî", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ps", "Pashto", "پښتو", "rtl", "FALLBACK"),
            new DeepLLanguageDto("sd", "Sindhi", "سنڌي", "rtl", "FALLBACK"),
            new DeepLLanguageDto("ug", "Uyghur", "ئۇيغۇرچە", "rtl", "FALLBACK"),
            new DeepLLanguageDto("haw", "Hawaiian", "ʻŌlelo Hawaiʻi", "ltr", "FALLBACK"),
            new DeepLLanguageDto("mi", "Maori", "Māori", "ltr", "FALLBACK"),
            new DeepLLanguageDto("sm", "Samoan", "Gagana Sāmoa", "ltr", "FALLBACK"),
            new DeepLLanguageDto("mg", "Malagasy", "Malagasy", "ltr", "FALLBACK"),
            new DeepLLanguageDto("sn", "Shona", "chiShona", "ltr", "FALLBACK"),
            new DeepLLanguageDto("st", "Sesotho", "Sesotho", "ltr", "FALLBACK"),
            new DeepLLanguageDto("su", "Sundanese", "Basa Sunda", "ltr", "FALLBACK"),
            new DeepLLanguageDto("jw", "Javanese", "Basa Jawa", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ceb", "Cebuano", "Sinugboanon", "ltr", "FALLBACK"),
            new DeepLLanguageDto("ny", "Chichewa", "Chichewa", "ltr", "FALLBACK"),
            new DeepLLanguageDto("co", "Corsican", "Corsu", "ltr", "FALLBACK")
    );

    private final Set<String> supportedCodes = new HashSet<>();

    @Autowired
    public FallbackTranslationProvider() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(12000);
        this.restTemplate = new RestTemplate(factory);
        this.restTemplate.getMessageConverters().add(0, new org.springframework.http.converter.StringHttpMessageConverter(StandardCharsets.UTF_8));
        this.objectMapper = new ObjectMapper();
        for (DeepLLanguageDto dto : FALLBACK_LANGUAGES) {
            supportedCodes.add(dto.getCode().toLowerCase());
        }
    }

    public FallbackTranslationProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        for (DeepLLanguageDto dto : FALLBACK_LANGUAGES) {
            supportedCodes.add(dto.getCode().toLowerCase());
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getProviderName() {
        return "Cloud / Fallback Provider";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !"none".equalsIgnoreCase(apiKey.trim());
    }

    @Override
    public boolean supportsLanguage(String targetLanguageCode) {
        if (targetLanguageCode == null || targetLanguageCode.trim().isEmpty()) return false;
        return true;
    }

    @Override
    public List<DeepLLanguageDto> getSupportedLanguages() {
        return Collections.unmodifiableList(FALLBACK_LANGUAGES);
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("provider", getProviderName());
        status.put("configured", isConfigured());
        status.put("status", isConfigured() ? "active" : "fallback-ready");
        status.put("cachedEntries", cache.size());
        status.put("supportedLanguagesCount", FALLBACK_LANGUAGES.size());
        return status;
    }

    public void clearCache() {
        cache.clear();
    }

    public String normalizeGoogleLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) return "en";
        String lower = lang.trim().toLowerCase();
        if ("zh".equals(lower) || "zh-hans".equals(lower)) return "zh-CN";
        if ("zh-hant".equals(lower)) return "zh-TW";
        if ("jw".equals(lower)) return "jv";
        if ("he".equals(lower)) return "iw";
        if ("nb".equals(lower)) return "no";
        return lower;
    }

    @Override
    public TranslationResponse translate(TranslationRequest request) {
        if (request == null || (request.getTexts() == null && request.getText() == null)) {
            return new TranslationResponse(
                    request != null ? request.getTargetLanguage() : "en",
                    Collections.emptyList()
            );
        }

        String targetLang = request.getTargetLanguage() != null ? request.getTargetLanguage().trim().toLowerCase() : "en";
        String sourceLang = request.getSourceLanguage() != null && !request.getSourceLanguage().trim().isEmpty()
                ? request.getSourceLanguage().trim().toLowerCase()
                : "en";

        List<String> inputTexts = new ArrayList<>();
        if (request.getTexts() != null && !request.getTexts().isEmpty()) {
            inputTexts.addAll(request.getTexts());
        } else if (request.getText() != null) {
            inputTexts.add(request.getText());
        }

        if ("en".equals(targetLang) && "en".equals(sourceLang)) {
            return new TranslationResponse(targetLang, new ArrayList<>(inputTexts));
        }

        List<String> resultTranslations = new ArrayList<>(inputTexts.size());
        List<String> uncachedTexts = new ArrayList<>();
        List<Integer> uncachedIndices = new ArrayList<>();

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
                resultTranslations.add(null);
                uncachedTexts.add(text);
                uncachedIndices.add(i);
            }
        }

        if (!uncachedTexts.isEmpty()) {
            List<String> translatedBatch = fetchFromCloudOrFallback(uncachedTexts, targetLang, sourceLang);
            for (int k = 0; k < uncachedTexts.size(); k++) {
                int originalIndex = uncachedIndices.get(k);
                String translated = (k < translatedBatch.size() && translatedBatch.get(k) != null)
                        ? translatedBatch.get(k)
                        : uncachedTexts.get(k);

                String cacheKey = targetLang + "::" + uncachedTexts.get(k);
                cache.put(cacheKey, translated);
                resultTranslations.set(originalIndex, translated);
            }
        }

        String first = resultTranslations.isEmpty() ? "" : resultTranslations.get(0);
        return new TranslationResponse(first, resultTranslations, sourceLang, targetLang);
    }

    private List<String> fetchFromCloudOrFallback(List<String> texts, String targetLang, String sourceLang) {
        if (isConfigured()) {
            try {
                return callGoogleTranslationApi(texts, targetLang, sourceLang);
            } catch (Exception e) {
                log.warn("Cloud Translation API request failed: {}. Trying fallback translation.", e.getMessage());
            }
        }
        try {
            return callPublicTranslationEndpoint(texts, targetLang, sourceLang);
        } catch (Exception e) {
            log.warn("Fallback translation failed: {}. Returning original text.", e.getMessage());
            return new ArrayList<>(texts);
        }
    }

    private List<String> callPublicTranslationEndpoint(List<String> texts, String targetLang, String sourceLang) {
        List<String> results = new ArrayList<>(texts.size());
        String src = (sourceLang != null && !sourceLang.isEmpty()) ? normalizeGoogleLanguage(sourceLang) : "en";
        String tgt = (targetLang != null) ? normalizeGoogleLanguage(targetLang) : "en";

        int batchSize = 25;
        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> chunk = texts.subList(i, end);
            List<String> translatedChunk = translateBatchViaPublicGoogle(chunk, tgt, src);
            if (translatedChunk != null && translatedChunk.size() == chunk.size()) {
                results.addAll(translatedChunk);
            } else {
                for (String text : chunk) {
                    results.add(translateSingleViaPublicGoogleOrMyMemory(text, tgt, src));
                }
            }
        }
        return results;
    }

    private List<String> translateBatchViaPublicGoogle(List<String> chunk, String tgt, String src) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chunk.size(); i++) {
                String t = chunk.get(i) != null ? chunk.get(i).replace("\r\n", " ").replace("\n", " ") : "";
                sb.append(t);
                if (i < chunk.size() - 1) {
                    sb.append("\n");
                }
            }
            String joined = sb.toString();
            String encoded = URLEncoder.encode(joined, StandardCharsets.UTF_8);
            String url = "https://translate.googleapis.com/translate_a/single?client=dict-chrome-ex&sl=" + src + "&tl=" + tgt + "&dt=t&q=" + encoded;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "*/*");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode sentences = root.get(0);
                if (sentences != null && sentences.isArray()) {
                    StringBuilder translatedFull = new StringBuilder();
                    for (int s = 0; s < sentences.size(); s++) {
                        JsonNode part = sentences.get(s);
                        if (part != null && part.isArray() && part.size() > 0 && !part.get(0).isNull()) {
                            translatedFull.append(part.get(0).asText());
                        }
                    }
                    String[] lines = translatedFull.toString().split("\\r?\\n", -1);
                    if (lines.length >= chunk.size()) {
                        List<String> list = new ArrayList<>();
                        for (int k = 0; k < chunk.size(); k++) {
                            String val = lines[k] != null ? lines[k].trim() : "";
                            list.add(!val.isEmpty() ? HtmlUtils.htmlUnescape(val) : chunk.get(k));
                        }
                        return list;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Batch Google public translation error: {}", e.getMessage());
        }
        return null;
    }

    private String translateSingleViaPublicGoogleOrMyMemory(String text, String tgt, String src) {
        if (text == null || text.trim().isEmpty()) {
            return text != null ? text : "";
        }
        // 1. Try Google Chrome Client Single
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.googleapis.com/translate_a/single?client=dict-chrome-ex&sl=" + src + "&tl=" + tgt + "&dt=t&q=" + encoded;
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode sentences = root.get(0);
                if (sentences != null && sentences.isArray() && sentences.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int s = 0; s < sentences.size(); s++) {
                        JsonNode part = sentences.get(s);
                        if (part != null && part.isArray() && part.size() > 0 && !part.get(0).isNull()) {
                            sb.append(part.get(0).asText());
                        }
                    }
                    if (sb.length() > 0) {
                        return HtmlUtils.htmlUnescape(sb.toString().trim());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Single Google translation error: {}", e.getMessage());
        }

        // 2. Try MyMemory
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=" + src + "|" + tgt;
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "CodeNovaPlatform/1.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode respData = root.path("responseData");
                String translated = respData.path("translatedText").asText(null);
                if (translated != null && !translated.isBlank() && !translated.startsWith("MYMEMORY WARNING")) {
                    return HtmlUtils.htmlUnescape(translated);
                }
            }
        } catch (Exception e) {
            log.debug("MyMemory translation error: {}", e.getMessage());
        }

        return text;
    }

    private List<String> callGoogleTranslationApi(List<String> texts, String targetLang, String sourceLang) throws Exception {
        String url = GOOGLE_TRANSLATE_V2_URL + "?key=" + apiKey.trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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
                    String unescaped = HtmlUtils.htmlUnescape(translatedText);
                    result.add(unescaped);
                }
                return result;
            }
        }

        return new ArrayList<>(texts);
    }
}
