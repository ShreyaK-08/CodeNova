/**
 * CodeNova Human Language Registry — 100+ Multi-Provider Languages
 * Supports 105+ languages via DeepL API (European & East Asian neural translations)
 * and Cloud / Fallback translation providers (for Indian regional & global languages).
 */
export const LANGUAGES = [
  // Pinned Primary Languages
  { code: 'en', name: 'English', nativeName: 'English', direction: 'ltr', pinned: true, provider: 'DeepL' },
  { code: 'hi', name: 'Hindi', nativeName: 'हिन्दी', direction: 'ltr', pinned: true, provider: 'Cloud' },
  { code: 'kn', name: 'Kannada', nativeName: 'ಕನ್ನಡ', direction: 'ltr', pinned: true, provider: 'Cloud' },
  { code: 'de', name: 'German', nativeName: 'Deutsch', direction: 'ltr', pinned: true, provider: 'DeepL' },
  { code: 'es', name: 'Spanish', nativeName: 'Español', direction: 'ltr', pinned: true, provider: 'DeepL' },
  { code: 'fr', name: 'French', nativeName: 'Français', direction: 'ltr', pinned: true, provider: 'DeepL' },
  { code: 'ja', name: 'Japanese', nativeName: '日本語', direction: 'ltr', pinned: true, provider: 'DeepL' },

  // Indian / Indic Regional Languages
  { code: 'ta', name: 'Tamil', nativeName: 'தமிழ்', direction: 'ltr', pinned: true, provider: 'Cloud' },
  { code: 'te', name: 'Telugu', nativeName: 'తెలుగు', direction: 'ltr', pinned: true, provider: 'Cloud' },
  { code: 'ml', name: 'Malayalam', nativeName: 'മലയാളം', direction: 'ltr', provider: 'Cloud' },
  { code: 'bn', name: 'Bengali', nativeName: 'বাংলা', direction: 'ltr', provider: 'Cloud' },
  { code: 'mr', name: 'Marathi', nativeName: 'मराठी', direction: 'ltr', provider: 'Cloud' },
  { code: 'gu', name: 'Gujarati', nativeName: 'ગુજરાતી', direction: 'ltr', provider: 'Cloud' },
  { code: 'pa', name: 'Punjabi', nativeName: 'ਪੰਜਾਬੀ', direction: 'ltr', provider: 'Cloud' },
  { code: 'ur', name: 'Urdu', nativeName: 'اردو', direction: 'rtl', provider: 'Cloud' },
  { code: 'or', name: 'Odia', nativeName: 'ଓଡ଼ିଆ', direction: 'ltr', provider: 'Cloud' },
  { code: 'as', name: 'Assamese', nativeName: 'অসমীয়া', direction: 'ltr', provider: 'Cloud' },
  { code: 'sa', name: 'Sanskrit', nativeName: 'संस्कृतम्', direction: 'ltr', provider: 'Cloud' },
  { code: 'ne', name: 'Nepali', nativeName: 'नेपाली', direction: 'ltr', provider: 'Cloud' },
  { code: 'si', name: 'Sinhala', nativeName: 'සිංහල', direction: 'ltr', provider: 'Cloud' },

  // DeepL Supported World Languages
  { code: 'zh', name: 'Chinese (Simplified)', nativeName: '简体中文', direction: 'ltr', provider: 'DeepL' },
  { code: 'pt', name: 'Portuguese', nativeName: 'Português', direction: 'ltr', provider: 'DeepL' },
  { code: 'it', name: 'Italian', nativeName: 'Italiano', direction: 'ltr', provider: 'DeepL' },
  { code: 'ru', name: 'Russian', nativeName: 'Русский', direction: 'ltr', provider: 'DeepL' },
  { code: 'nl', name: 'Dutch', nativeName: 'Nederlands', direction: 'ltr', provider: 'DeepL' },
  { code: 'pl', name: 'Polish', nativeName: 'Polski', direction: 'ltr', provider: 'DeepL' },
  { code: 'ar', name: 'Arabic', nativeName: 'العربية', direction: 'rtl', provider: 'DeepL' },
  { code: 'ko', name: 'Korean', nativeName: '한국어', direction: 'ltr', provider: 'DeepL' },
  { code: 'tr', name: 'Turkish', nativeName: 'Türkçe', direction: 'ltr', provider: 'DeepL' },
  { code: 'uk', name: 'Ukrainian', nativeName: 'Українська', direction: 'ltr', provider: 'DeepL' },
  { code: 'sv', name: 'Swedish', nativeName: 'Svenska', direction: 'ltr', provider: 'DeepL' },
  { code: 'da', name: 'Danish', nativeName: 'Dansk', direction: 'ltr', provider: 'DeepL' },
  { code: 'fi', name: 'Finnish', nativeName: 'Suomi', direction: 'ltr', provider: 'DeepL' },
  { code: 'no', name: 'Norwegian', nativeName: 'Norsk', direction: 'ltr', provider: 'DeepL' },
  { code: 'cs', name: 'Czech', nativeName: 'Čeština', direction: 'ltr', provider: 'DeepL' },
  { code: 'el', name: 'Greek', nativeName: 'Ελληνικά', direction: 'ltr', provider: 'DeepL' },
  { code: 'ro', name: 'Romanian', nativeName: 'Română', direction: 'ltr', provider: 'DeepL' },
  { code: 'hu', name: 'Hungarian', nativeName: 'Magyar', direction: 'ltr', provider: 'DeepL' },
  { code: 'sk', name: 'Slovak', nativeName: 'Slovenčina', direction: 'ltr', provider: 'DeepL' },
  { code: 'bg', name: 'Bulgarian', nativeName: 'Български', direction: 'ltr', provider: 'DeepL' },
  { code: 'id', name: 'Indonesian', nativeName: 'Bahasa Indonesia', direction: 'ltr', provider: 'DeepL' },
  { code: 'et', name: 'Estonian', nativeName: 'Eesti', direction: 'ltr', provider: 'DeepL' },
  { code: 'lt', name: 'Lithuanian', nativeName: 'Lietuvių', direction: 'ltr', provider: 'DeepL' },
  { code: 'lv', name: 'Latvian', nativeName: 'Latviešu', direction: 'ltr', provider: 'DeepL' },
  { code: 'sl', name: 'Slovenian', nativeName: 'Slovenščina', direction: 'ltr', provider: 'DeepL' },
  { code: 'he', name: 'Hebrew', nativeName: 'עברית', direction: 'rtl', provider: 'DeepL' },

  // Asian Languages
  { code: 'vi', name: 'Vietnamese', nativeName: 'Tiếng Việt', direction: 'ltr', provider: 'Cloud' },
  { code: 'th', name: 'Thai', nativeName: 'ไทย', direction: 'ltr', provider: 'Cloud' },
  { code: 'my', name: 'Burmese', nativeName: 'မြန်မာစာ', direction: 'ltr', provider: 'Cloud' },
  { code: 'km', name: 'Khmer', nativeName: 'ភាសាខ្មែរ', direction: 'ltr', provider: 'Cloud' },
  { code: 'lo', name: 'Lao', nativeName: 'ພາສາລາວ', direction: 'ltr', provider: 'Cloud' },
  { code: 'tl', name: 'Tagalog (Filipino)', nativeName: 'Tagalog', direction: 'ltr', provider: 'Cloud' },
  { code: 'ms', name: 'Malay', nativeName: 'Bahasa Melayu', direction: 'ltr', provider: 'Cloud' },
  { code: 'mn', name: 'Mongolian', nativeName: 'Монгол', direction: 'ltr', provider: 'Cloud' },

  // Middle Eastern & African Languages
  { code: 'fa', name: 'Persian (Farsi)', nativeName: 'فارسی', direction: 'rtl', provider: 'Cloud' },
  { code: 'sw', name: 'Swahili', nativeName: 'Kiswahili', direction: 'ltr', provider: 'Cloud' },
  { code: 'am', name: 'Amharic', nativeName: 'አማርኛ', direction: 'ltr', provider: 'Cloud' },
  { code: 'ha', name: 'Hausa', nativeName: 'Hausa', direction: 'ltr', provider: 'Cloud' },
  { code: 'yo', name: 'Yoruba', nativeName: 'Yorùbá', direction: 'ltr', provider: 'Cloud' },
  { code: 'ig', name: 'Igbo', nativeName: 'Asụsụ Igbo', direction: 'ltr', provider: 'Cloud' },
  { code: 'zu', name: 'Zulu', nativeName: 'isiZulu', direction: 'ltr', provider: 'Cloud' },
  { code: 'xh', name: 'Xhosa', nativeName: 'isiXhosa', direction: 'ltr', provider: 'Cloud' },
  { code: 'af', name: 'Afrikaans', nativeName: 'Afrikaans', direction: 'ltr', provider: 'Cloud' },
  { code: 'so', name: 'Somali', nativeName: 'Soomaaliga', direction: 'ltr', provider: 'Cloud' },

  // European & Caucasian Languages
  { code: 'hy', name: 'Armenian', nativeName: 'Հայերեն', direction: 'ltr', provider: 'Cloud' },
  { code: 'az', name: 'Azerbaijani', nativeName: 'Azərbaycan', direction: 'ltr', provider: 'Cloud' },
  { code: 'ka', name: 'Georgian', nativeName: 'ქართული', direction: 'ltr', provider: 'Cloud' },
  { code: 'kk', name: 'Kazakh', nativeName: 'Қазақша', direction: 'ltr', provider: 'Cloud' },
  { code: 'uz', name: 'Uzbek', nativeName: 'Oʻzbekcha', direction: 'ltr', provider: 'Cloud' },
  { code: 'tg', name: 'Tajik', nativeName: 'Тоҷикӣ', direction: 'ltr', provider: 'Cloud' },
  { code: 'ky', name: 'Kyrgyz', nativeName: 'Кыргызча', direction: 'ltr', provider: 'Cloud' },
  { code: 'is', name: 'Icelandic', nativeName: 'Íslenska', direction: 'ltr', provider: 'Cloud' },
  { code: 'ga', name: 'Irish', nativeName: 'Gaeilge', direction: 'ltr', provider: 'Cloud' },
  { code: 'cy', name: 'Welsh', nativeName: 'Cymraeg', direction: 'ltr', provider: 'Cloud' },
  { code: 'eu', name: 'Basque', nativeName: 'Euskara', direction: 'ltr', provider: 'Cloud' },
  { code: 'gl', name: 'Galician', nativeName: 'Galego', direction: 'ltr', provider: 'Cloud' },
  { code: 'ca', name: 'Catalan', nativeName: 'Català', direction: 'ltr', provider: 'Cloud' },
  { code: 'sq', name: 'Albanian', nativeName: 'Shqip', direction: 'ltr', provider: 'Cloud' },
  { code: 'mk', name: 'Macedonian', nativeName: 'Македонски', direction: 'ltr', provider: 'Cloud' },
  { code: 'bs', name: 'Bosnian', nativeName: 'Bosanski', direction: 'ltr', provider: 'Cloud' },
  { code: 'hr', name: 'Croatian', nativeName: 'Hrvatski', direction: 'ltr', provider: 'Cloud' },
  { code: 'sr', name: 'Serbian', nativeName: 'Српски', direction: 'ltr', provider: 'Cloud' },
  { code: 'be', name: 'Belarusian', nativeName: 'Беларуская', direction: 'ltr', provider: 'Cloud' },
  { code: 'mt', name: 'Maltese', nativeName: 'Malti', direction: 'ltr', provider: 'Cloud' },
  { code: 'lb', name: 'Luxembourgish', nativeName: 'Lëtzebuergesch', direction: 'ltr', provider: 'Cloud' },
  { code: 'eo', name: 'Esperanto', nativeName: 'Esperanto', direction: 'ltr', provider: 'Cloud' },
  { code: 'la', name: 'Latin', nativeName: 'Latina', direction: 'ltr', provider: 'Cloud' },
  { code: 'fy', name: 'Frisian', nativeName: 'Frysk', direction: 'ltr', provider: 'Cloud' },
  { code: 'gd', name: 'Scots Gaelic', nativeName: 'Gàidhlig', direction: 'ltr', provider: 'Cloud' },
  { code: 'yi', name: 'Yiddish', nativeName: 'ייִדיש', direction: 'rtl', provider: 'Cloud' },
  { code: 'ku', name: 'Kurdish', nativeName: 'Kurdî', direction: 'ltr', provider: 'Cloud' },
  { code: 'ps', name: 'Pashto', nativeName: 'پښتو', direction: 'rtl', provider: 'Cloud' },
  { code: 'sd', name: 'Sindhi', nativeName: 'سنڌي', direction: 'rtl', provider: 'Cloud' },
  { code: 'ug', name: 'Uyghur', nativeName: 'ئۇيغۇرچە', direction: 'rtl', provider: 'Cloud' },
  { code: 'haw', name: 'Hawaiian', nativeName: 'ʻŌlelo Hawaiʻi', direction: 'ltr', provider: 'Cloud' },
  { code: 'mi', name: 'Maori', nativeName: 'Māori', direction: 'ltr', provider: 'Cloud' },
  { code: 'sm', name: 'Samoan', nativeName: 'Gagana Sāmoa', direction: 'ltr', provider: 'Cloud' },
  { code: 'mg', name: 'Malagasy', nativeName: 'Malagasy', direction: 'ltr', provider: 'Cloud' },
  { code: 'sn', name: 'Shona', nativeName: 'chiShona', direction: 'ltr', provider: 'Cloud' },
  { code: 'st', name: 'Sesotho', nativeName: 'Sesotho', direction: 'ltr', provider: 'Cloud' },
  { code: 'su', name: 'Sundanese', nativeName: 'Basa Sunda', direction: 'ltr', provider: 'Cloud' },
  { code: 'jw', name: 'Javanese', nativeName: 'Basa Jawa', direction: 'ltr', provider: 'Cloud' },
  { code: 'ceb', name: 'Cebuano', nativeName: 'Sinugboanon', direction: 'ltr', provider: 'Cloud' },
  { code: 'ny', name: 'Chichewa', nativeName: 'Chichewa', direction: 'ltr', provider: 'Cloud' },
  { code: 'co', name: 'Corsican', nativeName: 'Corsu', direction: 'ltr', provider: 'Cloud' },
];

/** Lookup map by language code */
export const LANGUAGE_MAP = LANGUAGES.reduce((acc, lang) => {
  acc[lang.code.toLowerCase()] = lang;
  return acc;
}, {});

/** Get language details by code, fallback to English */
export const getLanguageDetails = (code) => {
  if (!code) return LANGUAGE_MAP['en'];
  const normalized = code.trim().toLowerCase();
  return LANGUAGE_MAP[normalized] || LANGUAGE_MAP['en'];
};
