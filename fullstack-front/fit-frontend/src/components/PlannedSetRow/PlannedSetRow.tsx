import { useState } from "react";
import type { PlannedSetRequest, PlannedSetResponse } from "../../api/workouts";
import "./PlannedSetRow.css";

interface Props {
  set: PlannedSetResponse;
  onUpdate: (request: PlannedSetRequest) => void;
  onRemove: () => void;
}

function PlannedSetRow({ set, onUpdate, onRemove }: Props) {
  const [reps, setReps] = useState(set.targetReps ?? "");
  const [weight, setWeight] = useState(set.targetWeight ?? "");
  const [duration, setDuration] = useState(set.targetDurationSeconds ?? "");
  const [rest, setRest] = useState(set.restTimeSeconds ?? "");

  const commit = () => {
    onUpdate({
      targetReps: reps === "" ? undefined : Number(reps),
      targetWeight: weight === "" ? undefined : Number(weight),
      targetDurationSeconds: duration === "" ? undefined : Number(duration),
      restTimeSeconds: rest === "" ? undefined : Number(rest),
    });
  };

  return (
    <div className="set-row d-flex align-items-center gap-2 mb-1">
      <span style={{ minWidth: "3.5rem" }}>Set {set.setNumber}</span>
      <input
        type="number"
        className="form-control form-control-sm"
        style={{ width: "5rem" }}
        placeholder="Reps"
        value={reps}
        onChange={(e) => setReps(e.target.value === "" ? "" : Number(e.target.value))}
        onBlur={commit}
      />
      <input
        type="number"
        className="form-control form-control-sm"
        style={{ width: "6rem" }}
        placeholder="Weight"
        value={weight}
        onChange={(e) => setWeight(e.target.value === "" ? "" : Number(e.target.value))}
        onBlur={commit}
      />
      <input
        type="number"
        className="form-control form-control-sm"
        style={{ width: "6rem" }}
        placeholder="Duration (s)"
        value={duration}
        onChange={(e) => setDuration(e.target.value === "" ? "" : Number(e.target.value))}
        onBlur={commit}
      />
      <input
        type="number"
        className="form-control form-control-sm"
        style={{ width: "5rem" }}
        placeholder="Rest (s)"
        value={rest}
        onChange={(e) => setRest(e.target.value === "" ? "" : Number(e.target.value))}
        onBlur={commit}
      />
      <button type="button" className="btn btn-outline-danger btn-sm" onClick={onRemove}>
        x
      </button>
    </div>
  );
}

export default PlannedSetRow;
