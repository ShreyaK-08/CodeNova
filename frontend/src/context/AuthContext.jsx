import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

function decodeJwt(jwt) {
  try {
    const parts = jwt.split('.');
    if (parts.length < 2) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window.atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

export const AuthProvider = ({ children }) => {
  const getInitialAuth = () => {
    try {
      const urlParams = new URLSearchParams(window.location.search);
      const urlToken = urlParams.get('token');
      if (urlToken) {
        const decoded = decodeJwt(urlToken);
        if (decoded) {
          const role = Array.isArray(decoded.roles) && decoded.roles.length > 0
            ? decoded.roles[0]
            : (decoded.role || 'ROLE_USER');
          const userData = {
            id: decoded.sub ? Number(decoded.sub) : undefined,
            username: decoded.username || '',
            email: decoded.email || '',
            role: role,
          };
          localStorage.setItem('token', urlToken);
          localStorage.setItem('user', JSON.stringify(userData));
          return { token: urlToken, user: userData };
        }
      }
    } catch (e) {
      console.warn('Could not parse magic link token:', e);
    }

    const storedToken = localStorage.getItem('token') || null;
    let storedUser = null;
    if (storedToken) {
      try {
        const saved = localStorage.getItem('user');
        if (saved) storedUser = JSON.parse(saved);
      } catch (e) {
        localStorage.removeItem('user');
      }
    }
    return { token: storedToken, user: storedUser };
  };

  const initialAuth = getInitialAuth();
  const [user, setUser] = useState(initialAuth.user);
  const [token, setToken] = useState(initialAuth.token);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Sync stored user if token changes after initial load
    if (token && !user) {
      const savedUser = localStorage.getItem('user');
      if (savedUser) {
        try {
          setUser(JSON.parse(savedUser));
        } catch (e) {
          localStorage.removeItem('user');
        }
      }
    }
  }, [token, user]);

  const login = (userData, authToken) => {
    setUser(userData);
    setToken(authToken);
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('token', authToken);
  };

  const updateUser = (updatedData) => {
    setUser(updatedData);
    localStorage.setItem('user', JSON.stringify(updatedData));
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, updateUser, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
