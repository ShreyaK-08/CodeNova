import React, { useState, useRef, useEffect, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { Globe, Search, Check, ChevronDown, X, Loader2, AlertCircle } from 'lucide-react';
import { LANGUAGES, getLanguageDetails } from '../i18n/languages';
import { changeAppLanguage } from '../i18n/i18n';

const LanguageSelector = () => {
  const { i18n, t } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [notice, setNotice] = useState(null);
  const dropdownRef = useRef(null);
  const searchInputRef = useRef(null);

  const currentLangCode = (i18n.language || 'en').trim().toLowerCase();
  const currentDetails = getLanguageDetails(currentLangCode);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (isOpen && searchInputRef.current) {
      searchInputRef.current.focus();
    }
  }, [isOpen]);

  // Listen for non-blocking translation notices (e.g. DeepL unavailable)
  useEffect(() => {
    const handleNotice = (e) => {
      const msg = e.detail?.message || 'Translation is temporarily unavailable. Showing English.';
      setNotice(msg);
      setTimeout(() => setNotice(null), 5000);
    };
    window.addEventListener('codenova_translation_notice', handleNotice);
    return () => window.removeEventListener('codenova_translation_notice', handleNotice);
  }, []);

  const handleSelectLanguage = async (code) => {
    setIsLoading(true);
    setIsOpen(false);
    setSearch('');
    try {
      await changeAppLanguage(code);
    } catch (err) {
      console.warn('Language switch encountered an issue:', err);
      setNotice('Translation is temporarily unavailable. Showing English.');
      setTimeout(() => setNotice(null), 5000);
    } finally {
      setIsLoading(false);
    }
  };

  const filteredLanguages = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) return LANGUAGES;
    return LANGUAGES.filter(
      (l) =>
        l.name.toLowerCase().includes(query) ||
        l.nativeName.toLowerCase().includes(query) ||
        l.code.toLowerCase().includes(query)
    );
  }, [search]);

  const pinnedLanguages = useMemo(() => {
    return LANGUAGES.filter((l) => l.pinned);
  }, []);

  return (
    <>
      <div className="language-selector-wrapper" ref={dropdownRef} style={{ position: 'relative' }}>
        <button
          type="button"
          className="language-selector-trigger"
          onClick={() => setIsOpen((prev) => !prev)}
          title={t('common.selectLanguage', 'Select Language')}
          aria-label="Change language"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.35rem 0.75rem',
            height: '36px',
            width: 'auto',
            minWidth: '120px',
            borderRadius: 'var(--radius-md)',
            border: isOpen ? '1px solid var(--primary)' : '1px solid var(--border-color)',
            background: isOpen ? 'var(--primary-soft)' : 'var(--bg-card)',
            color: 'var(--text-main)',
            fontSize: '0.85rem',
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'all 0.2s ease',
          }}
          onMouseEnter={(e) => {
            if (!isOpen) {
              e.currentTarget.style.borderColor = 'var(--primary)';
              e.currentTarget.style.background = 'var(--bg-card-hover)';
            }
          }}
          onMouseLeave={(e) => {
            if (!isOpen) {
              e.currentTarget.style.borderColor = 'var(--border-color)';
              e.currentTarget.style.background = 'var(--bg-card)';
            }
          }}
        >
          <span
            className="lang-icon-badge"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '22px',
              height: '22px',
              borderRadius: '5px',
              background: 'var(--primary-soft)',
              color: 'var(--primary)',
              flexShrink: 0,
            }}
          >
            {isLoading ? (
              <Loader2 size={13} className="animate-spin" />
            ) : (
              <Globe size={14} strokeWidth={2.2} />
            )}
          </span>
          <span
            className="language-name-label"
            style={{
              maxWidth: '100px',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              flex: 1,
              textAlign: 'left',
            }}
          >
            {currentDetails?.nativeName || currentDetails?.name || 'English'}
          </span>
          <ChevronDown
            size={14}
            color="var(--text-muted)"
            style={{ transform: isOpen ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s', flexShrink: 0 }}
          />
        </button>

        {isOpen && (
          <div
            className="navbar-dropdown language-dropdown-panel"
            style={{
              position: 'absolute',
              top: 'calc(100% + 6px)',
              right: 0,
              width: '290px',
              maxHeight: '400px',
              display: 'flex',
              flexDirection: 'column',
              background: 'var(--bg-card)',
              borderRadius: 'var(--radius-lg)',
              border: '1px solid var(--border-color)',
              boxShadow: 'var(--shadow-lg)',
              zIndex: 1100,
              overflow: 'hidden',
            }}
          >
            {/* Search Header */}
            <div
              style={{
                padding: '0.75rem',
                borderBottom: '1px solid var(--border-color)',
                background: 'var(--bg-elevated)',
              }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.35rem 0.65rem',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--border-color)',
                  background: 'var(--bg-input)',
                }}
              >
                <Search size={14} color="var(--text-muted)" />
                <input
                  ref={searchInputRef}
                  type="text"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="Search 100+ languages..."
                  style={{
                    border: 'none',
                    outline: 'none',
                    width: '100%',
                    fontSize: '0.85rem',
                    color: 'var(--text-main)',
                    background: 'transparent',
                  }}
                />
                {search && (
                  <button
                    type="button"
                    onClick={() => setSearch('')}
                    style={{ border: 'none', background: 'transparent', cursor: 'pointer', padding: 0, display: 'flex' }}
                  >
                    <X size={13} color="var(--text-muted)" />
                  </button>
                )}
              </div>
            </div>

            {/* Language Options List */}
            <div style={{ overflowY: 'auto', flex: 1, padding: '0.35rem 0' }}>
              {!search && (
                <>
                  <div
                    style={{
                    padding: '0.4rem 0.85rem 0.2rem',
                    fontSize: '0.7rem',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    color: 'var(--text-subtle)',
                    letterSpacing: '0.05em',
                  }}
                >
                  Featured Languages
                </div>
                {pinnedLanguages.map((lang) => {
                  const isSelected = currentLangCode === lang.code;
                  return (
                    <button
                      key={`pinned-${lang.code}`}
                      type="button"
                      onClick={() => handleSelectLanguage(lang.code)}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        width: '100%',
                        padding: '0.45rem 0.85rem',
                        border: 'none',
                        background: isSelected ? 'var(--primary-soft)' : 'transparent',
                        color: isSelected ? 'var(--primary)' : 'var(--text-main)',
                        fontSize: '0.85rem',
                        textAlign: 'left',
                        cursor: 'pointer',
                        transition: 'background 0.15s ease',
                      }}
                      onMouseEnter={(e) => {
                        if (!isSelected) e.currentTarget.style.background = 'var(--bg-card-hover)';
                      }}
                      onMouseLeave={(e) => {
                        if (!isSelected) e.currentTarget.style.background = 'transparent';
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span style={{ fontSize: '0.65rem', fontWeight: 700, padding: '1px 5px', borderRadius: '4px', background: isSelected ? 'var(--primary)' : 'var(--bg-elevated)', color: isSelected ? '#fff' : 'var(--text-subtle)', border: '1px solid var(--border-color)', minWidth: '24px', textAlign: 'center' }}>
                          {lang.code.toUpperCase()}
                        </span>
                        <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.45rem' }}>
                          <span style={{ fontWeight: 600 }}>{lang.nativeName}</span>
                          {lang.name !== lang.nativeName && (
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>({lang.name})</span>
                          )}
                        </div>
                      </div>
                      {isSelected && <Check size={14} color="var(--primary)" />}
                    </button>
                  );
                })}
                <div style={{ height: '1px', background: 'var(--border-color)', margin: '0.35rem 0' }} />
                <div
                  style={{
                    padding: '0.35rem 0.85rem 0.2rem',
                    fontSize: '0.7rem',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    color: 'var(--text-subtle)',
                    letterSpacing: '0.05em',
                  }}
                >
                  All Languages ({LANGUAGES.length})
                </div>
              </>
            )}

            {filteredLanguages.length === 0 ? (
              <div style={{ padding: '1.25rem 0.85rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                No matching languages found
              </div>
            ) : (
              filteredLanguages.map((lang) => {
                const isSelected = currentLangCode === lang.code;
                return (
                  <button
                    key={lang.code}
                    type="button"
                    onClick={() => handleSelectLanguage(lang.code)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      width: '100%',
                      padding: '0.45rem 0.85rem',
                      border: 'none',
                      background: isSelected ? 'var(--primary-soft)' : 'transparent',
                      color: isSelected ? 'var(--primary)' : 'var(--text-main)',
                      fontSize: '0.85rem',
                      textAlign: 'left',
                      cursor: 'pointer',
                      transition: 'background 0.15s ease',
                    }}
                    onMouseEnter={(e) => {
                      if (!isSelected) e.currentTarget.style.background = 'var(--bg-card-hover)';
                    }}
                    onMouseLeave={(e) => {
                      if (!isSelected) e.currentTarget.style.background = 'transparent';
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <span style={{ fontSize: '0.65rem', fontWeight: 700, padding: '1px 5px', borderRadius: '4px', background: isSelected ? 'var(--primary)' : 'var(--bg-elevated)', color: isSelected ? '#fff' : 'var(--text-subtle)', border: '1px solid var(--border-color)', minWidth: '24px', textAlign: 'center' }}>
                        {lang.code.toUpperCase()}
                      </span>
                      <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.45rem' }}>
                        <span style={{ fontWeight: 600 }}>{lang.nativeName}</span>
                        {lang.name !== lang.nativeName && (
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>({lang.name})</span>
                        )}
                      </div>
                    </div>
                    {isSelected && <Check size={14} color="var(--primary)" />}
                  </button>
                );
              })
            )}
          </div>

          {/* Provider Footer */}
          <div
            style={{
              padding: '0.45rem 0.85rem',
              borderTop: '1px solid var(--border-color)',
              background: 'var(--bg-elevated)',
              fontSize: '0.72rem',
              color: 'var(--text-muted)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <span>Multi-Engine (DeepL + Cloud)</span>
            <span style={{ fontSize: '0.68rem', opacity: 0.8 }}>{LANGUAGES.length} Languages</span>
          </div>
          </div>
        )}
      </div>

      {/* Non-blocking translation notification toast */}
      {notice && (
        <div
          role="status"
          style={{
            position: 'fixed',
            bottom: '1.5rem',
            right: '1.5rem',
            background: 'var(--bg-card)',
            border: '1px solid #f59e0b',
            borderLeft: '4px solid #f59e0b',
            color: 'var(--text-main)',
            padding: '0.75rem 1rem',
            borderRadius: 'var(--radius-md)',
            boxShadow: 'var(--shadow-lg)',
            zIndex: 9999,
            display: 'flex',
            alignItems: 'center',
            gap: '0.65rem',
            fontSize: '0.85rem',
            maxWidth: '380px',
            animation: 'fadeIn 0.2s ease-in-out',
          }}
        >
          <AlertCircle size={18} color="#f59e0b" style={{ flexShrink: 0 }} />
          <span style={{ flex: 1 }}>{notice}</span>
          <button
            type="button"
            onClick={() => setNotice(null)}
            style={{
              border: 'none',
              background: 'transparent',
              color: 'var(--text-muted)',
              cursor: 'pointer',
              padding: '0.15rem',
              display: 'flex',
              alignItems: 'center',
            }}
            aria-label="Dismiss notice"
          >
            <X size={14} />
          </button>
        </div>
      )}
    </>
  );
};

export default LanguageSelector;
