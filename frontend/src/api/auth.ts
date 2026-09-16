/*
 * Filename: auth.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - handleAuthResponse is AI-generated so login/register can show API errors
 *   without the authenticated 401 redirect.
 * - AI-generated sections are marked with comments: // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */
const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
}

// [AI-GENERATED: Cursor]
interface ApiErrorResponse {
  status: number;
  message: string;
}

// [AI-GENERATED: Cursor]
async function handleAuthResponse(response: Response): Promise<AuthResponse> {
  if (!response.ok) {
    const error: ApiErrorResponse = await response.json();
    throw new Error(error.message || "Request failed");
  }
  return response.json();
}

/**
 * Public login. Uses its own error path so a 401 stays on this page instead
 * of bouncing through the authenticated client.
 */
export function login(request: LoginRequest): Promise<AuthResponse> {
  return fetch(`${API_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  }).then(handleAuthResponse);
}

/** Public register; returns a JWT so the client does not need a second login. */
export function register(request: RegisterRequest): Promise<AuthResponse> {
  return fetch(`${API_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  }).then(handleAuthResponse);
}
