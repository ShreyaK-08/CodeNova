import api from './api';

const BUNDLE_STORAGE_PREFIX = 'codenova_bundle_';
const CHUNK_SIZE = 60;
const memoryBundleCache = new Map();
const singleTextMemoryCache = new Map();

// Programming terms, technical identifiers, and keywords that should NEVER be translated
const PROTECTED_TERMS = new Set([
  'Java', 'Python', 'C', 'C++', 'JavaScript', 'SQL', 'HTML', 'CSS',
  'java', 'python', 'c', 'cpp', 'javascript', 'sql', 'html', 'css',
  'CodeNova', 'Monaco', 'REST', 'JWT', 'API', 'DeepL',
  'ACCEPTED', 'WRONG_ANSWER', 'TIME_LIMIT_EXCEEDED', 'COMPILATION_ERROR',
  'RUNTIME_ERROR', 'PENDING', 'RUNNING', 'DRAFT', 'PUBLISHED', 'ARCHIVED',
  'int', 'long', 'double', 'float', 'boolean', 'char', 'String', 'void',
  'public', 'private', 'protected', 'class', 'static', 'return'
]);

/**
 * Recursively flatten nested JSON object to array of { key: string, val: any }
 */
function flattenObject(obj, prefix = '') {
  let items = [];
  for (const [key, val] of Object.entries(obj)) {
    const fullKey = prefix ? `${prefix}.${key}` : key;
    if (val && typeof val === 'object' && !Array.isArray(val)) {
      items = items.concat(flattenObject(val, fullKey));
    } else {
      items.push({ key: fullKey, val });
    }
  }
  return items;
}

/**
 * Reconstruct nested JSON object from array of { key: string, val: any }
 */
function unflattenObject(items) {
  const result = {};
  for (const item of items) {
    const keys = item.key.split('.');
    let current = result;
    for (let i = 0; i < keys.length - 1; i++) {
      const k = keys[i];
      if (!current[k] || typeof current[k] !== 'object') {
        current[k] = {};
      }
      current = current[k];
    }
    current[keys[keys.length - 1]] = item.val;
  }
  return result;
}

/**
 * Dispatches non-blocking translation notification
 */
function notifyTranslationUnavailable() {
  window.dispatchEvent(
    new CustomEvent('codenova_translation_notice', {
      detail: { message: 'Translation is temporarily unavailable. Showing English.' },
    })
  );
}

/**
 * Translates an entire dictionary bundle from English to target language via DeepL backend.
 * Uses multi-tier caching:
 * 1. In-memory Map cache
 * 2. localStorage ('codenova_bundle_${lang}')
 * 3. Backend DeepL Translation API (/api/translation/translate)
 *
 * @param {string} targetLang - ISO language code (e.g. 'de', 'es', 'fr', 'ja', 'zh')
 * @param {Object} englishBundle - Source English dictionary object
 * @returns {Promise<Object>} Translated dictionary bundle (or English fallback)
 */
export const translateBundle = async (targetLang, englishBundle) => {
  if (!targetLang || targetLang === 'en' || !englishBundle) {
    return englishBundle;
  }

  const normalizedLang = targetLang.trim().toLowerCase();

  // 1. Check in-memory cache
  if (memoryBundleCache.has(normalizedLang)) {
    return memoryBundleCache.get(normalizedLang);
  }

  // 2. Check localStorage cache
  try {
    const cached = localStorage.getItem(`${BUNDLE_STORAGE_PREFIX}${normalizedLang}`);
    if (cached) {
      const parsed = JSON.parse(cached);
      memoryBundleCache.set(normalizedLang, parsed);
      return parsed;
    }
  } catch (err) {
    console.warn('Failed to read translation bundle from localStorage:', err);
  }

  // 3. Flatten and identify user-facing strings to translate
  const flattened = flattenObject(englishBundle);
  const toTranslateIndices = [];
  const textsToSend = [];

  flattened.forEach((item, idx) => {
    if (typeof item.val === 'string' && item.val.trim() !== '') {
      const trimmed = item.val.trim();
      if (!PROTECTED_TERMS.has(trimmed)) {
        toTranslateIndices.push(idx);
        textsToSend.push(item.val);
      }
    }
  });

  if (textsToSend.length === 0) {
    return englishBundle;
  }

  // 4. Batch translation requests to backend service
  const translatedTexts = new Array(textsToSend.length);
  let anySuccess = false;

  for (let i = 0; i < textsToSend.length; i += CHUNK_SIZE) {
    const chunk = textsToSend.slice(i, i + CHUNK_SIZE);
    try {
      const response = await api.post('/translation/translate', {
        targetLanguage: normalizedLang.toUpperCase(),
        sourceLanguage: 'EN',
        texts: chunk,
      });

      const chunkResults = response.data?.translations || chunk;
      for (let j = 0; j < chunk.length; j++) {
        translatedTexts[i + j] = chunkResults[j] || chunk[j];
      }
      anySuccess = true;
    } catch (chunkErr) {
      console.warn(`Translation chunk ${i} failed for '${normalizedLang}':`, chunkErr);
      for (let j = 0; j < chunk.length; j++) {
        translatedTexts[i + j] = chunk[j];
      }
    }
  }

  if (!anySuccess) {
    notifyTranslationUnavailable();
    return englishBundle;
  }

  // Apply translations back into flattened items
  toTranslateIndices.forEach((flatIdx, k) => {
    if (translatedTexts[k] !== undefined) {
      flattened[flatIdx].val = translatedTexts[k];
    }
  });

  const translatedBundle = unflattenObject(flattened);

  // Save to in-memory cache
  memoryBundleCache.set(normalizedLang, translatedBundle);

  // Save to localStorage cache
  try {
    localStorage.setItem(
      `${BUNDLE_STORAGE_PREFIX}${normalizedLang}`,
      JSON.stringify(translatedBundle)
    );
  } catch (storageErr) {
    console.warn('Could not save translation bundle to localStorage (quota exceeded?):', storageErr);
  }

  return translatedBundle;
};

