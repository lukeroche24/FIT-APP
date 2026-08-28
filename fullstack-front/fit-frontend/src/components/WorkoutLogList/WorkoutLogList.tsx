import { useEffect, useState } from "react";
import type { MouseEvent } from "react";
import { useNavigate } from "react-router-dom";
import { deleteWorkoutLog, listWorkoutLogs } from "../../api/workoutLogs";
import { DEFAULT_PAGE_SIZE } from "../../api/paging";
import type { WorkoutLogResponse } from "../../api/workoutLogs";
import { useActiveSession } from "../../hooks/ActiveSession";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import Pager from "../Pager/Pager";
import "./WorkoutLogList.css";

function WorkoutLogList() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();
  const { clearInProgress } = useActiveSession();

  const [workoutLogs, setWorkoutLogs] = useState<WorkoutLogResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const query = useDebouncedValue(search);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setPage(0);
  }, [query]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    setLoading(true);
    listWorkoutLogs({ query, page, size: DEFAULT_PAGE_SIZE })
      .then((result) => {
        if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
          setPage(result.number - 1);
          return;
        }
        setWorkoutLogs(result.content);
        setTotalPages(result.totalPages);
        setTotalElements(result.totalElements);
        setLoading(false);
      })
      .catch((err) => {
        setError(toErrorMessage(err, "Failed to load history"));
        setLoading(false);
      });
  }, [isAuthenticated, query, page]);

  const handleDelete = async (e: MouseEvent, id: number) => {
    e.stopPropagation();
    if (!window.confirm("Delete this session?")) {
      return;
    }

    try {
      await deleteWorkoutLog(id);
      clearInProgress(id);
      const result = await listWorkoutLogs({ query, page, size: DEFAULT_PAGE_SIZE });
      if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
        setPage(result.number - 1);
        return;
      }
      setWorkoutLogs(result.content);
      setTotalPages(result.totalPages);
      setTotalElements(result.totalElements);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to delete session"));
    }
  };

  return (
    <PageLayout width="narrow">
      <div className="page-header">
        <h1>History</h1>
      </div>
      <input
        type="search"
        className="form-control mb-3"
        placeholder="Search sessions..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        aria-label="Search sessions"
      />
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && workoutLogs.length === 0 && (
        <p className="text-muted">{query ? "No sessions match that search." : "No sessions logged yet."}</p>
      )}
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
              <br />
              <small className="text-muted">
                {log.completedAt
                  ? new Date(log.completedAt).toLocaleString()
                  : `Started ${new Date(log.startedAt).toLocaleString()}`}
              </small>
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
      <Pager page={page} totalPages={totalPages} onPageChange={setPage} />
      {!loading && totalElements > 0 && (
        <p className="text-muted small text-center mt-2">
          {totalElements} session{totalElements === 1 ? "" : "s"}
        </p>
      )}
    </PageLayout>
  );
}

export default WorkoutLogList;
