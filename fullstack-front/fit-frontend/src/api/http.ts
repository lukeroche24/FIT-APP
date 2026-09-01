import { clearToken, getToken } from "./token";

/**
 * Bearer token plus JSON content type. {@link getToken} returns null when the
 * JWT is missing or expired, so the header is omitted in that case.
 */
export function authHeaders(): HeadersInit {
  const token = getToken();
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

interface ApiErrorResponse {
  status: number;
  message: string;
}

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
