import { describe, expect, it } from "vitest";
import { trackingFrom } from "./tracking";

describe("trackingFrom", () => {
  it("turns weight on by default so a missing flag still shows kg", () => {
    expect(trackingFrom()).toEqual({
      tracksWeight: true,
      tracksDuration: false,
      tracksDistance: false,
    });
    expect(trackingFrom({ tracksWeight: null }).tracksWeight).toBe(true);
    expect(trackingFrom({ tracksWeight: true }).tracksWeight).toBe(true);
    expect(trackingFrom({ tracksWeight: false }).tracksWeight).toBe(false);
  });

  it("turns duration and distance off unless they are explicitly true", () => {
    expect(trackingFrom({ tracksDuration: null, tracksDistance: null })).toEqual({
      tracksWeight: true,
      tracksDuration: false,
      tracksDistance: false,
    });
    expect(trackingFrom({ tracksDuration: true, tracksDistance: true })).toEqual({
      tracksWeight: true,
      tracksDuration: true,
      tracksDistance: true,
    });
  });
});
