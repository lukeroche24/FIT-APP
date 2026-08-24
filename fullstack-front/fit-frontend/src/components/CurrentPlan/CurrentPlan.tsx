import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { getActivePlan, getUpcomingWorkouts, removePlanDay, setPlanDay } from "../../api/plans";
import type { PlanResponse, UpcomingWorkoutResponse } from "../../api/plans";
import { startSession } from "../../api/workoutLogs";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import PlanDayGrid from "../PlanDayGrid/PlanDayGrid";
import type { PlanGridDay } from "../PlanDayGrid/PlanDayGrid";
import AssignWorkoutToDay from "../AssignWorkoutToDay/AssignWorkoutToDay";
import "./CurrentPlan.css";

function formatDayLabel(dateStr: string): string {
  const date = new Date(`${dateStr}T00:00:00`);
  return date.toLocaleDateString(undefined, { weekday: "short", day: "numeric", month: "short" });
}

function chunk(items: UpcomingWorkoutResponse[], size: number): UpcomingWorkoutResponse[][] {
  const chunks: UpcomingWorkoutResponse[][] = [];
  for (let i = 0; i < items.length; i += size) {
    chunks.push(items.slice(i, i + size));
  }
  return chunks;
}

function CurrentPlan() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();

  const [activePlan, setActivePlan] = useState<PlanResponse | null>(null);
  const [upcoming, setUpcoming] = useState<UpcomingWorkoutResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [weekOffset, setWeekOffset] = useState(0);
  const [assigningDay, setAssigningDay] = useState<number | null>(null);

  const refresh = useCallback(async () => {
    const plan = await getActivePlan();
    setActivePlan(plan);
    if (plan) {
      const list = await getUpcomingWorkouts(plan.weeks);
      setUpcoming(list);
    } else {
      setUpcoming([]);
    }
  }, []);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    refresh()
      .catch((err) => setError(toErrorMessage(err, "Failed to load current plan")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, refresh]);

  const handleDayClick = async (day: PlanGridDay) => {
    if (!day.workout) {
      return;
    }

    try {
      const log = await startSession(day.workout.id, {});
      navigate(`/workout-logs/${log.id}`);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to start session"));
    }
  };

  const handleClear = async (dayOfWeek: number) => {
    if (!activePlan) {
      return;
    }

    try {
      await removePlanDay(activePlan.id, dayOfWeek);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to clear day"));
    }
  };

  const handleMove = async (fromDay: number, toDay: number) => {
    if (!activePlan) {
      return;
    }

    const source = upcoming.find((entry) => entry.dayOfWeek === fromDay && entry.workout);
    if (!source?.workout) {
      return;
    }

    try {
      await setPlanDay(activePlan.id, toDay, source.workout.id);
      await removePlanDay(activePlan.id, fromDay);
      await refresh();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to move workout"));
      await refresh();
    }
  };

  const handleAssigned = () => {
    setAssigningDay(null);
    refresh();
  };

  if (loading) {
    return (
      <PageLayout width="wide">
        <p>Loading...</p>
      </PageLayout>
    );
  }

  if (!activePlan) {
    return (
      <PageLayout width="wide">
        <h1>Current Plan</h1>
        <ErrorBanner message={error} />
        <p className="text-muted">
          No active plan. <Link to="/plans">Go to Plans</Link> to activate one.
        </p>
      </PageLayout>
    );
  }

  const weekChunks = chunk(upcoming, 7);
  const currentWeek = weekChunks[weekOffset] ?? [];

  const days: PlanGridDay[] = currentWeek.map((entry) => ({
    key: entry.date,
    dayOfWeek: entry.dayOfWeek,
    label: formatDayLabel(entry.date),
    workout: entry.workout,
  }));

  return (
    <PageLayout width="wide">
      <ErrorBanner message={error} />

      <div className="page-header">
        <h1>{activePlan.name}</h1>
        <div className="d-flex gap-2">
          <button
            type="button"
            className="btn btn-outline-primary"
            disabled={weekOffset === 0}
            onClick={() => setWeekOffset((w) => Math.max(0, w - 1))}
          >
            &larr; Prev Week
          </button>
          <button
            type="button"
            className="btn btn-outline-primary"
            disabled={weekOffset >= weekChunks.length - 1}
            onClick={() => setWeekOffset((w) => Math.min(weekChunks.length - 1, w + 1))}
          >
            Next Week &rarr;
          </button>
        </div>
      </div>

      {days.length === 0 ? (
        <p className="text-muted">No more scheduled weeks on this plan.</p>
      ) : (
        <PlanDayGrid
          days={days}
          onAssign={(dayOfWeek) => setAssigningDay(dayOfWeek)}
          onClear={handleClear}
          onMove={handleMove}
          onDayClick={handleDayClick}
        />
      )}

      <Modal isOpen={assigningDay !== null} onClose={() => setAssigningDay(null)}>
        {assigningDay !== null && (
          <AssignWorkoutToDay
            planId={activePlan.id}
            dayOfWeek={assigningDay}
            onAssigned={handleAssigned}
            onCancel={() => setAssigningDay(null)}
          />
        )}
      </Modal>
    </PageLayout>
  );
}

export default CurrentPlan;
