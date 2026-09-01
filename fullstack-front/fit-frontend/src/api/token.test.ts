import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { clearToken, getToken, setToken } from "./token";
import { authHeaders } from "./http";

const TOKEN_KEY = "fit_token";

function encodeSegment(value: unknown): string {
  return btoa(JSON.stringify(value))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
}

function jwtWithPayload(payload: Record<string, unknown>): string {
  return `${encodeSegment({ alg: "none" })}.${encodeSegment(payload)}.sig`;
}

describe("getToken", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it("returns a token whose exp is in the future as seconds", () => {
    const token = jwtWithPayload({ exp: Math.floor(Date.now() / 1000) + 3600 });
    setToken(token);
    expect(getToken()).toBe(token);
  });

  it("clears an expired token instead of treating it as a session", () => {
    const token = jwtWithPayload({ exp: Math.floor(Date.now() / 1000) - 60 });
    localStorage.setItem(TOKEN_KEY, token);
    expect(getToken()).toBeNull();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it("does not treat JWT exp seconds as milliseconds", () => {
    const expSeconds = Math.floor(Date.now() / 1000) + 60;
    const token = jwtWithPayload({ exp: expSeconds });
    setToken(token);
    expect(expSeconds).toBeLessThan(Date.now());
    expect(getToken()).toBe(token);
  });

  it("treats missing, malformed, or unreadable tokens as logged out", () => {
    expect(getToken()).toBeNull();

    localStorage.setItem(TOKEN_KEY, "not-a-jwt");
    expect(getToken()).toBeNull();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();

    setToken(jwtWithPayload({ sub: "a@ex.com" }));
    expect(getToken()).toBeNull();
  });

  it("omits Authorization when there is no valid token", () => {
    expect(authHeaders()).toEqual({ "Content-Type": "application/json" });
    const token = jwtWithPayload({ exp: Math.floor(Date.now() / 1000) + 3600 });
    setToken(token);
    expect(authHeaders()).toEqual({
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    });
    clearToken();
    expect(authHeaders()).toEqual({ "Content-Type": "application/json" });
  });
});
