import { useState } from "react";
import type { LoggedSetRequest, LoggedSetResponse } from "../../api/workoutLogs";
import { trackingFrom, type TrackingFlags } from "../../utils/tracking";
import {
  lateralityFrom,
  isPerSideWeight,
  isUnilateral,
  type LateralityFlags,
} from "../../utils/laterality";
import { loadStepInputProps, snapToLoadStep } from "../../utils/loadStep";
import "./LoggedSetRow.css";

interface Props {
  set: LoggedSetResponse;
  tracking?: TrackingFlags;
  laterality?: LateralityFlags;
  addedLoad?: boolean;
  loadStep?: number | null;
  readOnly?: boolean;
  onUpdate?: (request: LoggedSetRequest) => void;
  onRemove?: () => void;
}

function formatSide(reps: number | null, weight: number | null, tracksWeight: boolean, failed: boolean): string {
  const parts: string[] = [];
  if (reps != null) {
    parts.push(`${reps}`);
  }
  if (tracksWeight && weight != null) {
    parts.push(`${weight} kg`);
  }
  const body = parts.length > 0 ? parts.join(" × ") : "—";
  return failed ? `${body} (fail)` : body;
}

function formatLoggedSetSummary(
  set: LoggedSetResponse,
  tracking: TrackingFlags,
  laterality: LateralityFlags,
): string {
  const parts: string[] = [];
  const perSide = isPerSideWeight(laterality);
  if (isUnilateral(laterality)) {
    parts.push(`L ${formatSide(set.actualReps, set.actualWeight, tracking.tracksWeight, set.failed === true)}`);
    parts.push(`R ${formatSide(set.rightReps, set.rightWeight, tracking.tracksWeight, set.rightFailed === true)}`);
  } else {
    if (set.actualReps != null) {
      parts.push(laterality.limbPattern === "ALTERNATING" ? `${set.actualReps} reps/side` : `${set.actualReps} reps`);
    }
    if (tracking.tracksWeight && set.actualWeight != null) {
      parts.push(perSide ? `${set.actualWeight} kg/side` : `${set.actualWeight} kg`);
    }
    if (set.failed) {
      parts.push("Failed");
    }
  }
  if (tracking.tracksDuration && set.actualDurationSeconds != null) {
    parts.push(`${set.actualDurationSeconds}s`);
  }
  if (tracking.tracksDistance && set.actualDistance != null) {
    parts.push(`${set.actualDistance} m`);
  }
  if (set.notes) {
    parts.push(set.notes);
  }
  return parts.length > 0 ? parts.join(" · ") : "No data";
}

