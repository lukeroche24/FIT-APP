import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { activatePlan, getPlan, removePlanDay, setPlanDay, updatePlan } from "../../api/plans";
import type { PlanResponse } from "../../api/plans";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import PlanDayGrid from "../PlanDayGrid/PlanDayGrid";
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

function PlanBuilder() {
  const isAuthenticated = useRequireAuth();
  const { id } = useParams();
  const planId = Number(id);

  const [plan, setPlan] = useState<PlanResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [editingName, setEditingName] = useState(false);
  const [nameDraft, setNameDraft] = useState("");

  const [assigningDay, setAssigningDay] = useState<number | null>(null);

  const refresh = useCallback(() => {
    return getPlan(planId).then(setPlan);
  }, [planId]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    refresh()
      .catch((err) => setError(toErrorMessage(err, "Failed to load plan")))
      .finally(() => setLoading(false));
  }, [isAuthenticated, refresh]);

  const handleSaveName = async () => {
    if (!plan) {
      return;
    }

    try {
      const updated = await updatePlan(plan.id, { name: nameDraft });
      setPlan(updated);
      setEditingName(false);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to rename plan"));
    }
  };

  const handleActivate = async () => {
    if (!plan) {
      return;
    }

    try {
      const updated = await activatePlan(plan.id);
      setPlan(updated);
    } catch (err) {
      setError(toErrorMessage(err, "Failed to activate plan"));
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

    const source = plan.days.find((d) => d.dayOfWeek === fromDay);
    if (!source) {
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

  if (!plan) {
    return (
      <PageLayout width="wide">
        <ErrorBanner message={error} />
      </PageLayout>
    );
  }

  const days = Object.entries(DAY_LABELS).map(([dow, label]) => {
    const dayOfWeek = Number(dow);
    const planDay = plan.days.find((d) => d.dayOfWeek === dayOfWeek);
    return {
      key: dayOfWeek,
      dayOfWeek,
      label,
      workout: planDay?.workout ?? null,
    };
  });

  return (
    <PageLayout width="wide">
      <ErrorBanner message={error} />

      <div className="page-header">
        {editingName ? (
          <div className="d-flex gap-2">
            <input
              type="text"
              className="form-control"
              value={nameDraft}
              onChange={(e) => setNameDraft(e.target.value)}
            />
            <button type="button" className="btn btn-primary" onClick={handleSaveName}>
              Save
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => setEditingName(false)}>
              Cancel
            </button>
          </div>
        ) : (
          <h1
            role="button"
            title="Click to rename"
            onClick={() => {
              setNameDraft(plan.name);
              setEditingName(true);
            }}
          >
            {plan.name}
          </h1>
        )}
        {plan.active ? (
          <span className="badge-status completed">Active</span>
        ) : (
          <button type="button" className="btn btn-success" onClick={handleActivate}>
            Activate this Plan
          </button>
        )}
      </div>

      <p className="text-muted">{plan.weeks} weeks</p>

      <PlanDayGrid
        days={days}
        onAssign={(dayOfWeek) => setAssigningDay(dayOfWeek)}
        onClear={handleClear}
        onMove={handleMove}
      />

      <Modal isOpen={assigningDay !== null} onClose={() => setAssigningDay(null)}>
        {assigningDay !== null && (
          <AssignWorkoutToDay
            planId={plan.id}
            dayOfWeek={assigningDay}
            onAssigned={handleAssigned}
            onCancel={() => setAssigningDay(null)}
          />
        )}
      </Modal>
    </PageLayout>
  );
}

export default PlanBuilder;
