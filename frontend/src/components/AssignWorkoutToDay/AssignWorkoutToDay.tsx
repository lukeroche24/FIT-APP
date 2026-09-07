import { useEffect, useState } from "react";
import { listWorkouts } from "../../api/workouts";
import { DEFAULT_PAGE_SIZE } from "../../api/paging";
import type { WorkoutResponse } from "../../api/workouts";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Pager from "../Pager/Pager";
import "./AssignWorkoutToDay.css";

interface Props {
  onPicked: (workout: WorkoutResponse) => void;
  onCancel: () => void;
}

function AssignWorkoutToDay({ onPicked, onCancel }: Props) {
  const [workouts, setWorkouts] = useState<WorkoutResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const query = useDebouncedValue(search);

  useEffect(() => {
    setPage(0);
  }, [query]);

  useEffect(() => {
    setLoading(true);
    listWorkouts({ query, page, size: DEFAULT_PAGE_SIZE })
      .then((result) => {
        if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
          setPage(result.number - 1);
          return;
        }
        setWorkouts(result.content);
        setTotalPages(result.totalPages);
      })
      .catch((err) => setError(toErrorMessage(err, "Failed to load workouts")))
      .finally(() => setLoading(false));
  }, [query, page]);

  return (
    <div className="p-3">
      <h2>Assign Workout</h2>
      <ErrorBanner message={error} />
      <input
        type="search"
        className="form-control mb-3"
        placeholder="Search workouts..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        aria-label="Search workouts"
      />
      {loading && <p>Loading...</p>}
      {!loading && workouts.length === 0 && <p className="text-muted">No workouts found.</p>}
      <ul className="list-group mb-3">
        {workouts.map((workout) => (
          <li
            key={workout.id}
            className="list-group-item"
            role="button"
            onClick={() => onPicked(workout)}
          >
            {workout.name}
          </li>
        ))}
      </ul>
      <Pager page={page} totalPages={totalPages} onPageChange={setPage} />
      <button type="button" className="btn btn-secondary" onClick={onCancel}>
        Cancel
      </button>
    </div>
  );
}

export default AssignWorkoutToDay;
