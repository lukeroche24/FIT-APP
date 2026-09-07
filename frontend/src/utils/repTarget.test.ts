import { describe, expect, it } from "vitest";
import { formatRepTarget, isSingleRepTarget } from "./repTarget";

describe("formatRepTarget", () => {
  it("shows a single target when min and max match", () => {
    expect(formatRepTarget(8, 8)).toBe("8 reps");
    expect(isSingleRepTarget(8, 8)).toBe(true);
  });

  it("shows a range otherwise", () => {
    expect(formatRepTarget(6, 12)).toBe("6–12 reps");
    expect(isSingleRepTarget(6, 12)).toBe(false);
  });
});