function LoggedSetRow({
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

  const [reps, setReps] = useState(set.actualReps ?? "");
  const [weight, setWeight] = useState(set.actualWeight ?? "");
  const [rightReps, setRightReps] = useState(set.rightReps ?? "");
  const [rightWeight, setRightWeight] = useState(set.rightWeight ?? "");
  const [duration, setDuration] = useState(set.actualDurationSeconds ?? "");
  const [distance, setDistance] = useState(set.actualDistance ?? "");
  const [notes, setNotes] = useState(set.notes ?? "");
  const [failed, setFailed] = useState(set.failed === true);
  const [rightFailed, setRightFailed] = useState(set.rightFailed === true);

  const snapWeightValue = (value: string | number): string | number => {
    if (value === "") {
      return value;
    }
    return snapToLoadStep(Number(value), loadStep);
  };

  const commit = (
    nextRightReps = rightReps,
    nextRightWeight = rightWeight,
    nextFailed = failed,
    nextRightFailed = rightFailed,
    nextWeight = weight,
  ) => {
    const snappedWeight = snapWeightValue(nextWeight);
    const snappedRightWeight = snapWeightValue(nextRightWeight);
    if (snappedWeight !== weight) {
      setWeight(snappedWeight);
    }
    if (snappedRightWeight !== rightWeight) {
      setRightWeight(snappedRightWeight);
    }
    onUpdate?.({
      actualReps: reps === "" ? undefined : Number(reps),
      actualWeight: fields.tracksWeight && snappedWeight !== "" ? Number(snappedWeight) : undefined,
      rightReps: unilateral && nextRightReps !== "" ? Number(nextRightReps) : undefined,
      rightWeight:
        unilateral && fields.tracksWeight && snappedRightWeight !== "" ? Number(snappedRightWeight) : undefined,
      actualDurationSeconds: fields.tracksDuration && duration !== "" ? Number(duration) : undefined,
      actualDistance: fields.tracksDistance && distance !== "" ? Number(distance) : undefined,
      notes: notes === "" ? undefined : notes,
      failed: nextFailed,
      rightFailed: unilateral ? nextRightFailed : false,
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
      <div className="set-row d-flex align-items-center gap-2 mb-1 flex-wrap">
        <span className="set-number">Set {set.setNumber}</span>
        <span>{formatLoggedSetSummary(set, fields, limbs)}</span>
        {set.loggedAt && (
          <small className="text-muted">logged {new Date(set.loggedAt).toLocaleTimeString()}</small>
        )}
      </div>
    );
  }

  return (
    <div className="set-row d-flex align-items-center gap-2 mb-1 flex-wrap">
      <span className="set-number">Set {set.setNumber}</span>
      {unilateral ? (
        <>
          <input
            type="number"
            className="form-control form-control-sm"
            style={{ width: "5.5rem" }}
            placeholder={`L ${repsPlaceholder}`}
            value={reps}
            onChange={(e) => setReps(e.target.value === "" ? "" : Number(e.target.value))}
            onBlur={copyLeftToRightIfEmpty}
          />
          <label className="set-fail">
            <input
              type="checkbox"
              className="form-check-input set-fail-input"
              checked={failed}
              onChange={(e) => {
                const next = e.target.checked;
                setFailed(next);
                commit(rightReps, rightWeight, next, rightFailed);
              }}
            />
            Fail L
          </label>
          {fields.tracksWeight && (
            <input
              type="number"
              className="form-control form-control-sm"
              style={{ width: "6.5rem" }}
              placeholder={`L ${weightPlaceholder}`}
              value={weight}
              min={weightAttrs.min}
              step={weightAttrs.step}
              onChange={(e) => setWeight(e.target.value === "" ? "" : Number(e.target.value))}
              onBlur={copyLeftToRightIfEmpty}
            />
          )}
          <input
            type="number"
            className="form-control form-control-sm"
            style={{ width: "5.5rem" }}
            placeholder={`R ${repsPlaceholder}`}
            value={rightReps}
            onChange={(e) => setRightReps(e.target.value === "" ? "" : Number(e.target.value))}
            onBlur={() => commit()}
          />
          <label className="set-fail">
            <input
              type="checkbox"
              className="form-check-input set-fail-input"
              checked={rightFailed}
              onChange={(e) => {
                const next = e.target.checked;
                setRightFailed(next);
                commit(rightReps, rightWeight, failed, next);
              }}
            />
            Fail R
          </label>
          {fields.tracksWeight && (
            <input
              type="number"
              className="form-control form-control-sm"
              style={{ width: "6.5rem" }}
              placeholder={`R ${weightPlaceholder}`}
              value={rightWeight}
              min={weightAttrs.min}
              step={weightAttrs.step}
              onChange={(e) => setRightWeight(e.target.value === "" ? "" : Number(e.target.value))}
              onBlur={() => commit()}
            />
          )}
        </>
      ) : (
        <>
          <input
            type="number"
            className="form-control form-control-sm"
            style={{ width: "5.5rem" }}
            placeholder={repsPlaceholder}
            value={reps}
            onChange={(e) => setReps(e.target.value === "" ? "" : Number(e.target.value))}
            onBlur={() => commit()}
          />
          <label className="set-fail">
            <input
              type="checkbox"
              className="form-check-input set-fail-input"
              checked={failed}
              onChange={(e) => {
                const next = e.target.checked;
                setFailed(next);
                commit(rightReps, rightWeight, next, rightFailed);
              }}
            />
            Fail
          </label>
          {fields.tracksWeight && (
            <input
              type="number"
              className="form-control form-control-sm"
              style={{ width: "6.5rem" }}
              placeholder={weightPlaceholder}
              value={weight}
              min={weightAttrs.min}
              step={weightAttrs.step}
              onChange={(e) => setWeight(e.target.value === "" ? "" : Number(e.target.value))}
              onBlur={() => commit()}
            />
          )}
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
        type="text"
        className="form-control form-control-sm"
        style={{ width: "8rem" }}
        placeholder="Notes"
        value={notes}
        onChange={(e) => setNotes(e.target.value)}
        onBlur={() => commit()}
      />
      {set.loggedAt && (
        <small className="text-muted">logged {new Date(set.loggedAt).toLocaleTimeString()}</small>
      )}
      {onRemove && (
        <button type="button" className="btn btn-outline-danger btn-sm" onClick={onRemove}>
          x
        </button>
      )}
    </div>
  );
}

export default LoggedSetRow;
