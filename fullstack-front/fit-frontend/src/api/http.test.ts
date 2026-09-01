import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { handleJsonResponse } from "./http";
import { setToken } from "./token";

function mockPath(pathname: string) {
  const replace = vi.fn();
  vi.stubGlobal("location", { pathname, replace });
  return replace;
}

describe("handleJsonResponse", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
  });

  it("clears the token and redirects to login on 401", async () => {
    setToken("stale");
    const replace = mockPath("/workouts");

    await expect(handleJsonResponse(new Response(null, { status: 401 }))).rejects.toThrow(
      "Please log in",
    );

    expect(localStorage.getItem("fit_token")).toBeNull();
    expect(replace).toHaveBeenCalledWith("/login");
  });

  it("clears the token on 401 from login without bouncing the page", async () => {
    setToken("stale");
    const replace = mockPath("/login");

    await expect(handleJsonResponse(new Response(null, { status: 401 }))).rejects.toThrow(
      "Please log in",
    );

    expect(localStorage.getItem("fit_token")).toBeNull();
    expect(replace).not.toHaveBeenCalled();
  });

  it("does not log out on 403 or 404", async () => {
    setToken("keep-me");
    const replace = mockPath("/workouts");

    await expect(
      handleJsonResponse(
        new Response(JSON.stringify({ status: 403, message: "Forbidden" }), { status: 403 }),
      ),
    ).rejects.toThrow("Forbidden");

    await expect(
      handleJsonResponse(
        new Response(JSON.stringify({ status: 404, message: "Not found" }), { status: 404 }),
      ),
    ).rejects.toThrow("Not found");

    expect(localStorage.getItem("fit_token")).toBe("keep-me");
    expect(replace).not.toHaveBeenCalled();
  });

  it("returns JSON on success and undefined on 204", async () => {
    await expect(
      handleJsonResponse<{ id: number }>(
        new Response(JSON.stringify({ id: 7 }), { status: 200 }),
      ),
    ).resolves.toEqual({ id: 7 });

    await expect(handleJsonResponse(new Response(null, { status: 204 }))).resolves.toBeUndefined();
  });

  it("uses a generic message when the error body is empty", async () => {
    await expect(handleJsonResponse(new Response("", { status: 500 }))).rejects.toThrow(
      "Request failed",
    );
  });
});
