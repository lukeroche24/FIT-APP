import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { listExercises } from "../../api/exercises";
import { listWorkouts } from "../../api/workouts";
import { listWorkoutLogs } from "../../api/workoutLogs";
import type { WorkoutLogResponse } from "../../api/workoutLogs";
import { getNextWorkout } from "../../api/plans";
import type { UpcomingWorkoutResponse } from "../../api/plans";
import { useActiveSession } from "../../hooks/ActiveSession";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import PageLayout from "../PageLayout/PageLayout";
import "./Home.css";

function Home() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();
  const { startOrResume } = useActiveSession();

  const [exerciseCount, setExerciseCount] = useState(0);
  const [workoutCount, setWorkoutCount] = useState(0);
  const [recentLogs, setRecentLogs] = useState<WorkoutLogResponse[]>([]);
  const [nextWorkout, setNextWorkout] = useState<UpcomingWorkoutResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    Promise.all([
      listExercises({ page: 0, size: 1 }),
      listWorkouts({ page: 0, size: 1 }),
      listWorkoutLogs({ page: 0, size: 5 }),
      getNextWorkout(),
    ])
      .then(([exercises, workouts, logs, next]) => {
        setExerciseCount(exercises.totalElements);
        setWorkoutCount(workouts.totalElements);
        setRecentLogs(logs.content);
        setNextWorkout(next);
      })
      .catch((err) => setError(toErrorMessage(err, "Failed to load dashboard")))
      .finally(() => setIsLoading(false));
  }, [isAuthenticated]);

  const handleStartNext = async () => {
    if (!nextWorkout?.workout) {
      return;
    }

    try {
      const session = await startOrResume(nextWorkout.workout.id);
      navigate(`/workout-logs/${session.id}`);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to start session"));
    }
  };

  return (
    <PageLayout>
      <h1>Welcome back</h1>
      <ErrorBanner message={error} />

      {!isLoading && nextWorkout?.workout && (
        <div className="card-row d-flex justify-content-between align-items-center mb-4">
          <div>
            <div className="section-label mb-1">Next Planned Workout</div>
            <strong>{nextWorkout.workout.name}</strong>{" "}
            <small className="text-muted">{new Date(`${nextWorkout.date}T00:00:00`).toLocaleDateString()}</small>
          </div>
          <button type="button" className="btn btn-success" onClick={handleStartNext}>
            Start Now
          </button>
        </div>
      )}
      {!isLoading && !nextWorkout && (
        <p className="text-muted mb-4">
          No upcoming planned workout. <Link to="/plans">View Plans</Link>
        </p>
      )}

      {!isLoading && (
        <div className="row g-3 mb-4">
          <div className="col-4">
            <div className="stat">
              <div className="stat-number">{exerciseCount}</div>
              <div className="stat-label">Exercises</div>
            </div>
          </div>
          <div className="col-4">
            <div className="stat">
              <div className="stat-number">{workoutCount}</div>
              <div className="stat-label">Workouts</div>
            </div>
          </div>
          <div className="col-4">
            <div className="stat">
              <div className="stat-number">{recentLogs.length}</div>
              <div className="stat-label">Recent Sessions</div>
            </div>
          </div>
        </div>
      )}

      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <div className="card-row" role="button" onClick={() => navigate("/exercises")}>
            <strong>Exercise Library</strong>
            <p className="text-muted mb-0">Browse and manage your exercises</p>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card-row" role="button" onClick={() => navigate("/workouts")}>
            <strong>Workouts</strong>
            <p className="text-muted mb-0">Build and edit your workout plans</p>
          </div>
        </div>
        <div className="col-md-4">
          <div className="card-row" role="button" onClick={() => navigate("/workout-logs")}>
            <strong>History</strong>
            <p className="text-muted mb-0">Review your logged sessions</p>
          </div>
        </div>
      </div>

      <div className="section-label">Recent Sessions</div>
      {isLoading && <p>Loading...</p>}
      {!isLoading && recentLogs.length === 0 && (
        <div className="empty-state">
          <p>No sessions yet — start one from a workout.</p>
        </div>
      )}
      <ul className="list-group">
        {recentLogs.map((log) => (
          <li
            key={log.id}
            className="list-group-item card-row d-flex justify-content-between align-items-center gap-2"
            role="button"
            onClick={() => navigate(`/workout-logs/${log.id}`)}
          >
            <div className="list-row-body">
              <span className="list-row-title">{log.name}</span>
              <span className="list-row-meta">{new Date(log.startedAt).toLocaleDateString()}</span>
            </div>
            <span className={`badge-status ${log.completedAt ? "completed" : "in-progress"}`}>
              {log.completedAt ? "Completed" : "In progress"}
            </span>
          </li>
        ))}
      </ul>
    </PageLayout>
  );
}

export default Home;
