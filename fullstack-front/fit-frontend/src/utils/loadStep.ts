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

export function loadStepInputProps(loadStep: number | null | undefined): {
  step: number | "any";
  min: number;
} {
  return {
    step: loadStep != null && loadStep > 0 ? loadStep : "any",
    min: 0,
  };
}
