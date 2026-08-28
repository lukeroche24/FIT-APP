import { useState } from "react";
import type { PlannedSetRequest, PlannedSetResponse } from "../../api/workouts";
import { trackingFrom, type TrackingFlags } from "../../utils/tracking";
import {
  lateralityFrom,
  isPerSideWeight,
  isUnilateral,
  type LateralityFlags,
} from "../../utils/laterality";
import { loadStepInputProps, snapToLoadStep } from "../../utils/loadStep";
import "./PlannedSetRow.css";

interface Props {
  set: PlannedSetResponse;
  tracking?: TrackingFlags;
  laterality?: LateralityFlags;
  addedLoad?: boolean;
  loadStep?: number | null;
  readOnly?: boolean;
  onUpdate?: (request: PlannedSetRequest) => void;
  onRemove?: () => void;
}

function formatSide(reps: number | null, weight: number | null, tracksWeight: boolean): string {
  if (reps == null && (weight == null || !tracksWeight)) {
    return "—";
  }
  const parts: string[] = [];
  if (reps != null) {
    parts.push(`${reps}`);
  }
  if (tracksWeight && weight != null) {
    parts.push(`${weight} kg`);
  }
  return parts.join("×");
}

function formatSetSummary(
  set: PlannedSetResponse,
  tracking: TrackingFlags,
  laterality: LateralityFlags,
): string {
  const parts: string[] = [];
  const perSide = isPerSideWeight(laterality);
  if (isUnilateral(laterality)) {
    parts.push(
      `L ${formatSide(set.targetReps, set.targetWeight, tracking.tracksWeight)}`,
    );
    parts.push(
      `R ${formatSide(set.rightReps, set.rightWeight, tracking.tracksWeight)}`,
    );
  } else {
    if (set.targetReps != null) {
      parts.push(laterality.limbPattern === "ALTERNATING" ? `${set.targetReps} reps/side` : `${set.targetReps} reps`);
    }
    if (tracking.tracksWeight && set.targetWeight != null) {
      parts.push(perSide ? `${set.targetWeight} kg/side` : `${set.targetWeight} kg`);
    }
  }
  if (tracking.tracksDuration && set.targetDurationSeconds != null) {
    parts.push(`${set.targetDurationSeconds}s`);
  }
  if (tracking.tracksDistance && set.targetDistance != null) {
    parts.push(`${set.targetDistance} m`);
  }
  if (set.restTimeSeconds != null) {
    parts.push(`${set.restTimeSeconds}s rest`);
  }
  return parts.length > 0 ? parts.join(" · ") : "No targets";
}

