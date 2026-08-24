import { useEffect, useState } from "react";
import type { FormEvent, MouseEvent } from "react";
import { useNavigate } from "react-router-dom";
import { activatePlan, createPlan, deletePlan, listPlans } from "../../api/plans";
import type { PlanResponse } from "../../api/plans";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import "./PlanList.css";

function PlanList() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();

  const [plans, setPlans] = useState<PlanResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [name, setName] = useState("");
  const [weeks, setWeeks] = useState(4);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    listPlans()
      .then(setPlans)
      .catch((err) => setError(toErrorMessage(err, "Failed to load plans")))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const closeModal = () => {
    setIsCreating(false);
    setName("");
    setWeeks(4);
    setSaveError(null);
  };

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setSaveError(null);
    setSaving(true);
    try {
      const created = await createPlan({ name, weeks });
      setPlans((prev) => [...prev, created]);
      closeModal();
      navigate(`/plans/${created.id}`);
    } catch (err) {
      setSaveError(toErrorMessage(err, "Failed to create plan"));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (e: MouseEvent, id: number) => {
    e.stopPropagation();
    if (!window.confirm("Delete this plan?")) {
      return;
    }

    try {
      await deletePlan(id);
      setPlans((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      setError(toErrorMessage(err, "Failed to delete plan"));
    }
  };

  const handleActivate = async (e: MouseEvent, id: number) => {
    e.stopPropagation();
    try {
      await activatePlan(id);
      setPlans((prev) => prev.map((p) => ({ ...p, active: p.id === id })));
    } catch (err) {
      setError(toErrorMessage(err, "Failed to activate plan"));
    }
  };

  return (
    <PageLayout width="narrow">
      <div className="page-header">
        <h1>Plans</h1>
        <div className="d-flex gap-2">
          <button type="button" className="btn btn-outline-primary" onClick={() => navigate("/plans/current")}>
            View Current Plan
          </button>
          <button type="button" className="btn btn-primary" onClick={() => setIsCreating(true)}>
            + New Plan
          </button>
        </div>
      </div>
      <ErrorBanner message={error} />
      {loading && <p>Loading...</p>}
      {!loading && plans.length === 0 && <p className="text-muted">No plans yet.</p>}
      <ul className="list-group">
        {plans.map((plan) => (
          <li
            key={plan.id}
            className="list-group-item card-row d-flex justify-content-between align-items-center"
            role="button"
            onClick={() => navigate(`/plans/${plan.id}`)}
          >
            <span>
              <strong>{plan.name}</strong>{" "}
              <small className="text-muted">{plan.weeks} weeks</small>{" "}
              {plan.active && <span className="badge-status completed">Active</span>}
            </span>
            <div className="d-flex gap-2">
              {!plan.active && (
                <button
                  type="button"
                  className="btn btn-outline-primary btn-sm"
                  onClick={(e) => handleActivate(e, plan.id)}
                >
                  Activate
                </button>
              )}
              <button
                type="button"
                className="btn btn-outline-danger btn-sm"
                onClick={(e) => handleDelete(e, plan.id)}
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>

      <Modal isOpen={isCreating} onClose={closeModal}>
        <div className="p-3">
          <h2>New Plan</h2>
          <ErrorBanner message={saveError} />
          <form onSubmit={handleCreate}>
            <div className="mb-3">
              <label htmlFor="planName" className="form-label">
                Name
              </label>
              <input
                id="planName"
                type="text"
                className="form-control"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
            <div className="mb-3">
              <label htmlFor="planWeeks" className="form-label">
                Weeks
              </label>
              <input
                id="planWeeks"
                type="number"
                min={1}
                className="form-control"
                value={weeks}
                onChange={(e) => setWeeks(Number(e.target.value))}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? "Saving..." : "Save"}
            </button>
            <button
              type="button"
              className="btn btn-secondary ms-2"
              onClick={closeModal}
              disabled={saving}
            >
              Cancel
            </button>
          </form>
        </div>
      </Modal>
    </PageLayout>
  );
}

export default PlanList;
