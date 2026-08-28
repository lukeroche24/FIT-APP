import { useCallback, useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { activatePlan, getPlan, persistPlanEdits } from "../../api/plans";
import type { PlanResponse } from "../../api/plans";
import type { WorkoutResponse } from "../../api/workouts";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import PlanDayGrid from "../PlanDayGrid/PlanDayGrid";
import type { PlanGridDay } from "../PlanDayGrid/PlanDayGrid";
import AssignWorkoutToDay from "../AssignWorkoutToDay/AssignWorkoutToDay";
import "./PlanBuilder.css";

const DAY_LABELS: Record<number, string> = {
  1: "Monday",
  2: "Tuesday",
  3: "Wednesday",
  4: "Thursday",
  5: "Friday",
  6: "Saturday",
  7: "Sunday",
};

interface PlanPageState {
  isNew?: boolean;
}

function daysFromPlan(plan: PlanResponse): PlanGridDay[] {
  return Object.entries(DAY_LABELS).map(([dow, label]) => {
    const dayOfWeek = Number(dow);
    const planDay = plan.days.find((d) => d.dayOfWeek === dayOfWeek);
    return {
      key: dayOfWeek,
      dayOfWeek,
      label,
      workout: planDay?.workout ?? null,
    };
  });
}

function cloneDays(days: PlanGridDay[]): PlanGridDay[] {
  return days.map((day) => ({ ...day }));
}

function PlanBuilder() {
  const isAuthenticated = useRequireAuth();
  const { id } = useParams();
  const planId = Number(id);
  const navigate = useNavigate();
  const location = useLocation();
  const isNewFromNav = Boolean((location.state as PlanPageState | null)?.isNew);

  const [plan, setPlan] = useState<PlanResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isNew, setIsNew] = useState(isNewFromNav);
  const [editing, setEditing] = useState(isNewFromNav);
  const [nameDraft, setNameDraft] = useState("");
  const [weeksDraft, setWeeksDraft] = useState(4);
  const [daysDraft, setDaysDraft] = useState<PlanGridDay[]>([]);
  const [saving, setSaving] = useState(false);

  const [assigningDay, setAssigningDay] = useState<number | null>(null);

  const loadSavedPlan = useCallback(() => {
    return getPlan(planId).then((loaded) => {
      setPlan(loaded);
      setNameDraft(loaded.name);
      setWeeksDraft(loaded.weeks);
      setDaysDraft(daysFromPlan(loaded));
    });
  }, [planId]);

  useEffect(() => {
    const navIsNew = Boolean((location.state as PlanPageState | null)?.isNew);
    setIsNew(navIsNew);
    setEditing(navIsNew);
  }, [planId]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    loadSavedPlan()
      .catch((err) => setError(toErrorMessage(err, "Failed to load plan")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, loadSavedPlan]);

  const handleActivate = async () => {
    if (!plan) {
      return;
    }

    try {
      await activatePlan(plan.id);
      navigate("/plans");
    } catch (err) {
      setError(toErrorMessage(err, "Failed to activate plan"));
    }
  };

  const handleStartEditing = () => {
    if (!plan) {
      return;
    }
    setNameDraft(plan.name);
    setWeeksDraft(plan.weeks);
    setDaysDraft(daysFromPlan(plan));
    setEditing(true);
  };

  const handleSave = async () => {
    if (!plan) {
      return;
    }

    setError(null);
    setSaving(true);
    try {
      await persistPlanEdits(plan, nameDraft, daysDraft, weeksDraft);
      if (isNew) {
        navigate("/plans");
        return;
      }
      await loadSavedPlan();
      setIsNew(false);
      setEditing(false);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to save plan"));
    } finally {
      setSaving(false);
    }
  };

  const handleClear = (dayOfWeek: number) => {
    setDaysDraft((prev) =>
      prev.map((day) => (day.dayOfWeek === dayOfWeek ? { ...day, workout: null } : day)),
    );
  };

  const handleMove = (fromDay: number, toDay: number) => {
    setDaysDraft((prev) => {
      const next = cloneDays(prev);
      const source = next.find((day) => day.dayOfWeek === fromDay);
      const target = next.find((day) => day.dayOfWeek === toDay);
      if (!source?.workout || !target) {
        return prev;
      }
      target.workout = source.workout;
      source.workout = null;
      return next;
    });
  };

  const handlePicked = (workout: WorkoutResponse) => {
    if (assigningDay === null) {
      return;
    }
    const dayOfWeek = assigningDay;
    setDaysDraft((prev) =>
      prev.map((day) => (day.dayOfWeek === dayOfWeek ? { ...day, workout } : day)),
    );
    setAssigningDay(null);
  };

  if (loading) {
    return (
      <PageLayout width="wide">
        <div className="page-header">
          <h1>Plan</h1>
          <Link to="/plans" className="btn btn-outline-secondary">
            Back to Plans
          </Link>
        </div>
        <p>Loading...</p>
      </PageLayout>
    );
  }

  if (!plan) {
    return (
      <PageLayout width="wide">
        <div className="page-header">
          <h1>Plan</h1>
          <Link to="/plans" className="btn btn-outline-secondary">
            Back to Plans
          </Link>
        </div>
        <ErrorBanner message={error} />
      </PageLayout>
    );
  }

  const days = editing ? daysDraft : daysFromPlan(plan);

  return (
    <PageLayout width="wide">
      <ErrorBanner message={error} />

      <div className="page-header">
        {editing ? (
          <input
            type="text"
            className="form-control plan-name-input"
            value={nameDraft}
            onChange={(e) => setNameDraft(e.target.value)}
            aria-label="Plan name"
          />
        ) : (
          <h1>{plan.name}</h1>
        )}
        <div className="plan-header-actions">
          <Link to="/plans" className="btn btn-outline-secondary">
            Back to Plans
          </Link>
          {editing ? (
            <button type="button" className="btn btn-primary" onClick={handleSave} disabled={saving}>
              {saving ? "Saving..." : "Save"}
            </button>
          ) : (
            <>
              <button type="button" className="btn btn-outline-primary" onClick={handleStartEditing}>
                Edit
              </button>
              {plan.active ? (
                <span className="badge-status completed">Active</span>
              ) : (
                <button type="button" className="btn btn-success" onClick={handleActivate}>
                  Activate this Plan
                </button>
              )}
            </>
          )}
        </div>
      </div>

      {editing ? (
        <div className="plan-weeks-row">
          <label htmlFor="planWeeks" className="text-muted mb-0">
            Weeks
          </label>
          <input
            id="planWeeks"
            type="number"
            min={1}
            className="form-control"
            value={weeksDraft}
            onChange={(e) => setWeeksDraft(Number(e.target.value))}
          />
        </div>
      ) : (
        <p className="text-muted">{plan.weeks} weeks</p>
      )}

      <PlanDayGrid
        days={days}
        onAssign={(dayOfWeek) => setAssigningDay(dayOfWeek)}
        onClear={handleClear}
        onMove={handleMove}
        readOnly={!editing}
      />

      <Modal isOpen={assigningDay !== null} onClose={() => setAssigningDay(null)}>
        {assigningDay !== null && (
          <AssignWorkoutToDay onPicked={handlePicked} onCancel={() => setAssigningDay(null)} />
        )}
      </Modal>
    </PageLayout>
  );
}

export default PlanBuilder;
