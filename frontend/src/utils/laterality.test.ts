/*
 * Filename: laterality.test.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - I decided which behaviours to test.
 * - This test file was generated with Cursor from those cases.
 * - Tool Used: Cursor
 * I have reviewed, tested, and understood all AI-generated code.
 */

import { describe, expect, it } from "vitest";
import {
  isPerSideWeight,
  isUnilateral,
  lateralityFrom,
  perSideHint,
} from "./laterality";

describe("lateralityFrom", () => {
  it("defaults missing pattern to bilateral and independent loads to dumbbell", () => {
    expect(lateralityFrom()).toEqual({ limbPattern: "BILATERAL", independentLoads: false });
    expect(lateralityFrom({ loadingType: "DUMBBELL" })).toEqual({
      limbPattern: "BILATERAL",
      independentLoads: true,
    });
    expect(lateralityFrom({ loadingType: "BARBELL" }).independentLoads).toBe(false);
  });

  it("keeps an explicit independentLoads flag even on dumbbells", () => {
    expect(lateralityFrom({ loadingType: "DUMBBELL", independentLoads: false }).independentLoads).toBe(
      false,
    );
    expect(lateralityFrom({ loadingType: "BARBELL", independentLoads: true }).independentLoads).toBe(
      true,
    );
  });

  it("reads laterality from a nested exercise when the slot omits it", () => {
    expect(
      lateralityFrom({
        exercise: { limbPattern: "UNILATERAL", independentLoads: true, loadingType: "DUMBBELL" },
      }),
    ).toEqual({ limbPattern: "UNILATERAL", independentLoads: true });
  });
});

describe("per-side UI", () => {
  it("shows per-side weight for independent loads or any non-bilateral pattern", () => {
    expect(isPerSideWeight({ limbPattern: "BILATERAL", independentLoads: true })).toBe(true);
    expect(isPerSideWeight({ limbPattern: "UNILATERAL", independentLoads: false })).toBe(true);
    expect(isPerSideWeight({ limbPattern: "ALTERNATING", independentLoads: false })).toBe(true);
    expect(isPerSideWeight({ limbPattern: "BILATERAL", independentLoads: false })).toBe(false);
    expect(isUnilateral({ limbPattern: "UNILATERAL", independentLoads: false })).toBe(true);
    expect(isUnilateral({ limbPattern: "ALTERNATING", independentLoads: false })).toBe(false);
  });

  it("labels the set inputs from the pattern", () => {
    expect(perSideHint({ limbPattern: "ALTERNATING", independentLoads: false })).toBe(
      "Reps are per side",
    );
    expect(perSideHint({ limbPattern: "UNILATERAL", independentLoads: false })).toBe(
      "Log both sides — right copies left",
    );
    expect(perSideHint({ limbPattern: "BILATERAL", independentLoads: true })).toBe(
      "Weight is per side",
    );
    expect(perSideHint({ limbPattern: "BILATERAL", independentLoads: false })).toBeNull();
  });
});
