/*
 * Filename: WorkoutLogList.tsx
 * Author: Luke Roche
 * Date: 2026-09-16
 * AI Usage Declaration:
 * - This file contains JSX/markup generated with the help of AI tools.
 * - Tool Used: Cursor
 * - I wrote an initial HTML/JSX draft to show the layout I wanted.
 * - AI rewrote that markup so it looked and structured better. The version in this file is that rewrite.
 * - AI-generated JSX/markup sections are marked with comments: // [AI-GENERATED]
 * - Catch/display of API failures is also AI-generated and marked // [AI-GENERATED]
 * - I wrote the comments, then used AI to touch up the wording.
 * I have reviewed, tested, and understood all AI-generated code.
 */

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
      // [AI-GENERATED: Cursor]
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
      // [AI-GENERATED: Cursor]
      setError(toErrorMessage(err, "Failed to delete session"));
    }
  };

  // [AI-GENERATED: Cursor]
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
        <div className="empty-state">
          <p>
            {query
              ? "No sessions match that search."
              : "No sessions logged yet. Start one from a workout."}
          </p>
        </div>
      )}
      <ul className="list-group">
        {workoutLogs.map((log) => {
          const exerciseCount = log.loggedExercises?.length ?? 0;
          const when = log.completedAt
            ? new Date(log.completedAt).toLocaleString()
            : `Started ${new Date(log.startedAt).toLocaleString()}`;
          const meta = [
            when,
            exerciseCount > 0 ? `${exerciseCount} exercise${exerciseCount === 1 ? "" : "s"}` : null,
          ]
            .filter(Boolean)
            .join(" · ");
          return (
            <li
              key={log.id}
              className="list-group-item card-row d-flex justify-content-between align-items-center gap-2"
              role="button"
              onClick={() => navigate(`/workout-logs/${log.id}`)}
            >
              <div className="list-row-body">
                <span className="list-row-title">{log.name}</span>{" "}
                <span className={`badge-status ${log.completedAt ? "completed" : "in-progress"}`}>
                  {log.completedAt ? "Completed" : "In progress"}
                </span>
                <span className="list-row-meta">{meta}</span>
              </div>
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                onClick={(e) => handleDelete(e, log.id)}
              >
                Delete
              </button>
            </li>
          );
        })}
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
