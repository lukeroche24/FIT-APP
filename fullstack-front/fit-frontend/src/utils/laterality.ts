import type { LoadingType } from "../api/exercises";

export type LimbPattern = "BILATERAL" | "UNILATERAL" | "ALTERNATING";

export interface LateralityFlags {
  limbPattern: LimbPattern;
  independentLoads: boolean;
}

export const DEFAULT_LATERALITY: LateralityFlags = {
  limbPattern: "BILATERAL",
  independentLoads: false,
};

export function lateralityFrom(source?: {
  limbPattern?: LimbPattern | null;
  independentLoads?: boolean | null;
  loadingType?: LoadingType | null;
  exercise?: {
    limbPattern?: LimbPattern | null;
    independentLoads?: boolean | null;
    loadingType?: LoadingType | null;
  };
} | null): LateralityFlags {
  const raw = source?.limbPattern ?? source?.exercise?.limbPattern;
  const limbPattern: LimbPattern =
    raw === "UNILATERAL" || raw === "ALTERNATING" ? raw : "BILATERAL";
  const stored = source?.independentLoads ?? source?.exercise?.independentLoads;
  const loadingType = source?.loadingType ?? source?.exercise?.loadingType;
  return {
    limbPattern,
    independentLoads: stored != null ? stored : loadingType === "DUMBBELL",
  };
}

export function isUnilateral(laterality: LateralityFlags): boolean {
  return laterality.limbPattern === "UNILATERAL";
}

export function isPerSideWeight(laterality: LateralityFlags): boolean {
  return (
    laterality.independentLoads ||
    laterality.limbPattern === "UNILATERAL" ||
    laterality.limbPattern === "ALTERNATING"
  );
}

export function perSideHint(laterality: LateralityFlags): string | null {
  if (laterality.limbPattern === "ALTERNATING") {
    return "Reps are per side";
  }
  if (laterality.limbPattern === "UNILATERAL") {
    return "Log both sides — right copies left";
  }
  if (isPerSideWeight(laterality)) {
    return "Weight is per side";
  }
  return null;
}
