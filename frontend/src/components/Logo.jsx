import React from 'react';

/**
 * Reusable CodeNova brand mark: a code-bracket glyph + "CN" monogram.
 * Uses currentColor for the icon strokes so it adapts to dark/light theme
 * automatically when placed in contexts that set `color`, and accepts an
 * explicit `accent` for the gradient fill used on the wordmark logos.
 */
const Logo = ({ size = 28, withWordmark = false, className = '' }) => {
  const gradientId = `codenova-grad-${size}-${withWordmark ? 'w' : 'i'}`;

  return (
    <span
      className={`codenova-logo ${className}`}
      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
    >
      <svg
        width={size}
        height={size}
        viewBox="0 0 40 40"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
      >
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="40" y2="40" gradientUnits="userSpaceOnUse">
            <stop offset="0%" stopColor="#a78bfa" />
            <stop offset="100%" stopColor="#7c3aed" />
          </linearGradient>
        </defs>
        <rect x="1" y="1" width="38" height="38" rx="10" fill={`url(#${gradientId})`} />
        <path
          d="M15.5 12.5L9.5 20L15.5 27.5"
          stroke="white"
          strokeWidth="2.6"
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
        <path
          d="M24.5 12.5L30.5 20L24.5 27.5"
          stroke="white"
          strokeWidth="2.6"
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
        <circle cx="20" cy="20" r="1.8" fill="white" />
      </svg>
      {withWordmark && (
        <span style={{ fontWeight: 800, fontSize: `${Math.round(size * 0.64)}px`, letterSpacing: '-0.02em' }}>
          Code<span style={{ color: '#a78bfa' }}>Nova</span>
        </span>
      )}
    </span>
  );
};

export default Logo;
