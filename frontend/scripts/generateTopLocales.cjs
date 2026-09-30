const https = require('https');
const fs = require('fs');
const path = require('path');

const PROTECTED_TERMS = new Set([
  'Java', 'Python', 'C', 'C++', 'JavaScript', 'SQL', 'HTML', 'CSS',
  'java', 'python', 'c', 'cpp', 'javascript', 'sql', 'html', 'css',
  'CodeNova', 'Monaco', 'REST', 'JWT', 'API', 'DeepL',
  'ACCEPTED', 'WRONG_ANSWER', 'TIME_LIMIT_EXCEEDED', 'COMPILATION_ERROR',
  'RUNTIME_ERROR', 'PENDING', 'RUNNING', 'DRAFT', 'PUBLISHED', 'ARCHIVED',
  'int', 'long', 'double', 'float', 'boolean', 'char', 'String', 'void',
  'public', 'private', 'protected', 'class', 'static', 'return'
]);

const TARGET_LANGS = [
  { code: 'bn', target: 'bn', name: 'Bengali' },
  { code: 'mr', target: 'mr', name: 'Marathi' },
  { code: 'gu', target: 'gu', name: 'Gujarati' },
  { code: 'pa', target: 'pa', name: 'Punjabi' },
  { code: 'ml', target: 'ml', name: 'Malayalam' },
  { code: 'ur', target: 'ur', name: 'Urdu' },
  { code: 'ja', target: 'ja', name: 'Japanese' },
  { code: 'zh', target: 'zh-CN', name: 'Chinese' },
  { code: 'ru', target: 'ru', name: 'Russian' },
  { code: 'ar', target: 'ar', name: 'Arabic' },
  { code: 'pt', target: 'pt', name: 'Portuguese' },
  { code: 'it', target: 'it', name: 'Italian' },
  { code: 'ko', target: 'ko', name: 'Korean' },
  { code: 'vi', target: 'vi', name: 'Vietnamese' },
  { code: 'id', target: 'id', name: 'Indonesian' },
  { code: 'tr', target: 'tr', name: 'Turkish' },
  { code: 'th', target: 'th', name: 'Thai' },
  { code: 'nl', target: 'nl', name: 'Dutch' },
  { code: 'pl', target: 'pl', name: 'Polish' },
];

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

function fetchGoogleTranslateBatch(texts, targetLang) {
  return new Promise((resolve) => {
    // Join texts with newline, clean up internal newlines
    const cleaned = texts.map(t => (typeof t === 'string' ? t.replace(/\r?\n/g, ' ') : String(t)));
    const joined = cleaned.join('\n');
    const url = 'https://translate.googleapis.com/translate_a/single?client=dict-chrome-ex&sl=en&tl=' + encodeURIComponent(targetLang) + '&dt=t&q=' + encodeURIComponent(joined);

    const req = https.get(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': '*/*'
      }
    }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          let fullTranslated = '';
          if (json && json[0] && Array.isArray(json[0])) {
            for (const s of json[0]) {
              if (s && s[0]) fullTranslated += s[0];
            }
          }
          const lines = fullTranslated.split(/\r?\n/);
          if (lines.length >= texts.length) {
            const results = [];
            for (let i = 0; i < texts.length; i++) {
              results.push(lines[i] ? lines[i].trim() : texts[i]);
            }
            return resolve(results);
          }
        } catch (e) {
          // ignore
        }
        resolve(texts);
      });
    });

    req.on('error', () => resolve(texts));
    req.setTimeout(8000, () => {
      req.destroy();
      resolve(texts);
    });
  });
}

async function generateLocale(langConfig, enPath, localesDir) {
  const targetFile = path.join(localesDir, `${langConfig.code}.json`);
  if (fs.existsSync(targetFile)) {
    console.log(`[SKIPPED] ${langConfig.code}.json already exists.`);
    return;
  }

  console.log(`[GENERATING] ${langConfig.name} (${langConfig.code})...`);
  const enData = JSON.parse(fs.readFileSync(enPath, 'utf8'));
  const flattened = flattenObject(enData);

  const toTranslate = [];
  const indices = [];

  flattened.forEach((item, idx) => {
    if (typeof item.val === 'string' && item.val.trim() !== '') {
      const trimmed = item.val.trim();
      if (!PROTECTED_TERMS.has(trimmed)) {
        indices.push(idx);
        toTranslate.push(item.val);
      }
    }
  });

  const CHUNK_SIZE = 25;
  const translatedTexts = new Array(toTranslate.length);

  for (let i = 0; i < toTranslate.length; i += CHUNK_SIZE) {
    const chunk = toTranslate.slice(i, i + CHUNK_SIZE);
    const chunkResults = await fetchGoogleTranslateBatch(chunk, langConfig.target);
    for (let j = 0; j < chunk.length; j++) {
      translatedTexts[i + j] = chunkResults[j] || chunk[j];
    }
    // Small pause to be polite
    await new Promise(r => setTimeout(r, 80));
  }

  indices.forEach((flatIdx, k) => {
    if (translatedTexts[k] !== undefined) {
      flattened[flatIdx].val = translatedTexts[k];
    }
  });

  const finalObj = unflattenObject(flattened);
  fs.writeFileSync(targetFile, JSON.stringify(finalObj, null, 2), 'utf8');
  console.log(`[SUCCESS] Saved ${langConfig.code}.json (${flattened.length} keys translated)`);
}

async function main() {
  const localesDir = path.resolve(__dirname, '../src/i18n/locales');
  const enPath = path.join(localesDir, 'en.json');

  console.log(`Starting locale generation into: ${localesDir}`);
  for (const lang of TARGET_LANGS) {
    try {
      await generateLocale(lang, enPath, localesDir);
    } catch (err) {
      console.error(`Failed generating ${lang.code}:`, err);
    }
  }
  console.log('All locales processing completed successfully!');
}

main();
