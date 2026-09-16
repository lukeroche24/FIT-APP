/*
 * Filename: token.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - JWT expiry checking in getToken and tokenIsExpired is AI-generated.
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */
const TOKEN_KEY = "fit_token";

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * Reads the stored JWT. Missing, malformed, or expired tokens are cleared
 * and treated as logged out so the UI never treats a stale string as a session.
 */
// [AI-GENERATED: Cursor]
export function getToken(): string | null {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token || tokenIsExpired(token)) {
    localStorage.removeItem(TOKEN_KEY);
    return null;
  }
  return token;
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

// [AI-GENERATED: Cursor]
function tokenIsExpired(token: string): boolean {
  try {
    const parts = token.split(".");
    if (parts.length < 2) {
      return true;
    }
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
    const payload = JSON.parse(atob(padded)) as { exp?: number };
    if (typeof payload.exp !== "number") {
      return true;
    }
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}
