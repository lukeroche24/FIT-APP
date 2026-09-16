/*
 * Filename: loadStep.ts
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - Tool Used: Cursor
 * - The code in this file was written by me.
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed and understood all AI-assisted comments.
 */
/**
 * Round `weight` onto `loadStep`. Zero stays 0; a positive weight that would
 * snap to 0 is raised to one step so the field never shows an unloadable load.
 */
export function snapToLoadStep(
  weight: number,
  loadStep: number | null | undefined,
): number {
  if (loadStep == null || loadStep <= 0 || !Number.isFinite(weight)) {
    return weight;
  }
  if (weight <= 0) {
    return 0;
  }
  let snapped = Math.round(weight / loadStep) * loadStep;
  if (snapped <= 0) {
    snapped = loadStep;
  }
  return Math.round(snapped * 1000) / 1000;
}

/** HTML number-input `step`/`min`. Unknown step uses `any` so typing is free. */
export function loadStepInputProps(loadStep: number | null | undefined): {
  step: number | "any";
  min: number;
} {
  return {
    step: loadStep != null && loadStep > 0 ? loadStep : "any",
    min: 0,
  };
}
