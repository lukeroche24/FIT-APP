import { useState } from "react";
import type { PlannedSetRequest, PlannedSetResponse } from "../../api/workouts";
import { trackingFrom, type TrackingFlags } from "../../utils/tracking";
import type { LateralityFlags } from "../../utils/laterality";
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

function formatSetSummary(set: PlannedSetResponse, tracking: TrackingFlags): string {
  const parts: string[] = [];
  if (tracking.tracksDuration && set.targetDurationSeconds != null) {
    parts.push(`${set.targetDurationSeconds}s`);
  }
  if (tracking.tracksDistance && set.targetDistance != null) {
    parts.push(`${set.targetDistance} m`);
  }
  return parts.length > 0 ? parts.join(" · ") : "Set";
}

/** Planned set on a workout template: count, plus duration/distance when tracked. */
function PlannedSetRow({ set, tracking, readOnly = false, onUpdate, onRemove }: Props) {
  const fields = trackingFrom(tracking);

  const [duration, setDuration] = useState(set.targetDurationSeconds ?? "");
  const [distance, setDistance] = useState(set.targetDistance ?? "");

  const commit = () => {
    onUpdate?.({
      targetDurationSeconds: fields.tracksDuration && duration !== "" ? Number(duration) : undefined,
      targetDistance: fields.tracksDistance && distance !== "" ? Number(distance) : undefined,
    });
  };

  if (readOnly) {
    return (
      <div className="set-row d-flex align-items-center gap-2 mb-1">
        <span style={{ minWidth: "3.5rem" }}>Set {set.setNumber}</span>
        <span>{formatSetSummary(set, fields)}</span>
      </div>
    );
  }

  return (
    <div className="set-row d-flex align-items-center gap-2 mb-1 flex-wrap">
      <span style={{ minWidth: "3.5rem" }}>Set {set.setNumber}</span>
      {fields.tracksDuration && (
        <input
          type="number"
          className="form-control form-control-sm"
          style={{ width: "6rem" }}
          placeholder="Duration (s)"
          value={duration}
          onChange={(e) => setDuration(e.target.value === "" ? "" : Number(e.target.value))}
          onBlur={commit}
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
          onBlur={commit}
        />
      )}
      <button type="button" className="btn btn-outline-danger btn-sm" onClick={onRemove}>
        x
      </button>
    </div>
  );
}

export default PlannedSetRow;
