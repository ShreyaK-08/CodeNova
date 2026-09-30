import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import enTranslations from './locales/en.json';
import knTranslations from './locales/kn.json';
import hiTranslations from './locales/hi.json';
import deTranslations from './locales/de.json';
import frTranslations from './locales/fr.json';
import esTranslations from './locales/es.json';
import teTranslations from './locales/te.json';
import taTranslations from './locales/ta.json';
// Generated world & Indic language bundles
import bnTranslations from './locales/bn.json';
import mrTranslations from './locales/mr.json';
import guTranslations from './locales/gu.json';
import paTranslations from './locales/pa.json';
import mlTranslations from './locales/ml.json';
import urTranslations from './locales/ur.json';
import jaTranslations from './locales/ja.json';
import zhTranslations from './locales/zh.json';
import ruTranslations from './locales/ru.json';
import arTranslations from './locales/ar.json';
import ptTranslations from './locales/pt.json';
import itTranslations from './locales/it.json';
import koTranslations from './locales/ko.json';
import viTranslations from './locales/vi.json';
import idTranslations from './locales/id.json';
import trTranslations from './locales/tr.json';
import thTranslations from './locales/th.json';
import nlTranslations from './locales/nl.json';
import plTranslations from './locales/pl.json';
import { getLanguageDetails, LANGUAGE_MAP } from './languages';
import { translateBundle } from '../services/translationService';

const STORAGE_KEY = 'codenova_language';
const rawStored = localStorage.getItem(STORAGE_KEY);
const initialLanguage = rawStored && LANGUAGE_MAP[rawStored.trim().toLowerCase()]
  ? rawStored.trim().toLowerCase()
  : 'en';

const resources = {
  en: { translation: enTranslations },
  kn: { translation: knTranslations },
  hi: { translation: hiTranslations },
  de: { translation: deTranslations },
  fr: { translation: frTranslations },
  es: { translation: esTranslations },
  te: { translation: teTranslations },
  ta: { translation: taTranslations },
  // Generated world & Indic language bundles (instant, zero API calls)
  bn: { translation: bnTranslations },
  mr: { translation: mrTranslations },
  gu: { translation: guTranslations },
  pa: { translation: paTranslations },
  ml: { translation: mlTranslations },
  ur: { translation: urTranslations },
  ja: { translation: jaTranslations },
  zh: { translation: zhTranslations },
  ru: { translation: ruTranslations },
  ar: { translation: arTranslations },
  pt: { translation: ptTranslations },
  it: { translation: itTranslations },
  ko: { translation: koTranslations },
  vi: { translation: viTranslations },
  id: { translation: idTranslations },
  tr: { translation: trTranslations },
  th: { translation: thTranslations },
  nl: { translation: nlTranslations },
  pl: { translation: plTranslations },
};

// Check if initial language has a pre-cached bundle in localStorage
if (initialLanguage && !resources[initialLanguage]) {
  try {
    const cached = localStorage.getItem(`codenova_bundle_${initialLanguage}`);
    if (cached) {
      resources[initialLanguage] = { translation: JSON.parse(cached) };
    }
  } catch (e) {
    console.warn('Failed to parse pre-cached bundle for', initialLanguage, e);
  }
}

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: initialLanguage,
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false, // React already protects from XSS
    },
  });

/**
 * Apply HTML lang and dir attributes to document element
 */
export const applyDocumentDirection = (langCode) => {
  const details = getLanguageDetails(langCode);
  const direction = details?.direction || 'ltr';
  document.documentElement.lang = langCode;
  document.documentElement.dir = direction;
};

// Initial setup on module load
applyDocumentDirection(initialLanguage);

// Asynchronously load initial non-static language bundle if not already pre-cached
if (!resources[initialLanguage]) {
  translateBundle(initialLanguage, enTranslations)
    .then((bundle) => {
      if (bundle) {
        i18n.addResourceBundle(initialLanguage, 'translation', bundle, true, true);
        i18n.changeLanguage(initialLanguage);
      }
    })
    .catch((err) => {
      console.warn('Could not asynchronously load initial language bundle:', err);
    });
}

/**
 * Changes language globally.
 * If the language is pre-compiled (en, kn, hi, de, fr, es, te, ta), switches synchronously with zero latency.
 * If dynamic, fetches from backend translation service and caches in localStorage.
 * Saves to localStorage ('codenova_language') and sets text direction (LTR/RTL).
 */
export const changeAppLanguage = async (langCode) => {
  try {
    const normalized = (langCode || 'en').trim().toLowerCase();
    
    // Dynamic loading via backend for any supported language beyond precompiled bundles
    if (!i18n.hasResourceBundle(normalized, 'translation')) {
      const bundle = await translateBundle(normalized, enTranslations);
      if (bundle && Object.keys(bundle).length > 0) {
        i18n.addResourceBundle(normalized, 'translation', bundle, true, true);
      }
    }

    await i18n.changeLanguage(normalized);
    localStorage.setItem(STORAGE_KEY, normalized);
    applyDocumentDirection(normalized);
    window.dispatchEvent(new CustomEvent('codenova_language_changed', { detail: { language: normalized } }));
  } catch (err) {
    console.warn('Failed to change language, remaining on current or fallback to English:', err);
    if (!i18n.language) {
      await i18n.changeLanguage('en');
      localStorage.setItem(STORAGE_KEY, 'en');
      applyDocumentDirection('en');
    }
    window.dispatchEvent(
      new CustomEvent('codenova_translation_notice', {
        detail: { message: 'Translation is temporarily unavailable. Showing available text.' },
      })
    );
  }
};

export default i18n;
