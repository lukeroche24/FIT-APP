/*
 * Filename: loadStep.test.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains test code generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I decided which behaviours to test. Cursor wrote the file from those cases.
 * I have reviewed, tested, and understood all AI-generated code.
 */

import { describe, expect, it } from "vitest";
import { loadStepInputProps, snapToLoadStep } from "./loadStep";

describe("snapToLoadStep", () => {
  it("leaves the weight alone when the step is missing or not positive", () => {
    expect(snapToLoadStep(81.1, null)).toBe(81.1);
    expect(snapToLoadStep(81.1, 0)).toBe(81.1);
    expect(snapToLoadStep(81.1, -2.5)).toBe(81.1);
  });

  it("keeps zero at zero and raises a tiny positive load to one step", () => {
    expect(snapToLoadStep(0, 2.5)).toBe(0);
    expect(snapToLoadStep(-10, 2.5)).toBe(0);
    expect(snapToLoadStep(0.1, 2.5)).toBe(2.5);
  });

  it("rounds to the nearest step", () => {
    expect(snapToLoadStep(81.1, 2.5)).toBe(80);
    expect(snapToLoadStep(81.4, 2.5)).toBe(82.5);
  });
});

describe("loadStepInputProps", () => {
  it("uses the step when it is positive and any otherwise", () => {
    expect(loadStepInputProps(2.5)).toEqual({ step: 2.5, min: 0 });
    expect(loadStepInputProps(null)).toEqual({ step: "any", min: 0 });
    expect(loadStepInputProps(0)).toEqual({ step: "any", min: 0 });
  });
});
