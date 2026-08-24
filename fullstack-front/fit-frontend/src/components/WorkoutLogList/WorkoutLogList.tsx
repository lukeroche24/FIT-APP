import { useEffect, useState } from "react";
import type { MouseEvent } from "react";
import { useNavigate } from "react-router-dom";
import { deleteWorkoutLog, listWorkoutLogs } from "../../api/workoutLogs";
import type { WorkoutLogResponse } from "../../api/workoutLogs";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import "./WorkoutLogList.css";

function WorkoutLogList() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();

  const [workoutLogs, setWorkoutLogs] = useState<WorkoutLogResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    listWorkoutLogs()
      .then(setWorkoutLogs)
      .catch((err) => setError(toErrorMessage(err, "Failed to load history")))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const handleDelete = async (e: MouseEvent, id: number) => {
    e.stopPropagation();
    if (!window.confirm("Delete this session?")) {
      return;
    }

    try {
      await deleteWorkoutLog(id);
      setWorkoutLogs((prev) => prev.filter((log) => log.id !== id));
    } catch (err) {
      setError(toErrorMessage(err, "Failed to delete session"));
    }
  };

  return (
    <PageLayout width="narrow">
      <h1>History</h1>
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && workoutLogs.length === 0 && <p className="text-muted">No sessions logged yet.</p>}
      <ul className="list-group">
        {workoutLogs.map((log) => (
          <li
            key={log.id}
            className="list-group-item card-row d-flex justify-content-between align-items-center"
            role="button"
            onClick={() => navigate(`/workout-logs/${log.id}`)}
          >
            <span>
              <strong>{log.name}</strong>{" "}
              <span className={`badge-status ${log.completedAt ? "completed" : "in-progress"}`}>
                {log.completedAt ? "Completed" : "In progress"}
              </span>
            </span>
            <button
              type="button"
              className="btn btn-outline-danger btn-sm"
              onClick={(e) => handleDelete(e, log.id)}
            >
              Delete
            </button>
          </li>
        ))}
      </ul>
    </PageLayout>
  );
}

export default WorkoutLogList;
