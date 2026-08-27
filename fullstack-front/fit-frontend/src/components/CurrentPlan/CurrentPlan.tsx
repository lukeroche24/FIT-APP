import { useCallback, useEffect, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { getUpcomingWorkouts, removePlanDay, setPlanDay } from "../../api/plans";
import type { PlanResponse, UpcomingWorkoutResponse } from "../../api/plans";
import type { WorkoutResponse } from "../../api/workouts";
import { useActiveSession } from "../../hooks/ActiveSession";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PlanDayGrid from "../PlanDayGrid/PlanDayGrid";
import type { PlanGridDay } from "../PlanDayGrid/PlanDayGrid";
import AssignWorkoutToDay from "../AssignWorkoutToDay/AssignWorkoutToDay";
import "./CurrentPlan.css";

function formatDayLabel(dateStr: string): string {
  const date = new Date(`${dateStr}T00:00:00`);
  return date.toLocaleDateString(undefined, { weekday: "short", day: "numeric", month: "short" });
}

function localIsoDate(date = new Date()): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function weekOffsetForToday(entries: UpcomingWorkoutResponse[]): number {
  const today = localIsoDate();
  const todayIndex = entries.findIndex((entry) => entry.date === today);
  if (todayIndex >= 0) {
    return Math.floor(todayIndex / 7);
  }
  if (entries.length === 0) {
    return 0;
  }
  if (entries[entries.length - 1].date < today) {
    return Math.floor((entries.length - 1) / 7);
  }
  return 0;
}

function chunk(items: UpcomingWorkoutResponse[], size: number): UpcomingWorkoutResponse[][] {
  const chunks: UpcomingWorkoutResponse[][] = [];
  for (let i = 0; i < items.length; i += size) {
    chunks.push(items.slice(i, i + size));
  }
  return chunks;
}

interface CurrentPlanScheduleProps {
  plan: PlanResponse | null;
  onDelete?: (id: number) => void;
}

export function CurrentPlanSchedule({ plan, onDelete }: CurrentPlanScheduleProps) {
  const navigate = useNavigate();
  const { startOrResume } = useActiveSession();

  const [upcoming, setUpcoming] = useState<UpcomingWorkoutResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [weekOffset, setWeekOffset] = useState(0);
  const [assigningDay, setAssigningDay] = useState<number | null>(null);

  const loadSchedule = useCallback(async (active: PlanResponse) => {
    const list = await getUpcomingWorkouts(active.weeks);
    setUpcoming(list);
    setWeekOffset(weekOffsetForToday(list));
  }, []);

  useEffect(() => {
    if (!plan) {
      setUpcoming([]);
      setLoading(false);
      return;
    }

    let cancelled = false;
    setUpcoming([]);
    setLoading(true);
    setError(null);
    loadSchedule(plan)
      .catch((err) => {
        if (!cancelled) {
          setError(toErrorMessage(err, "Failed to load current plan"));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [plan, loadSchedule]);

  const refresh = async () => {
    if (!plan) {
      return;
    }
    await loadSchedule(plan);
  };

  const handleDayClick = async (day: PlanGridDay) => {
    if (!day.workout) {
      return;
    }

    if (day.status === "COMPLETED" && day.workoutLogId) {
      navigate(`/workout-logs/${day.workoutLogId}`);
      return;
    }

    try {
      const session = await startOrResume(day.workout.id);
      navigate(`/workout-logs/${session.id}`);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to start session"));
    }
  };

  const handleClear = async (dayOfWeek: number) => {
    if (!plan) {
      return;
    }

    try {
      await removePlanDay(plan.id, dayOfWeek);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to clear day"));
    }
  };

  const handleMove = async (fromDay: number, toDay: number) => {
    if (!plan) {
      return;
    }

    const source = upcoming.find((entry) => entry.dayOfWeek === fromDay && entry.workout);
    if (!source?.workout) {
      return;
    }

    try {
      await setPlanDay(plan.id, toDay, source.workout.id);
      await removePlanDay(plan.id, fromDay);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to move workout"));
      await refresh();
    }
  };

  const handlePicked = async (workout: WorkoutResponse) => {
    if (!plan || assigningDay === null) {
      return;
    }

    try {
      await setPlanDay(plan.id, assigningDay, workout.id);
      setAssigningDay(null);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to assign workout"));
    }
  };

  if (!plan) {
    return (
      <div className="mb-4">
        <h2 className="h4">Current Plan</h2>
        <p className="text-muted mb-0">No plan is active. Activate one from the list below.</p>
      </div>
    );
  }

  const weekChunks = chunk(upcoming, 7);
  const currentWeek = weekChunks[weekOffset] ?? [];
  const days: PlanGridDay[] = currentWeek.map((entry) => ({
    key: entry.date,
    dayOfWeek: entry.dayOfWeek,
    label: formatDayLabel(entry.date),
    workout: entry.workout,
    status: entry.status,
    workoutLogId: entry.workoutLogId,
  }));

  return (
    <div className="mb-4">
      <ErrorBanner message={error} />
      <div className="page-header">
        <div>
          <div className="section-label mb-1">Current Plan</div>
          <h2 className="h4 mb-0">{plan.name}</h2>
        </div>
        <div className="d-flex gap-2 flex-wrap">
          <button
            type="button"
            className="btn btn-outline-primary"
            disabled={weekOffset === 0 || loading}
            onClick={() => setWeekOffset((w) => Math.max(0, w - 1))}
          >
            &larr; Prev Week
          </button>
          <button
            type="button"
            className="btn btn-outline-primary"
            disabled={weekOffset >= weekChunks.length - 1 || loading}
            onClick={() => setWeekOffset((w) => Math.min(weekChunks.length - 1, w + 1))}
          >
            Next Week &rarr;
          </button>
          <button type="button" className="btn btn-outline-secondary" onClick={() => navigate(`/plans/${plan.id}`)}>
            Edit Plan
          </button>
          {onDelete && (
            <button type="button" className="btn btn-outline-danger" onClick={() => onDelete(plan.id)}>
              Delete
            </button>
          )}
        </div>
      </div>

      {loading && <p>Loading this week...</p>}
      {!loading && days.length === 0 && <p className="text-muted">No scheduled weeks on this plan.</p>}
      {!loading && days.length > 0 && (
        <>
          <PlanDayGrid
            days={days}
            onAssign={(dayOfWeek) => setAssigningDay(dayOfWeek)}
            onClear={handleClear}
            onMove={handleMove}
            onDayClick={handleDayClick}
          />
          <p className="text-muted small">
            Green with a check means you finished that session. Red means the scheduled day has passed
            without a completed log. Click a finished day to reopen it.
          </p>
        </>
      )}

      <Modal isOpen={assigningDay !== null} onClose={() => setAssigningDay(null)}>
        {assigningDay !== null && (
          <AssignWorkoutToDay onPicked={handlePicked} onCancel={() => setAssigningDay(null)} />
        )}
      </Modal>
    </div>
  );
}

function CurrentPlan() {
  return <Navigate to="/plans" replace />;
}

export default CurrentPlan;