/**
 * Translates a single text string using DeepL backend
 */
export const translateText = async (text, targetLang, sourceLang = 'EN') => {
  if (!text || typeof text !== 'string' || text.trim() === '' || targetLang === 'en' || targetLang === 'EN') {
    return text;
  }

  const trimmed = text.trim();
  if (PROTECTED_TERMS.has(trimmed)) {
    return text;
  }

  const normalizedLang = targetLang.trim().toUpperCase();
  const cacheKey = `${normalizedLang}::${text}`;
  if (singleTextMemoryCache.has(cacheKey)) {
    return singleTextMemoryCache.get(cacheKey);
  }

  try {
    const response = await api.post('/translation/translate', {
      text: text,
      targetLanguage: normalizedLang,
      sourceLanguage: sourceLang,
    });

    const translated = response.data?.translatedText || response.data?.translations?.[0] || text;
    singleTextMemoryCache.set(cacheKey, translated);
    return translated;
  } catch (err) {
    console.warn(`DeepL single translation failed for '${text}':`, err);
    notifyTranslationUnavailable();
    return text;
  }
};

/**
 * Translates an array of individual strings with in-memory caching.
 */
export const translateTexts = async (targetLang, texts, sourceLang = 'EN') => {
  if (!texts || texts.length === 0 || targetLang === 'en' || targetLang === 'EN') {
    return texts;
  }

  const normalizedLang = targetLang.trim().toUpperCase();
  const results = new Array(texts.length);
  const uncachedIndices = [];
  const uncachedTexts = [];

  for (let i = 0; i < texts.length; i++) {
    const text = texts[i];
    if (!text || typeof text !== 'string' || text.trim() === '' || PROTECTED_TERMS.has(text.trim())) {
      results[i] = text;
      continue;
    }

    const cacheKey = `${normalizedLang}::${text}`;
    if (singleTextMemoryCache.has(cacheKey)) {
      results[i] = singleTextMemoryCache.get(cacheKey);
    } else {
      uncachedIndices.push(i);
      uncachedTexts.push(text);
    }
  }

  if (uncachedTexts.length > 0) {
    try {
      const response = await api.post('/translation/translate', {
        targetLanguage: normalizedLang,
        sourceLanguage: sourceLang,
        texts: uncachedTexts,
      });

      const translations = response.data?.translations || uncachedTexts;
      for (let k = 0; k < uncachedTexts.length; k++) {
        const trans = translations[k] || uncachedTexts[k];
        const cacheKey = `${normalizedLang}::${uncachedTexts[k]}`;
        singleTextMemoryCache.set(cacheKey, trans);
        results[uncachedIndices[k]] = trans;
      }
    } catch (err) {
      console.warn('DeepL translateTexts API failed, falling back:', err);
      notifyTranslationUnavailable();
      for (let k = 0; k < uncachedTexts.length; k++) {
        results[uncachedIndices[k]] = uncachedTexts[k];
      }
    }
  }

  return results;
};

/**
 * Clear cached bundles for a specific language or all languages
 */
export const clearTranslationCache = (langCode) => {
  if (langCode) {
    memoryBundleCache.delete(langCode.toLowerCase());
    try {
      localStorage.removeItem(`${BUNDLE_STORAGE_PREFIX}${langCode.toLowerCase()}`);
    } catch (e) {
      // ignore
    }
  } else {
    memoryBundleCache.clear();
    singleTextMemoryCache.clear();
    try {
      const keysToRemove = [];
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith(BUNDLE_STORAGE_PREFIX)) {
          keysToRemove.push(key);
        }
      }
      keysToRemove.forEach((k) => localStorage.removeItem(k));
    } catch (e) {
      // ignore
    }
  }
};

const translationService = {
  translateBundle,
  translateText,
  translateTexts,
  clearTranslationCache,
};

export default translationService;
