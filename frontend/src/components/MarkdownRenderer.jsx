import React from 'react';

/**
 * Lightweight, dependency-free Markdown renderer component for problem descriptions,
 * hints, complexity notes, and editorial solutions.
 */
const MarkdownRenderer = ({ content }) => {
  if (!content) return null;

  // Split content by code blocks first
  const parts = [];
  const codeBlockRegex = /```([a-zA-Z0-9_-]*)\n([\s\S]*?)```/g;
  let lastIndex = 0;
  let match;

  while ((match = codeBlockRegex.exec(content)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ type: 'text', text: content.slice(lastIndex, match.index) });
    }
    parts.push({ type: 'code', lang: match[1] || 'text', code: match[2].trimEnd() });
    lastIndex = match.index + match[0].length;
  }

  if (lastIndex < content.length) {
    parts.push({ type: 'text', text: content.slice(lastIndex) });
  }

  const renderInline = (text) => {
    // Process bold, italic, inline code
    const tokens = [];
    const inlineRegex = /(`[^`]+`|\*\*[^*]+\*\*|\*[^*]+\*)/g;
    let idx = 0;
    let m;

    while ((m = inlineRegex.exec(text)) !== null) {
      if (m.index > idx) {
        tokens.push(text.slice(idx, m.index));
      }
      const token = m[0];
      if (token.startsWith('`') && token.endsWith('`')) {
        tokens.push(
          <code key={m.index} style={{
            background: 'var(--bg-main)',
            padding: '2px 6px',
            borderRadius: '4px',
            color: 'var(--primary)',
            fontSize: '0.88em',
            border: '1px solid var(--border-color)',
            fontWeight: 600,
          }}>
            {token.slice(1, -1)}
          </code>
        );
      } else if (token.startsWith('**') && token.endsWith('**')) {
        tokens.push(<strong key={m.index} style={{ color: 'var(--text-main)', fontWeight: 700 }}>{token.slice(2, -2)}</strong>);
      } else if (token.startsWith('*') && token.endsWith('*')) {
        tokens.push(<em key={m.index}>{token.slice(1, -1)}</em>);
      }
      idx = m.index + token.length;
    }

    if (idx < text.length) {
      tokens.push(text.slice(idx));
    }

    return tokens;
  };

  const renderTextBlock = (text, blockIdx) => {
    const lines = text.split('\n');
    const elements = [];
    let currentList = [];

    const flushList = (listKey) => {
      if (currentList.length > 0) {
        elements.push(
          <ul key={`ul-${listKey}`} style={{ paddingLeft: '1.5rem', marginBottom: '0.75rem', lineHeight: 1.6, color: 'var(--text-main)' }}>
            {currentList.map((item, i) => (
              <li key={i} style={{ marginBottom: '0.35rem', color: 'var(--text-main)' }}>{renderInline(item)}</li>
            ))}
          </ul>
        );
        currentList = [];
      }
    };

    lines.forEach((line, lineIdx) => {
      const trimmed = line.trim();

      if (trimmed.startsWith('# ')) {
        flushList(lineIdx);
        elements.push(<h2 key={lineIdx} style={{ fontSize: '1.35rem', fontWeight: 800, marginTop: '1.25rem', marginBottom: '0.5rem', color: 'var(--text-main)' }}>{renderInline(trimmed.slice(2))}</h2>);
      } else if (trimmed.startsWith('## ')) {
        flushList(lineIdx);
        elements.push(<h3 key={lineIdx} style={{ fontSize: '1.15rem', fontWeight: 700, marginTop: '1rem', marginBottom: '0.4rem', color: 'var(--primary)' }}>{renderInline(trimmed.slice(3))}</h3>);
      } else if (trimmed.startsWith('### ')) {
        flushList(lineIdx);
        elements.push(<h4 key={lineIdx} style={{ fontSize: '1rem', fontWeight: 700, marginTop: '0.85rem', marginBottom: '0.35rem', color: 'var(--text-main)' }}>{renderInline(trimmed.slice(4))}</h4>);
      } else if (trimmed.startsWith('- ') || trimmed.startsWith('* ')) {
        currentList.push(trimmed.slice(2));
      } else if (/^\d+\.\s/.test(trimmed)) {
        flushList(lineIdx);
        const matchNum = trimmed.match(/^(\d+\.\s)(.*)$/);
        elements.push(
          <div key={lineIdx} style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.4rem', lineHeight: 1.6, color: 'var(--text-main)' }}>
            <span style={{ fontWeight: 700, color: 'var(--primary)' }}>{matchNum[1]}</span>
            <span>{renderInline(matchNum[2])}</span>
          </div>
        );
      } else if (trimmed === '') {
        flushList(lineIdx);
      } else {
        flushList(lineIdx);
        elements.push(
          <p key={lineIdx} style={{ marginBottom: '0.75rem', lineHeight: 1.65, color: 'var(--text-main)' }}>
            {renderInline(line)}
          </p>
        );
      }
    });

    flushList('end');
    return <div key={blockIdx}>{elements}</div>;
  };

  return (
    <div className="markdown-content" style={{ fontSize: '0.95rem', color: 'var(--text-main)' }}>
      {parts.map((p, idx) => {
        if (p.type === 'code') {
          return (
            <div key={idx} style={{
              position: 'relative',
              margin: '1rem 0',
              borderRadius: '8px',
              overflow: 'hidden',
              border: '1px solid var(--border-color)',
              background: '#0a0a12',
            }}>
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '0.35rem 0.85rem',
                background: 'rgba(255, 255, 255, 0.04)',
                borderBottom: '1px solid var(--border-color)',
                fontSize: '0.75rem',
                color: 'var(--text-subtle)',
                fontWeight: 600,
                textTransform: 'uppercase',
              }}>
                <span>{p.lang}</span>
                <button
                  onClick={() => navigator.clipboard?.writeText(p.code)}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: 'var(--primary)',
                    cursor: 'pointer',
                    fontSize: '0.75rem',
                  }}
                  title="Copy code"
                >
                  Copy
                </button>
              </div>
              <pre style={{
                padding: '1rem',
                margin: 0,
                overflowX: 'auto',
                fontFamily: 'JetBrains Mono, monospace',
                fontSize: '0.875rem',
                lineHeight: 1.55,
                color: '#e2e8f0',
              }}>
                <code>{p.code}</code>
              </pre>
            </div>
          );
        }
        return renderTextBlock(p.text, idx);
      })}
    </div>
  );
};

export default MarkdownRenderer;
