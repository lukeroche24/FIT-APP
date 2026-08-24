import { getToken } from "./token";

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

export async function handleJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error: ApiErrorResponse = await response.json();
    throw new Error(error.message || "Request failed");
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json();
}
