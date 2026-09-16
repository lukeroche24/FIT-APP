/*
 * Filename: http.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - Spread syntax in authHeaders, API error-body parsing, and the 401 logout path are AI-generated.
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */
import { clearToken, getToken } from "./token";

/**
 * Bearer token plus JSON content type. {@link getToken} returns null when the
 * JWT is missing or expired, so the header is omitted in that case.
 */
export function authHeaders(): HeadersInit {
  const token = getToken();
  return {
    "Content-Type": "application/json",
    // [AI-GENERATED: Cursor]
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

// [AI-GENERATED: Cursor]
interface ApiErrorResponse {
  status: number;
  message: string;
}

// [AI-GENERATED: Cursor]
function redirectToLogin(): void {
  clearToken();
  const path = window.location.pathname;
  if (path !== "/login" && path !== "/register") {
    window.location.replace("/login");
  }
}

/**
 * Parses JSON API responses. 401 clears the token and sends the user to
 * login (except on login/register). Empty error bodies become "Request failed".
 * 403/404 are left as errors so hidden resources do not look like a logout.
 */
// [AI-GENERATED: Cursor]
export async function handleJsonResponse<T>(response: Response): Promise<T> {
  if (response.status === 401) {
    redirectToLogin();
    throw new Error("Please log in");
  }
  if (!response.ok) {
    const fallback = "Request failed";
    const body = await response.text();
    if (body) {
      try {
        const error: ApiErrorResponse = JSON.parse(body);
        throw new Error(error.message || fallback);
      } catch (err) {
        if (!(err instanceof SyntaxError)) {
          throw err;
        }
      }
    }
    throw new Error(fallback);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json();
}
