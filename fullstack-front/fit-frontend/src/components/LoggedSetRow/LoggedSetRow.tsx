import { useState } from "react";
import type { LoggedSetRequest, LoggedSetResponse } from "../../api/workoutLogs";
import "./LoggedSetRow.css";

interface Props {
  set: LoggedSetResponse;
  onUpdate: (request: LoggedSetRequest) => void;
  onRemove: () => void;
}

function LoggedSetRow({ set, onUpdate, onRemove }: Props) {
  const [reps, setReps] = useState(set.actualReps ?? "");
  const [weight, setWeight] = useState(set.actualWeight ?? "");
  //const [duration, setDuration] = useState(set.actualDurationSeconds ?? "");
  //const [distance, setDistance] = useState(set.actualDistance ?? "");
  const [notes, setNotes] = useState(set.notes ?? "");

  const commit = () => {
    onUpdate({
      actualReps: reps === "" ? undefined : Number(reps),
      actualWeight: weight === "" ? undefined : Number(weight),
      //actualDurationSeconds: duration === "" ? undefined : Number(duration),
      //actualDistance: distance === "" ? undefined : Number(distance),
      notes: notes === "" ? undefined : notes,
    });
  };

  return (
    <div className="set-row d-flex align-items-center gap-2 mb-1 flex-wrap">
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
        //value={duration}
        //onChange={(e) => setDuration(e.target.value === "" ? "" : Number(e.target.value))}
        onBlur={commit}
      />
      <input
        type="number"
        className="form-control form-control-sm"
        style={{ width: "6rem" }}
        placeholder="Distance"
        //value={distance}
        //onChange={(e) => setDistance(e.target.value === "" ? "" : Number(e.target.value))}
        onBlur={commit}
      />
      <input
        type="text"
        className="form-control form-control-sm"
        style={{ width: "8rem" }}
        placeholder="Notes"
        value={notes}
        onChange={(e) => setNotes(e.target.value)}
        onBlur={commit}
      />
      {set.loggedAt && (
        <small className="text-muted">logged {new Date(set.loggedAt).toLocaleTimeString()}</small>
      )}
      <button type="button" className="btn btn-outline-danger btn-sm" onClick={onRemove}>
        x
      </button>
    </div>
  );
}

export default LoggedSetRow;
