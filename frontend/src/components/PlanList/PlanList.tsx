import { useEffect, useState } from "react";
import type { FormEvent, MouseEvent } from "react";
import { useNavigate } from "react-router-dom";
import { activatePlan, createPlan, deletePlan, getActivePlan, listPlans, type PlanResponse } from "../../api/plans";
import { DEFAULT_PAGE_SIZE, type ListPage } from "../../api/paging";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { useRequireAuth } from "../../hooks/useRequireAuth";
import { toErrorMessage } from "../../utils/errors";
import ErrorBanner from "../ErrorBanner/ErrorBanner";
import Modal from "../Modal/Modal";
import PageLayout from "../PageLayout/PageLayout";
import Pager from "../Pager/Pager";
import { CurrentPlanSchedule } from "../CurrentPlan/CurrentPlan";
import "./PlanList.css";

function applyOtherPlansPage(
  result: ListPage<PlanResponse>,
  setPage: (page: number) => void,
  setOtherPlans: (plans: PlanResponse[]) => void,
  setTotalPages: (n: number) => void,
  setTotalElements: (n: number) => void,
): boolean {
  if (result.content.length === 0 && result.number > 0 && result.totalElements > 0) {
    setPage(result.number - 1);
    return true;
  }
  setOtherPlans(result.content);
  setTotalPages(result.totalPages);
  setTotalElements(result.totalElements);
  return false;
}

function PlanList() {
  const isAuthenticated = useRequireAuth();
  const navigate = useNavigate();

  const [activePlan, setActivePlan] = useState<PlanResponse | null>(null);
  const [activeReady, setActiveReady] = useState(false);
  const [otherPlans, setOtherPlans] = useState<PlanResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");
  const query = useDebouncedValue(search);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [name, setName] = useState("");
  const [weeks, setWeeks] = useState(4);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    setPage(0);
  }, [query]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    getActivePlan()
      .then(setActivePlan)
      .catch((err) => setError(toErrorMessage(err, "Failed to load plans")))
      .finally(() => setActiveReady(true));
  }, [isAuthenticated]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    setLoading(true);
    listPlans({ query, page, size: DEFAULT_PAGE_SIZE, excludeActive: true })
      .then((result) => {
        if (applyOtherPlansPage(result, setPage, setOtherPlans, setTotalPages, setTotalElements)) {
          return;
        }
        setLoading(false);
      })
      .catch((err) => {
        setError(toErrorMessage(err, "Failed to load plans"));
        setLoading(false);
      });
  }, [isAuthenticated, query, page]);

  const reloadOtherPlans = () =>
    listPlans({ query, page, size: DEFAULT_PAGE_SIZE, excludeActive: true }).then((result) => {
      applyOtherPlansPage(result, setPage, setOtherPlans, setTotalPages, setTotalElements);
    });

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
      closeModal();
      navigate(`/plans/${created.id}`, { state: { isNew: true } });
    } catch (err) {
      setSaveError(toErrorMessage(err, "Failed to create plan"));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (e: MouseEvent, id: number) => {
    e.stopPropagation();
    await removePlan(id);
  };

  const removePlan = async (id: number) => {
    if (!window.confirm("Delete this plan?")) {
      return;
    }

    try {
      await deletePlan(id);
      if (activePlan?.id === id) {
        setActivePlan(null);
      }
      await reloadOtherPlans();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to delete plan"));
    }
  };

  const handleActivate = async (e: MouseEvent, id: number) => {
    e.stopPropagation();
    try {
      const activated = await activatePlan(id);
      setActivePlan(activated);
      await reloadOtherPlans();
    } catch (err) {
      setError(toErrorMessage(err, "Failed to activate plan"));
    }
  };

  return (
    <PageLayout width="wide">
      <div className="page-header">
        <h1>Plans</h1>
        <button type="button" className="btn btn-primary" onClick={() => setIsCreating(true)}>
          + New Plan
        </button>
      </div>
      <ErrorBanner message={error} />
      {!activeReady && <p>Loading...</p>}
      {activeReady && <CurrentPlanSchedule plan={activePlan} onDelete={removePlan} />}
      {activeReady && (
        <>
          <div className="section-label other-plans-label">{activePlan ? "Other Plans" : "Your Plans"}</div>
          <input
            type="search"
            className="form-control mb-3"
            placeholder="Search plans..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            aria-label="Search plans"
          />
          {loading && <p>Loading...</p>}
          {!loading && otherPlans.length === 0 && (
            <div className="empty-state">
              <p>
                {query
                  ? "No plans match that search."
                  : activePlan
                    ? "No other plans. Create another one if you want a spare template."
                    : "No plans yet. Create one to schedule your week."}
              </p>
            </div>
          )}
          {!loading && otherPlans.length > 0 && (
            <ul className="list-group">
              {otherPlans.map((plan) => (
                <li
                  key={plan.id}
                  className="list-group-item card-row d-flex justify-content-between align-items-center gap-2"
                  role="button"
                  onClick={() => navigate(`/plans/${plan.id}`)}
                >
                  <div className="list-row-body">
                    <span className="list-row-title">{plan.name}</span>
                    <span className="list-row-meta">
                      {plan.weeks} week{plan.weeks === 1 ? "" : "s"}
                      {plan.days?.length
                        ? ` · ${plan.days.length} day${plan.days.length === 1 ? "" : "s"} assigned`
                        : ""}
                    </span>
                  </div>
                  <div className="d-flex gap-2">
                    <button
                      type="button"
                      className="btn btn-outline-primary btn-sm"
                      onClick={(e) => handleActivate(e, plan.id)}
                    >
                      Activate
                    </button>
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
          )}
          {!loading && <Pager page={page} totalPages={totalPages} onPageChange={setPage} />}
          {!loading && totalElements > 0 && (
            <p className="text-muted small text-center mt-2">
              {totalElements} plan{totalElements === 1 ? "" : "s"}
            </p>
          )}
        </>
      )}

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
