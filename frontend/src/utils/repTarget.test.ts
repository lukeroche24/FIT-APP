/*
 * Filename: repTarget.test.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

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