function PlannedSetRow({
  set,
  tracking,
  laterality,
  addedLoad = false,
  loadStep,
  readOnly = false,
  onUpdate,
  onRemove,
}: Props) {
  const fields = trackingFrom(tracking);
  const limbs = lateralityFrom(laterality);
  const unilateral = isUnilateral(limbs);
  const weightAttrs = loadStepInputProps(loadStep);
  const weightPlaceholder = addedLoad
    ? "Added kg"
    : isPerSideWeight(limbs)
      ? "kg / side"
      : "Weight";
  const repsPlaceholder = limbs.limbPattern === "ALTERNATING" ? "Reps / side" : "Reps";

  const [reps, setReps] = useState(set.targetReps ?? "");
  const [weight, setWeight] = useState(set.targetWeight ?? "");
  const [rightReps, setRightReps] = useState(set.rightReps ?? "");
  const [rightWeight, setRightWeight] = useState(set.rightWeight ?? "");
  const [duration, setDuration] = useState(set.targetDurationSeconds ?? "");
  const [distance, setDistance] = useState(set.targetDistance ?? "");
  const [rest, setRest] = useState(set.restTimeSeconds ?? "");

  const snapWeightValue = (value: string | number): string | number => {
    if (value === "") {
      return value;
    }
    return snapToLoadStep(Number(value), loadStep);
  };

  const commit = (nextRightReps = rightReps, nextRightWeight = rightWeight, nextWeight = weight) => {
    const snappedWeight = snapWeightValue(nextWeight);
    const snappedRightWeight = snapWeightValue(nextRightWeight);
    if (snappedWeight !== weight) {
      setWeight(snappedWeight);
    }
    if (snappedRightWeight !== rightWeight) {
      setRightWeight(snappedRightWeight);
    }
    onUpdate?.({
      targetReps: reps === "" ? undefined : Number(reps),
      targetWeight: fields.tracksWeight && snappedWeight !== "" ? Number(snappedWeight) : undefined,
      rightReps: unilateral && nextRightReps !== "" ? Number(nextRightReps) : undefined,
      rightWeight:
        unilateral && fields.tracksWeight && snappedRightWeight !== "" ? Number(snappedRightWeight) : undefined,
      targetDurationSeconds: fields.tracksDuration && duration !== "" ? Number(duration) : undefined,
      targetDistance: fields.tracksDistance && distance !== "" ? Number(distance) : undefined,
      restTimeSeconds: rest === "" ? undefined : Number(rest),
    });
  };

  const copyLeftToRightIfEmpty = () => {
    const nextReps = rightReps === "" && reps !== "" ? reps : rightReps;
    const nextWeight = rightWeight === "" && weight !== "" ? weight : rightWeight;
    if (nextReps !== rightReps) {
      setRightReps(nextReps);
    }
    if (nextWeight !== rightWeight) {
      setRightWeight(nextWeight);
    }
    commit(nextReps, nextWeight);
  };

  if (readOnly) {
    return (
      <div className="set-row d-flex align-items-center gap-2 mb-1">
        <span style={{ minWidth: "3.5rem" }}>Set {set.setNumber}</span>
        <span>{formatSetSummary(set, fields, limbs)}</span>
      </div>
    );
  }

  const repsInput = (
    side: "L" | "R" | null,
    value: string | number,
    onChange: (value: string | number) => void,
    onBlur: () => void,
  ) => (
    <input
      type="number"
      className="form-control form-control-sm"
      style={{ width: "5.5rem" }}
      placeholder={side ? `${side} ${repsPlaceholder}` : repsPlaceholder}
      value={value}
      onChange={(e) => onChange(e.target.value === "" ? "" : Number(e.target.value))}
      onBlur={onBlur}
    />
  );

  const weightInput = (
    side: "L" | "R" | null,
    value: string | number,
    onChange: (value: string | number) => void,
    onBlur: () => void,
  ) =>
    fields.tracksWeight ? (
      <input
        type="number"
        className="form-control form-control-sm"
        style={{ width: "6.5rem" }}
        placeholder={side ? `${side} ${weightPlaceholder}` : weightPlaceholder}
        value={value}
        min={weightAttrs.min}
        step={weightAttrs.step}
        onChange={(e) => onChange(e.target.value === "" ? "" : Number(e.target.value))}
        onBlur={onBlur}
      />
    ) : null;

  return (
    <div className="set-row d-flex align-items-center gap-2 mb-1 flex-wrap">
      <span style={{ minWidth: "3.5rem" }}>Set {set.setNumber}</span>
      {unilateral ? (
        <>
          {repsInput("L", reps, setReps, copyLeftToRightIfEmpty)}
          {weightInput("L", weight, setWeight, copyLeftToRightIfEmpty)}
          {repsInput("R", rightReps, setRightReps, () => commit())}
          {weightInput("R", rightWeight, setRightWeight, () => commit())}
        </>
      ) : (
        <>
          {repsInput(null, reps, setReps, () => commit())}
          {weightInput(null, weight, setWeight, () => commit())}
        </>
      )}
      {fields.tracksDuration && (
        <input
          type="number"
          className="form-control form-control-sm"
          style={{ width: "6rem" }}
          placeholder="Duration (s)"
          value={duration}
          onChange={(e) => setDuration(e.target.value === "" ? "" : Number(e.target.value))}
          onBlur={() => commit()}
        />
      )}
      {fields.tracksDistance && (
        <input
          type="number"
          className="form-control form-control-sm"
          style={{ width: "6rem" }}
          placeholder="Distance (m)"
          value={distance}
          onChange={(e) => setDistance(e.target.value === "" ? "" : Number(e.target.value))}
          onBlur={() => commit()}
        />
      )}
      <input
        type="number"
        className="form-control form-control-sm"
        style={{ width: "5rem" }}
        placeholder="Rest (s)"
        value={rest}
        onChange={(e) => setRest(e.target.value === "" ? "" : Number(e.target.value))}
        onBlur={() => commit()}
      />
      <button type="button" className="btn btn-outline-danger btn-sm" onClick={onRemove}>
        x
      </button>
    </div>
  );
}

export default PlannedSetRow;
