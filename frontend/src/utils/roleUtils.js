// Central place for role-related display logic.
// Internal role values (as returned by the backend / stored in the JWT-derived
// user object) remain the existing ROLE_USER / ROLE_ADMIN strings - this file
// only maps them to clean, human-readable labels for the UI, and centralizes
// the "is this user an admin?" check so it isn't duplicated across components.

const ROLE_LABELS = {
  ROLE_ADMIN: 'Platform Administrator',
  ADMIN: 'Platform Administrator',
  ROLE_USER: 'Student / User',
  USER: 'Student / User',
};

/**
 * Returns true if the given role string (from the authenticated user) is an admin role.
 * Accepts both the "ROLE_" prefixed values used by the backend/Spring Security and the
 * unprefixed shorthand, since both appear in various parts of the existing codebase.
 */
export const isAdminRole = (role) => role === 'ROLE_ADMIN' || role === 'ADMIN';

/**
 * Returns a clean, human-readable label for a role value, without exposing the raw
 * ROLE_ADMIN / ROLE_USER internal naming in the UI. Falls back to the raw value for any
 * role not in the known list, so nothing is ever hidden/blank.
 */
export const getRoleLabel = (role) => ROLE_LABELS[role] || role || 'Unknown';
