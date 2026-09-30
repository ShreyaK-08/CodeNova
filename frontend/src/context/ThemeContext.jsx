import React, { createContext, useContext, useEffect } from 'react';

const ThemeContext = createContext(null);

/**
 * Task 5A: dark mode has been removed - CodeNova is now light-mode only. This context
 * is kept (rather than deleted outright) purely so existing call sites like
 * ProblemDetail.jsx's Monaco theme selection (`theme === 'dark' ? 'vs-dark' : 'light'`)
 * keep working unchanged and always resolve to the light theme, without needing to hunt
 * down and edit every consumer. There is no toggle, no localStorage persistence, and no
 * `data-theme` attribute anymore - index.css now defines the light palette directly on
 * `:root` as the only palette.
 */
export const ThemeProvider = ({ children }) => {
  useEffect(() => {
    document.documentElement.removeAttribute('data-theme');
  }, []);

  return (
    <ThemeContext.Provider value={{ theme: 'light' }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used within a ThemeProvider');
  return ctx;
};

export default ThemeContext;
