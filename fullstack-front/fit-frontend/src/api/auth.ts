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

interface ApiErrorResponse {
  status: number;
  message: string;
}

async function handleAuthResponse(response: Response): Promise<AuthResponse> {
  if (!response.ok) {
    const error: ApiErrorResponse = await response.json();
    throw new Error(error.message || "Request failed");
  }
  return response.json();
}

export function login(request: LoginRequest): Promise<AuthResponse> {
  return fetch(`${API_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  }).then(handleAuthResponse);
}

export function register(request: RegisterRequest): Promise<AuthResponse> {
  return fetch(`${API_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  }).then(handleAuthResponse);
}
