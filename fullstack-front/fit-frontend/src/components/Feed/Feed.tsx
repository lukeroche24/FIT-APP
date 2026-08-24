import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { copyWorkoutLogToLibrary, getFeed } from "../../api/friends";
import type { FeedItemResponse } from "../../api/friends";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import "./Feed.css";

function Feed() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();

  const [items, setItems] = useState<FeedItemResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [copyingId, setCopyingId] = useState<number | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    getFeed()
      .then(setItems)
      .catch((err) => setError(toErrorMessage(err, "Failed to load feed")))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const handleCopy = async (workoutLogId: number) => {
    setError(null);
    setCopyingId(workoutLogId);
    try {
      const copy = await copyWorkoutLogToLibrary(workoutLogId);
      navigate(`/workouts/${copy.id}`);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to copy workout"));
    } finally {
      setCopyingId(null);
    }
  };

  return (
    <PageLayout>
      <div className="page-header">
        <h1>Feed</h1>
      </div>
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && items.length === 0 && (
        <p className="text-muted">
          No sessions from friends yet. Add friends to see their completed workouts here.
        </p>
      )}
      <ul className="list-group">
        {items.map((item) => (
          <li key={item.workoutLog.id} className="list-group-item card-row">
            <div className="d-flex justify-content-between align-items-start">
              <span>
                <strong>{item.friendUsername}</strong> completed <strong>{item.workoutLog.name}</strong>
                <br />
                <small className="text-muted">
                  {item.workoutLog.completedAt && new Date(item.workoutLog.completedAt).toLocaleString()}
                </small>
              </span>
              <button
                type="button"
                className="btn btn-outline-primary btn-sm"
                disabled={copyingId === item.workoutLog.id}
                onClick={() => handleCopy(item.workoutLog.id)}
              >
                {copyingId === item.workoutLog.id ? "Copying..." : "Copy to My Library"}
              </button>
            </div>
            <div className="mt-2 d-flex flex-wrap gap-2">
              {item.workoutLog.loggedExercises.map((le) => (
                <span key={le.id} className="badge-status in-progress">
                  {le.exercise.name}
                </span>
              ))}
            </div>
          </li>
        ))}
      </ul>
    </PageLayout>
  );
}

export default Feed;
