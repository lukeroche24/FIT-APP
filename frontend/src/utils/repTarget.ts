export function formatRepTarget(
  minReps: number | null | undefined,
  maxReps: number | null | undefined,
): string {
  const min = minReps ?? 6;
  const max = maxReps ?? 12;
  return min === max ? `${min} reps` : `${min}–${max} reps`;
}

export function isSingleRepTarget(
  minReps: number | null | undefined,
  maxReps: number | null | undefined,
): boolean {
  return minReps != null && maxReps != null && minReps === maxReps;
}
