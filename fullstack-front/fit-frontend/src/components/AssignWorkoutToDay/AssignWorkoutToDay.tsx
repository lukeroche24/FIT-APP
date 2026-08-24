import { useEffect, useState } from "react";
import { listWorkouts } from "../../api/workouts";
import type { WorkoutResponse } from "../../api/workouts";
import { setPlanDay } from "../../api/plans";
import type { PlanDayResponse } from "../../api/plans";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import "./AssignWorkoutToDay.css";

interface Props {
  planId: number;
  dayOfWeek: number;
  onAssigned: (planDay: PlanDayResponse) => void;
  onCancel: () => void;
}

function AssignWorkoutToDay({ planId, dayOfWeek, onAssigned, onCancel }: Props) {
  const [workouts, setWorkouts] = useState<WorkoutResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState("");
  const [assigningId, setAssigningId] = useState<number | null>(null);

  useEffect(() => {
    listWorkouts()
      .then(setWorkouts)
      .catch((err) => setError(toErrorMessage(err, "Failed to load workouts")))
      .finally(() => setLoading(false));
  }, []);

  const handleSelect = async (workout: WorkoutResponse) => {
    setError(null);
    setAssigningId(workout.id);
    try {
      const planDay = await setPlanDay(planId, dayOfWeek, workout.id);
      onAssigned(planDay);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to assign workout"));
    } finally {
      setAssigningId(null);
    }
  };

  const filtered = workouts.filter((w) => w.name.toLowerCase().includes(filter.toLowerCase()));

  return (
    <div className="p-3">
      <h2>Assign Workout</h2>
      <ErrorBanner message={error} />
      <input
        type="text"
        className="form-control mb-3"
        placeholder="Search workouts..."
        value={filter}
        onChange={(e) => setFilter(e.target.value)}
      />
      {loading && <p>Loading...</p>}
      {!loading && filtered.length === 0 && <p className="text-muted">No workouts found.</p>}
      <ul className="list-group mb-3">
        {filtered.map((workout) => (
          <li
            key={workout.id}
            className="list-group-item"
            role="button"
            onClick={() => handleSelect(workout)}
          >
            {assigningId === workout.id ? "Assigning..." : workout.name}
          </li>
        ))}
      </ul>
      <button type="button" className="btn btn-secondary" onClick={onCancel}>
        Cancel
      </button>
    </div>
  );
}

export default AssignWorkoutToDay;
