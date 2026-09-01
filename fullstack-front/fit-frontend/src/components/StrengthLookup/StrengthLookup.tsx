import { useEffect, useState } from "react";
import { getStrengthStats, listStrengthExercises } from "../../api/users";
import type { StrengthExerciseOption, StrengthStatsResponse } from "../../api/users";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import "./StrengthLookup.css";

interface StrengthLookupProps {
  userId: string;
}

function formatKg(value: number | null | undefined): string {
  if (value == null) {
    return "—";
  }
  return `${formatKgAmount(value)} kg`;
}

function formatKgAmount(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(1);
}

function formatLift(weight: number | null, reps: number | null): string {
  if (weight == null || reps == null) {
    return "—";
  }
  return `${formatKgAmount(weight)} kg × ${reps}`;
}

/** Estimated 1RM (recent window), tested 1RM, and heaviest successful set. */
function StrengthLookup({ userId }: StrengthLookupProps) {
  const [exercises, setExercises] = useState<StrengthExerciseOption[]>([]);
  const [exerciseId, setExerciseId] = useState<number | "">("");
  const [stats, setStats] = useState<StrengthStatsResponse | null>(null);
  const [loadingList, setLoadingList] = useState(true);
  const [loadingStats, setLoadingStats] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoadingList(true);
    setError(null);
    setStats(null);
    setExerciseId("");
    listStrengthExercises(userId)
      .then((list) => {
        if (cancelled) {
          return;
        }
        setExercises(list);
        if (list.length > 0) {
          setExerciseId(list[0].id);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(toErrorMessage(err, "Failed to load exercises"));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoadingList(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [userId]);

  useEffect(() => {
    if (exerciseId === "") {
      setStats(null);
      return;
    }

    let cancelled = false;
    setLoadingStats(true);
    setError(null);
    getStrengthStats(userId, exerciseId)
      .then((result) => {
        if (!cancelled) {
          setStats(result);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(toErrorMessage(err, "Failed to load strength stats"));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoadingStats(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [userId, exerciseId]);

  return (
    <div className="profile-card strength-card mb-4">
      <div className="section-label">Strength</div>
      <ErrorBanner message={error} />
      {loadingList && <p className="text-muted mb-0">Loading exercises...</p>}
      {!loadingList && exercises.length === 0 && (
        <p className="text-muted mb-0">No weighted lifts logged yet.</p>
      )}
      {!loadingList && exercises.length > 0 && (
        <>
          <label htmlFor="strengthExercise" className="form-label">
            Exercise
          </label>
          <select
            id="strengthExercise"
            className="form-select mb-3"
            value={exerciseId}
            onChange={(e) => setExerciseId(Number(e.target.value))}
          >
            {exercises.map((exercise) => (
              <option key={exercise.id} value={exercise.id}>
                {exercise.name}
              </option>
            ))}
          </select>

          {loadingStats && <p className="text-muted mb-0">Loading stats...</p>}
          {!loadingStats && stats && (
            <div className="row g-3">
              <div className="col-md-4">
                <div className="stat">
                  <div className="stat-number stat-number--text">{formatKg(stats.estimatedOneRm)}</div>
                  <div className="stat-label">Estimated 1RM · last 4 weeks</div>
                </div>
              </div>
              <div className="col-md-4">
                <div className="stat">
                  <div className="stat-number stat-number--text">{formatKg(stats.testedOneRm)}</div>
                  <div className="stat-label">Tested 1RM · all time</div>
                </div>
              </div>
              <div className="col-md-4">
                <div className="stat">
                  <div className="stat-number stat-number--text">{formatLift(stats.heaviestWeight, stats.heaviestReps)}</div>
                  <div className="stat-label">
                    Heaviest lift · all time
                    {stats.heaviestAt
                      ? ` · ${new Date(stats.heaviestAt).toLocaleDateString()}`
                      : ""}
                  </div>
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default StrengthLookup;
